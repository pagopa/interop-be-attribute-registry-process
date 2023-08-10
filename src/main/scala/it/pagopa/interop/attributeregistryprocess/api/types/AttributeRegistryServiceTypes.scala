package it.pagopa.interop.attributeregistryprocess.api.types

import it.pagopa.interop.attributeregistrymanagement.client.{model => AttributeRegistryManagementDependency}
import it.pagopa.interop.attributeregistrymanagement.model.persistence.{attribute => AttributeModel}
import it.pagopa.interop.attributeregistryprocess.model.{
  Attribute,
  AttributeKind,
  AttributeSeed,
  CertifiedAttributeSeed,
  InternalCertifiedAttributeSeed
}

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

  implicit class CertifiedAttributeSeedConverter(private val attributeSeed: CertifiedAttributeSeed) extends AnyVal {

    def toManagement(origin: String): AttributeRegistryManagementDependency.AttributeSeed =
      AttributeRegistryManagementDependency.AttributeSeed(
        code = Some(attributeSeed.code),
        kind = AttributeRegistryManagementDependency.AttributeKind.CERTIFIED,
        description = attributeSeed.description,
        origin = Some(origin),
        name = attributeSeed.name
      )
  }

  implicit class InternalCertifiedAttributeSeedConverter(private val attributeSeed: InternalCertifiedAttributeSeed)
      extends AnyVal {

    def toManagement: AttributeRegistryManagementDependency.AttributeSeed =
      AttributeRegistryManagementDependency.AttributeSeed(
        code = Some(attributeSeed.code),
        kind = AttributeRegistryManagementDependency.AttributeKind.CERTIFIED,
        description = attributeSeed.description,
        origin = Some(attributeSeed.origin),
        name = attributeSeed.name
      )
  }

  implicit class AttributeSeedConverter(private val attributeSeed: AttributeSeed) extends AnyVal {

    def toManagement(kind: AttributeKind): AttributeRegistryManagementDependency.AttributeSeed =
      AttributeRegistryManagementDependency.AttributeSeed(
        code = None,
        kind = kind.toClient,
        description = attributeSeed.description,
        origin = None,
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

  implicit class PersistentAttributeKindTypeConverter(private val kind: AttributeModel.PersistentAttributeKind.type)
      extends AnyVal {
    def fromApi(kind: AttributeKind): AttributeModel.PersistentAttributeKind = kind match {
      case AttributeKind.CERTIFIED => AttributeModel.Certified
      case AttributeKind.DECLARED  => AttributeModel.Declared
      case AttributeKind.VERIFIED  => AttributeModel.Verified
    }
  }
}
