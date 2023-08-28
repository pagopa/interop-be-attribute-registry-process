package it.pagopa.interop.attributeregistryprocess

import it.pagopa.interop.attributeregistryprocess.model._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import it.pagopa.interop.commons.utils.{
  ORGANIZATION_EXTERNAL_ID_ORIGIN,
  ORGANIZATION_EXTERNAL_ID_VALUE,
  ORGANIZATION_ID_CLAIM,
  USER_ROLES
}
import it.pagopa.interop.attributeregistryprocess.api.types.AttributeRegistryServiceTypes._
import it.pagopa.interop.attributeregistryprocess.api.impl.AttributeRegistryApiMarshallerImpl._
import it.pagopa.interop.attributeregistrymanagement.client.{model => AttributeDependency}

import java.util.UUID

class AttributeRegistryApiServiceSpec
    extends AnyWordSpecLike
    with SpecHelper
    with ScalatestRouteTest
    with ScalaFutures {

  "Certified attribute creation" should {
    "succeed" in {

      val requesterUuid = UUID.randomUUID()

      implicit val context: Seq[(String, String)] =
        Seq("bearer" -> bearerToken, USER_ROLES -> "admin", ORGANIZATION_ID_CLAIM -> requesterUuid.toString)

      val attributeSeed: CertifiedAttributeSeed =
        CertifiedAttributeSeed(name = "name", description = "description", code = "code")
      val tenant = SpecData.tenant.copy(id = requesterUuid, features = List(SpecData.certifiedFeature))

      val expected: AttributeDependency.Attribute = AttributeDependency.Attribute(
        id = UUID.randomUUID(),
        code = Some("code"),
        kind = AttributeDependency.AttributeKind.CERTIFIED,
        description = "description",
        origin = Some("certifier"),
        name = "name",
        creationTime = SpecData.timestamp
      )

      mockOrganizationRetrieve(requesterUuid, tenant)
      mockAttributeCreate(attributeSeed.toManagement("certifier"), expected)

      Post() ~> service.createCertifiedAttribute(attributeSeed) ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Attribute] shouldEqual expected.toApi
      }
    }
    "fail if certifier is empty" in {

      val requesterUuid = UUID.randomUUID()

      implicit val context: Seq[(String, String)] =
        Seq("bearer" -> bearerToken, USER_ROLES -> "admin", ORGANIZATION_ID_CLAIM -> requesterUuid.toString)

      val attributeSeed: CertifiedAttributeSeed =
        CertifiedAttributeSeed(name = "name", description = "description", code = "code")
      val tenant = SpecData.tenant.copy(id = requesterUuid, features = List(SpecData.emptyCertifiedFeature))

      mockOrganizationRetrieve(requesterUuid, tenant)

      Post() ~> service.createCertifiedAttribute(attributeSeed) ~> check {
        status shouldEqual StatusCodes.Forbidden
      }
    }
  }
  "Declared attribute creation" should {
    "succeed" in {

      val requesterUuid = UUID.randomUUID()

      implicit val context: Seq[(String, String)] =
        Seq(
          "bearer"                        -> bearerToken,
          USER_ROLES                      -> "admin",
          ORGANIZATION_ID_CLAIM           -> requesterUuid.toString,
          ORGANIZATION_EXTERNAL_ID_ORIGIN -> "IPA",
          ORGANIZATION_EXTERNAL_ID_VALUE  -> "12345"
        )

      val attributeSeed: AttributeSeed =
        AttributeSeed(name = "name", description = "description")

      val expected: AttributeDependency.Attribute = AttributeDependency.Attribute(
        id = UUID.randomUUID(),
        code = None,
        kind = AttributeDependency.AttributeKind.DECLARED,
        description = "description",
        origin = None,
        name = "name",
        creationTime = SpecData.timestamp
      )

      mockAttributeCreate(attributeSeed.toManagement(AttributeKind.DECLARED), expected)

      Post() ~> service.createDeclaredAttribute(attributeSeed) ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Attribute] shouldEqual expected.toApi
      }
    }
  }
  "Verified attribute creation" should {
    "succeed" in {

      val requesterUuid = UUID.randomUUID()

      implicit val context: Seq[(String, String)] =
        Seq(
          "bearer"                        -> bearerToken,
          USER_ROLES                      -> "admin",
          ORGANIZATION_ID_CLAIM           -> requesterUuid.toString,
          ORGANIZATION_EXTERNAL_ID_ORIGIN -> "IPA",
          ORGANIZATION_EXTERNAL_ID_VALUE  -> "12345"
        )

      val attributeSeed: AttributeSeed =
        AttributeSeed(name = "name", description = "description")

      val expected: AttributeDependency.Attribute = AttributeDependency.Attribute(
        id = UUID.randomUUID(),
        code = None,
        kind = AttributeDependency.AttributeKind.VERIFIED,
        description = "description",
        origin = None,
        name = "name",
        creationTime = SpecData.timestamp
      )

      mockAttributeCreate(attributeSeed.toManagement(AttributeKind.VERIFIED), expected)

      Post() ~> service.createVerifiedAttribute(attributeSeed) ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Attribute] shouldEqual expected.toApi
      }
    }
  }
  "Internal certified attribute creation" should {
    "succeed" in {

      implicit val context: Seq[(String, String)] =
        Seq("bearer" -> bearerToken, USER_ROLES -> "internal")

      val attributeSeed: InternalCertifiedAttributeSeed =
        InternalCertifiedAttributeSeed(name = "name", description = "description", origin = "origin", code = "code")

      val expected: AttributeDependency.Attribute = AttributeDependency.Attribute(
        id = UUID.randomUUID(),
        code = Some("code"),
        kind = AttributeDependency.AttributeKind.CERTIFIED,
        description = "description",
        origin = Some("origin"),
        name = "name",
        creationTime = SpecData.timestamp
      )

      mockAttributeCreate(attributeSeed.toManagement, expected)

      Post() ~> service.createInternalCertifiedAttribute(attributeSeed) ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Attribute] shouldEqual expected.toApi
      }
    }
    "fail if certifier is empty" in {

      val requesterUuid = UUID.randomUUID()

      implicit val context: Seq[(String, String)] =
        Seq("bearer" -> bearerToken, USER_ROLES -> "admin", ORGANIZATION_ID_CLAIM -> requesterUuid.toString)

      val attributeSeed: CertifiedAttributeSeed =
        CertifiedAttributeSeed(name = "name", description = "description", code = "code")
      val tenant = SpecData.tenant.copy(id = requesterUuid, features = List(SpecData.emptyCertifiedFeature))

      mockOrganizationRetrieve(requesterUuid, tenant)

      Post() ~> service.createCertifiedAttribute(attributeSeed) ~> check {
        status shouldEqual StatusCodes.Forbidden
      }
    }
  }
}
