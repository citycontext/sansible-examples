package common.tasks

import ansible.Modules.{Apt, Copy, File, GetUrl}
import ansible.Task

class SBT(userName: String, userHome: String) {
  val version = "0.13.11"
  val sbtPluginDir = s"$userHome/.sbt/0.13/plugins"
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
    owner = Some(userName)
  ))

  val assemblyPlugin = Task("add sbt-assembly plugin", Copy(
    dest = s"$sbtPluginDir/build.sbt",
    owner = Some(userName),
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
