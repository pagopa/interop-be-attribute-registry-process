package it.pagopa.interop.attributeregistryprocess.api.impl

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.onComplete
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiService
import it.pagopa.interop.attributeregistryprocess.api.types.AttributeRegistryServiceTypes._
import it.pagopa.interop.attributeregistryprocess.error.ResponseHandlers._
import it.pagopa.interop.attributeregistryprocess.model.{Attribute, AttributeSeed, Attributes, Problem}
import it.pagopa.interop.attributeregistryprocess.service._
import it.pagopa.interop.commons.jwt.{ADMIN_ROLE, API_ROLE, INTERNAL_ROLE, M2M_ROLE, SECURITY_ROLE, authorize}
import it.pagopa.interop.commons.logging.{CanLogContextFields, ContextFieldsToLog}
import it.pagopa.interop.commons.utils.TypeConversions._
import it.pagopa.interop.commons.utils.service.{OffsetDateTimeSupplier, UUIDSupplier}

import scala.concurrent.{ExecutionContext, Future}

final case class AttributeRegistryApiServiceImpl(
  attributeRegistryManagementService: AttributeRegistryManagementService,
  uuidSupplier: UUIDSupplier,
  dateTimeSupplier: OffsetDateTimeSupplier
)(implicit ec: ExecutionContext)
    extends AttributeApiService {

  private implicit val logger: LoggerTakingImplicit[ContextFieldsToLog] =
    Logger.takingImplicit[ContextFieldsToLog](this.getClass)

  override def createAttribute(attributeSeed: AttributeSeed)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, M2M_ROLE) {
    val operationLabel: String = s"Creating attribute with name ${attributeSeed.name}"
    logger.info(operationLabel)

    val result: Future[Attribute] =
      attributeRegistryManagementService.createAttribute(attributeSeed.toClient).map(_.toApi)

    onComplete(result) {
      createAttributeResponse[Attribute](operationLabel) { res =>
        logger.info(s"Attribute created with id ${res.id}")
        createAttribute200(res)
      }
    }
  }

  override def getAttributeById(attributeId: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, SECURITY_ROLE, M2M_ROLE) {
    val operationLabel: String = s"Retrieving attribute with ID $attributeId"
    logger.info(operationLabel)

    val result: Future[Attribute] = for {
      attributeUUID <- attributeId.toFutureUUID
      result        <- attributeRegistryManagementService.getAttributeById(attributeUUID).map(_.toApi)
    } yield result

    onComplete(result) {
      getAttributeByIdResponse[Attribute](operationLabel) { res =>
        getAttributeById200(res)
      }
    }
  }

  override def getAttributeByName(name: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, SECURITY_ROLE, M2M_ROLE) {
    val operationLabel: String = s"Retrieving attribute with name $name"
    logger.info(operationLabel)

    val result: Future[Attribute] = for {
      result <- attributeRegistryManagementService.getAttributeByName(name).map(_.toApi)
    } yield result

    onComplete(result) {
      getAttributeByNameResponse[Attribute](operationLabel) { res =>
        getAttributeByName200(res)
      }
    }
  }

  override def getAttributeByOriginAndCode(origin: String, code: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, INTERNAL_ROLE, M2M_ROLE) {
    val operationLabel: String = s"Retrieving attribute $origin/$code"
    logger.info(operationLabel)

    val result: Future[Attribute] =
      attributeRegistryManagementService.getAttributeByOriginAndCode(origin, code).map(_.toApi)

    onComplete(result) {
      getAttributeByOriginAndCodeResponse[Attribute](operationLabel) { res =>
        getAttributeByOriginAndCode200(res)
      }
    }
  }

  override def getBulkedAttributes(ids: Option[String])(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttributesResponse: ToEntityMarshaller[Attributes]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, SECURITY_ROLE, M2M_ROLE) {
    val operationLabel: String = s"Retrieving attributes in bulk by identifiers in (${ids.getOrElse("")})"
    logger.info(operationLabel)

    val result: Future[Attributes] = for {
      result <- attributeRegistryManagementService.getBulkedAttributes(ids).map(_.attributes.map(_.toApi))
    } yield Attributes(attributes = result)

    onComplete(result) {
      getBulkedAttributesResponse[Attributes](operationLabel)(getBulkedAttributes200)
    }
  }

  override def getAttributes(
    search: Option[String]
  )(implicit contexts: Seq[(String, String)], toEntityMarshallerAttributes: ToEntityMarshaller[Attributes]): Route =
    authorize(ADMIN_ROLE, API_ROLE, SECURITY_ROLE, M2M_ROLE) {
      val operationLabel: String = s"Retrieving attributes by search string ${search.getOrElse("")}"
      logger.info(operationLabel)

      val result: Future[Attributes] = for {
        result <- attributeRegistryManagementService.getAttributes(search).map(_.attributes.map(_.toApi))
      } yield Attributes(attributes = result)

      onComplete(result) {
        getAttributesResponse[Attributes](operationLabel)(getAttributes200)
      }
    }
}
