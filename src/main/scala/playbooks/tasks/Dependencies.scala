package playbooks.tasks

import ansible.Modules.{Apt, AptRepository, Shell, User}
import ansible.Task
import playbooks.Conf.appName

object Dependencies {
   val appUser = Task(s"create user $appName",
    User(appName, state = Some(User.State.present))
  )

  val installGit = Task(
    "install git",
    Apt(name = Some("git"), state = Some(Apt.State.present)))

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

  val installJava8 = Task(
    "Instal java8",
    Apt(name = Some("oracle-java8-installer"), state = Some(Apt.State.present)))

  val updateAlternatives = Task(
    "update java alternatives",
    Shell("update-java-alternatives -s java-8-oracle")
  )

  val all = List(
    addRepo,
    debConf,
    installJava8,
    updateAlternatives
  )
}
