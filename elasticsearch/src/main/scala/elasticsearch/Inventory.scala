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
      val hosts = esInstances.map { d =>
        (findIp4Address(d, "public"),
         findIp4Address(d, "private"))

      }.collect { case (Some(publicIp), Some(privateIp)) =>
        Hostname(publicIp, Map(
          "private_ip" -> privateIp,
          "ansible_ssh_user" -> "root"
        ))
      }

      ansible.Inventory(List(Group(esGroupName, hosts.toList)))
    }
  }

  private def findIp4Address(d: Droplet, networkType: String): Option[String] = {
    val networks = d.getNetworks.getVersion4Networks.asScala
    networks.find(_.getType.toLowerCase() == networkType).map(_.getIpAddress)
  }

}
