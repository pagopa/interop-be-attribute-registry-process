package it.pagopa.interop.attributeregistryprocess

import akka.actor.ActorSystem
import it.pagopa.interop._

import scala.concurrent.ExecutionContextExecutor

package object service {
  type AttributeRegistryManagementInvoker = attributeregistrymanagement.client.invoker.ApiInvoker
  type AttributeRegistryManagementApi     = attributeregistrymanagement.client.api.AttributeApi

  object AttributeRegistryManagementInvoker {
    def apply(
      blockingEc: ExecutionContextExecutor
    )(implicit actorSystem: ActorSystem): AttributeRegistryManagementInvoker =
      attributeregistrymanagement.client.invoker
        .ApiInvoker(attributeregistrymanagement.client.api.EnumsSerializers.all, blockingEc)
  }

  object AttributeRegistryManagementApi {
    def apply(baseUrl: String): AttributeRegistryManagementApi =
      attributeregistrymanagement.client.api.AttributeApi(baseUrl)
  }
}
