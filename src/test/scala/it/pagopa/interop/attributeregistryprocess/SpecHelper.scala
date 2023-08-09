package it.pagopa.interop.attributeregistryprocess

import org.scalamock.scalatest.MockFactory
import spray.json._
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import com.typesafe.config.{Config, ConfigFactory}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import it.pagopa.interop.attributeregistryprocess.service.{TenantManagementService, AttributeRegistryManagementService}
import it.pagopa.interop.commons.utils.service.UUIDSupplier
import it.pagopa.interop.commons.utils.service.OffsetDateTimeSupplier
import it.pagopa.interop.commons.utils.SprayCommonFormats.{offsetDateTimeFormat, uuidFormat}
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiService
import it.pagopa.interop.attributeregistryprocess.api.impl.AttributeRegistryApiServiceImpl
import it.pagopa.interop.attributeregistryprocess.model.Attribute
import it.pagopa.interop.attributeregistrymanagement.client.{model => AttributeDependency}
import it.pagopa.interop.tenantmanagement.model.tenant.PersistentTenant

import scala.concurrent.{Future, ExecutionContext}
import java.util.UUID

trait SpecHelper extends SprayJsonSupport with DefaultJsonProtocol with MockFactory {

  final val bearerToken: String = "token"

  val config: Config = ConfigFactory
    .parseResourcesAnySyntax("application-test")
    .resolve()

  implicit val mockReadModel: ReadModelService                                   = mock[ReadModelService]
  val mockAttributeRegistryManagementService: AttributeRegistryManagementService =
    mock[AttributeRegistryManagementService]
  val mockTenantManagementService: TenantManagementService                       = mock[TenantManagementService]

  val mockUUIDSupplier: UUIDSupplier               = mock[UUIDSupplier]
  val mockDateTimeSupplier: OffsetDateTimeSupplier = mock[OffsetDateTimeSupplier]

  val service: AttributeApiService = AttributeRegistryApiServiceImpl(
    mockAttributeRegistryManagementService,
    mockTenantManagementService,
    mockUUIDSupplier,
    mockDateTimeSupplier
  )(ExecutionContext.global, mockReadModel)

  def mockOrganizationRetrieve(tenantId: UUID, result: PersistentTenant) = {
    (mockTenantManagementService
      .getTenantById(_: UUID)(_: ExecutionContext, _: ReadModelService))
      .expects(tenantId, *, *)
      .once()
      .returns(Future.successful(result))
  }

  def mockAttributeCreate(attributeSeed: AttributeDependency.AttributeSeed, result: AttributeDependency.Attribute)(
    implicit contexts: Seq[(String, String)]
  ) =
    (mockAttributeRegistryManagementService
      .createAttribute(_: AttributeDependency.AttributeSeed)(_: Seq[(String, String)]))
      .expects(attributeSeed, contexts)
      .once()
      .returns(Future.successful(result))

  implicit def attributeFormat: RootJsonFormat[Attribute]                           = jsonFormat7(Attribute)
  implicit def fromResponseUnmarshallerAttribute: FromEntityUnmarshaller[Attribute] =
    sprayJsonUnmarshaller[Attribute]

}
