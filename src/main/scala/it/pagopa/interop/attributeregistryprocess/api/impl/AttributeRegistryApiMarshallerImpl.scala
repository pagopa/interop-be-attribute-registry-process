package it.pagopa.interop.attributeregistryprocess.api.impl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiMarshaller
import it.pagopa.interop.attributeregistryprocess.model.{Attribute, AttributeSeed, AttributesResponse, Problem}
import spray.json.DefaultJsonProtocol

object AttributeRegistryApiMarshallerImpl
    extends AttributeApiMarshaller
    with SprayJsonSupport
    with DefaultJsonProtocol {
  override implicit def fromEntityUnmarshallerAttributeSeed: FromEntityUnmarshaller[AttributeSeed] =
    sprayJsonUnmarshaller[AttributeSeed]

  override implicit def toEntityMarshallerAttributesResponse: ToEntityMarshaller[AttributesResponse] =
    sprayJsonMarshaller[AttributesResponse]

  override implicit def toEntityMarshallerAttribute: ToEntityMarshaller[Attribute] = sprayJsonMarshaller[Attribute]

  override implicit def toEntityMarshallerProblem: ToEntityMarshaller[Problem] = sprayJsonMarshaller[Problem]
}
