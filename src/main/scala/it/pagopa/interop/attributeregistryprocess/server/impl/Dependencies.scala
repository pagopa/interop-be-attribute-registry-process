package it.pagopa.interop.attributeregistryprocess.server.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.SecurityDirectives
import com.atlassian.oai.validator.report.ValidationReport
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import it.pagopa.interop.attributeregistrymanagement.client.api.{AttributeApi => AttributeRegistryManagementApi}
import it.pagopa.interop.attributeregistrymanagement.client.invoker.{ApiInvoker => AttributeRegistryManagementInvoker}
import it.pagopa.interop.attributeregistryprocess.api.impl.{
  AttributeRegistryApiMarshallerImpl,
  AttributeRegistryApiServiceImpl,
  HealthApiMarshallerImpl,
  HealthServiceApiImpl
}
import it.pagopa.interop.attributeregistryprocess.api.{AttributeApi, HealthApi}
import it.pagopa.interop.attributeregistryprocess.common.system.ApplicationConfiguration
import it.pagopa.interop.attributeregistryprocess.error.ResponseHandlers.serviceCode
import it.pagopa.interop.attributeregistryprocess.service._
import it.pagopa.interop.attributeregistryprocess.service.impl._
import it.pagopa.interop.commons.cqrs.service.{MongoDbReadModelService, ReadModelService}
import it.pagopa.interop.commons.jwt.service.JWTReader
import it.pagopa.interop.commons.jwt.service.impl.{DefaultJWTReader, getClaimsVerifier}
import it.pagopa.interop.commons.jwt.{JWTConfiguration, KID, PublicKeysHolder, SerializedKey}
import it.pagopa.interop.commons.logging.{CanLogContextFields, ContextFieldsToLog}
import it.pagopa.interop.commons.utils.TypeConversions._
import it.pagopa.interop.commons.utils.errors.{Problem => CommonProblem}
import it.pagopa.interop.commons.utils.service.{OffsetDateTimeSupplier, UUIDSupplier}
import it.pagopa.interop.commons.utils.{AkkaUtils, OpenapiUtils}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

trait Dependencies {

  implicit val loggerTI: LoggerTakingImplicit[ContextFieldsToLog] =
    Logger.takingImplicit[ContextFieldsToLog]("OAuth2JWTValidatorAsContexts")

  val uuidSupplier: UUIDSupplier               = UUIDSupplier
  val dateTimeSupplier: OffsetDateTimeSupplier = OffsetDateTimeSupplier

  def jwtValidator(): Future[JWTReader] = JWTConfiguration.jwtReader
    .loadKeyset()
    .map(keyset =>
      new DefaultJWTReader with PublicKeysHolder {
        var publicKeyset: Map[KID, SerializedKey]                                        = keyset
        override protected val claimsVerifier: DefaultJWTClaimsVerifier[SecurityContext] =
          getClaimsVerifier(audience = ApplicationConfiguration.jwtAudience)
      }
    )
    .toFuture

  val validationExceptionToRoute: ValidationReport => Route = report => {
    val error =
      CommonProblem(StatusCodes.BadRequest, OpenapiUtils.errorFromRequestValidationReport(report), serviceCode, None)
    complete(error.status, error)
  }

  val healthApi: HealthApi = new HealthApi(
    new HealthServiceApiImpl(),
    HealthApiMarshallerImpl,
    SecurityDirectives.authenticateOAuth2("SecurityRealm", AkkaUtils.PassThroughAuthenticator),
    loggingEnabled = false
  )

  implicit val readModelService: ReadModelService = new MongoDbReadModelService(
    ApplicationConfiguration.readModelConfig
  )

  def attributeRegistryApi(jwtReader: JWTReader, blockingEc: ExecutionContextExecutor)(implicit
    actorSystem: ActorSystem[_],
    ec: ExecutionContext
  ): AttributeApi =
    new AttributeApi(
      AttributeRegistryApiServiceImpl(
        attributeRegistryManagement(blockingEc),
        TenantManagementServiceImpl,
        uuidSupplier,
        dateTimeSupplier
      ),
      AttributeRegistryApiMarshallerImpl,
      jwtReader.OAuth2JWTValidatorAsContexts
    )

  private def attributeRegistryManagementInvoker(blockingEc: ExecutionContextExecutor)(implicit
    actorSystem: ActorSystem[_]
  ): AttributeRegistryManagementInvoker =
    AttributeRegistryManagementInvoker(blockingEc)(actorSystem.classicSystem)

  private final val attributeRegistryManagementApi: AttributeRegistryManagementApi =
    AttributeRegistryManagementApi(ApplicationConfiguration.attributeRegistryManagementURL)

  def attributeRegistryManagement(
    blockingEc: ExecutionContextExecutor
  )(implicit actorSystem: ActorSystem[_]): AttributeRegistryManagementService =
    AttributeRegistryManagementServiceImpl(
      attributeRegistryManagementInvoker(blockingEc),
      attributeRegistryManagementApi
    )(blockingEc)

}
