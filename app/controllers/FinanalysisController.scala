package controllers

import models.Book._
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import uk.co.finanlysis.AnalysisCategoryType._
import uk.co.finanlysis._
import uk.co.finanlysis.dal.DataModel


import scala.collection.mutable.ListBuffer
import scala.concurrent.Future


class FinanalysisController extends Controller  {


  implicit val statementCategoryWrites = new Writes[StatementCategory] {
    def writes(statementCategory: StatementCategory) = Json.obj (
    "amount" -> statementCategory.amount,
    "category" -> statementCategory.categoryName
    )
  }

  implicit val statementCategoriesWrites = new Writes[StatementCategories] {
    def writes(statementCategories: StatementCategories) = Json.obj (
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

      DataModel.MonthlyAnalysis = Finanalysis.start(fileThing)

      Ok(views.html.react("Results"))
    }.getOrElse {
      Redirect(routes.FinanalysisController.index).flashing(
        "error" -> "Missing file")
    }
  }


  def listBooks = Action {
    Ok(Json.toJson(books))
  }

  def statement = Action {
    Ok(Json.toJson(DataModel.MonthlyAnalysis))
  }

  def react = Action {
    Ok(views.html.react("React Example"))
  }

  def listofthings(title: String) = Action { implicit  request =>
    Ok(views.html.index(title))
  }

  def charts = Action {
    Ok(views.js.chart.render())
  }
}
