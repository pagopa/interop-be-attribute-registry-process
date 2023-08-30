package it.pagopa.interop.attributeregistryprocess.api.impl

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.onComplete
import akka.http.scaladsl.server.Route
import cats.syntax.all._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import it.pagopa.interop.attributeregistrymanagement.model.persistence.attribute.PersistentAttributeKind
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiService
import it.pagopa.interop.attributeregistryprocess.api.types.AttributeRegistryServiceTypes._
import it.pagopa.interop.attributeregistryprocess.common.readmodel.ReadModelRegistryAttributeQueries
import it.pagopa.interop.attributeregistryprocess.error.ResponseHandlers._
import it.pagopa.interop.attributeregistryprocess.error.AttributeRegistryProcessErrors.{
  OrganizationIsNotACertifier,
  OriginIsNotCompliant
}
import it.pagopa.interop.attributeregistryprocess.model._
import it.pagopa.interop.attributeregistryprocess.service._
import it.pagopa.interop.tenantmanagement.model.tenant.PersistentTenantFeature
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import it.pagopa.interop.commons.jwt._
import it.pagopa.interop.commons.logging.{CanLogContextFields, ContextFieldsToLog}
import it.pagopa.interop.commons.utils.AkkaUtils._
import it.pagopa.interop.commons.utils.OpenapiUtils.parseArrayParameters
import it.pagopa.interop.commons.utils.TypeConversions._
import it.pagopa.interop.commons.utils.service.{OffsetDateTimeSupplier, UUIDSupplier}

import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

