package elasticsearch

import ansible.Inventory.HostPattern
import ansible.Options.Become
import ansible.{Runner, Playbook}
import elasticsearch.task.{Config, Dependencies}

object ElasticSearch extends App {
  val playbook = Playbook(
    hosts = List(HostPattern(Inventory.Groups.es.name)),
    tasks = Dependencies.all ++ Config.all,
    options = Playbook.Options(become = Some(Become()))
  )

  Runner.runPlaybook(Inventory.hosts)(playbook)
}
