package finanalysis.controllers

import java.util.Calendar

import finanalysis.models.AnalysisCategoryType
import finanalysis.models._
import play.api.libs.json.Json._
import play.api.mvc.{Action, Controller}
import finanalysis.dal.{StatementDAO, Statement,DataModel}
import slick.driver.PostgresDriver.api._
import play.api.libs.json._

import play.api.libs.concurrent.Execution.Implicits.defaultContext


import javax.inject.Inject

import scala.concurrent.Future
import scala.util.{Success, Failure}


class FinanalysisController @Inject()(thingDAO: StatementDAO) extends Controller {

  implicit val statementCategoryWrites = new Writes[AnalysisCategory] {
    def writes(statementCategory: AnalysisCategory) = Json.obj (
    "amount" -> statementCategory.amount,
    "category" -> statementCategory.categoryName
    )
  }

  implicit val statementCategoriesWrites = new Writes[AnalysisCategories] {
    def writes(statementCategories: AnalysisCategories) = Json.obj (
      "categories" -> statementCategories.toList
    )
  }

  implicit val monthlyExpenditureWrites = new Writes[MonthlyExpenditure] {
    def writes(monthlyExpenditure: MonthlyExpenditure) = Json.obj (
      "month" -> monthlyExpenditure.monthName,
      "categories" -> monthlyExpenditure.toList
    )
  }

  implicit val monthlyStatementAnalysis = new Writes[MonthlyStatementAnalysis] {
    def writes(monthlyStatementAnalysis: MonthlyStatementAnalysis) = Json.obj (
      "analysis" -> monthlyStatementAnalysis.toList,
      "categories" -> AnalysisCategoryType.values.toList.sortWith(_.toString < _.toString)
    )
  }


  implicit val oMonthlyStatementAnalysis = new OWrites[MonthlyStatementAnalysis] {
    def writes(monthlyStatementAnalysis: MonthlyStatementAnalysis) = Json.obj (
      "analysis" -> monthlyStatementAnalysis.toList,
      "categories" -> AnalysisCategoryType.values.toList.sortWith(_.toString < _.toString)
    )
  }


  implicit val allCostsWrites = new Writes[(String, Double)] {
    def writes(allCosts: (String, Double)) = Json.obj (
      "category" -> allCosts._1,
      "amount" -> allCosts._2
    )
  }

  def index = Action { implicit request =>
    Ok(views.html.index("Testing"))
  }

  def showparam(num: Int) = Action { implicit request =>
    Ok("Param " + num + " received.")
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType

      val uploadFile = File.createTempFile(s"uploadedFile", ".tmp")
      val fileThing = picture.ref.moveTo(uploadFile, true)

      thingDAO.create

      DataModel.MonthlyAnalysis = Finanalysis.start(thingDAO, fileThing)

      Ok(views.html.react("Results"))
    }.getOrElse {
      Redirect(routes.FinanalysisController.index).flashing(
        "error" -> "Missing file")
    }
  }

  def statement = Action.async {

    val res: Future[Seq[Statement]] = thingDAO.filterForMonth(12, 2015)

    res.map(statements => {
      val monthlyStatement: MonthlyStatement = new MonthlyStatement(12)
      monthlyStatement.add(statements)
      Ok(Json.toJson(monthlyStatement.allCosts))
    })
  }

  def react = Action {
    Ok(views.html.react("React Example"))
  }

  def listofthings(title: String) = Action { implicit  request =>
    Ok(views.html.index(title))
  }
}
