package elasticsearch.task

import better.files.{File => F}
import ansible.Modules._
import ansible.{Inventory, Task}
import ansible.std._
import ansible.dsl._
import elasticsearch.Templates
import Templates.{Tinc => TPL}

object TincVPN {
  case class Host(name: String,
                  publicIp: String,
                  privateIp: String,
                  subnetIp: String)

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
  private def hostFile(h: Host) = s"$hostsPath/${h.name}"
  private val localKeyPath = F("/tmp/ansible-tinc-keys/")

  private val install = Task("install tinc",
    Apt(name = Some("tinc"), state = Some(Apt.State.present))
  )

  private val hostsDir = Task(s"create $hostsPath", File(
    path = hostsPath,
    state = Some(File.State.directory)
  ))

  private def tincConf(h: Host) = {
    val connectTo =
      Some(masterHost).filterNot(_ => h == masterHost)

    Task(s"Update $etcPath/tinc.conf", Copy(
      dest = s"$etcPath/tinc.conf",
      content = Some(TPL.conf(h, connectTo))
    ))
  }

  private def hostConf(h: Host) = Task(s"Create ${hostFile(h)}", Copy(
    dest = hostFile(h),
    content = Some(TPL.host(h))
  ))

  private val genKeyPair = Task("Generate tinc key pair", Shell(
    free_form = s"tincd -n $netName -K",
    creates = Some(s"$etcPath/rsa_key.priv")
  )).becoming("root")

  private def fetchKeyPair(h: Host) =
    Task("fetch tinc keys", Fetch(
      src = hostFile(h),
      fail_on_missing = Some(true),
      flat = Some("yes"),
      dest = (localKeyPath / h.name).toString
    ))

  private def tincUp(host: Host) = Task(s"create $etcPath/tinc-up", Copy(
    dest = s"$etcPath/tinc-up",
    content = Some(TPL.up(host)),
    mode = Some("755")
  ))

  private def tincDown = Task(s"create $etcPath/tinc-down", Copy(
    dest = s"$etcPath/tinc-down",
    content = Some(
      """#!/bin/sh
        |ifconfig $INTERFACE down
      """.stripMargin),
    mode = Some("755")
  ))

  private val enableNetwork = Task(s"adding $netName to /etc/tinc/nets.boot", Lineinfile(
    dest = "/etc/tinc/nets.boot",
    line = Some(netName)
  ).withState(Lineinfile.State.present))

  private val startService =
    Task("start tinc", Service(name = "tinc").withState(Service.State.started))

  def initLocalKeyDir(): Unit = {
    if (localKeyPath.exists) localKeyPath.delete()
    localKeyPath.createDirectory()
  }

  def configHost(h: Host): List[Task] = {
    List(
      install,
      hostsDir,
      tincConf(h),
      hostConf(h),
      tincUp(h),
      tincDown,
      genKeyPair,
      fetchKeyPair(h)
    )
  }

  def distributeKeys = vpnHosts.map(h => Task(s"distribute pub key for host: ${h.name}", Copy(
    dest = hostFile(h),
    src = Some(s"$localKeyPath/${h.name}"
  ))))

  def start = List(enableNetwork, startService)
}
