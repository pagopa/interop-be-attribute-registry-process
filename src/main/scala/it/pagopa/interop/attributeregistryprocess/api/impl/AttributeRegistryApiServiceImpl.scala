package it.pagopa.interop.attributeregistryprocess.api.impl

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.onComplete
import akka.http.scaladsl.server.Route
import com.mongodb.client.model.Filters
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import it.pagopa.interop.attributeregistrymanagement.model.persistence.JsonFormats.paFormat
import it.pagopa.interop.attributeregistrymanagement.model.persistence.attribute.PersistentAttribute
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiService
import it.pagopa.interop.attributeregistryprocess.api.types.AttributeRegistryServiceTypes._
import it.pagopa.interop.attributeregistryprocess.error.ResponseHandlers._
import it.pagopa.interop.attributeregistryprocess.model.{Attribute, AttributeKind, AttributeSeed, Problem}
import it.pagopa.interop.attributeregistryprocess.service._
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import it.pagopa.interop.commons.jwt._
import it.pagopa.interop.commons.logging.{CanLogContextFields, ContextFieldsToLog}
import it.pagopa.interop.commons.utils.AkkaUtils.getFutureBearer
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
        bearer     <- getFutureBearer(contexts)
        categories <- partyRegistryService.getCategories(bearer)
        attributeSeedsCategories   = categories.items.map(c =>
          AttributeSeed(
            code = Option(c.code),
            kind = AttributeKind.CERTIFIED,
            description = c.name, // passing the name since no description exists at party-registry-proxy
            origin = Option(c.origin),
            name = c.name
          )
        )
        institutions <- partyRegistryService.getInstitutions(bearer)
        attributeSeedsInstitutions = institutions.items.map(i =>
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
    def delta(attrs: Seq[PersistentAttribute]): Set[AttributeSeed] =
      attributesSeeds.foldLeft[Set[AttributeSeed]](Set.empty)((delta, seed) =>
        attrs
          .find(persisted => {
            seed.origin.contains(persisted.origin.getOrElse("")) && seed.code.contains(persisted.code.getOrElse(""))
          })
          .fold(delta + seed)(_ => Set.empty)
      )

    def getAndProcessAttributes(offset: Int, limit: Int): Future[Seq[PersistentAttribute]] = for {
      attributesFromRM <- readModelService.find[PersistentAttribute]("attributes", Filters.empty(), offset, limit)
      _                <- Future.traverse(delta(attributesFromRM))(attributeSeed => // create all new attributes
        attributeRegistryManagementService.createAttribute(attributeSeed.toClient)
      )
    } yield attributesFromRM

    getAndProcess(50)(getAndProcessAttributes).flatMap(_ => Future.unit)
  }

  private def getAndProcess[T](limit: Int)(get: (Int, Int) => Future[Seq[T]]): Future[Seq[T]] = {
    def go(offset: Int)(acc: Seq[T]): Future[Seq[T]] = {
      get(offset, limit).flatMap(xs =>
        if (xs.size < limit) Future.successful(xs ++ acc)
        else go(offset + xs.size)(xs ++ acc)
      )
    }

    go(0)(Nil)
  }
}
