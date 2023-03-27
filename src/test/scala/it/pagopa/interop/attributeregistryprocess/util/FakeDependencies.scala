package it.pagopa.interop.attributeregistryprocess.util

import it.pagopa.interop.attributeregistrymanagement.client.model.{Attribute, AttributeKind, AttributeSeed}
import it.pagopa.interop.attributeregistryprocess.service.{AttributeRegistryManagementService, PartyRegistryService}
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import it.pagopa.interop.partyregistryproxy.client.model.{Categories, Category, Institution, Institutions}
import org.mongodb.scala.bson.conversions.Bson
import spray.json.JsonReader

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

object FakeDependencies {
  val verifiedAttributeId: UUID = UUID.randomUUID()

  case class FakeAttributeRegistryManagement() extends AttributeRegistryManagementService {

    override def getAttributeById(id: UUID)(implicit contexts: Seq[(String, String)]): Future[Attribute] =
      Future.successful(
        Attribute(
          id = UUID.randomUUID(),
          code = Some(UUID.randomUUID().toString),
          kind = AttributeKind.CERTIFIED,
          description = "Attribute x",
          origin = Some("IPA"),
          name = "AttributeX",
          creationTime = OffsetDateTime.now()
        )
      )

    override def createAttribute(
      attributeSeed: AttributeSeed
    )(implicit contexts: Seq[(String, String)]): Future[Attribute] =
      Future.successful(
        Attribute(
          id = UUID.randomUUID(),
          code = Some(UUID.randomUUID().toString),
          kind = AttributeKind.CERTIFIED,
          description = "Attribute x",
          origin = Some("IPA"),
          name = "AttributeX",
          creationTime = OffsetDateTime.now()
        )
      )

    override def getAttributeByName(name: String)(implicit contexts: Seq[(String, String)]): Future[Attribute] =
      Future.successful(
        Attribute(
          id = UUID.randomUUID(),
          code = Some(UUID.randomUUID().toString),
          kind = AttributeKind.CERTIFIED,
          description = "Attribute x",
          origin = Some("IPA"),
          name = "AttributeX",
          creationTime = OffsetDateTime.now()
        )
      )
    override def getAttributeByOriginAndCode(origin: String, code: String)(implicit
      contexts: Seq[(String, String)]
    ): Future[Attribute] =
      Future.successful(
        Attribute(
          id = UUID.randomUUID(),
          code = Some(UUID.randomUUID().toString),
          kind = AttributeKind.CERTIFIED,
          description = "Attribute x",
          origin = Some("IPA"),
          name = "AttributeX",
          creationTime = OffsetDateTime.now()
        )
      )
  }

  case class FakePartyProcessService() extends PartyRegistryService {
    override def getCategories(page: Option[Int] = None, limit: Option[Int] = None)(implicit
      contexts: Seq[(String, String)]
    ): Future[Categories] =
      Future.successful(Categories(Seq(Category("YADA", "Proxied", "test", "IPA")), 1))

    override def getInstitutions(page: Option[Int] = None, limit: Option[Int] = None)(implicit
      contexts: Seq[(String, String)]
    ): Future[Institutions] =
      Future.successful(
        Institutions(
          Seq(
            Institution(
              id = "1111",
              originId = "104532",
              o = Option("test"),
              ou = Option("test"),
              aoo = Option("test"),
              taxCode = "19530",
              category = "C7",
              description = "YADA",
              digitalAddress = "test",
              address = "test",
              zipCode = "49300",
              origin = "ipa"
            )
          ),
          1
        )
      )
  }

  class FakeReadModelService extends ReadModelService {
    override def findOne[T](collectionName: String, filter: Bson)(implicit
      evidence$1: JsonReader[T],
      ec: ExecutionContext
    ): Future[Option[T]] = Future.successful(None)

    override def find[T](collectionName: String, filter: Bson, offset: Int, limit: Int)(implicit
      evidence$2: JsonReader[T],
      ec: ExecutionContext
    ): Future[Seq[T]] = Future.successful(Nil)

    override def find[T](collectionName: String, filter: Bson, projection: Bson, offset: Int, limit: Int)(implicit
      evidence$3: JsonReader[T],
      ec: ExecutionContext
    ): Future[Seq[T]] = Future.successful(Nil)

    override def aggregate[T](collectionName: String, pipeline: Seq[Bson], offset: Int, limit: Int)(implicit
      evidence$4: JsonReader[T],
      ec: ExecutionContext
    ): Future[Seq[T]] = Future.successful(Nil)

    override def close(): Unit = ()

    override def aggregateRaw[T: JsonReader](collectionName: String, pipeline: Seq[Bson], offset: Int, limit: Int)(
      implicit ec: ExecutionContext
    ): Future[Seq[T]] = Future.successful(Nil)
  }

}
