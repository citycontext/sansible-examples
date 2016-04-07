package elasticsearch.task

import ansible.Modules._
import ansible.Task
import elasticsearch.Templates

class EsConfig(private val privateIps: Seq[String]) {
  val updateConfig = Task("update /etc/elasticsearch/elasticsearch.yml", Copy(
    dest = "/etc/elasticsearch/elasticsearch.yml",
    content = Some(Templates.etcElasticsearchYaml(privateIps))
  ))

  val restartService = Task("restart elasticsearch service", Service(
    name = "elasticsearch",
    state = Some(Service.State.restarted),
    enabled = Some(true)
  ))

  val all = List(updateConfig, restartService)
}
