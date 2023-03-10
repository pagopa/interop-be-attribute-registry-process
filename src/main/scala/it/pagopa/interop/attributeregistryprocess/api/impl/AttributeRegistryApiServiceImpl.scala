package it.pagopa.interop.attributeregistryprocess.api.impl

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Route
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiService
import it.pagopa.interop.attributeregistryprocess.model.{Attribute, AttributeSeed, AttributesResponse, Problem}
import it.pagopa.interop.attributeregistryprocess.service._
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import it.pagopa.interop.commons.utils.service.{OffsetDateTimeSupplier, UUIDSupplier}

import scala.concurrent.ExecutionContext

final case class AttributeRegistryApiServiceImpl(
  service: AttributeRegistryManagementService,
  service1: TenantManagementService,
  service2: AgreementProcessService,
  service3: AgreementManagementService,
  service4: CatalogManagementService,
  readModelService: ReadModelService,
  uuidSupplier: UUIDSupplier,
  dateTimeSupplier: OffsetDateTimeSupplier
)(implicit ec: ExecutionContext)
    extends AttributeApiService {
  override def createAttribute(attributeSeed: AttributeSeed)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = ???

  override def deleteAttributeById(
    attributeId: String
  )(implicit contexts: Seq[(String, String)], toEntityMarshallerProblem: ToEntityMarshaller[Problem]): Route = ???

  override def getAttributeById(attributeId: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = ???

  override def getAttributeByName(name: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = ???

  override def getAttributeByOriginAndCode(origin: String, code: String)(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttribute: ToEntityMarshaller[Attribute],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = ???

  override def getAttributes(search: Option[String])(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttributesResponse: ToEntityMarshaller[AttributesResponse],
    toEntityMarshallerProblem: ToEntityMarshaller[Problem]
  ): Route = ???

  override def getBulkedAttributes(ids: Option[String])(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerAttributesResponse: ToEntityMarshaller[AttributesResponse]
  ): Route = ???

  override def loadCertifiedAttributes()(implicit contexts: Seq[(String, String)]): Route = ???
}
