package it.pagopa.interop.attributeregistryprocess.common.system

import com.typesafe.config.{Config, ConfigFactory}
import it.pagopa.interop.commons.cqrs.model.ReadModelConfig

object ApplicationConfiguration {
  val config: Config = ConfigFactory.load()

  val serverPort: Int          = config.getInt("attribute-registry-process.port")
  val jwtAudience: Set[String] =
    config.getString("attribute-registry-process.jwt.audience").split(",").toSet.filter(_.nonEmpty)

  val attributeRegistryManagementURL: String =
    config.getString("attribute-registry-process.services.attribute-registry-management")
  val agreementProcessURL: String            = config.getString("attribute-registry-process.services.agreement-process")
  val agreementManagementURL: String = config.getString("attribute-registry-process.services.agreement-management")
  val catalogManagementURL: String   = config.getString("attribute-registry-process.services.catalog-management")
  val tenantManagementURL: String    = config.getString("attribute-registry-process.services.tenant-management")

  val readModelConfig: ReadModelConfig = {
    val connectionString: String = config.getString("attribute-registry-process.read-model.db.connection-string")
    val dbName: String           = config.getString("attribute-registry-process.read-model.db.name")

    ReadModelConfig(connectionString, dbName)
  }

  require(jwtAudience.nonEmpty, "Audience cannot be empty")
}
