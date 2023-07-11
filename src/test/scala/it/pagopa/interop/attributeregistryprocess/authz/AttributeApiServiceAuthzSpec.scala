package it.pagopa.interop.attributeregistryprocess.authz

import it.pagopa.interop.attributeregistryprocess.api.impl.AttributeRegistryApiMarshallerImpl._
import it.pagopa.interop.attributeregistryprocess.api.impl.AttributeRegistryApiServiceImpl
import it.pagopa.interop.attributeregistryprocess.model.{AttributeKind, AttributeSeed}
import it.pagopa.interop.attributeregistryprocess.util.FakeDependencies.{
  FakeAttributeRegistryManagement,
  FakeReadModelService
}
import it.pagopa.interop.attributeregistryprocess.util.{AuthorizedRoutes, ClusteredMUnitRouteTest}
import it.pagopa.interop.commons.cqrs.service.ReadModelService

import java.time.OffsetDateTime
import java.util.UUID

class AttributeApiServiceAuthzSpec extends ClusteredMUnitRouteTest {
  val fakeAttributeRegistryManagement: FakeAttributeRegistryManagement = FakeAttributeRegistryManagement()
  val fakeReadModel: ReadModelService                                  = new FakeReadModelService

  val service: AttributeRegistryApiServiceImpl = AttributeRegistryApiServiceImpl(
    fakeAttributeRegistryManagement,
    () => UUID.randomUUID(),
    () => OffsetDateTime.now(),
    fakeReadModel
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

  test("method authorization must succeed for getAttributes") {
    val endpoint = AuthorizedRoutes.endpoints("getAttributes")
    validateAuthorization(
      endpoint,
      { implicit c: Seq[(String, String)] => service.getAttributes(Some("name"), 0, 0, "???") }
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
