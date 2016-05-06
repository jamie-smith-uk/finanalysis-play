package finanalysis.controllers

import java.io.{File, FileInputStream}
import java.util.{Date, Calendar}
import finanalysis.dal.{StatementDAO, Statement}
import finanalysis.models.AnalysisCategoryType
import finanalysis.models.AnalysisCategoryType._
import finanalysis.models.DebitType.DebitType
import finanalysis.models._
import org.apache.poi.ss.usermodel.{Cell, WorkbookFactory, Row}
import StatementInfoType.StatementCellType
import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class StatementReader(bankExportFile: File, statementDAO: StatementDAO,  mapping: MappingConfig){

  private val cellMapping = mapping
  private val file:File = bankExportFile

  def extractStatements = {
    val sheet = WorkbookFactory.create(new FileInputStream(file.getPath)).getSheetAt(0)
    sheet.rowIterator.foreach(r => {
      val statement = translateRow(r)
      if (statement.isDefined) {
        val fut: Future[Unit] = statementDAO.insert(statement.get)
        fut onFailure {
          case e => FinanalysisLogger.error("Future failed. " + e.getMessage)
        }
      }
    })
  }

  def translateRow( row: Row ): Option[Statement] = {
    if (row.getRowNum > 1) {

      var desc: String = ""
      var amount: Double = 0.00
      var debitType: DebitType = DebitType.None
      var date: Date = Calendar.getInstance.getTime

      row.cellIterator.foreach(c => {
        val cellInfo: Option[(Any, StatementCellType)] = translateCell(c)
        if (cellInfo.isDefined) {
          cellInfo.get._2 match {
            case StatementInfoType.Description => {
              if (debitType == DebitType.PointOfSale)
                desc = cellInfo.get._1.toString.split(",")(1).trim
              else
                desc = cellInfo.get._1.toString
            }
            case StatementInfoType.Amount => amount = math.abs(cellInfo.get._1.asInstanceOf[Double])
            case StatementInfoType.Type => debitType = DebitType withName (cellInfo.get._1.toString)
            case StatementInfoType.Date => date = cellInfo.get._1.asInstanceOf[Date]
          }
        }
        else
          None
      })
      Some(new Statement(new java.sql.Date(date.getTime),debitType.toString, desc, amount, Some(mapCategory(debitType, desc).toString)))
    }
    else
      scala.None
  }

  def mapCategory (statementDebitType: DebitType, statementDesc: String): AnalysisCategoryType = {
    statementDebitType match {
      case DebitType.PointOfSale => {
        val analysisCategory = getMatchingCategory(statementDesc)
        analysisCategory.getOrElse(AnalysisCategoryType.GeneralExpense)
      }
      case DebitType.StandingOrder => {
        val analysisCategory = getMatchingCategory(statementDesc)
        analysisCategory.getOrElse(AnalysisCategoryType.Savings)
      }
      case DebitType.DirectDebit => AnalysisCategoryType.Bills
      case DebitType.CashMachine => AnalysisCategoryType.GeneralExpense
      case DebitType.BACSPayment | DebitType.InternationalTransfer => AnalysisCategoryType.MoneyIn
      case _ => AnalysisCategoryType.None
    }
  }

  private def getMatchingCategory(description: String) : Option[AnalysisCategoryType] = {
    var category:Option[AnalysisCategoryType] = scala.None
    for ((key, value) <- FinanalysisConfig.categoryMapping.CategoryMap) {
      if (description.toLowerCase.contains(key.toLowerCase)) {
        AnalysisCategoryType.values.foreach(x => {
          if(x.toString.trim.replace(" ", "").toLowerCase == value)
            category = Some(x)
        })
      }
    }
    category
  }

  def translateCell(cell: Cell): Option[(Any, StatementCellType)] = {
    val mapping:Option[Mapping] = cellMapping.mappingAtIndex(cell.getColumnIndex)
    if (mapping.isDefined) Some(getData(cell), mapping.get.mappingType) else scala.None
  }

  def getData(cell: Cell) = {
    cell.getCellType match {
      case Cell.CELL_TYPE_NUMERIC => if (cell.getCellStyle.getDataFormat == 14) cell.getDateCellValue else cell.getNumericCellValue
      case Cell.CELL_TYPE_STRING => cell.getStringCellValue
      case _ => ""
    }
  }
}
