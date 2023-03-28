package it.pagopa.interop.attributeregistryprocess.api.types

import it.pagopa.interop.attributeregistrymanagement.client.{model => AttributeRegistryManagementDependency}
import it.pagopa.interop.attributeregistrymanagement.model.persistence.{attribute => AttributeModel}
import it.pagopa.interop.attributeregistryprocess.model.{Attribute, AttributeKind, AttributeSeed}

object AttributeRegistryServiceTypes {

  implicit class ManagementKindConverter(private val kind: AttributeRegistryManagementDependency.AttributeKind)
      extends AnyVal {
    def toApi: AttributeKind = kind match {
      case AttributeRegistryManagementDependency.AttributeKind.CERTIFIED => AttributeKind.CERTIFIED
      case AttributeRegistryManagementDependency.AttributeKind.DECLARED  => AttributeKind.DECLARED
      case AttributeRegistryManagementDependency.AttributeKind.VERIFIED  => AttributeKind.VERIFIED
    }
  }

  implicit class AttributeConverter(private val attribute: AttributeRegistryManagementDependency.Attribute)
      extends AnyVal {
    def toApi: Attribute =
      Attribute(
        id = attribute.id,
        code = attribute.code,
        kind = attribute.kind.toApi,
        description = attribute.description,
        origin = attribute.origin,
        name = attribute.name,
        creationTime = attribute.creationTime
      )
  }

  implicit class ProcessKindConverter(private val kind: AttributeKind) extends AnyVal {
    def toClient: AttributeRegistryManagementDependency.AttributeKind = kind match {
      case AttributeKind.CERTIFIED => AttributeRegistryManagementDependency.AttributeKind.CERTIFIED
      case AttributeKind.DECLARED  => AttributeRegistryManagementDependency.AttributeKind.DECLARED
      case AttributeKind.VERIFIED  => AttributeRegistryManagementDependency.AttributeKind.VERIFIED
    }
  }

  implicit class AttributeSeedConverter(private val attributeSeed: AttributeSeed) extends AnyVal {

    def toClient: AttributeRegistryManagementDependency.AttributeSeed =
      AttributeRegistryManagementDependency.AttributeSeed(
        code = attributeSeed.code,
        kind = attributeSeed.kind.toClient,
        description = attributeSeed.description,
        origin = attributeSeed.origin,
        name = attributeSeed.name
      )
  }

  implicit class ManagementAttributeSeedConverter(
    private val attributeSeed: AttributeRegistryManagementDependency.AttributeSeed
  ) extends AnyVal {

    def toApi: AttributeSeed =
      AttributeSeed(
        code = attributeSeed.code,
        kind = attributeSeed.kind.toApi,
        description = attributeSeed.description,
        origin = attributeSeed.origin,
        name = attributeSeed.name
      )
  }

  implicit class PersistentAttributeConverter(private val pa: AttributeModel.PersistentAttribute) extends AnyVal {
    def toApi: Attribute = Attribute(
      id = pa.id,
      code = pa.code,
      kind = pa.kind.toApi,
      description = pa.description,
      origin = pa.origin,
      name = pa.name,
      creationTime = pa.creationTime
    )
  }

  implicit class PersistentAttributeKindConverter(private val kind: AttributeModel.PersistentAttributeKind)
      extends AnyVal {
    def toApi: AttributeKind = kind match {
      case AttributeModel.Certified => AttributeKind.CERTIFIED
      case AttributeModel.Declared  => AttributeKind.DECLARED
      case AttributeModel.Verified  => AttributeKind.VERIFIED
    }
  }

  implicit class CatalogAttributeConverter(private val ca: ) extends AnyVal {
    def convertToApiEService(eService: readmodel.CatalogItem): EService = EService(
      id = eService.id,
      producerId = eService.producerId,
      name = eService.name,
      description = eService.description,
      technology = convertToApiTechnology(eService.technology),
      attributes = convertToApiAttributes(eService.attributes),
      descriptors = eService.descriptors.map(convertToApiDescriptor)
    )
  }
}
