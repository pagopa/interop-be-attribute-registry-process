package it.pagopa.interop.attributeregistryprocess

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.nimbusds.jwt.JWTClaimsSet
import com.typesafe.config.{Config, ConfigFactory}
import it.pagopa.interop.attributeregistrymanagement.client.{model => ManagementDependency}
import it.pagopa.interop.attributeregistryprocess.api.AttributeApiService
import it.pagopa.interop.attributeregistryprocess.api.impl.AttributeRegistryApiServiceImpl
import it.pagopa.interop.attributeregistryprocess.model.{AttributeKind, AttributeSeed}
import it.pagopa.interop.attributeregistryprocess.service.{AttributeRegistryManagementService, PartyRegistryService}
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import it.pagopa.interop.commons.utils.Digester
import it.pagopa.interop.commons.utils.service.{OffsetDateTimeSupplier, UUIDSupplier}
import it.pagopa.interop.partyregistryproxy.client.model.{Categories, Category, Institution, Institutions}
import org.mongodb.scala.bson.conversions.Bson
import org.scalamock.scalatest.MockFactory
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

trait SpecHelper extends SprayJsonSupport with DefaultJsonProtocol with MockFactory {

  final val bearerToken: String           = "token"
  final val admittedAttributeKind: String = "Pubbliche Amministrazioni"

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

  val categories: Categories =
    Categories(
      Seq(Category("YADA", "YADA", admittedAttributeKind, "IPA"), Category("OPA", "OPA", admittedAttributeKind, "IPA")),
      2
    )

  val institutions: Institutions = Institutions(
    Seq(
      Institution(
        id = "1111",
        originId = "104532",
        o = Option("test"),
        ou = Option("test"),
        aoo = Option("test"),
        taxCode = "19530",
        category = "C7",
        description = "104532",
        digitalAddress = "test",
        address = "test",
        zipCode = "49300",
        origin = "IPA",
        kind = admittedAttributeKind
      ),
      Institution(
        id = "2222",
        originId = "205942",
        o = Option("test"),
        ou = Option("test"),
        aoo = Option("test"),
        taxCode = "19530",
        category = "L8",
        description = "205942",
        digitalAddress = "test",
        address = "test",
        zipCode = "90142",
        origin = "IPA",
        kind = admittedAttributeKind
      )
    ),
    2
  )

  val attributeSeeds: Seq[AttributeSeed] = Seq(
    AttributeSeed(
      code = Some("YADA"),
      kind = AttributeKind.CERTIFIED,
      description = "YADA",
      origin = Some("IPA"),
      name = "YADA"
    ),
    AttributeSeed(
      code = Some("OPA"),
      kind = AttributeKind.CERTIFIED,
      description = "OPA",
      origin = Some("IPA"),
      name = "OPA"
    ),
    AttributeSeed(
      code = Some(Digester.toSha256(admittedAttributeKind.getBytes())),
      kind = AttributeKind.CERTIFIED,
      description = admittedAttributeKind,
      origin = Some("IPA"),
      name = admittedAttributeKind
    ),
    AttributeSeed(
      code = Some("104532"),
      kind = AttributeKind.CERTIFIED,
      description = "104532",
      origin = Some("IPA"),
      name = "104532"
    ),
    AttributeSeed(
      code = Some("205942"),
      kind = AttributeKind.CERTIFIED,
      description = "205942",
      origin = Some("IPA"),
      name = "205942"
    )
  )

  def mockSubject(uuid: String): Try[JWTClaimsSet] = Success(new JWTClaimsSet.Builder().subject(uuid).build())

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
