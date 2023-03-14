package it.pagopa.interop.attributeregistryprocess.utils

import it.pagopa.interop.attributeregistrymanagement.client.model.Attribute
import it.pagopa.interop.attributeregistryprocess.service._
import it.pagopa.interop.commons.utils._
import it.pagopa.interop.commons.utils.service.{OffsetDateTimeSupplier, UUIDSupplier}
import org.scalamock.scalatest.MockFactory

import java.util.UUID
import scala.concurrent.Future

trait SpecHelper extends MockFactory with SpecData {

  val bearerToken          = "token"
  val organizationId: UUID = UUID.randomUUID()

  val selfcareContext: Seq[(String, String)] =
    Seq(
      "bearer"              -> bearerToken,
      USER_ROLES            -> "admin",
      UID                   -> UUID.randomUUID().toString,
      ORGANIZATION_ID_CLAIM -> organizationId.toString
    )
  val m2mContext: Seq[(String, String)]      =
    Seq("bearer" -> bearerToken, USER_ROLES -> "m2m", ORGANIZATION_ID_CLAIM -> organizationId.toString)
  val internalContext: Seq[(String, String)] =
    Seq("bearer" -> bearerToken, USER_ROLES -> "internal")
  val adminContext: Seq[(String, String)]    =
    Seq("bearer" -> bearerToken, USER_ROLES -> "admin", ORGANIZATION_ID_CLAIM -> organizationId.toString)

  val mockAttributeRegistryManagement: AttributeRegistryManagementService = mock[AttributeRegistryManagementService]

  val mockUuidSupplier: UUIDSupplier               = mock[UUIDSupplier]
  val mockDateTimeSupplier: OffsetDateTimeSupplier = mock[OffsetDateTimeSupplier]

  def mockGetAttributeById(id: UUID, result: Attribute)(implicit contexts: Seq[(String, String)]) =
    (mockAttributeRegistryManagement
      .getAttributeById(_: UUID)(_: Seq[(String, String)]))
      .expects(id, contexts)
      .once()
      .returns(Future.successful(result))

  def mockDateTimeGet() = (() => mockDateTimeSupplier.get()).expects().returning(timestamp).once()

  def mockUuidGet(uuid: UUID) = (() => mockUuidSupplier.get()).expects().returning(uuid).once()

}
