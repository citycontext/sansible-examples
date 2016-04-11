package elasticsearch

import elasticsearch.task.{Dependencies, TincVPN, EsConfig}

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import ansible.Inventory.HostPattern
import ansible.std._
import ansible.dsl._
import ansible.{Runner, Playbook}
import Inventory.esGroupName

object Main extends App {
  Inventory.fromDigitalOcean.onComplete {
    case Failure(err) =>
      sys.error(s"Failed to generate inventory from Digital Ocean droplets: ${err.getMessage}")

    case Success(inv) =>
      val run      = Runner.runPlaybook(inv)(_: Playbook, None)
      val hosts    = inv.groupHosts(esGroupName)
      val vpnHosts = hosts.flatMap(TincVPN.Host.fromHostname)
      val vpnIps   = vpnHosts.map(_.subnetIp)

      if (vpnHosts.nonEmpty) {
        val vpn = new TincVPN(esGroupName, vpnHosts.head, vpnHosts)
        vpn.initLocalKeyDir()

        vpnHosts.foreach { h =>
          run(Playbook(
            hosts = List(HostPattern(h.publicIp)),
            tasks = vpn.configHost(h)
          ).usingSudo)
        }
        run(Playbook(
          hosts = List(HostPattern(esGroupName)),
          tasks = vpn.distributeKeys ++ vpn.start
        ))
        run(Playbook(
          hosts = List(HostPattern(esGroupName)),
          tasks = Dependencies.all ++ EsConfig(vpnIps).all
        ))
      }
      else sys.error(s"Cannot generate a non-empty vpn list from hosts: $hosts")
  }
}
