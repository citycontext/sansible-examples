package gitbucket.tasks

import ansible.Modules._
import ansible.std._
import ansible.dsl._
import ansible.Task
import gitbucket.Conf.{appName, appUser}

object App {
  val warUrl = "https://github.com/gitbucket/gitbucket/releases/download/3.12/gitbucket.war"
  val warPath = s"/home/$appUser/gitbucket.war"
  val logPath = s"/var/log/$appName.log"

  val downloadWar: Task = Task("Download gitbucket war archive", GetUrl(
    url = warUrl,
    dest = warPath
  )).becoming(appUser)

  val logFile = Task("Create log file", File(
    logPath,
    state = Some(File.State.touch),
    owner = Some(appUser)
  ))

  val createUpstartTask = Task("create upstart task", Copy(
    dest = s"/etc/init/$appName.conf",
    content = Some(
      s"""
        |start on runlevel [2345]
        |stop on runlevel [!2345]
        |
        |script
        |  exec su -c "/usr/bin/java \\
        |    -jar $warPath %> $logPath" $appName
        |end script
      """.stripMargin)
  ))

  val startApp = Task("start app", Service(
    appName, state = Some(Service.State.started), enabled = Some(true)
  ))

  val all = List(
    downloadWar,
    logFile,
    createUpstartTask,
    startApp)
}
