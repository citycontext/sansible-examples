package gitbucket

import ansible.Inventory._

object Inventory {
  object Hosts {
    val api1 = Hostname("sansible-web1", HostVars(Map("ansible_ssh_user" -> "root")))
  }

  object Groups {
    val web = Group("web", List(Hosts.api1))
  }

  val hosts = ansible.Inventory(List(Groups.web))
}
