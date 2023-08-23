package it.pagopa.interop.attributeregistryprocess.api.impl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiMarshaller
import it.pagopa.interop.attributeregistryprocess.model.{
  Attribute,
  AttributeSeed,
  Attributes,
  CertifiedAttributeSeed,
  InternalCertifiedAttributeSeed,
  Problem
}
import spray.json.DefaultJsonProtocol

object AttributeRegistryApiMarshallerImpl
    extends AttributeApiMarshaller
    with SprayJsonSupport
    with DefaultJsonProtocol {
  override implicit def fromEntityUnmarshallerAttributeSeed: FromEntityUnmarshaller[AttributeSeed] =
    sprayJsonUnmarshaller[AttributeSeed]

  override implicit def fromEntityUnmarshallerInternalCertifiedAttributeSeed
    : FromEntityUnmarshaller[InternalCertifiedAttributeSeed] =
    sprayJsonUnmarshaller[InternalCertifiedAttributeSeed]

  override implicit def fromEntityUnmarshallerCertifiedAttributeSeed: FromEntityUnmarshaller[CertifiedAttributeSeed] =
    sprayJsonUnmarshaller[CertifiedAttributeSeed]

  override implicit def toEntityMarshallerAttribute: ToEntityMarshaller[Attribute] = sprayJsonMarshaller[Attribute]

  override implicit def toEntityMarshallerProblem: ToEntityMarshaller[Problem] = entityMarshallerProblem

  override implicit def toEntityMarshallerAttributes: ToEntityMarshaller[Attributes] = sprayJsonMarshaller[Attributes]

  override implicit def fromEntityUnmarshallerStringList: FromEntityUnmarshaller[Seq[String]] =
    sprayJsonUnmarshaller[Seq[String]]
}