final case class AttributeRegistryApiServiceImpl(
  attributeRegistryManagementService: AttributeRegistryManagementService,
  tenantManagementService: TenantManagementService,
  uuidSupplier: UUIDSupplier,
  dateTimeSupplier: OffsetDateTimeSupplier
)(implicit ec: ExecutionContext, readModelService: ReadModelService)
    extends AttributeApiService {

  private implicit val logger: LoggerTakingImplicit[ContextFieldsToLog] =
    Logger.takingImplicit[ContextFieldsToLog](this.getClass)

  val IPA = "IPA"

  private def getCertifier(tenantId: UUID): Future[String] = for {
    tenant <- tenantManagementService.getTenantById(tenantId)
    certifier = tenant.features
      .collect { case f: PersistentTenantFeature.PersistentCertifier => f }
      .map(_.certifierId)
      .find(_.trim().nonEmpty)
    result <- certifier.toFuture(OrganizationIsNotACertifier(tenantId))
  } yield result

  override def createInternalCertifiedAttribute(attributeSeed: InternalCertifiedAttributeSeed)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(INTERNAL_ROLE) {
    val operationLabel: String =
      s"Creating certified attribute with origin ${attributeSeed.origin} and code ${attributeSeed.code} - Internal Request"
    logger.info(operationLabel)

    val result: Future[Attribute] =
      attributeRegistryManagementService.createAttribute(attributeSeed.toManagement).map(_.toApi)

    onComplete(result) {
      createInternalCertifiedAttributeResponse[Attribute](operationLabel) { res =>
        logger.info(s"Certified attribute created with id ${res.id} - Internal Request")
        createCertifiedAttribute200(res)
      }
    }
  }

  override def createCertifiedAttribute(attributeSeed: CertifiedAttributeSeed)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, M2M_ROLE) {
    val operationLabel: String = s"Creating certified attribute with code ${attributeSeed.code}"
    logger.info(operationLabel)

    val result: Future[Attribute] = for {
      requesterUuid <- getOrganizationIdFutureUUID(contexts)
      certifier     <- getCertifier(requesterUuid)
      attribute     <- attributeRegistryManagementService.createAttribute(attributeSeed.toManagement(certifier))
    } yield attribute.toApi

    onComplete(result) {
      createCertifiedAttributeResponse[Attribute](operationLabel) { res =>
        logger.info(s"Certified attribute created with id ${res.id}")
        createCertifiedAttribute200(res)
      }
    }
  }

  override def createDeclaredAttribute(attributeSeed: AttributeSeed)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, M2M_ROLE) {
    val operationLabel: String = s"Creating declared attribute with name ${attributeSeed.name}"
    logger.info(operationLabel)

    val result: Future[Attribute] = for {
      _         <- checkIPAOrganization(contexts)
      attribute <- attributeRegistryManagementService.createAttribute(
        attributeSeed.toManagement(AttributeKind.DECLARED)
      )
    } yield attribute.toApi

    onComplete(result) {
      createDeclaredAttributeResponse[Attribute](operationLabel) { res =>
        logger.info(s"Declared attribute created with id ${res.id}")
        createCertifiedAttribute200(res)
      }
    }
  }

  override def createVerifiedAttribute(attributeSeed: AttributeSeed)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, M2M_ROLE) {
    val operationLabel: String = s"Creating verified attribute with name ${attributeSeed.name}"
    logger.info(operationLabel)

    val result: Future[Attribute] = for {
      _         <- checkIPAOrganization(contexts)
      attribute <- attributeRegistryManagementService.createAttribute(
        attributeSeed.toManagement(AttributeKind.VERIFIED)
      )
    } yield attribute.toApi

    onComplete(result) {
      createVerifiedAttributeResponse[Attribute](operationLabel) { res =>
        logger.info(s"Declared attribute created with id ${res.id}")
        createCertifiedAttribute200(res)
      }
    }
  }

  private def checkIPAOrganization(contexts: Seq[(String, String)]): Future[Unit] = {
    for {
      origin <- getExternalIdOriginFuture(contexts)
      _      <- if (origin == IPA) Future.unit else Future.failed(OriginIsNotCompliant(IPA))
    } yield ()
  }

  override def getAttributeById(attributeId: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, SECURITY_ROLE, M2M_ROLE, SUPPORT_ROLE) {
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
  ): Route = authorize(ADMIN_ROLE, API_ROLE, SECURITY_ROLE, M2M_ROLE, SUPPORT_ROLE) {
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
  ): Route = authorize(ADMIN_ROLE, INTERNAL_ROLE, M2M_ROLE, SUPPORT_ROLE) {
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

  override def getAttributes(name: Option[String], origin: Option[String], limit: Int, offset: Int, kinds: String)(
    implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttributes: ToEntityMarshaller[Attributes]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, SECURITY_ROLE, M2M_ROLE, SUPPORT_ROLE) {
    val operationLabel =
      s"Getting attributes with name = $name, limit = $limit, offset = $offset, kinds = $kinds"
    logger.info(operationLabel)

    val result: Future[Attributes] = for {
      kindsList <- parseArrayParameters(kinds)
        .traverse(AttributeKind.fromValue(_).map(PersistentAttributeKind.fromApi))
        .toFuture
      result    <- ReadModelRegistryAttributeQueries.getAttributes(name, origin, kindsList, Nil, offset, limit)
    } yield Attributes(results = result.results.map(_.toApi), totalCount = result.totalCount)

    onComplete(result) {
      getAttributesResponse(operationLabel)(getAttributes200)
    }
  }

  override def getBulkedAttributes(limit: Int, offset: Int, ids: Seq[String])(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttributes: ToEntityMarshaller[Attributes]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, SECURITY_ROLE, M2M_ROLE, SUPPORT_ROLE) {
    val operationLabel = s"Retrieving attributes in bulk by id in [$ids]"
    logger.info(operationLabel)

    val result: Future[Attributes] =
      if (ids.isEmpty) Future.successful(Attributes(results = Seq.empty, totalCount = 0))
      else
        for {
          uuids  <- ids.toList.distinct.traverse(_.toFutureUUID)
          result <- ReadModelRegistryAttributeQueries.getAttributes(None, None, Nil, uuids, offset, limit)
        } yield Attributes(results = result.results.map(_.toApi), totalCount = result.totalCount)

    onComplete(result) {
      getAttributesResponse(operationLabel)(getAttributes200)
    }
  }
}
