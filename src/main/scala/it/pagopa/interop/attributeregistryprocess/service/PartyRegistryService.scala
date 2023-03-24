package it.pagopa.interop.attributeregistryprocess.service

import it.pagopa.interop.partyregistryproxy.client.model.{Categories, Institutions}

import scala.concurrent.Future

trait PartyRegistryService {
  def getCategories(bearerToken: String, page: Option[Int], limit: Option[Int])(implicit
    contexts: Seq[(String, String)]
  ): Future[Categories]
  def getInstitutions(bearerToken: String, page: Option[Int], limit: Option[Int])(implicit
    contexts: Seq[(String, String)]
  ): Future[Institutions]
}
