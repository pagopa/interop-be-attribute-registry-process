package it.pagopa.interop.attributeregistryprocess.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import it.pagopa.interop.attributeregistrymanagement.client.model.AttributeKind._
import it.pagopa.interop.attributeregistrymanagement.client.model.{AttributeKind, Attribute => AttributeManagement}
import it.pagopa.interop.attributeregistryprocess.model.{
  AttributeSeed,
  Attributes,
  Problem,
  ProblemError,
  Attribute => AttributeProcess
}
import it.pagopa.interop.commons.utils.SprayCommonFormats._
import spray.json._

package object impl extends SprayJsonSupport with DefaultJsonProtocol {
  implicit def problemErrorFormat: RootJsonFormat[ProblemError] = jsonFormat2(ProblemError)
  implicit def problemFormat: RootJsonFormat[Problem]           = jsonFormat6(Problem)

  final val entityMarshallerProblem: ToEntityMarshaller[Problem]        = sprayJsonMarshaller[Problem]
  implicit def attributeSeedFormat: RootJsonFormat[AttributeSeed]       = jsonFormat5(AttributeSeed)
  implicit def attributeProcessFormat: RootJsonFormat[AttributeProcess] = jsonFormat7(AttributeProcess)

  implicit object AttributeKindFormat extends RootJsonFormat[AttributeKind] {
    def write(obj: AttributeKind): JsValue =
      obj match {
        case CERTIFIED => JsString("CERTIFIED")
        case DECLARED  => JsString("DECLARED")
        case VERIFIED  => JsString("VERIFIED")
      }

    def read(json: JsValue): AttributeKind =
      json match {
        case JsString("CERTIFIED") => CERTIFIED
        case JsString("DECLARED")  => DECLARED
        case JsString("VERIFIED")  => VERIFIED
        case unrecognized => deserializationError(s"AttributeKind serialization error ${unrecognized.toString}")
      }
  }

  implicit def attributeManagementFormat: RootJsonFormat[AttributeManagement] = jsonFormat7(AttributeManagement)

  implicit def attributesResponseFormat: RootJsonFormat[Attributes] = jsonFormat2(Attributes)

}
