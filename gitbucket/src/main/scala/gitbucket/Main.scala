package gitbucket

import ansible.Inventory.HostPattern
import ansible.{Playbook, Runner}
import ansible.std._
import ansible.dsl._
import gitbucket.tasks._

object Main extends App {
  val playbook = Playbook(
    hosts = List(HostPattern(Inventory.Groups.web.name)),
    tasks = Dependencies.all ++ App.all
  ).usingSudo

  Runner.runPlaybook(Inventory.hosts)(playbook)
}
