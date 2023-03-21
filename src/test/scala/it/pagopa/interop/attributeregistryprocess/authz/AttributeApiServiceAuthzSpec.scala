package it.pagopa.interop.attributeregistryprocess.authz

import it.pagopa.interop.attributeregistryprocess.api.impl.AttributeRegistryApiMarshallerImpl._
import it.pagopa.interop.attributeregistryprocess.api.impl.AttributeRegistryApiServiceImpl
import it.pagopa.interop.attributeregistryprocess.model.{AttributeKind, AttributeSeed}
import it.pagopa.interop.attributeregistryprocess.util.FakeDependencies.{
  FakeAttributeRegistryManagement,
  FakePartyProcessService
}
import it.pagopa.interop.attributeregistryprocess.util.{AuthorizedRoutes, ClusteredMUnitRouteTest}

import java.time.OffsetDateTime
import java.util.UUID

class AttributeApiServiceAuthzSpec extends ClusteredMUnitRouteTest {
  val fakeAttributeRegistryManagement: FakeAttributeRegistryManagement = FakeAttributeRegistryManagement()
  val fakePartyProcessService: FakePartyProcessService                 = FakePartyProcessService()

  val service: AttributeRegistryApiServiceImpl = AttributeRegistryApiServiceImpl(
    fakeAttributeRegistryManagement,
    () => UUID.randomUUID(),
    () => OffsetDateTime.now(),
    fakePartyProcessService
  )

  test("method authorization must succeed for createAttribute") {

    val endpoint = AuthorizedRoutes.endpoints("createAttribute")
    val fakeSeed =
      AttributeSeed(code = None, kind = AttributeKind.CERTIFIED, description = "???", origin = None, name = "???")
    validateAuthorization(endpoint, { implicit c: Seq[(String, String)] => service.createAttribute(fakeSeed) })
  }

  test("method authorization must succeed for getAttributeById") {
    val endpoint = AuthorizedRoutes.endpoints("getAttributeById")
    validateAuthorization(endpoint, { implicit c: Seq[(String, String)] => service.getAttributeById("fakeSeed") })
  }

  test("method authorization must succeed for getAttributeByName") {
    val endpoint = AuthorizedRoutes.endpoints("getAttributeByName")
    validateAuthorization(endpoint, { implicit c: Seq[(String, String)] => service.getAttributeByName("fakeSeed") })
  }

  test("method authorization must succeed for getAttributeByOriginAndCode") {
    val endpoint = AuthorizedRoutes.endpoints("getAttributeByOriginAndCode")
    validateAuthorization(
      endpoint,
      { implicit c: Seq[(String, String)] => service.getAttributeByOriginAndCode("fakeSeed", "code") }
    )
  }

  test("method authorization must succeed for loadCertifiedAttributes") {
    val endpoint = AuthorizedRoutes.endpoints("loadCertifiedAttributes")
    validateAuthorization(endpoint, { implicit c: Seq[(String, String)] => service.loadCertifiedAttributes() })
  }
}
