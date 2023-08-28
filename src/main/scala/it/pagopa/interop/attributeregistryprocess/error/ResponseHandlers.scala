package it.pagopa.interop.attributeregistryprocess.error

import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LoggerTakingImplicit
import it.pagopa.interop.commons.logging.ContextFieldsToLog
import it.pagopa.interop.commons.utils.errors.GenericComponentErrors.OperationForbidden
import it.pagopa.interop.commons.utils.errors.{AkkaResponses, ServiceCode}
import it.pagopa.interop.attributeregistryprocess.error.AttributeRegistryProcessErrors._

import scala.util.{Failure, Success, Try}

object ResponseHandlers extends AkkaResponses {
  implicit val serviceCode: ServiceCode = ServiceCode("021")

  def createInternalCertifiedAttributeResponse[T](logMessage: String)(
    success: T => Route
  )(result: Try[T])(implicit contexts: Seq[(String, String)], logger: LoggerTakingImplicit[ContextFieldsToLog]): Route =
    result match {
      case Success(s)                               => success(s)
      case Failure(ex: OrganizationIsNotACertifier) => forbidden(ex, logMessage)
      case Failure(ex)                              => internalServerError(ex, logMessage)
    }

  def createCertifiedAttributeResponse[T](logMessage: String)(
    success: T => Route
  )(result: Try[T])(implicit contexts: Seq[(String, String)], logger: LoggerTakingImplicit[ContextFieldsToLog]): Route =
    result match {
      case Success(s)                               => success(s)
      case Failure(ex: OrganizationIsNotACertifier) => forbidden(ex, logMessage)
      case Failure(ex)                              => internalServerError(ex, logMessage)
    }

  def createDeclaredAttributeResponse[T](logMessage: String)(
    success: T => Route
  )(result: Try[T])(implicit contexts: Seq[(String, String)], logger: LoggerTakingImplicit[ContextFieldsToLog]): Route =
    result match {
      case Success(s)                        => success(s)
      case Failure(ex: OriginIsNotCompliant) => forbidden(ex, logMessage)
      case Failure(ex)                       => internalServerError(ex, logMessage)
    }

  def createVerifiedAttributeResponse[T](logMessage: String)(
    success: T => Route
  )(result: Try[T])(implicit contexts: Seq[(String, String)], logger: LoggerTakingImplicit[ContextFieldsToLog]): Route =
    result match {
      case Success(s)                        => success(s)
      case Failure(ex: OriginIsNotCompliant) => forbidden(ex, logMessage)
      case Failure(ex)                       => internalServerError(ex, logMessage)
    }

  def getAttributeByIdResponse[T](logMessage: String)(
    success: T => Route
  )(result: Try[T])(implicit contexts: Seq[(String, String)], logger: LoggerTakingImplicit[ContextFieldsToLog]): Route =
    result match {
      case Success(s)                             => success(s)
      case Failure(ex: OperationForbidden.type)   => forbidden(ex, logMessage)
      case Failure(ex: RegistryAttributeNotFound) => notFound(ex, logMessage)
      case Failure(ex)                            => internalServerError(ex, logMessage)
    }

  def getAttributeByNameResponse[T](logMessage: String)(
    success: T => Route
  )(result: Try[T])(implicit contexts: Seq[(String, String)], logger: LoggerTakingImplicit[ContextFieldsToLog]): Route =
    result match {
      case Success(s)                             => success(s)
      case Failure(ex: OperationForbidden.type)   => forbidden(ex, logMessage)
      case Failure(ex: RegistryAttributeNotFound) => notFound(ex, logMessage)
      case Failure(ex)                            => internalServerError(ex, logMessage)
    }

  def getAttributeByOriginAndCodeResponse[T](logMessage: String)(
    success: T => Route
  )(result: Try[T])(implicit contexts: Seq[(String, String)], logger: LoggerTakingImplicit[ContextFieldsToLog]): Route =
    result match {
      case Success(s)                             => success(s)
      case Failure(ex: OperationForbidden.type)   => forbidden(ex, logMessage)
      case Failure(ex: RegistryAttributeNotFound) => notFound(ex, logMessage)
      case Failure(ex)                            => internalServerError(ex, logMessage)
    }

  def getAttributesResponse[T](logMessage: String)(
    success: T => Route
  )(result: Try[T])(implicit contexts: Seq[(String, String)], logger: LoggerTakingImplicit[ContextFieldsToLog]): Route =
    result match {
      case Success(s)                           => success(s)
      case Failure(ex: OperationForbidden.type) => forbidden(ex, logMessage)
      case Failure(ex)                          => internalServerError(ex, logMessage)
    }

}
