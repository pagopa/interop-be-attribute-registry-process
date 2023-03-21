package it.pagopa.interop.attributeregistryprocess.common.system

import com.typesafe.config.{Config, ConfigFactory}

object ApplicationConfiguration {
  val config: Config = ConfigFactory.load()

  val serverPort: Int          = config.getInt("attribute-registry-process.port")
  val jwtAudience: Set[String] =
    config.getString("attribute-registry-process.jwt.audience").split(",").toSet.filter(_.nonEmpty)

  val attributeRegistryManagementURL: String =
    config.getString("attribute-registry-process.services.attribute-registry-management")
  val partyProxyUrl: String                  = config.getString("services.party-proxy")
}
