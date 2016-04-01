package elasticsearch.task

import ansible.Modules._
import ansible.Task
import elasticsearch.Templates
import elasticsearch.Inventory.esGroupName

class EsConfig(inventory: ansible.Inventory) {
  private val privateIps =
    inventory.group(esGroupName).map(_.hostnames.flatMap(_.hostVars.get("private_ip"))).
      getOrElse(Nil)

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
