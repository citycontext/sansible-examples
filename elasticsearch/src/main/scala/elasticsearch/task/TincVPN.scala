package elasticsearch.task


import java.nio.file.{Files, Paths}

import ansible.Modules._
import ansible.Options.{Sudo, Become}
import ansible.{Inventory, Task}
import elasticsearch.Templates
import Templates.{Tinc => TPL}

object TincVPN {
  case class Host(name: String,
                  publicIp: String,
                  privateIp: String,
                  subnet: String)

  object Host {
    def fromHostname(h: Inventory.Hostname): Option[Host] =
      for {
        name <- h.hostVars.get("vpn_nodename")
        privateIp <- h.hostVars.get("private_ip")
        subnet <- h.hostVars.get("vpn_subnet")
      } yield Host(name, h.id, privateIp, subnet)
  }
}

import TincVPN._
class TincVPN(netName: String, masterHost: Host, vpnHosts: List[Host]) {
  val msg = s"master host ${masterHost.name} is not in the vpn host list." ++
    s"vpn hosts are: ${vpnHosts.map(_.name).mkString(", ")}"

  require(vpnHosts.contains(masterHost), msg)

  private val etcPath = s"/etc/tinc/$netName"
  private val hostsPath = s"$etcPath/hosts"
  private val localKeyPath = s"/tmp/ansible-tinc-keys/"

  val install = Task("install tinc",
    Apt(name = Some("tinc"), state = Some(Apt.State.present))
  )

  val hostsDir = Task(s"create $hostsPath", File(
    path = hostsPath,
    state = Some(File.State.directory)
  ))

  def tincConf(h: Host) = {
    val connectTo =
      Some(masterHost).filterNot(_ => h == masterHost)

    Task(s"Update $etcPath/tinc.conf", Copy(
      dest = s"$etcPath/tinc.conf",
      content = Some(TPL.conf(h, connectTo))
    ))
  }

  def hostConf(h: Host) = Task(s"Create $etcPath/${h.name}.conf", Copy(
    dest = s"$hostsPath/${h.name}",
    content = Some(TPL.host(h))
  ))

  val genKeyPair = Task("Generate tinc key pair", Shell(
    free_form = s"tincd -n $netName -K4096"
  ), Task.Options(become = Some(Become("root", Sudo))))

  def fetchKeyPair(host: Host) = {
    val dirPath = Paths.get(localKeyPath)
    if (!Files.isDirectory(dirPath)) Files.createDirectory(dirPath)
    Task("fetch tinc keys", Fetch(
      src = s"$hostsPath/${host.name}",
      fail_on_missing = Some(true),
      flat = Some("yes"),
      dest = localKeyPath
    ))
  }

  def uploadKeyPairs = vpnHosts.map(h => Task(s"distribute pub key for host: ${h.name}", Copy(
    dest = s"$hostsDir/${h.name}",
    src = Some(s"$localKeyPath/${h.name}"
  ))))

  def tincUp(host: Host) = Task(s"create $etcPath/tinc-up", Copy(
    dest = s"$etcPath/tinc-up",
    content = Some(TPL.up(host)),
    mode = Some("755")
  ))

  def tincDown(host: Host) = Task(s"create $etcPath/tinc-down", Copy(
    dest = s"$etcPath/tinc-down",
    content = Some(
      """#!/bin/sh
        |ifconfig $INTERFACE down
      """.stripMargin),
    mode = Some("755")
  ))

  def forHost(h: Host): List[Task] = {
    List(
      install,
      hostsDir,
      tincConf(h),
      hostConf(h),
      genKeyPair,
      fetchKeyPair(h)
    )
  }
}
