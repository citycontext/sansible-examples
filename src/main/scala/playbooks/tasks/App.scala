package playbooks.tasks

import ansible.Modules._
import ansible.Options.{Sudo, Become}
import ansible.Task
import ansible.Task.Options
import playbooks.Conf.appName

object App {
  val warUrl = "https://github.com/gitbucket/gitbucket/releases/download/3.12/gitbucket.war"
  val warPath = s"/home/$appName/gitbucket.war"
  val logPath = s"/var/log/$appName.log"

  def asAppUser(t: Task): Task =
    t.copy(options = t.options.copy(
      become = Some(Become(appName, Sudo))))

  val downloadWar = Task("Download gitbucket war archive", GetUrl(
    url = warUrl,
    dest = warPath
  ))

  val logFile = Task("Create log file", File(
    logPath,
    state = Some(File.State.touch),
    owner = Some(appName)
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
    appName, state = Some(Service.State.started)
  ))

  val all = List(
    asAppUser(downloadWar),
    logFile,
    createUpstartTask,
    startApp)
}
