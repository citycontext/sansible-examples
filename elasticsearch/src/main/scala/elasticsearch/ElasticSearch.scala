package elasticsearch

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import ansible.Inventory.HostPattern
import ansible.Options.Become
import ansible.{Runner, Playbook}
import elasticsearch.task.{EsConfig, Dependencies}


object ElasticSearch extends App {
  Inventory.fromDigitalOcean.onComplete {
    case Failure(err) =>
      sys.error(s"Failed to generate inventory from digital ocean: ${err.getMessage}")
    case Success(hosts) =>
      val playbook = Playbook(
        hosts = List(HostPattern(Inventory.esGroupName)),
        tasks = Dependencies.all ++ new EsConfig(hosts).all,
        options = Playbook.Options(become = Some(Become()))
      )
      Runner.runPlaybook(hosts)(playbook)
  }
}
