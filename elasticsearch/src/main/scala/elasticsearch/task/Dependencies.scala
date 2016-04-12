package elasticsearch.task

import ansible.Modules.{Apt, AptKey, AptRepository}
import ansible.Task
import common.tasks.Java8

object Dependencies {
  val addEsKey = Task("add elasticsearch key", AptKey(
    url = Some("https://packages.elastic.co/GPG-KEY-elasticsearch"),
    state = Some(AptKey.State.present)
  ))

  val addRepo = Task("add elasticsearch repo", AptRepository(
    repo = "deb http://packages.elastic.co/elasticsearch/2.x/debian stable main",
    state = Some(AptRepository.State.present)
  ))

  val aptUpdate = Task("run apt-get update", Apt(
    update_cache = Some(true)
  ))

  val install = Task("install elasticsearch package", Apt(
    name = Some("elasticsearch"),
    state = Some(Apt.State.present),
    force = Some(true)
  ))

  val all = Java8.all ++ List(addEsKey, addRepo, install)
}
