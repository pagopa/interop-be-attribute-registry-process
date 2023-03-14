package it.pagopa.interop.attributeregistryprocess.utils

import it.pagopa.interop.attributeregistrymanagement.client.model.{AttributeKind, Attribute => DependencyAttribute}

import java.time.{OffsetDateTime, ZoneOffset}
import java.util.UUID

trait SpecData {
  final val timestamp = OffsetDateTime.of(2022, 12, 31, 11, 22, 33, 44, ZoneOffset.UTC)

  val dependencyAttribute: DependencyAttribute = DependencyAttribute(
    id = UUID.randomUUID(),
    code = None,
    kind = AttributeKind.CERTIFIED,
    description = "An attribute",
    origin = None,
    name = "AttributeX",
    creationTime = timestamp
  )
}
