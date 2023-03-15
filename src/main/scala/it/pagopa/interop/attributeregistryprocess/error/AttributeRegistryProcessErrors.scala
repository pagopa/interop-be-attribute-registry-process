package it.pagopa.interop.attributeregistryprocess.error

import it.pagopa.interop.commons.utils.errors.ComponentError

import java.util.UUID

object AttributeRegistryProcessErrors {

  final case class RegistryAttributeNotFound(attributeIdentifier: String)
      extends ComponentError("0001", s"Attribute $attributeIdentifier not found in registry")
}
