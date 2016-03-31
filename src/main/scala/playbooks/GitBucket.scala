package playbooks

import ansible.Inventory.HostPattern
import ansible.Options.Become
import ansible.{Playbook, Runner}
import playbooks.tasks._

object GitBucket extends App {
  val playbook = Playbook(
    hosts = List(HostPattern(Inventory.Groups.web.name)),
    tasks = Dependencies.all ++ App.all,
    options = Playbook.Options(become = Some(Become(None, None)))
  )

  Runner.runPlaybook(Inventory.default)(playbook)
}
