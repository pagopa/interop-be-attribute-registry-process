package it.pagopa.interop.attributeregistryprocess.service

import it.pagopa.interop.partyregistryproxy.client.model.{Categories, Institutions}

import scala.concurrent.Future

trait PartyRegistryService {
  def getCategories(bearerToken: String)(implicit contexts: Seq[(String, String)]): Future[Categories]
  def getInstitutions(bearerToken: String)(implicit contexts: Seq[(String, String)]): Future[Institutions]
}
