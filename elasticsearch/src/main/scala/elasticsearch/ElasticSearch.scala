package elasticsearch

import elasticsearch.task.TincVPN

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import ansible.Inventory.HostPattern
import ansible.Options.Become
import ansible.{Runner, Playbook}
import Inventory.esGroupName

object ElasticSearch extends App {
  Inventory.fromDigitalOcean.onComplete {
    case Failure(err) =>
      sys.error(s"Failed to generate inventory from digital ocean: ${err.getMessage}")

    case Success(inv) =>
      val hosts = inv.groupHosts(esGroupName)
      val vpnHosts = hosts.flatMap(TincVPN.Host.fromHostname)
      val tasks = new TincVPN(esGroupName, vpnHosts.head, vpnHosts)

      if (vpnHosts.nonEmpty) {
        vpnHosts.take(1).foreach { h =>
          val playbook = Playbook(
            hosts = List(HostPattern(h.publicIp)),
            tasks = tasks.forHost(h),
            options = Playbook.Options(become = Some(Become()))
          )
          Runner.runPlaybook(inv)(playbook)
        }
      } else {
        sys.error(s"Cannot generate a vpn list from hosts: $hosts")
      }
  }
}
