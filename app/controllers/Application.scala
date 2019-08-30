package controllers

import akka.stream.scaladsl.{Flow, Sink, Source}
import com.google.cloud.bigquery.{FieldValueList, LegacySQLTypeName, Schema}
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc.{InjectedController, WebSocket}

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
class Application @Inject() extends InjectedController {

  def index = Action { implicit request =>
    Ok(views.html.index(routes.Application.questions().webSocketURL()))
  }

  implicit val fieldValueListWrites = new Writes[(Schema, FieldValueList)] {
    override def writes(o: (Schema, FieldValueList)): JsValue = {
      val (schema, fieldValueList) = o

      schema.getFields.asScala.foldLeft(JsObject.empty) { case (json, field) =>
        val fieldValue = fieldValueList.get(field.getName)
        val fieldValueJson = field.getType match {
          case LegacySQLTypeName.BOOLEAN => Try(fieldValue.getBooleanValue).map(JsBoolean).getOrElse(JsNull)
          case LegacySQLTypeName.STRING => Try(fieldValue.getStringValue).map(JsString).getOrElse(JsNull)
          case LegacySQLTypeName.INTEGER => Try(fieldValue.getLongValue).map(JsNumber(_)).getOrElse(JsNull)
          case _ => JsNull
        }

        json + (field.getName -> fieldValueJson)
      }
    }
  }

  def questions = WebSocket.accept[JsValue, JsValue] { _ =>
    val query = """
                  |SELECT CONCAT('https://stackoverflow.com/questions/', CAST(id as STRING)) as url, title, view_count, favorite_count
                  |FROM `bigquery-public-data.stackoverflow.posts_questions`
                  |ORDER BY favorite_count DESC
                  |""".stripMargin

    val (schema, questions) = StackOverflowBigQuery.query(query)
    val questionSource = Source.fromIterator(() => questions.iterator)
    val source = Source.tick(Duration.Zero, 1.second, schema).zip(questionSource).map(Json.toJson(_))
    Flow.fromSinkAndSource(Sink.ignore, source)
  }

}

object StackOverflowBigQuery {
  import java.util.UUID

  import com.google.cloud.bigquery.{BigQuery, BigQueryOptions, FieldValueList, JobId, JobInfo, QueryJobConfiguration}

  def query(query: String): (Schema, Iterable[FieldValueList]) = {
    val bigQuery = BigQueryOptions.getDefaultInstance.getService
    val jobId = JobId.of(UUID.randomUUID().toString)
    val queryConfig = QueryJobConfiguration.newBuilder(query).setUseLegacySql(false).build()
    val queryJob = bigQuery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build())
    queryJob.waitFor()
    val page = queryJob.getQueryResults(BigQuery.QueryResultsOption.pageSize(1))
    page.getSchema -> page.iterateAll().asScala
  }

}
