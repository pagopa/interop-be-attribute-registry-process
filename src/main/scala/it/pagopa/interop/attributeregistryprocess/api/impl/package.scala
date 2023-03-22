package it.pagopa.interop.attributeregistryprocess.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import it.pagopa.interop.attributeregistrymanagement.model.persistence.attribute._
import it.pagopa.interop.attributeregistryprocess.model._
import it.pagopa.interop.commons.utils.SprayCommonFormats._
import spray.json._

package object impl extends SprayJsonSupport with DefaultJsonProtocol {
  implicit def problemErrorFormat: RootJsonFormat[ProblemError] = jsonFormat2(ProblemError)
  implicit def problemFormat: RootJsonFormat[Problem]           = jsonFormat6(Problem)

  final val entityMarshallerProblem: ToEntityMarshaller[Problem]    = sprayJsonMarshaller[Problem]
  implicit def attributeSeedFormat: RootJsonFormat[AttributeSeed]   = jsonFormat5(AttributeSeed)
  implicit def attributeFormat: RootJsonFormat[Attribute]           = jsonFormat7(Attribute)
  implicit def attributesResponseFormat: RootJsonFormat[Attributes] = jsonFormat1(Attributes)

  implicit val persistentAttributeKindFormat: RootJsonFormat[PersistentAttributeKind] =
    new RootJsonFormat[PersistentAttributeKind] {
      override def read(json: JsValue): PersistentAttributeKind = json match {
        case JsString("Certified") => Certified
        case JsString("Declared")  => Declared
        case JsString("Verified")  => Verified
        case other => deserializationError(s"Unable to deserialize json as a PersistentAttributeKind: $other")
      }

      override def write(obj: PersistentAttributeKind): JsValue = obj match {
        case Certified => JsString("Certified")
        case Declared  => JsString("Declared")
        case Verified  => JsString("Verified")
      }
    }

  implicit val persistentAttributeFormat: RootJsonFormat[PersistentAttribute] = jsonFormat7(PersistentAttribute.apply)
}
