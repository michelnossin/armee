package io.armee.config

import java.io.{File, FileInputStream}

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import scala.beans.BeanProperty

class YamlConfig {
  @BeanProperty var shellPort = 1336
  @BeanProperty var masterPort = 1337
  @BeanProperty var workerPort = 1338
  @BeanProperty var apiPort = 1335
  @BeanProperty var masterServer = "127.0.0.1"
  @BeanProperty var numExecutorsPerServer = 4
  @BeanProperty var awsAccessKey = ""
  @BeanProperty var awsSecretKey = ""

  def readConfig() : YamlConfig = {

    //load config yaml file, using some java code due to The Snake Yaml lib
    val fileName = "config.yaml"
    val classLoader = getClass().getClassLoader();

    val input : FileInputStream =
      try {
        new FileInputStream(new File(fileName))
    }
    catch {
      case x: java.io.FileNotFoundException => {
        println("config.yaml in current dir not found. Using file in resource directory: ")
        new FileInputStream(classLoader.getResource(fileName).getFile());
      }
    }
    

    val yaml = new Yaml(new Constructor(classOf[YamlConfig]))
    val e = yaml.load(input).asInstanceOf[YamlConfig]

    println("Configuration loaded:")
    println(e)
    println("")
    e
  }

  override def toString: String = s"executors : $numExecutorsPerServer, apiPort: $apiPort, shellPort: $shellPort, workerPort: $workerPort, masterPort: $masterPort, masterServer: $masterServer"
}
