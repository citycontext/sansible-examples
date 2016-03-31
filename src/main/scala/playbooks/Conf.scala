package playbooks

import java.io.File

import com.typesafe.config.ConfigFactory

object Conf {
  val userConfigFile = new File(s"${System.getProperty("user.dir")}/user.conf")
  private val userC = ConfigFactory.parseFile(userConfigFile)
  private val c = userC.withFallback(ConfigFactory.load())

  val awsAccessKey = c.getString("aws.access-key")
  val awsSecretKey = c.getString("aws.secret-key")

  val appName = "gitbucket"
}
