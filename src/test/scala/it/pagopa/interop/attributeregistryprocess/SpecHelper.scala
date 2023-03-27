package it.pagopa.interop.attributeregistryprocess

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.nimbusds.jwt.JWTClaimsSet
import com.typesafe.config.{Config, ConfigFactory}
import it.pagopa.interop.attributeregistrymanagement.client.{model => ManagementDependency}
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiService
import it.pagopa.interop.attributeregistryprocess.{model => ProcessDependency}
import it.pagopa.interop.attributeregistryprocess.api.impl.AttributeRegistryApiServiceImpl
import it.pagopa.interop.attributeregistryprocess.service.{AttributeRegistryManagementService, PartyRegistryService}
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import it.pagopa.interop.commons.utils.service.{OffsetDateTimeSupplier, UUIDSupplier}
import it.pagopa.interop.partyregistryproxy.client.model.{Categories, Institutions}
import org.mongodb.scala.bson.conversions.Bson
import org.scalamock.scalatest.MockFactory
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

trait SpecHelper extends SprayJsonSupport with DefaultJsonProtocol with MockFactory {

  final val bearerToken: String = "token"

  val config: Config = ConfigFactory
    .parseResourcesAnySyntax("application-test")
    .resolve()

  val mockReadModel: ReadModelService                                            = mock[ReadModelService]
  val mockAttributeRegistryManagementService: AttributeRegistryManagementService =
    mock[AttributeRegistryManagementService]
  val mockPartyRegistryService: PartyRegistryService                             = mock[PartyRegistryService]

  val mockUUIDSupplier: UUIDSupplier               = mock[UUIDSupplier]
  val mockDateTimeSupplier: OffsetDateTimeSupplier = mock[OffsetDateTimeSupplier]

  val service: AttributeApiService = AttributeRegistryApiServiceImpl(
    mockAttributeRegistryManagementService,
    mockUUIDSupplier,
    mockDateTimeSupplier,
    mockPartyRegistryService,
    mockReadModel
  )(ExecutionContext.global)

  def mockSubject(uuid: String): Try[JWTClaimsSet] = Success(new JWTClaimsSet.Builder().subject(uuid).build())

  def delta(
    attrs: List[ProcessDependency.Attribute],
    attributesSeeds: Seq[ProcessDependency.AttributeSeed]
  ): Set[ProcessDependency.AttributeSeed] =
    attributesSeeds.foldLeft[Set[ProcessDependency.AttributeSeed]](Set.empty)((attributesDelta, seed) =>
      attrs
        .find(persisted => seed.origin == persisted.origin && seed.code == persisted.code)
        .fold(attributesDelta + seed)(_ => attributesDelta)
    )

  def mockCreateAttribute(attributeSeed: ManagementDependency.AttributeSeed, result: ManagementDependency.Attribute)(
    implicit contexts: Seq[(String, String)]
  ) =
    (mockAttributeRegistryManagementService
      .createAttribute(_: ManagementDependency.AttributeSeed)(_: Seq[(String, String)]))
      .expects(attributeSeed, contexts)
      .once()
      .returns(Future.successful(result))

  def mockGetCategories(page: Option[Int] = None, limit: Option[Int] = None, result: Categories)(implicit
    contexts: Seq[(String, String)]
  ) =
    (mockPartyRegistryService
      .getCategories(_: Option[Int], _: Option[Int])(_: Seq[(String, String)]))
      .expects(page, limit, contexts)
      .once()
      .returns(Future.successful(result))

  def mockGetInstitutions(page: Option[Int] = None, limit: Option[Int] = None, result: Institutions)(implicit
    contexts: Seq[(String, String)]
  ) =
    (mockPartyRegistryService
      .getInstitutions(_: Option[Int], _: Option[Int])(_: Seq[(String, String)]))
      .expects(page, limit, contexts)
      .once()
      .returns(Future.successful(result))

  def mockFind[T](collectionName: String, filter: Bson, offset: Int, limit: Int, result: Seq[T]) =
    (mockReadModel
      .find(_: String, _: Bson, _: Int, _: Int)(_: JsonReader[T], _: ExecutionContext))
      .expects(collectionName, filter, offset, limit, *, *)
      .once()
      .returns(Future.successful(result))
}
