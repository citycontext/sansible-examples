package elasticsearch

import ansible.Inventory._

object Inventory {
  object Hosts {
    val hostVars = HostVars(Map("ansible_ssh_user" -> "root"))

    val es1 = Hostname("sansible-es1", hostVars)
    val es2 = Hostname("sansible-es2", hostVars)
    val es3 = Hostname("sansible-es3", hostVars)

    val privateIps = Map(
      es1.id -> "private-ip1",
      es2.id -> "private-ip2",
      es3.id -> "private-ip3"
    )
  }

  object Groups {
    import Hosts._
    val es = Group("elasticsearch", List(es1, es2, es3))
  }

  val hosts = ansible.Inventory(List(Groups.es))
}
