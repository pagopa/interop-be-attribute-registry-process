package it.pagopa.interop.attributeregistryprocess.util

import it.pagopa.interop.attributeregistrymanagement.client.model.{
  Attribute,
  AttributeKind,
  AttributeSeed,
  AttributesResponse
}
import it.pagopa.interop.attributeregistryprocess.service.AttributeRegistryManagementService

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.Future

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

    override def deleteAttributeById(attributeId: UUID)(implicit contexts: Seq[(String, String)]): Future[Unit] =
      Future.successful(())

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

    override def getAttributes(
      search: Option[String]
    )(implicit contexts: Seq[(String, String)]): Future[AttributesResponse] =
      Future.successful(
        AttributesResponse(attributes =
          Seq(
            Attribute(
              id = UUID.randomUUID(),
              code = Some(UUID.randomUUID().toString),
              kind = AttributeKind.CERTIFIED,
              description = "Attribute x",
              origin = Some("IPA"),
              name = "AttributeX",
              creationTime = OffsetDateTime.now()
            ),
            Attribute(
              id = UUID.randomUUID(),
              code = Some(UUID.randomUUID().toString),
              kind = AttributeKind.CERTIFIED,
              description = "Attribute y",
              origin = Some("IPA"),
              name = "AttributeY",
              creationTime = OffsetDateTime.now()
            )
          )
        )
      )

    override def getBulkedAttributes(
      ids: Option[String]
    )(implicit contexts: Seq[(String, String)]): Future[AttributesResponse] =
      Future.successful(
        AttributesResponse(attributes =
          Seq(
            Attribute(
              id = UUID.randomUUID(),
              code = Some(UUID.randomUUID().toString),
              kind = AttributeKind.CERTIFIED,
              description = "Attribute x",
              origin = Some("IPA"),
              name = "AttributeX",
              creationTime = OffsetDateTime.now()
            ),
            Attribute(
              id = UUID.randomUUID(),
              code = Some(UUID.randomUUID().toString),
              kind = AttributeKind.CERTIFIED,
              description = "Attribute y",
              origin = Some("IPA"),
              name = "AttributeY",
              creationTime = OffsetDateTime.now()
            )
          )
        )
      )
  }
}
