package finanalysis.dal

import java.sql.Date
import java.text.SimpleDateFormat
import finanalysis.controllers.FinanalysisLogger
import finanalysis.models.AnalysisCategoryType.AnalysisCategoryType
import finanalysis.models.DebitType.DebitType

import scala.concurrent.Future

import javax.inject.Inject
import finanalysis.models.{DebitType, AnalysisCategoryType}
import play.api.db.slick.{DatabaseConfigProvider}

import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._

import play.api.libs.concurrent.Execution.Implicits.defaultContext


case class Statement(id: Option[Int]= None, date: Date, statementType: String, desc: String, value: Double, category : Option[String] = Some(AnalysisCategoryType.None.toString)) {
  def this(date: Date, statementType: String, desc: String, value: Double) = this(None, date, statementType, desc, value)
  def this(date: Date, statementType: String, desc: String, value: Double, category: Option[String]) = this(None, date, statementType, desc, value, category)
  def categoryType: AnalysisCategoryType = AnalysisCategoryType withName (category.getOrElse("None"))
  def debitType: DebitType = DebitType withName (statementType)
}

class StatementTable(tag: Tag) extends Table[Statement](tag, "statement") {
  def id = column[Option[Int]]("id",O.AutoInc)
  def date = column[Date]("date")
  def statementType = column[String]("statement_type")
  def desc = column[String]("description")
  def value = column[Double]("value")
  def category = column[Option[String]]("category")
  def * = (id, date, statementType, desc, value, category) <> ( (Statement.apply _).tupled, Statement.unapply _)
  def pk = primaryKey("pk_statement", (date, statementType, desc, value))
}


class StatementDAO @Inject()(dbConfigProvider: DatabaseConfigProvider){

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  private val things: TableQuery[StatementTable] = TableQuery[StatementTable]

  def create: Future[Unit] = {
    val createThing = DBIO.seq(things.schema.create)
    dbConfig.db.run(createThing)
  }

  private def monthQuery(monthNum: Int, year: Int): DBIO[Seq[Statement]] = {
    val fromMonthString : String = if(monthNum<10) "0"+ monthNum else monthNum.toString
    val toMonthString : String  = if((monthNum+1)<10) "0"+ (monthNum+1) else (monthNum+1).toString
    val fromDateString : String = "01/" + fromMonthString  + "/" + year
    val toDateString : String = if( monthNum == 12) "01/01/" + (year+1).toString else "01/" + toMonthString  + "/" + year
    things.filter(s => s.date >= new Date(new SimpleDateFormat("dd/MM/yyyy").parse(fromDateString).getTime) && s.date < new Date(new SimpleDateFormat("dd/MM/yyyy").parse(toDateString).getTime)).result
  }

  private def categoryQuery(analysisCategoryType: AnalysisCategoryType): DBIO[Seq[Statement]] ={
    things.filter(_.category === analysisCategoryType.toString).result
  }

  def filterForMonth(monthNum: Int, year: Int): Future[Seq[Statement]] = {
    dbConfig.db.run(monthQuery(monthNum, year))
  }

  def filterForCategory(analysisCategoryType: AnalysisCategoryType): Future[Seq[Statement]] = {
    dbConfig.db.run(categoryQuery(analysisCategoryType))
  }


  def insert(statement: Statement): Future[Unit] = {
    val insertThings = DBIO.seq(
      things ++= Seq (statement)
    )
    dbConfig.db.run(insertThings)
  }

}