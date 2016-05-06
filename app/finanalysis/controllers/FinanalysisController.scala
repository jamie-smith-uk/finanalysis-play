package finanalysis.controllers

import java.util.Calendar

import finanalysis.models._
import play.api.libs.json.Json._
import play.api.mvc.{Action, Controller}
import finanalysis.dal.{StatementDAO, Statement}
import slick.driver.PostgresDriver.api._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import play.api.libs.concurrent.Execution.Implicits.defaultContext


import javax.inject.Inject

import scala.concurrent.Future
import scala.util.{Success, Failure}


class FinanalysisController @Inject()(thingDAO: StatementDAO) extends Controller {

  implicit val allCostsWrites = new Writes[(String, Double)] {
    def writes(allCosts: (String, Double)) = Json.obj (
      "category" -> allCosts._1,
      "amount" -> allCosts._2
    )
  }

  def index = Action { implicit request =>
    Ok(views.html.index("Testing"))
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType

      val uploadFile = File.createTempFile(s"uploadedFile", ".tmp")
      val fileThing = picture.ref.moveTo(uploadFile, true)

      val excelReader = new StatementReader(fileThing, thingDAO, FinanalysisConfig.mappingConfig)
      excelReader.extractStatements

      Ok(views.html.react())
    }.getOrElse {
      Redirect(routes.FinanalysisController.index).flashing(
        "error" -> "Missing file")
    }
  }


  def monthStatement(month: Int, year: Int) = Action.async {
    val res: Future[Seq[Statement]] = thingDAO.filterForMonth(month, year)

    res.map(statements => {
      val monthlyStatement: StatementAnalysis = new StatementAnalysis()
      monthlyStatement.add(statements)
      Ok(Json.toJson(monthlyStatement.costsByCategory))
    })
  }

  def categoryStatement(category: String) = Action.async {

    val res: Future[Seq[Statement]] = thingDAO.filterForCategory(AnalysisCategoryType withName(category))

    res.map(statements => {
      val monthlyStatement: StatementAnalysis = new StatementAnalysis()
      monthlyStatement.add(statements)
      Ok(Json.toJson(monthlyStatement.costsByMonth))
    })
  }

  def react = Action {
    Ok(views.html.react())
  }
}
