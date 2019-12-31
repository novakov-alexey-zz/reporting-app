package com.reporting.search

import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._

object jsonCodes {

  trait search {
    implicit val queryTermEncoder: ObjectEncoder[QueryTerm] =
      Encoder.forProduct2("term", "and_token")(q => (q.term, q.andToken))
    implicit val queryTermDecoder: Decoder[QueryTerm] = Decoder.forProduct2("term", "and_token")(QueryTerm.apply)

    implicit val paginationEncoder: ObjectEncoder[Pagination] = deriveEncoder[Pagination]
    implicit val paginationDecoder: Decoder[Pagination] = deriveDecoder[Pagination]

    implicit val encodeIntOrString: Encoder[Either[Long, String]] =
      Encoder.instance(_.fold(_.asJson, _.asJson))

    implicit val decodeIntOrString: Decoder[Either[Long, String]] =
      Decoder[Long].map(Left(_)).or(Decoder[String].map(Right(_)))

    implicit val scriptPropertyEncoder: ObjectEncoder[ScriptProperty] = deriveEncoder[ScriptProperty]
    implicit val scriptPropertyDecoder: Decoder[ScriptProperty] = deriveDecoder[ScriptProperty]

    implicit val highlightEncoder: ObjectEncoder[Highlight] = deriveEncoder[Highlight]
    implicit val highlightDecoder: Decoder[Highlight] = deriveDecoder[Highlight]

    implicit val queryEncoder: ObjectEncoder[Query] =
      Encoder.forProduct4("query", "script_property", "highlights", "pagination")(
        c => (c.query, c.scriptProperty, c.highlights, c.pagination)
      )

    implicit val queryDecoder: Decoder[Query] =
      Decoder.forProduct4("query", "script_property", "highlights", "pagination")(Query.apply)

    implicit val rowEncoder: Encoder[Row] = (r: Row) => {
      val hs = r.highlights.map(h => Seq(("highlights", h.asJson))).getOrElse(Seq.empty)
      val props = r.props.map { case (k, v) => (k, valueEncoder.apply(v)) }

      Json.obj(props ++ hs: _*)
    }

    implicit val metadataDecoder: Decoder[Metadata] = deriveDecoder[Metadata]
    implicit val metadataEncoder: Encoder[Metadata] = (m: Metadata) => {
      Json.obj(
        List(
          Some(("count", Json.fromLong(m.count))),
          m.countBySource.map(
            c =>
              "count_by_source" -> Json.arr(c.map {
                case (k, v) => Json.obj(k -> Json.fromLong(v))
              }: _*)
          )
        ).flatten: _*
      )
    }

    implicit val objEncoder: Encoder[Any] = {
      case x: String => x.asJson
      case x: Int => x.asJson
      case x: Double => x.asJson
      case x: Boolean => x.asJson
    }

    implicit def valueEncoder: Encoder[Value[_]] = (a: Value[_]) => {
      a.v match {
        case i: Int => Json.fromInt(i)
        case s: Short => Json.fromInt(s)
        case d: Double => Json.fromDouble(d).getOrElse(Json.Null)
        case s: String => Json.fromString(s)
        case f: Float => Json.fromFloat(f).getOrElse(Json.Null)
        case b: Boolean => Json.fromBoolean(b)
        case l: Long => Json.fromLong(l)
        case a @ x :: _ => a.asJson
        case _ => Json.Null
      }
    }

    implicit val rowsetEncoder: ObjectEncoder[RowSet] = deriveEncoder[RowSet]
  }

  object search extends search

  trait correlation {
    import search.{paginationDecoder, paginationEncoder, queryTermDecoder, queryTermEncoder}

    implicit val correlationEncoder: ObjectEncoder[Correlation] =
      Encoder.forProduct5("query", "datasource_from", "datasource_to", "properties", "pagination")(
        c => (c.query, c.datasourceFrom, c.datasourceTo, c.properties, c.pagination)
      )
    implicit val correlationDecoder: Decoder[Correlation] =
      Decoder.forProduct5("query", "datasource_from", "datasource_to", "properties", "pagination")(Correlation.apply)

    implicit val corrStatsEncoder: ObjectEncoder[CorrStats] = deriveEncoder[CorrStats]
    implicit val corrStatsDecoder: Decoder[CorrStats] = deriveDecoder[CorrStats]

    implicit val bucketEncoder: ObjectEncoder[Bucket] = deriveEncoder[Bucket]
    implicit val bucketDecoder: Decoder[Bucket] = deriveDecoder[Bucket]
  }

  object correlation extends correlation

  trait metadata {
    implicit val datasourcesEncoder: ObjectEncoder[Datasources] = deriveEncoder[Datasources]

    implicit val propertyEncoder: ObjectEncoder[Property] = deriveEncoder[Property]
    implicit val propertyDecoder: Decoder[Property] = deriveDecoder[Property]

    implicit val schemaEncoder: ObjectEncoder[Schema] = deriveEncoder[Schema]
    implicit val schemaDecoder: Decoder[Schema] = deriveDecoder[Schema]

    implicit val datasourceDecoder: Decoder[Datasource] = deriveDecoder[Datasource]
    implicit val datasourceEncoder: ObjectEncoder[Datasource] = deriveEncoder[Datasource]
  }

  object metadata extends metadata

  object all extends metadata with correlation with search
}
