package it.pagopa.interop.attributeregistryprocess

import akka.actor.ActorSystem
import it.pagopa.interop._
import it.pagopa.interop.partyregistryproxy.client.invoker.{ApiInvoker => PartyProxyInvoker}
import it.pagopa.interop.attributeregistrymanagement.client.invoker.{ApiInvoker => AttributeRegistryManagementInvoker}
import it.pagopa.interop.attributeregistrymanagement.client.api.{AttributeApi => AttributeRegistryManagementApi}

import scala.concurrent.ExecutionContextExecutor

package object service {
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

  object PartyProxyInvoker {
    def apply(blockingEc: ExecutionContextExecutor)(implicit actorSystem: ActorSystem): PartyProxyInvoker =
      PartyProxyInvoker(blockingEc)(actorSystem)
  }
}
