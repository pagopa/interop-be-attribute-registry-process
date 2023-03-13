package it.pagopa.interop.attributeregistryprocess.api.impl

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.onComplete
import akka.http.scaladsl.server.Route
import it.pagopa.interop.commons.utils.TypeConversions._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiService
import it.pagopa.interop.attributeregistryprocess.api.types.AttributeRegistryServiceTypes._
import it.pagopa.interop.attributeregistryprocess.error.ResponseHandlers._
import it.pagopa.interop.attributeregistryprocess.model.{Attribute, AttributeSeed, AttributesResponse, Problem}
import it.pagopa.interop.attributeregistryprocess.service._
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import it.pagopa.interop.commons.jwt.{ADMIN_ROLE, API_ROLE, authorize}
import it.pagopa.interop.commons.logging.{CanLogContextFields, ContextFieldsToLog}
import it.pagopa.interop.commons.utils.service.{OffsetDateTimeSupplier, UUIDSupplier}

import scala.concurrent.{ExecutionContext, Future}

final case class AttributeRegistryApiServiceImpl(
  attributeRegistryManagementService: AttributeRegistryManagementService,
  agreementManagementService: AgreementManagementService,
  catalogManagementService: CatalogManagementService,
  readModelService: ReadModelService,
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
  ): Route = authorize(ADMIN_ROLE, API_ROLE) {
    val operationLabel: String = s"Creating attribute with name ${attributeSeed.name}"
    logger.info(operationLabel)

    val result: Future[Attribute] = for {
      createdAttribute <- attributeRegistryManagementService.createAttribute(attributeSeed.toClient).map(_.toApi)
    } yield createdAttribute

    onComplete(result) {
      createAttributeResponse[Attribute](operationLabel) { res =>
        logger.info(s"Attribute created with id ${res.id}")
        createAttribute201(res)
      }
    }
  }

  override def deleteAttributeById(
    attributeId: String
  )(implicit contexts: Seq[(String, String)], toEntityMarshallerProblem: ToEntityMarshaller[Problem]): Route =
    authorize(ADMIN_ROLE, API_ROLE) {
      val operationLabel: String = s"Deleting attribute with ID $attributeId"
      logger.info(operationLabel)

      val result: Future[Unit] = for {
        attributeUUID <- attributeId.toFutureUUID
        result        <- attributeRegistryManagementService.deleteAttributeById(attributeUUID)
      } yield result

      onComplete(result) {
        deleteAttributeResponse[Unit](operationLabel)(_ => deleteAttributeById204)
      }
    }

  override def getAttributeById(attributeId: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE) {
    val operationLabel: String = s"Retrieving attribute with ID $attributeId"
    logger.info(operationLabel)

    val result: Future[Attribute] = for {
      attributeUUID <- attributeId.toFutureUUID
      result        <- attributeRegistryManagementService.getAttributeById(attributeUUID).map(_.toApi)
    } yield result

    onComplete(result) {
      getAttributeByIdResponse[Attribute](operationLabel) { res =>
        logger.info(s"Retrieved attribute with id ${res.id}")
        getAttributeById200(res)
      }
    }
  }

  override def getAttributeByName(name: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE) {
    val operationLabel: String = s"Retrieving attribute with name $name"
    logger.info(operationLabel)

    val result: Future[Attribute] = for {
      result <- attributeRegistryManagementService.getAttributeByName(name).map(_.toApi)
    } yield result

    onComplete(result) {
      getAttributeByNameResponse[Attribute](operationLabel) { res =>
        logger.info(s"Retrieved attribute with id${res.id}")
        getAttributeByName200(res)
      }
    }
  }

  override def getAttributeByOriginAndCode(origin: String, code: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE) {
    val operationLabel: String = s"Retrieving attribute $origin/$code"
    logger.info(operationLabel)

    val result: Future[Attribute] = for {
      result <- attributeRegistryManagementService.getAttributeByOriginAndCode(origin, code).map(_.toApi)
    } yield result

    onComplete(result) {
      getAttributeByOriginAndCodeResponse[Attribute](operationLabel) { res =>
        logger.info(s"Retrieved attribute with id${res.id}")
        getAttributeByOriginAndCode200(res)
      }
    }
  }

  override def getAttributes(search: Option[String])(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttributesResponse: ToEntityMarshaller[AttributesResponse],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE) {
    val operationLabel: String = s"Retrieving attributes by search string ${search.getOrElse("")}"
    logger.info(operationLabel)

    val result: Future[AttributesResponse] = for {
      result <- attributeRegistryManagementService.getAttributes(search).map(_.attributes.map(_.toApi))
    } yield AttributesResponse(attributes = result)

    onComplete(result) {
      getAttributesResponse[AttributesResponse](operationLabel)(getAttributes200)
    }
  }

  override def getBulkedAttributes(ids: Option[String])(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttributesResponse: ToEntityMarshaller[AttributesResponse]
  ): Route = authorize(ADMIN_ROLE, API_ROLE) {
    val operationLabel: String = s"Retrieving attributes in bulk by identifiers in (${ids.getOrElse("")})"
    logger.info(operationLabel)

    val result: Future[AttributesResponse] = for {
      result <- attributeRegistryManagementService.getBulkedAttributes(ids).map(_.attributes.map(_.toApi))
    } yield AttributesResponse(attributes = result)

    onComplete(result) {
      getBulkedAttributesResponse[AttributesResponse](operationLabel)(getBulkedAttributes200)
    }
  }

  override def loadCertifiedAttributes()(implicit contexts: Seq[(String, String)]): Route =
    authorize(ADMIN_ROLE, API_ROLE) {
      val operationLabel: String = s"Loading certified attributes from Party Registry"
      logger.info(operationLabel)

      val result: Future[Unit] = for {
        result <- attributeRegistryManagementService.loadCertifiedAttributes()
      } yield result

      onComplete(result) {
        loadCertifiedAttributesResponse[Unit](operationLabel)(_ => loadCertifiedAttributes200)
      }
    }
}
