package finanalysis.controllers

import java.io.{File, FileInputStream}
import java.util.{Date, Calendar}
import javax.inject.Inject
import finanalysis.dal.{StatementDAO, Statement}
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

  def translateStatement : BankStatement = {
    val sheet = WorkbookFactory.create(new FileInputStream(file.getPath)).getSheetAt(0)
    val statement = new BankStatement
    sheet.rowIterator.foreach(r => {
      val bankStatementEntry = translateRow(r)

      if (bankStatementEntry.isDefined) {

        statement.add(bankStatementEntry.get)

        val fut: Future[Unit] = statementDAO.insert(
          new Statement(
            new java.sql.Date(bankStatementEntry.get.date.getTime),
            bankStatementEntry.get.debitType.toString,
            bankStatementEntry.get.description,
            bankStatementEntry.get.amount,
            Some(StatementAnalyser.mapCategory(bankStatementEntry.get).toString)
          )
        )

        fut onFailure {
          case e => FinanalysisLogger.error("Future failed. " + e.getMessage)
        }


      }
    })
    statement
  }

  def translateRow( row: Row ): Option[BankStatementEntry] = {
    if (row.getRowNum > 1) {
      val entry = new BankStatementEntry
      var desc: String = ""
      var amount: Double = 0.00
      var debitType: DebitType = DebitType.None
      var date: Date = Calendar.getInstance.getTime

      row.cellIterator.foreach(c => {
        val cellInfo: Option[(Any, StatementCellType)] = translateCell(c)
        if (cellInfo.isDefined) {
          entry.update(cellInfo.get._1, cellInfo.get._2)

          cellInfo.get._2 match {
            case StatementInfoType.Description => {
              if (debitType == DebitType.PointOfSale)
                desc = cellInfo.get._1.toString.split(",")(1).trim
              else
                desc = cellInfo.get._1.toString
            }
            case StatementInfoType.Amount => amount = math.abs(cellInfo.get._1.asInstanceOf[Double])
            case StatementInfoType.Type => debitType = DebitType.convertToDebitType(cellInfo.get._1.toString)
            case StatementInfoType.Date => date = cellInfo.get._1.asInstanceOf[Date]
            case _ => FinanalysisLogger.error("Map incorrectly requested for: " + cellInfo.get._1 + ", for type: " + cellInfo.get._2.toString)
          }
        }
        else
          None
      })
      val statement = new Statement(new java.sql.Date(date.getTime),debitType.toString, desc, amount)
      Some(entry)
    }
    else
      None
  }

  /*
   infoType match {
      case StatementInfoType.Description => {
        if(debitType == DebitType.PointOfSale)
          desc = infoValue.toString.split(",")(1).trim
        else
          desc = infoValue.toString
      }
      case StatementInfoType.Amount => amount = math.abs(infoValue.asInstanceOf[Double])
      case StatementInfoType.Type => debitType = DebitType.convertToDebitType(infoValue.toString)
      case StatementInfoType.Date => date = infoValue.asInstanceOf[Date]
      case _ => FinanalysisLogger.error("Map incorrectly requested for: " + infoValue + ", for type: " + infoType.toString)
    }
   */

  def translateCell(cell: Cell): Option[(Any, StatementCellType)] = {
    val mapping:Option[Mapping] = cellMapping.mappingAtIndex(cell.getColumnIndex)
    if (mapping.isDefined) Some(getData(cell), mapping.get.mappingType) else None
  }

  def getData(cell: Cell) = {
    cell.getCellType match {
      case Cell.CELL_TYPE_NUMERIC => if (cell.getCellStyle.getDataFormat == 14) cell.getDateCellValue else cell.getNumericCellValue
      case Cell.CELL_TYPE_STRING => cell.getStringCellValue
      case _ => ""
    }
  }
}
