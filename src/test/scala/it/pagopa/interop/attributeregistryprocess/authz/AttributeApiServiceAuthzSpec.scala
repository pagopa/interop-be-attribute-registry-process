package it.pagopa.interop.attributeregistryprocess.authz

import it.pagopa.interop.attributeregistryprocess.api.impl.AttributeRegistryApiMarshallerImpl._
import it.pagopa.interop.attributeregistryprocess.api.impl.AttributeRegistryApiServiceImpl
import it.pagopa.interop.attributeregistryprocess.model.{
  AttributeSeed,
  CertifiedAttributeSeed,
  InternalCertifiedAttributeSeed
}
import it.pagopa.interop.attributeregistryprocess.util.FakeDependencies.{
  FakeAttributeRegistryManagement,
  FakeReadModelService,
  FakeTenantManagement
}
import it.pagopa.interop.attributeregistryprocess.util.{AuthorizedRoutes, ClusteredMUnitRouteTest}
import it.pagopa.interop.commons.cqrs.service.ReadModelService

import java.time.OffsetDateTime
import java.util.UUID

class AttributeApiServiceAuthzSpec extends ClusteredMUnitRouteTest {
  val fakeAttributeRegistryManagement: FakeAttributeRegistryManagement = new FakeAttributeRegistryManagement()
  val fakeTenantManagement: FakeTenantManagement                       = new FakeTenantManagement()
  implicit val fakeReadModel: ReadModelService                         = new FakeReadModelService

  val service: AttributeRegistryApiServiceImpl = AttributeRegistryApiServiceImpl(
    fakeAttributeRegistryManagement,
    fakeTenantManagement,
    () => UUID.randomUUID(),
    () => OffsetDateTime.now()
  )

  test("method authorization must succeed for createInternalCertifiedAttribute") {
    val endpoint = AuthorizedRoutes.endpoints("createInternalCertifiedAttribute")
    val fakeSeed =
      InternalCertifiedAttributeSeed(origin = "origin", code = "code", description = "???", name = "???")
    validateAuthorization(
      endpoint,
      { implicit c: Seq[(String, String)] => service.createInternalCertifiedAttribute(fakeSeed) }
    )
  }

  test("method authorization must succeed for createCertifiedAttribute") {
    val endpoint = AuthorizedRoutes.endpoints("createCertifiedAttribute")
    val fakeSeed =
      CertifiedAttributeSeed(code = "code", description = "???", name = "???")
    validateAuthorization(endpoint, { implicit c: Seq[(String, String)] => service.createCertifiedAttribute(fakeSeed) })
  }

  test("method authorization must succeed for createDeclaredAttribute") {
    val endpoint = AuthorizedRoutes.endpoints("createDeclaredAttribute")
    val fakeSeed =
      AttributeSeed(name = "???", description = "???")
    validateAuthorization(endpoint, { implicit c: Seq[(String, String)] => service.createDeclaredAttribute(fakeSeed) })
  }

  test("method authorization must succeed for createVerifiedAttribute") {
    val endpoint = AuthorizedRoutes.endpoints("createVerifiedAttribute")
    val fakeSeed =
      AttributeSeed(name = "???", description = "???")
    validateAuthorization(endpoint, { implicit c: Seq[(String, String)] => service.createVerifiedAttribute(fakeSeed) })
  }

  test("method authorization must succeed for getAttributeById") {
    val endpoint = AuthorizedRoutes.endpoints("getAttributeById")
    validateAuthorization(endpoint, { implicit c: Seq[(String, String)] => service.getAttributeById("fakeSeed") })
  }

  test("method authorization must succeed for getAttributeByName") {
    val endpoint = AuthorizedRoutes.endpoints("getAttributeByName")
    validateAuthorization(endpoint, { implicit c: Seq[(String, String)] => service.getAttributeByName("fakeSeed") })
  }

  test("method authorization must succeed for getAttributes") {
    val endpoint = AuthorizedRoutes.endpoints("getAttributes")
    validateAuthorization(
      endpoint,
      { implicit c: Seq[(String, String)] => service.getAttributes(Some("name"), Some("origin"), 0, 0, "???") }
    )
  }

  test("method authorization must succeed for getAttributeByOriginAndCode") {
    val endpoint = AuthorizedRoutes.endpoints("getAttributeByOriginAndCode")
    validateAuthorization(
      endpoint,
      { implicit c: Seq[(String, String)] => service.getAttributeByOriginAndCode("fakeSeed", "code") }
    )
  }

  test("method authorization must succeed for getBulkedAttributes") {
    val endpoint = AuthorizedRoutes.endpoints("getBulkedAttributes")
    validateAuthorization(endpoint, { implicit c: Seq[(String, String)] => service.getBulkedAttributes(10, 0, Nil) })
  }
}
