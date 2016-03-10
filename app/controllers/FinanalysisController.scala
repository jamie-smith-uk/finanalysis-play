package controllers

import models.Book._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import uk.co.finanlysis.Finanalysis
import uk.co.finanlysis.dal.DataModel


import scala.concurrent.Future


class FinanalysisController extends Controller  {


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

      DataModel.StatementMap = Finanalysis.start(fileThing)

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
    Ok(Json.toJson(DataModel.StatementMap))
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
