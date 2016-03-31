package playbooks

import ansible.Inventory._

object Inventory {
  object Hosts {
    val api1 = Hostname("188.166.170.71", HostVars(Map("ansible_ssh_user" -> "root")))
  }

  object Groups {
    val web = Group("web", List(Hosts.api1))
  }

  val default = ansible.Inventory(List(Groups.web))
}
