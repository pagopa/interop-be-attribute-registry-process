package it.pagopa.interop.attributeregistryprocess.error

import it.pagopa.interop.commons.utils.errors.ComponentError
import java.util.UUID

object AttributeRegistryProcessErrors {

  final case class RegistryAttributeNotFound(attributeIdentifier: String)
      extends ComponentError("0001", s"Attribute $attributeIdentifier not found in registry")

  final case class TenantNotFound(tenantId: UUID)
      extends ComponentError("0002", s"Tenant ${tenantId.toString} not found")

  final case class OrganizationIsNotACertifier(tenantId: UUID)
      extends ComponentError("0003", s"Tenant ${tenantId.toString} is not a certifier")

  final case class OriginIsNotCompliant(origin: String)
      extends ComponentError("0004", s"Requester has not origin: $origin")
}
