package elasticsearch

import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import com.myjeeva.digitalocean.impl.DigitalOceanClient

object Config {
  private val c = ConfigFactory.load
  val templateDir = "templates"

  object DigitaOcean {
    val token = c.getString("digitalocean.token")
    val dropletPrefix = c.getString("digitalocean.droplet-prefix")
    val apiClient = new DigitalOceanClient(token)
  }
}