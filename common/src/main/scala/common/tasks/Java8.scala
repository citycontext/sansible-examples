package common.tasks

import ansible.Modules.{Apt, AptRepository, Shell}
import ansible.Task

object Java8 {
  val addRepo = Task(
    "add Java8 repo",
    AptRepository(
      repo = "ppa:webupd8team/java",
      update_cache = Some(true))
  )

  val debConf = Task(
    "accept Java8 license agreement",
    Shell("echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections")
  )

  val aptUpdate = Task("run apt-get update", Apt(
    update_cache = Some(true)
  ))

  val installJava8 = Task(
    "Instal java8",
    Apt(name = Some("oracle-java8-installer")).withState(Apt.State.present))

  val updateAlternatives = Task(
    "update java alternatives",
    Shell("update-java-alternatives -s java-8-oracle")
  )

  val all =  List(
    addRepo,
    debConf,
    aptUpdate,
    installJava8,
    updateAlternatives
  )
}
