package it.pagopa.interop.attributeregistryprocess.service

import it.pagopa.interop.partyregistryproxy.client.model.{Categories, Institutions}

import scala.concurrent.Future

trait PartyRegistryService {
  def getCategories(page: Option[Int], limit: Option[Int])(implicit contexts: Seq[(String, String)]): Future[Categories]
  def getInstitutions(page: Option[Int], limit: Option[Int])(implicit
    contexts: Seq[(String, String)]
  ): Future[Institutions]
}
