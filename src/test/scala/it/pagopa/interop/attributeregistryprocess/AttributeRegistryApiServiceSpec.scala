package it.pagopa.interop.attributeregistryprocess

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.mongodb.client.model.Filters
import it.pagopa.interop.attributeregistrymanagement.client.model.{Attribute => ManagementAttribute}
import it.pagopa.interop.attributeregistrymanagement.model.persistence.{attribute => PersistentAttributeDependency}
import it.pagopa.interop.attributeregistryprocess.api.types.AttributeRegistryServiceTypes.{
  AttributeSeedConverter,
  ProcessKindConverter
}
import it.pagopa.interop.attributeregistryprocess.model.{AttributeKind, AttributeSeed}
import it.pagopa.interop.commons.utils.service.OffsetDateTimeSupplier
import it.pagopa.interop.commons.utils.{ORGANIZATION_ID_CLAIM, USER_ROLES}
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
    "succeed with creation of all attributes retrieved from both Categories and Institutions if ReadModel is empty" in {
      implicit val context: Seq[(String, String)] =
        Seq("bearer" -> bearerToken, USER_ROLES -> "internal", ORGANIZATION_ID_CLAIM -> UUID.randomUUID().toString)

      mockGetCategories(Some(0), Some(50), result = categories)
      mockGetInstitutions(Some(0), Some(50), result = institutions)
      mockFind[PersistentAttributeDependency.PersistentAttribute](
        "attributes",
        Filters.empty(),
        0,
        50,
        result = Seq[PersistentAttributeDependency.PersistentAttribute]()
      )

      val expectedDelta: Set[AttributeSeed] = attributeSeeds.toSet

      expectedDelta.foreach(expectedAttributeSeed =>
        mockCreateAttribute(
          expectedAttributeSeed.toClient,
          ManagementAttribute(
            id = UUID.randomUUID(),
            code = expectedAttributeSeed.code,
            kind = expectedAttributeSeed.kind.toClient,
            description = expectedAttributeSeed.description,
            origin = expectedAttributeSeed.origin,
            name = expectedAttributeSeed.name,
            creationTime = OffsetDateTimeSupplier.get()
          )
        )
      )

      Post() ~> service.loadCertifiedAttributes() ~> check {
        status shouldEqual StatusCodes.NoContent
      }
    }
    "succeed with creation of delta of attributes retrieved from both Categories and Institutions if ReadModel is not empty" in {
      implicit val context: Seq[(String, String)] =
        Seq("bearer" -> bearerToken, USER_ROLES -> "internal", ORGANIZATION_ID_CLAIM -> UUID.randomUUID().toString)

      mockGetCategories(Some(0), Some(50), result = categories)
      mockGetInstitutions(Some(0), Some(50), result = institutions)

      val attributesfromRM: Seq[PersistentAttributeDependency.PersistentAttribute] =
        Seq[PersistentAttributeDependency.PersistentAttribute](
          PersistentAttributeDependency.PersistentAttribute(
            id = UUID.randomUUID(),
            code = Some("YADA"),
            kind = PersistentAttributeDependency.Certified,
            description = "YADA",
            origin = Some("IPA"),
            name = "YADA",
            creationTime = OffsetDateTimeSupplier.get()
          ),
          PersistentAttributeDependency.PersistentAttribute(
            id = UUID.randomUUID(),
            code = Some("104532"),
            kind = PersistentAttributeDependency.Certified,
            description = "104532",
            origin = Some("IPA"),
            name = "104532",
            creationTime = OffsetDateTimeSupplier.get()
          )
        )

      mockFind[PersistentAttributeDependency.PersistentAttribute](
        "attributes",
        Filters.empty(),
        0,
        50,
        result = attributesfromRM
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

      expectedDelta.foreach(expectedAttributeSeed =>
        mockCreateAttribute(
          expectedAttributeSeed.toClient,
          ManagementAttribute(
            id = UUID.randomUUID(),
            code = expectedAttributeSeed.code,
            kind = expectedAttributeSeed.kind.toClient,
            description = expectedAttributeSeed.description,
            origin = expectedAttributeSeed.origin,
            name = expectedAttributeSeed.name,
            creationTime = OffsetDateTimeSupplier.get()
          )
        )
      )

      Post() ~> service.loadCertifiedAttributes() ~> check {
        status shouldEqual StatusCodes.NoContent
      }
    }

    "succeed if no attributes are created when ReadModel already contains them" in {
      implicit val context: Seq[(String, String)] =
        Seq("bearer" -> bearerToken, USER_ROLES -> "internal", ORGANIZATION_ID_CLAIM -> UUID.randomUUID().toString)

      mockGetCategories(Some(0), Some(50), result = categories)
      mockGetInstitutions(Some(0), Some(50), result = institutions)

      val attributesfromRM: Seq[PersistentAttributeDependency.PersistentAttribute] =
        Seq[PersistentAttributeDependency.PersistentAttribute](
          PersistentAttributeDependency.PersistentAttribute(
            id = UUID.randomUUID(),
            code = Some("YADA"),
            kind = PersistentAttributeDependency.Certified,
            description = "YADA",
            origin = Some("IPA"),
            name = "YADA",
            creationTime = OffsetDateTimeSupplier.get()
          ),
          PersistentAttributeDependency.PersistentAttribute(
            id = UUID.randomUUID(),
            code = Some("104532"),
            kind = PersistentAttributeDependency.Certified,
            description = "104532",
            origin = Some("IPA"),
            name = "104532",
            creationTime = OffsetDateTimeSupplier.get()
          ),
          PersistentAttributeDependency.PersistentAttribute(
            id = UUID.randomUUID(),
            code = Some("OPA"),
            kind = PersistentAttributeDependency.Certified,
            description = "OPA",
            origin = Some("IPA"),
            name = "OPA",
            creationTime = OffsetDateTimeSupplier.get()
          ),
          PersistentAttributeDependency.PersistentAttribute(
            id = UUID.randomUUID(),
            code = Some("205942"),
            kind = PersistentAttributeDependency.Certified,
            description = "205942",
            origin = Some("IPA"),
            name = "205942",
            creationTime = OffsetDateTimeSupplier.get()
          )
        )

      mockFind[PersistentAttributeDependency.PersistentAttribute](
        "attributes",
        Filters.empty(),
        0,
        50,
        result = attributesfromRM
      )

      val expectedDelta: Set[AttributeSeed] = Set[AttributeSeed]()

      expectedDelta.foreach(expectedAttributeSeed =>
        mockCreateAttribute(
          expectedAttributeSeed.toClient,
          ManagementAttribute(
            id = UUID.randomUUID(),
            code = expectedAttributeSeed.code,
            kind = expectedAttributeSeed.kind.toClient,
            description = expectedAttributeSeed.description,
            origin = expectedAttributeSeed.origin,
            name = expectedAttributeSeed.name,
            creationTime = OffsetDateTimeSupplier.get()
          )
        )
      )

      Post() ~> service.loadCertifiedAttributes() ~> check {
        status shouldEqual StatusCodes.NoContent
      }
    }
  }

}
