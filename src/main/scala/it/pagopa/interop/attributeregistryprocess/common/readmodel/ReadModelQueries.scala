package it.pagopa.interop.attributeregistryprocess.common.readmodel

import it.pagopa.interop.attributeregistrymanagement.client.model.Attribute
import it.pagopa.interop.attributeregistryprocess.api.impl.attributeManagementFormat
import it.pagopa.interop.commons.cqrs.service.ReadModelService
import org.mongodb.scala.Document
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Aggregates.{`match`, count, project, sort}
import org.mongodb.scala.model.Projections.{computed, fields, include}
import org.mongodb.scala.model.Sorts.ascending

import scala.concurrent.{ExecutionContext, Future}

object ReadModelQueries {
  def getAttributes(kinds: List[String], offset: Int, limit: Int)(
    readModel: ReadModelService
  )(implicit ec: ExecutionContext): Future[PaginatedResult[Attribute]] = {
    val query = mapToVarArgs(kinds.map(Filters.eq("kind", _)))(Filters.or).getOrElse(Filters.empty())
    for {
      attributes <- readModel.aggregate[Attribute](
        "attributes",
        Seq(`match`(query), project(fields(include("data"))), sort(ascending("data.code"))),
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
