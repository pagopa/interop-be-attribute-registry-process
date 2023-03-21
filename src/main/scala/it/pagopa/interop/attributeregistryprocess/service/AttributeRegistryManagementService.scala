package it.pagopa.interop.attributeregistryprocess.service

import it.pagopa.interop.attributeregistrymanagement.client.model.{Attribute, AttributeSeed}

import java.util.UUID
import scala.concurrent.Future

trait AttributeRegistryManagementService {

  def getAttributeById(id: UUID)(implicit contexts: Seq[(String, String)]): Future[Attribute]

  def createAttribute(attributeSeed: AttributeSeed)(implicit contexts: Seq[(String, String)]): Future[Attribute]

  def getAttributeByName(name: String)(implicit contexts: Seq[(String, String)]): Future[Attribute]

  def getAttributeByOriginAndCode(origin: String, code: String)(implicit
    contexts: Seq[(String, String)]
  ): Future[Attribute]
}
