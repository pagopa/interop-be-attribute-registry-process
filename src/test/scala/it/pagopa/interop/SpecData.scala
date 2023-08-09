package it.pagopa.interop.attributeregistryprocess

import it.pagopa.interop.commons.utils.service.OffsetDateTimeSupplier
import it.pagopa.interop.tenantmanagement.model.tenant.{
  PersistentTenantKind,
  PersistentTenant,
  PersistentExternalId,
  PersistentTenantFeature
}
import java.time.{OffsetDateTime, ZoneOffset}
import java.util.UUID

object SpecData {

  final val timestamp = OffsetDateTime.of(2022, 12, 31, 11, 22, 33, 44, ZoneOffset.UTC)

  val certifiedFeature: PersistentTenantFeature.PersistentCertifier      =
    PersistentTenantFeature.PersistentCertifier("certifier")
  val emptyCertifiedFeature: PersistentTenantFeature.PersistentCertifier =
    PersistentTenantFeature.PersistentCertifier("  ")

  val tenant: PersistentTenant = PersistentTenant(
    id = UUID.randomUUID(),
    kind = Some(PersistentTenantKind.PA),
    selfcareId = Some(UUID.randomUUID.toString),
    externalId = PersistentExternalId("foo", "bar"),
    features = Nil,
    attributes = Nil,
    createdAt = OffsetDateTimeSupplier.get(),
    updatedAt = None,
    mails = Nil,
    name = "test_name"
  )
}
