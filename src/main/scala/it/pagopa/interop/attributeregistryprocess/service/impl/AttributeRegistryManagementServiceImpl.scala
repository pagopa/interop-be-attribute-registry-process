package it.pagopa.interop.attributeregistryprocess.service.impl

import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import it.pagopa.interop.attributeregistrymanagement.client.invoker.{ApiError, ApiRequest, BearerToken}
import it.pagopa.interop.attributeregistrymanagement.client.model.{Attribute, AttributeSeed, AttributesResponse}
import it.pagopa.interop.commons.logging.{CanLogContextFields, ContextFieldsToLog}
import it.pagopa.interop.commons.utils.withHeaders
import it.pagopa.interop.attributeregistryprocess.error.AttributeRegistryProcessErrors.RegistryAttributeNotFound
import it.pagopa.interop.attributeregistryprocess.service.{
  AttributeRegistryManagementApi,
  AttributeRegistryManagementInvoker,
  AttributeRegistryManagementService
}

import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

final case class AttributeRegistryManagementServiceImpl(
  invoker: AttributeRegistryManagementInvoker,
  api: AttributeRegistryManagementApi
)(implicit ec: ExecutionContext)
    extends AttributeRegistryManagementService {

  implicit val logger: LoggerTakingImplicit[ContextFieldsToLog] =
    Logger.takingImplicit[ContextFieldsToLog](this.getClass)

  override def getAttributeById(id: UUID)(implicit contexts: Seq[(String, String)]): Future[Attribute] = withHeaders {
    (bearerToken, correlationId, ip) =>
      val request: ApiRequest[Attribute] =
        api.getAttributeById(xCorrelationId = correlationId, attributeId = id, xForwardedFor = ip)(
          BearerToken(bearerToken)
        )
      invoker.invoke(request, s"Retrieving Attribute $id").recoverWith {
        case err: ApiError[_] if err.code == 404 =>
          Future.failed(RegistryAttributeNotFound(id.toString))
      }
  }

  override def createAttribute(
    attributeSeed: AttributeSeed
  )(implicit contexts: Seq[(String, String)]): Future[Attribute] = withHeaders { (bearerToken, correlationId, ip) =>
    val request: ApiRequest[Attribute] =
      api.createAttribute(xCorrelationId = correlationId, attributeSeed = attributeSeed, xForwardedFor = ip)(
        BearerToken(bearerToken)
      )
    invoker.invoke(request, s"Attribute with name ${attributeSeed.name} created")
  }

  override def deleteAttributeById(attributeId: UUID)(implicit contexts: Seq[(String, String)]): Future[Unit] =
    withHeaders { (bearerToken, correlationId, ip) =>
      val request: ApiRequest[Unit] =
        api.deleteAttributeById(xCorrelationId = correlationId, attributeId = attributeId, xForwardedFor = ip)(
          BearerToken(bearerToken)
        )
      invoker.invoke(request, s"Deleting attribute with ID $attributeId").recoverWith {
        case err: ApiError[_] if err.code == 404 =>
          Future.failed(RegistryAttributeNotFound(attributeId.toString))
      }
    }

  override def getAttributeByName(name: String)(implicit contexts: Seq[(String, String)]): Future[Attribute] =
    withHeaders { (bearerToken, correlationId, ip) =>
      val request: ApiRequest[Attribute] =
        api.getAttributeByName(xCorrelationId = correlationId, name = name, xForwardedFor = ip)(
          BearerToken(bearerToken)
        )
      invoker.invoke(request, s"Retrieving Attribute $name").recoverWith {
        case err: ApiError[_] if err.code == 404 =>
          Future.failed(RegistryAttributeNotFound(name))
      }
    }

  override def getAttributeByOriginAndCode(origin: String, code: String)(implicit
    contexts: Seq[(String, String)]
  ): Future[Attribute] = withHeaders { (bearerToken, correlationId, ip) =>
    val request: ApiRequest[Attribute] =
      api.getAttributeByOriginAndCode(xCorrelationId = correlationId, origin = origin, code = code, xForwardedFor = ip)(
        BearerToken(bearerToken)
      )
    invoker.invoke(request, s"Retrieving Attribute $origin/$code").recoverWith {
      case err: ApiError[_] if err.code == 404 =>
        Future.failed(RegistryAttributeNotFound(origin + '/' + code))
    }
  }

  override def getAttributes(search: Option[String])(implicit
    contexts: Seq[(String, String)]
  ): Future[AttributesResponse] = withHeaders { (bearerToken, correlationId, ip) =>
    val request: ApiRequest[AttributesResponse] =
      api.getAttributes(xCorrelationId = correlationId, search = search, xForwardedFor = ip)(BearerToken(bearerToken))
    invoker.invoke(request, s"Retrieving attributes by search string ${search.getOrElse("")}")
  }

  override def getBulkedAttributes(ids: Option[String])(implicit
    contexts: Seq[(String, String)]
  ): Future[AttributesResponse] = withHeaders { (bearerToken, correlationId, ip) =>
    val request: ApiRequest[AttributesResponse] =
      api.getBulkedAttributes(xCorrelationId = correlationId, ids = ids, xForwardedFor = ip)(BearerToken(bearerToken))
    invoker.invoke(request, s"Retrieving attributes in bulk by identifiers in (${ids.getOrElse("")})")
  }

  override def loadCertifiedAttributes()(implicit contexts: Seq[(String, String)]): Future[Unit] = withHeaders {
    (bearerToken, correlationId, ip) =>
      val request: ApiRequest[Unit] =
        api.loadCertifiedAttributes(xCorrelationId = correlationId, xForwardedFor = ip)(BearerToken(bearerToken))
      invoker.invoke(request, s"Loading certified attributes from Party Registry")
  }
}
