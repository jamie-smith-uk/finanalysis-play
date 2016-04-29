package finanalysis.dal

import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Calendar

import finanalysis.controllers.FinanalysisLogger
import finanalysis.models.AnalysisCategoryType
import finanalysis.models.AnalysisCategoryType.AnalysisCategoryType
import finanalysis.models.DebitType
import finanalysis.models.DebitType.DebitType
import finanalysis.models.StatementInfoType.StatementCellType
import play.Logger
import play.db.NamedDatabase

import scala.concurrent.Future

import javax.inject.Inject
import finanalysis.models.{DebitType, StatementInfoType, AnalysisCategoryType, MonthlyStatementAnalysis}
import play.api.Play
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}

import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._

import play.api.libs.concurrent.Execution.Implicits.defaultContext


object DataModel {
  var MonthlyAnalysis:MonthlyStatementAnalysis = new MonthlyStatementAnalysis
}

case class Statement(id: Option[Int]= None, date: Date, statementType: String, desc: String, value: Double, category : Option[String] = Some(AnalysisCategoryType.None.toString)) {

  def this(date: Date, statementType: String, desc: String, value: Double) = this(None, date, statementType, desc, value)

  def this(date: Date, statementType: String, desc: String, value: Double, category: Option[String]) = this(None, date, statementType, desc, value, category)

  def categoryType: AnalysisCategoryType = AnalysisCategoryType withName (category.getOrElse("None"))

  def debitType: DebitType = DebitType withName (statementType)

}

class StatementTable(tag: Tag) extends Table[Statement](tag, "statement") {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def date = column[Date]("date")
  def statementType = column[String]("statement_type")
  def desc = column[String]("description")
  def value = column[Double]("value")
  def category = column[Option[String]]("category")
  def * = (id, date, statementType, desc, value, category) <> ( (Statement.apply _).tupled, Statement.unapply _)
}


class StatementDAO @Inject()(dbConfigProvider: DatabaseConfigProvider){

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  private val things: TableQuery[StatementTable] = TableQuery[StatementTable]

  def create: Future[Unit] = {
    val createThing = DBIO.seq(things.schema.create)
    dbConfig.db.run(createThing)
  }

  private def monthQuery(monthNum: Int, year: Int): DBIO[Seq[Statement]] = {
    val fromMonthString = if(monthNum>=10) "0"+ monthNum else monthNum
    val toMonthString = if((monthNum+1)>=10) "0"+ (monthNum+1) else (monthNum+1)

    val fromDateString = "01/" + fromMonthString  + "/" + year
    val toDateString = if( monthNum == 12) "01/" + toMonthString  + "/" + year else "01/01/" + year+1

    things.filter(s => s.date >= new Date(new SimpleDateFormat("dd/MM/yyyy").parse(fromDateString).getTime) && s.date < new Date(new SimpleDateFormat("dd/MM/yyyy").parse(toDateString).getTime)).result
  }

  private def monthAndCategoryQuery(monthNum: Int, year: Int, categoryType: AnalysisCategoryType) : DBIO[Seq[Statement]]= {

    val fromMonthString = if(monthNum>=10) "0"+ monthNum else monthNum
    val toMonthString = if((monthNum+1)>=10) "0"+ (monthNum+1) else (monthNum+1)

    val fromDateString = "01/" + fromMonthString  + "/" + year
    val toDateString = if( monthNum == 12) "01/" + toMonthString  + "/" + year else "01/01/" + year+1

    things.filter(s => s.date >= new Date(new SimpleDateFormat("dd/MM/yyyy").parse(fromDateString).getTime) && s.date < new Date(new SimpleDateFormat("dd/MM/yyyy").parse(toDateString).getTime) && s.category === categoryType.toString).result
  }

  def filterForMonth(monthNum: Int, year: Int): Future[Seq[Statement]] = {
   dbConfig.db.run(monthQuery(monthNum, year))
  }

  def filterByMonthAndCategory(monthNum: Int, year: Int, category: AnalysisCategoryType): Future[Seq[Statement]] = {
    dbConfig.db.run(monthAndCategoryQuery(monthNum, year, category))
  }

  def insert(statement: Statement): Future[Unit] = {

    val insertThings = DBIO.seq(
      things ++= Seq (statement)
    )

    dbConfig.db.run(insertThings)
  }

}