package it.pagopa.interop.attributeregistryprocess

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.mongodb.client.model.Filters
import it.pagopa.interop.attributeregistrymanagement.client.model.{Attribute => ManagementAttribute}
import it.pagopa.interop.attributeregistrymanagement.model.persistence.attribute.PersistentAttribute
import it.pagopa.interop.attributeregistryprocess.api.types.AttributeRegistryServiceTypes.{
  AttributeSeedConverter,
  ProcessKindConverter
}
import it.pagopa.interop.attributeregistryprocess.model.{AttributeKind, AttributeSeed, Attribute => ProcessAttribute}
import it.pagopa.interop.commons.utils.service.OffsetDateTimeSupplier
import it.pagopa.interop.commons.utils.{ORGANIZATION_ID_CLAIM, USER_ROLES}
import it.pagopa.interop.partyregistryproxy.client.model.{Categories, Category, Institution, Institutions}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID

class AttributeRegistryApiServiceSpec
    extends AnyWordSpecLike
    with SpecHelper
    with ScalatestRouteTest
    with ScalaFutures {

  "Attributes loading" must {
    val categories: Categories =
      Categories(Seq(Category("YADA", "YADA", "test", "IPA"), Category("OPA", "OPA", "test", "IPA")), 2)

    val institutions: Institutions = Institutions(
      Seq(
        Institution(
          id = "1111",
          originId = "104532",
          o = Option("test"),
          ou = Option("test"),
          aoo = Option("test"),
          taxCode = "19530",
          category = "C7",
          description = "104532",
          digitalAddress = "test",
          address = "test",
          zipCode = "49300",
          origin = "IPA"
        ),
        Institution(
          id = "2222",
          originId = "205942",
          o = Option("test"),
          ou = Option("test"),
          aoo = Option("test"),
          taxCode = "19530",
          category = "L8",
          description = "205942",
          digitalAddress = "test",
          address = "test",
          zipCode = "90142",
          origin = "IPA"
        )
      ),
      2
    )

    val attributeSeeds: Seq[AttributeSeed] = Seq(
      AttributeSeed(
        code = Some("YADA"),
        kind = AttributeKind.CERTIFIED,
        description = "YADA",
        origin = Some("IPA"),
        name = "YADA"
      ),
      AttributeSeed(
        code = Some("OPA"),
        kind = AttributeKind.CERTIFIED,
        description = "OPA",
        origin = Some("IPA"),
        name = "OPA"
      ),
      AttributeSeed(
        code = Some("104532"),
        kind = AttributeKind.CERTIFIED,
        description = "104532",
        origin = Some("IPA"),
        name = "104532"
      ),
      AttributeSeed(
        code = Some("205942"),
        kind = AttributeKind.CERTIFIED,
        description = "205942",
        origin = Some("IPA"),
        name = "205942"
      )
    )
    "succeed with creation of all attributes retrieved from both Categories and Institutions if ReadModel is empty" in {
      val expectedDelta                            = attributeSeeds.toSet
      val attributesfromRM: List[ProcessAttribute] = List()
      val deltaAttributes                          = delta(attributesfromRM, attributeSeeds)

      implicit val context: Seq[(String, String)] =
        Seq("bearer" -> bearerToken, USER_ROLES -> "internal", ORGANIZATION_ID_CLAIM -> UUID.randomUUID().toString)

      mockGetCategories(Some(0), Some(50), result = categories)
      mockGetInstitutions(Some(0), Some(50), result = institutions)
      mockFind[PersistentAttribute]("attributes", Filters.empty(), 0, 50, result = Seq[PersistentAttribute]())
      attributeSeeds.foreach(attributeSeed =>
        mockCreateAttribute(
          attributeSeed.toClient,
          ManagementAttribute(
            id = UUID.randomUUID(),
            code = attributeSeed.code,
            kind = attributeSeed.kind.toClient,
            description = attributeSeed.description,
            origin = attributeSeed.origin,
            name = attributeSeed.name,
            creationTime = OffsetDateTimeSupplier.get()
          )
        )
      )

      Post() ~> service.loadCertifiedAttributes() ~> check {
        status shouldEqual StatusCodes.NoContent
        assert(expectedDelta == deltaAttributes)
      }
    }
    "succeed with creation of delta of attributes retrieved from both Categories and Institutions if ReadModel is not empty" in {
      val attributesfromRM: List[ProcessAttribute] = List(
        ProcessAttribute(
          id = UUID.randomUUID(),
          code = Some("YADA"),
          kind = AttributeKind.CERTIFIED,
          description = "YADA",
          origin = Some("IPA"),
          name = "YADA",
          creationTime = OffsetDateTimeSupplier.get()
        ),
        ProcessAttribute(
          id = UUID.randomUUID(),
          code = Some("104532"),
          kind = AttributeKind.CERTIFIED,
          description = "104532",
          origin = Some("IPA"),
          name = "104532",
          creationTime = OffsetDateTimeSupplier.get()
        )
      )

      val expectedDelta: Set[AttributeSeed] = Set[AttributeSeed](
        AttributeSeed(
          code = Some("OPA"),
          kind = AttributeKind.CERTIFIED,
          description = "OPA",
          origin = Some("IPA"),
          name = "OPA"
        ),
        AttributeSeed(
          code = Some("205942"),
          kind = AttributeKind.CERTIFIED,
          description = "205942",
          origin = Some("IPA"),
          name = "205942"
        )
      )
      val deltaAttributes                   = delta(attributesfromRM, attributeSeeds)

      implicit val context: Seq[(String, String)] =
        Seq("bearer" -> bearerToken, USER_ROLES -> "internal", ORGANIZATION_ID_CLAIM -> UUID.randomUUID().toString)

      mockGetCategories(Some(0), Some(50), result = categories)
      mockGetInstitutions(Some(0), Some(50), result = institutions)
      mockFind[PersistentAttribute]("attributes", Filters.empty(), 0, 50, result = Seq[PersistentAttribute]())
      attributeSeeds.foreach(attributeSeed =>
        mockCreateAttribute(
          attributeSeed.toClient,
          ManagementAttribute(
            id = UUID.randomUUID(),
            code = attributeSeed.code,
            kind = attributeSeed.kind.toClient,
            description = attributeSeed.description,
            origin = attributeSeed.origin,
            name = attributeSeed.name,
            creationTime = OffsetDateTimeSupplier.get()
          )
        )
      )

      Post() ~> service.loadCertifiedAttributes() ~> check {
        status shouldEqual StatusCodes.NoContent
        assert(expectedDelta == deltaAttributes)
      }
    }

    "succeed if no attributes are created when ReadModel already contains them" in {
      val attributesfromRM: List[ProcessAttribute] = List(
        ProcessAttribute(
          id = UUID.randomUUID(),
          code = Some("YADA"),
          kind = AttributeKind.CERTIFIED,
          description = "YADA",
          origin = Some("IPA"),
          name = "YADA",
          creationTime = OffsetDateTimeSupplier.get()
        ),
        ProcessAttribute(
          id = UUID.randomUUID(),
          code = Some("104532"),
          kind = AttributeKind.CERTIFIED,
          description = "104532",
          origin = Some("IPA"),
          name = "104532",
          creationTime = OffsetDateTimeSupplier.get()
        ),
        ProcessAttribute(
          id = UUID.randomUUID(),
          code = Some("OPA"),
          kind = AttributeKind.CERTIFIED,
          description = "OPA",
          origin = Some("IPA"),
          name = "OPA",
          creationTime = OffsetDateTimeSupplier.get()
        ),
        ProcessAttribute(
          id = UUID.randomUUID(),
          code = Some("205942"),
          kind = AttributeKind.CERTIFIED,
          description = "205942",
          origin = Some("IPA"),
          name = "205942",
          creationTime = OffsetDateTimeSupplier.get()
        )
      )

      val expectedDelta: Set[AttributeSeed] = Set[AttributeSeed]()
      val deltaAttributes                   = delta(attributesfromRM, attributeSeeds)

      implicit val context: Seq[(String, String)] =
        Seq("bearer" -> bearerToken, USER_ROLES -> "internal", ORGANIZATION_ID_CLAIM -> UUID.randomUUID().toString)

      mockGetCategories(Some(0), Some(50), result = categories)
      mockGetInstitutions(Some(0), Some(50), result = institutions)
      mockFind[PersistentAttribute]("attributes", Filters.empty(), 0, 50, result = Seq[PersistentAttribute]())
      attributeSeeds.foreach(attributeSeed =>
        mockCreateAttribute(
          attributeSeed.toClient,
          ManagementAttribute(
            id = UUID.randomUUID(),
            code = attributeSeed.code,
            kind = attributeSeed.kind.toClient,
            description = attributeSeed.description,
            origin = attributeSeed.origin,
            name = attributeSeed.name,
            creationTime = OffsetDateTimeSupplier.get()
          )
        )
      )

      Post() ~> service.loadCertifiedAttributes() ~> check {
        status shouldEqual StatusCodes.NoContent
        assert(expectedDelta == deltaAttributes)
      }
    }
  }

}
