package it.pagopa.interop.attributeregistryprocess

object Utils {
  final val kindToBeExcluded: Set[String] = Set(
    "Enti Nazionali di Previdenza ed Assistenza Sociale in Conto Economico Consolidato",
    "Gestori di Pubblici Servizi",
    "Societa' in Conto Economico Consolidato",
    "Stazioni Appaltanti"
  )
}
