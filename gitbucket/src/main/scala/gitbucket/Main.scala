package gitbucket

import ansible.Inventory.HostPattern
import ansible.Options.Become
import ansible.{Playbook, Runner}
import gitbucket.tasks._

object Main extends App {
  val playbook = Playbook(
    hosts = List(HostPattern(Inventory.Groups.web.name)),
    tasks = Dependencies.all ++ App.all,
    options = Playbook.Options(become = Some(Become()))
  )

  Runner.runPlaybook(Inventory.hosts)(playbook)
}
