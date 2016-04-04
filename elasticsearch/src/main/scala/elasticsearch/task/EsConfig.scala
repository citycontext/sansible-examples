package elasticsearch.task

import ansible.Modules._
import ansible.Task
import elasticsearch.Templates
import elasticsearch.Inventory.esGroupName

class EsConfig(inventory: ansible.Inventory) {
  private val privateIps = inventory.hostVarValues(esGroupName, "private_ip")

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
