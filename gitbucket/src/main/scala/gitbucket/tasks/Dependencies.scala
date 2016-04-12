package gitbucket.tasks

import ansible.Modules.{Apt, User}
import ansible.Task
import common.tasks.Java8
import gitbucket.Conf.appName

object Dependencies {
   val appUser = Task(s"create user $appName",
    User(appName).withState(User.State.present)
  )

  val installGit = Task(
    "install git",
    Apt(name = Some("git")).withState(Apt.State.present))

  val all = List(appUser, installGit) ++ Java8.all
}
