package it.pagopa.interop.attributeregistryprocess.util

import it.pagopa.interop.attributeregistrymanagement.client.model.{Attribute, AttributeKind, AttributeSeed}
import it.pagopa.interop.tenantmanagement.model.tenant.{PersistentTenant, PersistentExternalId, PersistentTenantKind}
import it.pagopa.interop.attributeregistryprocess.service._
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import org.mongodb.scala.bson.conversions.Bson
import spray.json.JsonReader

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

object FakeDependencies {
  val verifiedAttributeId: UUID = UUID.randomUUID()

  class FakeTenantManagement() extends TenantManagementService {

    override def getTenantById(
      tenantId: UUID
    )(implicit ec: ExecutionContext, readModel: ReadModelService): Future[PersistentTenant] =
      Future.successful(
        PersistentTenant(
          id = UUID.randomUUID(),
          kind = Some(PersistentTenantKind.PA),
          selfcareId = Some(UUID.randomUUID().toString),
          externalId = PersistentExternalId("Foo", "Bar"),
          features = Nil,
          attributes = Nil,
          createdAt = OffsetDateTime.now(),
          updatedAt = None,
          mails = Nil,
          name = "test_name"
        )
      )
  }

  class FakeAttributeRegistryManagement() extends AttributeRegistryManagementService {

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

    def aggregate[T: JsonReader](
      collectionName: String,
      pipeline: Seq[Bson],
      offset: Int,
      limit: Int,
      allowDiskUse: Boolean = false
    )(implicit ec: ExecutionContext): Future[Seq[T]] = Future.successful(Nil)

    override def close(): Unit = ()

    def aggregateRaw[T: JsonReader](
      collectionName: String,
      pipeline: Seq[Bson],
      offset: Int,
      limit: Int,
      allowDiskUse: Boolean = false
    )(implicit ec: ExecutionContext): Future[Seq[T]] = Future.successful(Nil)
  }

}
