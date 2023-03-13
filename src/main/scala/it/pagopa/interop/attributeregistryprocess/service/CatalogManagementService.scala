package it.pagopa.interop.attributeregistryprocess.service

import it.pagopa.interop.catalogmanagement.client.model.EService

import java.util.UUID
import scala.concurrent.Future

trait CatalogManagementService {
  def getEServiceById(eServiceId: UUID)(implicit contexts: Seq[(String, String)]): Future[EService]
}
