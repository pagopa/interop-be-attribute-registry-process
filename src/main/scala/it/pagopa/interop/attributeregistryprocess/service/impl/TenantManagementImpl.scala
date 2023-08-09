package it.pagopa.interop.attributeregistryprocess.service.impl

import it.pagopa.interop.attributeregistryprocess.service.TenantManagementService
import it.pagopa.interop.tenantmanagement.model.tenant.PersistentTenant
import it.pagopa.interop.attributeregistryprocess.common.readmodel.ReadModelTenantQueries
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import it.pagopa.interop.attributeregistryprocess.error.AttributeRegistryProcessErrors.TenantNotFound
import it.pagopa.interop.commons.utils.TypeConversions._

import java.util.UUID
import scala.concurrent.{Future, ExecutionContext}

final object TenantManagementServiceImpl extends TenantManagementService {
  override def getTenantById(
    tenantId: UUID
  )(implicit ec: ExecutionContext, readModel: ReadModelService): Future[PersistentTenant] = {
    ReadModelTenantQueries.getTenantById(tenantId).flatMap(_.toFuture(TenantNotFound(tenantId)))
  }
}
