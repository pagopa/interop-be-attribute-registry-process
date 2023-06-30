package it.pagopa.interop.attributeregistryprocess.common.readmodel

import it.pagopa.interop.attributeregistrymanagement.model.persistence.attribute.{
  PersistentAttribute,
  PersistentAttributeKind
}
import it.pagopa.interop.attributeregistrymanagement.model.persistence.JsonFormats._
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import org.mongodb.scala.Document
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Aggregates.{`match`, count, project, sort}
import org.mongodb.scala.model.Projections.{computed, fields, include}
import org.mongodb.scala.model.Sorts.ascending

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

object ReadModelQueries {
  def getAttributes(
    name: Option[String],
    kinds: List[PersistentAttributeKind],
    ids: List[UUID],
    offset: Int,
    limit: Int
  )(readModel: ReadModelService)(implicit ec: ExecutionContext): Future[PaginatedResult[PersistentAttribute]] = {

    val idsFilter   = mapToVarArgs(ids.map(id => Filters.eq("data.id", id.toString)))(Filters.or)
    val kindsFilter = mapToVarArgs(kinds.map(k => Filters.eq("data.kind", k.toString)))(Filters.or)
    val nameFilter  = name.map(Filters.regex("data.name", _, "i"))
    val query       = mapToVarArgs(idsFilter.toList ++ kindsFilter.toList ++ nameFilter.toList)(Filters.and)
      .getOrElse(Filters.empty())

    for {
      attributes <- readModel.aggregate[PersistentAttribute](
        "attributes",
        Seq(
          `match`(query),
          project(fields(include("data"), computed("lowerName", Document("""{ "$toLower" : "$data.name" }""")))),
          sort(ascending("lowerName"))
        ),
        offset = offset,
        limit = limit
      )

      // Note: This could be obtained using $facet function (avoiding to execute the query twice),
      //   but it is not supported by DocumentDB
      count      <- readModel.aggregate[TotalCountResult](
        "attributes",
        Seq(
          `match`(query),
          count("totalCount"),
          project(computed("data", Document("""{ "totalCount" : "$totalCount" }""")))
        ),
        offset = 0,
        limit = Int.MaxValue
      )
    } yield PaginatedResult(results = attributes, totalCount = count.headOption.map(_.totalCount).getOrElse(0))
  }

  private def mapToVarArgs[A, B](l: Seq[A])(f: Seq[A] => B): Option[B] = Option.when(l.nonEmpty)(f(l))
}
