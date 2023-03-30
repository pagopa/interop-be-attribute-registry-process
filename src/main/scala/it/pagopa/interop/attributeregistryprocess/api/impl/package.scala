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


  implicit def attributesResponseFormat: RootJsonFormat[Attributes] = jsonFormat2(Attributes)

}
