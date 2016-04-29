package finanalysis.test

import java.util.{Calendar, Date}

import org.apache.poi.ss.usermodel._
import org.apache.poi.ss.util.{CellAddress, CellRangeAddress}



case class MockCellStyle(dataFormat: Short) extends CellStyle{

  override def setFillBackgroundColor(i: Short): Unit = ???

  override def setShrinkToFit(b: Boolean): Unit = ???

  override def setFillForegroundColor(i: Short): Unit = ???

  override def getBorderBottom: Short = ???

  override def getBorderLeft: Short = ???

  override def setFillPattern(i: Short): Unit = ???

  override def setAlignment(i: Short): Unit = ???

  override def getBorderRight: Short = ???

  override def setBorderBottom(i: Short): Unit = ???

  override def setBottomBorderColor(i: Short): Unit = ???

  override def getBottomBorderColor: Short = ???

  override def getIndex: Short = ???

  override def getIndention: Short = ???

  override def getWrapText: Boolean = ???

  override def getFontIndex: Short = ???

  override def setLocked(b: Boolean): Unit = ???

  override def getDataFormatString: String = ???

  override def getFillForegroundColorColor: Color = ???

  override def setBorderLeft(i: Short): Unit = ???

  override def getBorderTop: Short = ???

  override def getShrinkToFit: Boolean = ???

  override def getFillBackgroundColor: Short = ???

  override def setHidden(b: Boolean): Unit = ???

  override def setRightBorderColor(i: Short): Unit = ???

  override def getTopBorderColor: Short = ???

  override def setWrapText(b: Boolean): Unit = ???

  override def getRotation: Short = ???

  override def getFillPattern: Short = ???

  override def getDataFormat: Short = dataFormat

  override def setIndention(i: Short): Unit = ???

  override def getVerticalAlignment: Short = ???

  override def setVerticalAlignment(i: Short): Unit = ???

  override def setLeftBorderColor(i: Short): Unit = ???

  override def setFont(font: Font): Unit = ???

  override def cloneStyleFrom(cellStyle: CellStyle): Unit = ???

  override def setBorderRight(i: Short): Unit = ???

  override def setTopBorderColor(i: Short): Unit = ???

  override def getFillForegroundColor: Short = ???

  override def getLocked: Boolean = ???

  override def setBorderTop(i: Short): Unit = ???

  override def setRotation(i: Short): Unit = ???

  override def getHidden: Boolean = ???

  override def getAlignment: Short = ???

  override def setDataFormat(i: Short): Unit = ???

  override def getRightBorderColor: Short = ???

  override def getLeftBorderColor: Short = ???

  override def getFillBackgroundColorColor: Color = ???
}

class MockCell extends Cell {

  var cellType: Int = Cell.CELL_TYPE_BLANK
  var cellStyle: MockCellStyle = new MockCellStyle(14)
  var dateData = new Date()
  var stringData = ""
  var numberData = 1.00

  override def getHyperlink: Hyperlink = ???

  override def removeHyperlink(): Unit = ???

  override def getCellType: Int = cellType

  override def setCellErrorValue(b: Byte): Unit = ???

  override def setCellType(i: Int): Unit = ???

  override def getCellStyle: CellStyle = cellStyle

  override def getColumnIndex: Int = ???

  override def getCellComment: Comment = ???

  override def getCellFormula: String = ???

  override def getStringCellValue: String = stringData

  override def setCellStyle(cellStyle: CellStyle): Unit = ???

  override def setHyperlink(hyperlink: Hyperlink): Unit = ???

  override def isPartOfArrayFormulaGroup: Boolean = ???

  override def getRow: Row = ???

  override def removeCellComment(): Unit = ???

  override def getRichStringCellValue: RichTextString = ???

  override def setAsActiveCell(): Unit = ???

  override def getNumericCellValue: Double = numberData

  override def getBooleanCellValue: Boolean = ???

  override def getArrayFormulaRange: CellRangeAddress = ???

  override def getErrorCellValue: Byte = ???

  override def getCachedFormulaResultType: Int = ???

  override def getAddress: CellAddress = ???

  override def getDateCellValue: Date = dateData

  override def setCellFormula(s: String): Unit = ???

  override def setCellComment(comment: Comment): Unit = ???

  override def getRowIndex: Int = ???

  override def setCellValue(v: Double): Unit = ???

  override def setCellValue(date: Date): Unit = ???

  override def setCellValue(calendar: Calendar): Unit = ???

  override def setCellValue(richTextString: RichTextString): Unit = ???

  override def setCellValue(s: String): Unit = ???

  override def setCellValue(b: Boolean): Unit = ???

  override def getSheet: Sheet = ???
}