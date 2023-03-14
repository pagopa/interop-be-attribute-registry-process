package it.pagopa.interop.attributeregistryprocess.service

import it.pagopa.interop.attributeregistrymanagement.client.model.{Attribute, AttributeSeed, AttributesResponse}

import scala.concurrent.Future
import java.util.UUID

trait AttributeRegistryManagementService {

  def getAttributeById(id: UUID)(implicit contexts: Seq[(String, String)]): Future[Attribute]

  def createAttribute(attributeSeed: AttributeSeed)(implicit contexts: Seq[(String, String)]): Future[Attribute]

  def deleteAttributeById(attributeId: UUID)(implicit contexts: Seq[(String, String)]): Future[Unit]

  def getAttributeByName(name: String)(implicit contexts: Seq[(String, String)]): Future[Attribute]

  def getAttributeByOriginAndCode(origin: String, code: String)(implicit
    contexts: Seq[(String, String)]
  ): Future[Attribute]

  def getAttributes(search: Option[String])(implicit contexts: Seq[(String, String)]): Future[AttributesResponse]

  def getBulkedAttributes(ids: Option[String])(implicit contexts: Seq[(String, String)]): Future[AttributesResponse]
}
