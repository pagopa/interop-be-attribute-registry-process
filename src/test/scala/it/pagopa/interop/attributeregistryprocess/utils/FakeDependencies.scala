package it.pagopa.interop.attributeregistryprocess.utils

import java.util.UUID

object FakeDependencies extends SpecData {
  val verifiedAttributeId: UUID = UUID.randomUUID()
  val (agreement, eService)     = matchingAgreementAndEService(verifiedAttributeId)
}
