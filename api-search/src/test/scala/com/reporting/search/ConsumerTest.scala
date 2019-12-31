package com.reporting.search

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.reporting.search.EsSchema._
import com.reporting.search.jsonCodes.all._
import com.reporting.search.http.{CorrelationRoutes, MetaRoutes, QueryRoutes}
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.duration._

class ConsumerTest extends FlatSpec with Matchers with Eventually with ScalatestRouteTest with BeforeAndAfterAll {
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(30.seconds, 200.millis)

  private val msg = "right is expected"

  private val esTestNode = new EsTestNode()
  private val search = esTestNode.searchService
  private val meta = esTestNode.metaService
  private val correlation = esTestNode.correlationService
  val queryRoute: Route = new QueryRoutes[Query, RowSet]().api(search.query)

  private val queryApiUrl = "/api/v1/query"
  private val metaApiUrl = "/api/v1/meta"
  private val correlationApiUrl = "/api/v1/correlation"

  it should "query all datasources via GET request" in {
    //given
    val reqEntity = HttpEntity(MediaTypes.`application/json`, Query(QueryTerm(firstName)).asJson.noSpaces)
    eventually {
      //when
      Post(uri = s"$queryApiUrl/all", entity = reqEntity) ~> queryRoute ~> check {
        response.status shouldEqual StatusCodes.OK

        val parsed = parse(responseAs[String])

        parsed.left.foreach(e => println(s"Failed to parse, reason: ${e.toString}"))
        // then
        parsed.isRight should be(true)

        val json = parsed.right.getOrElse(fail(msg))
        val metadata = json.hcursor.downField("metadata")

        metadata.downField(countField).as[Long].right.getOrElse(fail(msg)) should be(2)
        val counts = metadata.downField("count_by_source")

        val contactCounts = counts.downArray.first.downField(contactDs).as[Long]
        contactCounts.map(_ should be(1)).getOrElse(fail(msg))

        val itUsageCounts = counts.downArray.last.downField(itUsageDs).as[Long]
        itUsageCounts.map(_ should be(1)).getOrElse(fail("should be some counts"))

        readRowsJson(json)
          .map(d => d.size should be(2))
          .orElse(fail(s"should be non-empty rows"))
      }
    }
  }

  it should "query a datasource via GET request" in {
    val reqEntity = HttpEntity(MediaTypes.`application/json`, Query(QueryTerm(firstName)).asJson.noSpaces)
    //when
    def query(index: Seq[String], count: Int) = {
      Post(uri = s"$queryApiUrl/some?ds=${index.mkString(",")}", entity = reqEntity) ~> queryRoute ~> check {
        response.status shouldEqual StatusCodes.OK
        val parsed = parse(responseAs[String])

        parsed.isRight should be(true)

        val json = parsed.right.getOrElse(fail(msg))
        val metadata = json.hcursor.downField("metadata")

        metadata
          .downField(countField)
          .as[Long]
          .map(_ should be(count))
          .getOrElse(fail("should be some count"))
      }
    }

    query(Seq(contactDs), 1)
    query(Seq(contactDs, itUsageDs), 2)
  }

  it should "query datasource list via GET request" in {
    //given
    val datasourcesRoute = new MetaRoutes().datasources(_ => meta.datasources())

    eventually {
      //when
      Get(uri = s"$metaApiUrl/datasources") ~> datasourcesRoute ~> check {
        response.status shouldEqual StatusCodes.OK
        val parsed = parse(responseAs[String])
        //then
        parsed.isRight should be(true)

        val json = parsed.right.getOrElse(fail("should be parsed"))
        val list = json.hcursor.downField("list").values.getOrElse(fail("should be some list"))

        list.flatMap(_.asString).toSet should be(Set(contactDs, itUsageDs, bankingDs))
      }
    }
  }

  it should "query one datasource schema via GET request" in {
    //given
    val schemaRoute = new MetaRoutes().schema(meta.schema)
    val expected =
      Schema(
        Seq(
          Datasource(
            contactDs,
            Seq(
              Property(ageField, "integer"),
              Property(birthdayField, "date"),
              Property(firstNameField, "text"),
              Property(userIdField, "integer")
            )
          )
        )
      )

    eventually {
      //when
      Get(uri = s"$metaApiUrl/schema/$contactDs") ~> schemaRoute ~> check {
        response.status shouldEqual StatusCodes.OK

        val schema = decode[Schema](responseAs[String])
        //then
        schema.getOrElse(Schema(Seq())) should be(expected)
      }
    }
  }

  it should "query all datasource schemas via GET request" in {
    //given
    val schemaRoute = new MetaRoutes().schema(meta.schema)
    val expected = Set(
      (
        contactDs,
        Set(
          Property(ageField, "integer"),
          Property(birthdayField, "date"),
          Property(firstNameField, "text"),
          Property(userIdField, "integer")
        )
      ),
      (itUsageDs, Set(Property(emailField, "text"), Property(userIdField, "integer"))),
      (bankingDs, Set(Property(ibanField, "text"), Property(bicField, "text"), Property(userIdField, "integer")))
    )

    eventually {
      //when
      Get(uri = s"$metaApiUrl/schema") ~> schemaRoute ~> check {
        response.status shouldEqual StatusCodes.OK

        val schema = decode[Schema](responseAs[String])
        val datasources = schema.map(_.datasources.toSet).getOrElse(Set.empty)
        //then
        datasources.size shouldNot be(0)

        val ds = datasources.map(d => (d.name, d.properties.toSet))
        ds should be(expected)
      }
    }
  }

  it should "query correlated data via POST request" in {
    //given
    val corrSearchRoute = CorrelationRoutes.search[Correlation, RowSet](correlation.correlation)
    val reqEntity =
      HttpEntity(MediaTypes.`application/json`, Correlation(QueryTerm("11110"), contactDs).asJson.noSpaces)

    eventually {
      //when
      Post(uri = s"$correlationApiUrl/search", entity = reqEntity) ~> corrSearchRoute ~> check {
        val str = responseAs[String]
        val json = parse(str).right.getOrElse(fail(msg))

        //then
        readRowsJson(json)
          .map(d => d.size should be(2))
          .orElse(fail(s"should be non-empty rows"))
      }
    }
  }

  private def readRowsJson(json: Json) = {
    val rowsField = "rows"
    json.hcursor.downField(rowsField).values shouldNot be(None)
    json.hcursor
      .downField(rowsField)
      .values
  }

  override def afterAll(): Unit =
    esTestNode.close()
}
