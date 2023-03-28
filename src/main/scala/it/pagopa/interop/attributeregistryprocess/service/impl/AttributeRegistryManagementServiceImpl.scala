package it.pagopa.interop.attributeregistryprocess.service.impl

import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import it.pagopa.interop.attributeregistrymanagement.client.api.{AttributeApi => AttributeRegistryManagementApi}
import it.pagopa.interop.attributeregistrymanagement.client.invoker.{
  ApiError,
  ApiRequest,
  BearerToken,
  ApiInvoker => AttributeRegistryManagementInvoker
}
import it.pagopa.interop.attributeregistrymanagement.client.model.{Attribute, AttributeSeed}
import it.pagopa.interop.attributeregistryprocess.error.AttributeRegistryProcessErrors.RegistryAttributeNotFound
import it.pagopa.interop.attributeregistryprocess.service.AttributeRegistryManagementService
import it.pagopa.interop.commons.logging.{CanLogContextFields, ContextFieldsToLog}
import it.pagopa.interop.commons.utils.withHeaders

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

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
    invoker.invoke(request, s"Attribute creation with name ${attributeSeed.name}")
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
}
