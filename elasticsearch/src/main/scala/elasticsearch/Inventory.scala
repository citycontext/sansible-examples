package elasticsearch

import ansible.Inventory._
import com.myjeeva.digitalocean.pojo.Droplet
import scala.collection.JavaConverters._
import scala.concurrent._
import ExecutionContext.Implicits.global
import Config.DigitaOcean.{apiClient, dropletPrefix}

object Inventory {
  val esGroupName = "elasticsearch"

  def fromDigitalOcean: Future[ansible.Inventory] = {
    for {
      droplets <- Future(apiClient.getAvailableDroplets(1, 500).getDroplets.asScala.toList)
      esInstances = droplets.filter(_.getName.contains(dropletPrefix))
    } yield {
      val hosts = esInstances.map { case d =>
        (findIp4Address(d, "public"),
         findIp4Address(d, "private"),
         d.getName)
      }.collect { case (Some(publicIp), Some(privateIp), nodeName) =>
        Hostname(publicIp, Map(
          "vpn_nodename" -> nodeName,
          "vpn_subnet" -> subnetIp(nodeName),
          "private_ip" -> privateIp,
          "ansible_ssh_user" -> "root"
        ))
      }

      ansible.Inventory(List(Group(esGroupName, hosts.toList)))
    }
  }

  private def subnetIp(hostname: String): String = {
    val Pattern = s"""^$dropletPrefix([1-9]{1,3})""".r
    hostname match {
      case Pattern(n) => s"10.0.0.$n"
      case _ => sys.error(s"Cannot produce a subnet ip for $hostname")
    }
  }

  private def findIp4Address(d: Droplet, networkType: String): Option[String] = {
    val networks = d.getNetworks.getVersion4Networks.asScala
    networks.find(_.getType.toLowerCase() == networkType).map(_.getIpAddress)
  }
}
