package it.pagopa.interop.attributeregistryprocess.utils

import cats.implicits._
import it.pagopa.interop.agreementmanagement.client.model.{
  Stamps,
  Agreement => DependencyAgreement,
  AgreementState => DependencyAgreementState,
  VerifiedAttribute => DependencyVerifiedAttribute
}
import it.pagopa.interop.attributeregistrymanagement.client.model.{AttributeKind, Attribute => DependencyAttribute}
import it.pagopa.interop.catalogmanagement.client.model.{
  Attribute => CatalogAttribute,
  AttributeValue => CatalogAttributeValue,
  Attributes => CatalogAttributes,
  EService => CatalogEService,
  EServiceTechnology => CatalogEServiceTechnology
}
import it.pagopa.interop.tenantmanagement.client.model.{
  CertifiedTenantAttribute => DependencyCertifiedTenantAttribute,
  DeclaredTenantAttribute => DependencyDeclaredTenantAttribute,
  ExternalId => DependencyExternalId,
  Tenant => DependencyTenant,
  TenantAttribute => DependencyTenantAttribute,
  TenantRevoker => DependencyTenantRevoker,
  TenantVerifier => DependencyTenantVerifier,
  VerificationRenewal => DependencyVerificationRenewal,
  VerifiedTenantAttribute => DependencyVerifiedTenantAttribute
}

import java.time.{OffsetDateTime, ZoneOffset}
import java.util.UUID

trait SpecData {
  final val timestamp = OffsetDateTime.of(2022, 12, 31, 11, 22, 33, 44, ZoneOffset.UTC)

  val dependencyTenant: DependencyTenant = DependencyTenant(
    id = UUID.randomUUID(),
    selfcareId = None,
    externalId = DependencyExternalId("IPA", "org"),
    features = Nil,
    attributes = Nil,
    createdAt = timestamp,
    updatedAt = None,
    mails = Nil,
    name = "test_name"
  )

  val tenantVerifier: DependencyTenantVerifier = DependencyTenantVerifier(
    id = UUID.randomUUID(),
    verificationDate = timestamp,
    renewal = DependencyVerificationRenewal.AUTOMATIC_RENEWAL,
    expirationDate = None,
    extensionDate = None
  )

  val tenantRevoker: DependencyTenantRevoker = DependencyTenantRevoker(
    id = UUID.randomUUID(),
    verificationDate = timestamp,
    expirationDate = None,
    renewal = DependencyVerificationRenewal.AUTOMATIC_RENEWAL,
    extensionDate = None,
    revocationDate = timestamp
  )

  val dependencyTenantAttribute: DependencyTenantAttribute = DependencyTenantAttribute(certified =
    DependencyCertifiedTenantAttribute(
      id = UUID.randomUUID(),
      assignmentTimestamp = timestamp,
      revocationTimestamp = None
    ).some
  )

  val dependencyCertifiedTenantAttribute: DependencyTenantAttribute = dependencyTenantAttribute

  val dependencyDeclaredTenantAttribute: DependencyTenantAttribute = DependencyTenantAttribute(declared =
    DependencyDeclaredTenantAttribute(
      id = UUID.randomUUID(),
      assignmentTimestamp = timestamp,
      revocationTimestamp = None
    ).some
  )

  def dependencyVerifiedTenantAttribute(
    id: UUID = UUID.randomUUID(),
    verifiedBy: Seq[DependencyTenantVerifier] = Seq(tenantVerifier),
    revokedBy: Seq[DependencyTenantRevoker] = Seq(tenantRevoker),
    assignmentTimestamp: OffsetDateTime = timestamp
  ): DependencyTenantAttribute = DependencyTenantAttribute(verified =
    DependencyVerifiedTenantAttribute(
      id = id,
      assignmentTimestamp = assignmentTimestamp,
      verifiedBy = verifiedBy,
      revokedBy = revokedBy
    ).some
  )

  val dependencyAttribute: DependencyAttribute = DependencyAttribute(
    id = UUID.randomUUID(),
    code = None,
    kind = AttributeKind.CERTIFIED,
    description = "An attribute",
    origin = None,
    name = "AttributeX",
    creationTime = timestamp
  )

  def dependencyAgreement(
    eServiceId: UUID = UUID.randomUUID(),
    verifiedAttributeId: UUID = UUID.randomUUID()
  ): DependencyAgreement = DependencyAgreement(
    id = UUID.randomUUID(),
    eserviceId = eServiceId,
    descriptorId = UUID.randomUUID(),
    producerId = UUID.randomUUID(),
    consumerId = UUID.randomUUID(),
    state = DependencyAgreementState.ACTIVE,
    verifiedAttributes = Seq(DependencyVerifiedAttribute(verifiedAttributeId)),
    certifiedAttributes = Nil,
    declaredAttributes = Nil,
    consumerDocuments = Nil,
    createdAt = OffsetDateTime.now(),
    stamps = Stamps(),
    rejectionReason = None
  )

  def catalogEService(id: UUID = UUID.randomUUID(), verifiedAttributeId: UUID = UUID.randomUUID()): CatalogEService =
    CatalogEService(
      id = id,
      producerId = UUID.randomUUID(),
      name = "EService",
      description = "EService desc",
      technology = CatalogEServiceTechnology.REST,
      attributes = CatalogAttributes(
        Nil,
        Nil,
        verified = Seq(CatalogAttribute(single = Some(CatalogAttributeValue(verifiedAttributeId, true))))
      ),
      descriptors = Nil
    )

  def matchingAgreementAndEService(
    verifiedAttributeId: UUID = UUID.randomUUID()
  ): (DependencyAgreement, CatalogEService) = {
    val eServiceId = UUID.randomUUID()

    (dependencyAgreement(eServiceId, verifiedAttributeId), catalogEService(eServiceId, verifiedAttributeId))
  }

}
