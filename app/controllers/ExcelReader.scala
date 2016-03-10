package uk.co.finanlysis

import java.io.{File, FileInputStream}
import java.util.Date
import org.apache.poi.ss.usermodel.{Cell, WorkbookFactory, Row}
import uk.co.finanlysis.StatementCellType.StatementCellType
import scala.collection.JavaConversions._

object ExcelReader {

  def createStatement(file: File) : BankStatement = {
    val sheet = WorkbookFactory.create(new FileInputStream(file.getPath)).getSheetAt(0)
    val statememt = new BankStatement
    val rowIterator = sheet.rowIterator.flatMap(processRow)
    rowIterator.foreach(statememt.entries.+=:_)
    statememt
  }

  def processRow( row: Row ): Option[StatementEntry] = {
    if (row.getRowNum > 1) {
      var entry = new StatementEntry
      val cellIterator = row.cellIterator.flatMap(processCell)
      cellIterator.foreach{ case (v, t) => entry.addInformation(v,t) }
      Some(entry)
    }
    else
      None
  }

  def processCell(cell: Cell): Option[(Any, StatementCellType)] = {
    val mapping:Option[Mapping] = FinanalysisConfig.mappingConfig.mappingAtIndex(cell.getColumnIndex)
    if (mapping.isDefined) Some(getCellData(cell), mapping.get.mappingType)
    else None
  }


  def getCellData(cell: Cell) = {
    cell.getCellType match {
      case Cell.CELL_TYPE_NUMERIC => {
        if(cell.getCellStyle.getDataFormat == 14)
          cell.getDateCellValue
        else
          cell.getNumericCellValue
    }
      case Cell.CELL_TYPE_STRING =>
        cell.getStringCellValue
      case _ => " "
    }
  }
}
