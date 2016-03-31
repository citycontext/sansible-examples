package playbooks.tasks

import ansible.Modules.{Copy, File, Apt, GetUrl}
import ansible.Task
import playbooks.Conf.appName

object SBT {
  val version = "0.13.11"
  val sbtPluginDir = s"/home/$appName/.sbt/0.13/plugins"
  val dest = s"/tmp/sbt-$version.deb"
  val download = Task("Download sbt .deb package", GetUrl(
    url = s"https://dl.bintray.com/sbt/debian/sbt-$version.deb",
    dest = dest
  ))

  val install =
    Task("Install sbt package",
      Apt(deb = Some(dest), state = Some(Apt.State.present)))

  val pluginDir = Task("create sbt plugin dir", File(
    path = sbtPluginDir,
    state = Some(File.State.directory),
    owner = Some(appName)
  ))

  val assemblyPlugin = Task("add sbt-assembly plugin", Copy(
    dest = s"$sbtPluginDir/build.sbt",
    owner = Some(appName),
    content = Some(
      """
        |addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.2")
      """.stripMargin)
  ))

  val all = List(
    download,
    install,
    pluginDir,
    assemblyPlugin
  )
}
