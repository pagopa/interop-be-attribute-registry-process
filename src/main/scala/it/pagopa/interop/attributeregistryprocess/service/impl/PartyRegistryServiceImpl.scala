package it.pagopa.interop.attributeregistryprocess.service.impl

import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import it.pagopa.interop.attributeregistryprocess.service.PartyRegistryService
import it.pagopa.interop.partyregistryproxy.client.invoker.{ApiInvoker => PartyProxyInvoker}
import it.pagopa.interop.commons.logging.{CanLogContextFields, ContextFieldsToLog}
import it.pagopa.interop.commons.utils.withHeaders
import it.pagopa.interop.partyregistryproxy.client.api.{CategoryApi, InstitutionApi}
import it.pagopa.interop.partyregistryproxy.client.invoker.{ApiRequest, BearerToken}
import it.pagopa.interop.partyregistryproxy.client.model.{Categories, Institutions}

import scala.concurrent.Future

final case class PartyRegistryServiceImpl(
  invoker: PartyProxyInvoker,
  categoryApi: CategoryApi,
  institutionApi: InstitutionApi
) extends PartyRegistryService {

  implicit val logger: LoggerTakingImplicit[ContextFieldsToLog] =
    Logger.takingImplicit[ContextFieldsToLog](this.getClass)

  override def getCategories(bearerToken: String, page: Option[Int] = None, limit: Option[Int] = None)(implicit
    contexts: Seq[(String, String)]
  ): Future[Categories] =
    withHeaders { (bearerToken, correlationId, ip) =>
      val request: ApiRequest[Categories] =
        categoryApi.getCategories(
          origin = None,
          xCorrelationId = correlationId,
          xForwardedFor = ip,
          page = page,
          limit = limit
        )(BearerToken(bearerToken))
      invoker.invoke(request, "Retrieving categories")
    }

  override def getInstitutions(bearerToken: String, page: Option[Int] = None, limit: Option[Int] = None)(implicit
    contexts: Seq[(String, String)]
  ): Future[Institutions] =
    withHeaders { (bearerToken, correlationId, ip) =>
      val request: ApiRequest[Institutions] =
        institutionApi.searchInstitutions(
          xCorrelationId = correlationId,
          xForwardedFor = ip,
          page = page,
          limit = limit
        )(BearerToken(bearerToken))
      invoker.invoke(request, "Retrieving Institutions")
    }
}
