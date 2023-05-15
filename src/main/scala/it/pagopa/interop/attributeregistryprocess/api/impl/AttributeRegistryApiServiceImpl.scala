package it.pagopa.interop.attributeregistryprocess.api.impl

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.onComplete
import akka.http.scaladsl.server.Route
import cats.syntax.all._
import com.mongodb.client.model.Filters
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import it.pagopa.interop.attributeregistrymanagement.model.persistence.JsonFormats.paFormat
import it.pagopa.interop.attributeregistrymanagement.model.persistence.attribute.{
  PersistentAttribute,
  PersistentAttributeKind
}
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiService
import it.pagopa.interop.attributeregistryprocess.api.types.AttributeRegistryServiceTypes._
import it.pagopa.interop.attributeregistryprocess.common.readmodel.ReadModelQueries
import it.pagopa.interop.attributeregistryprocess.error.ResponseHandlers._
import it.pagopa.interop.attributeregistryprocess.model._
import it.pagopa.interop.attributeregistryprocess.service._
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import it.pagopa.interop.commons.jwt._
import it.pagopa.interop.commons.logging.{CanLogContextFields, ContextFieldsToLog}
import it.pagopa.interop.commons.utils.OpenapiUtils.parseArrayParameters
import it.pagopa.interop.commons.utils.TypeConversions._
import it.pagopa.interop.commons.utils.service.{OffsetDateTimeSupplier, UUIDSupplier}

import scala.concurrent.{ExecutionContext, Future}

final case class AttributeRegistryApiServiceImpl(
  attributeRegistryManagementService: AttributeRegistryManagementService,
  uuidSupplier: UUIDSupplier,
  dateTimeSupplier: OffsetDateTimeSupplier,
  partyRegistryService: PartyRegistryService,
  readModelService: ReadModelService
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

  override def loadCertifiedAttributes()(implicit contexts: Seq[(String, String)]): Route =
    authorize(INTERNAL_ROLE) {
      val operationLabel: String = s"Loading certified attributes from Party Registry"
      logger.info(operationLabel)

      val result: Future[Unit] = for {
        categories <- getAllPages(50)((page, limit) =>
          partyRegistryService.getCategories(Some(page), Some(limit)).map(_.items)
        )
        attributeSeedsCategories   = categories.map(c =>
          AttributeSeed(
            code = Option(c.code),
            kind = AttributeKind.CERTIFIED,
            description = c.name, // passing the name since no description exists at party-registry-proxy
            origin = Option(c.origin),
            name = c.name
          )
        )
        institutions <- getAllPages(50)((page, limit) =>
          partyRegistryService.getInstitutions(Some(page), Some(limit)).map(_.items)
        )
        attributeSeedsInstitutions = institutions.map(i =>
          AttributeSeed(
            code = Option(i.originId),
            kind = AttributeKind.CERTIFIED,
            description = i.description,
            origin = Option(i.origin),
            name = i.description
          )
        )

        _ <- addNewAttributes(attributeSeedsCategories ++ attributeSeedsInstitutions)
      } yield ()

      onComplete(result) {
        loadCertifiedAttributesResponse[Unit](operationLabel)(_ => loadCertifiedAttributes204)
      }
    }

  private def addNewAttributes(
    attributesSeeds: Seq[AttributeSeed]
  )(implicit contexts: Seq[(String, String)]): Future[Unit] = {

    // calculating the delta of attributes
    def delta(attrs: List[Attribute]): Set[AttributeSeed] =
      attributesSeeds.foldLeft[Set[AttributeSeed]](Set.empty)((attributesDelta, seed) =>
        attrs
          .find(persisted => seed.origin == persisted.origin && seed.code == persisted.code)
          .fold(attributesDelta + seed)(_ => attributesDelta)
      )

    // create all new attributes
    for {
      attributesfromRM <- getAll(50)(readModelService.find[PersistentAttribute]("attributes", Filters.empty(), _, _))
      deltaAttributes = delta(attributesfromRM.map(_.toApi).toList)
      // The client must log in case of errors
      _ <- Future.parCollectWithLatch(50)(deltaAttributes.toList)(attributeSeed =>
        attributeRegistryManagementService.createAttribute(attributeSeed.toClient)
      )
    } yield ()

  }

  private def getAllPages[T](limit: Int)(get: (Int, Int) => Future[Seq[T]]): Future[Seq[T]] = {
    def go(page: Int)(acc: Seq[T]): Future[Seq[T]] = {
      get(page, limit).flatMap(xs =>
        if (xs.size < limit) Future.successful(xs ++ acc)
        else go(page + 1)(xs ++ acc)
      )
    }

    go(1)(Nil)
  }

  private def getAll[T](limit: Int)(get: (Int, Int) => Future[Seq[T]]): Future[Seq[T]] = {
    def go(offset: Int)(acc: Seq[T]): Future[Seq[T]] = {
      get(offset, limit).flatMap(xs =>
        if (xs.size < limit) Future.successful(xs ++ acc)
        else go(offset + xs.size)(xs ++ acc)
      )
    }

    go(0)(Nil)
  }

  override def getAttributes(name: Option[String], limit: Int, offset: Int, kinds: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttributes: ToEntityMarshaller[Attributes]
  ): Route = authorize(ADMIN_ROLE, API_ROLE, SECURITY_ROLE, M2M_ROLE) {
    val operationLabel =
      s"Getting attributes with name = $name, limit = $limit, offset = $offset, kinds = $kinds"
    logger.info(operationLabel)

    val result: Future[Attributes] = for {
      kindsList <- parseArrayParameters(kinds)
        .traverse(AttributeKind.fromValue(_).map(PersistentAttributeKind.fromApi))
        .toFuture
      result    <- ReadModelQueries.getAttributes(name, kindsList, offset, limit)(readModelService)
    } yield Attributes(results = result.results.map(_.toApi), totalCount = result.totalCount)

    onComplete(result) {
      getAttributesResponse(operationLabel)(getAttributes200)
    }
  }
}
