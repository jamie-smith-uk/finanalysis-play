package finanalysis.test

import java.io.{FileNotFoundException, File}
import java.util.{Date, Calendar}

import finanalysis.models.{DebitType, StatementInfoType}
import org.apache.poi.ss.usermodel._
import org.apache.poi.ss.util.{CellRangeAddress, CellAddress}
import org.scalatest._
import finanalysis.controllers.{Mapping, MappingConfig, StatementReader}

class MockConfigObject extends MappingConfig {

  var dateIndex:Int = 0;
  var typeIndex:Int = 0;
  var descIndex:Int = 0;
  var amountIndex:Int = 0;

  var colMapping: Array[Mapping] = Array(new Mapping(dateIndex, StatementInfoType.Date), new Mapping(typeIndex, StatementInfoType.Type), new Mapping(descIndex, StatementInfoType.Description), new Mapping(amountIndex, StatementInfoType.Amount))
  var returnNone:Boolean = false

  def resetMapping (date: Int, typ: Int, desc: Int, amt: Int) = {
    dateIndex = date
    typeIndex = typ
    descIndex = desc
    amountIndex = amt
    colMapping = Array(new Mapping(dateIndex, StatementInfoType.Date), new Mapping(typeIndex, StatementInfoType.Type), new Mapping(descIndex, StatementInfoType.Description), new Mapping(amountIndex, StatementInfoType.Amount))
  }


  def mapping: Array[Mapping] = colMapping
  def mappingAtIndex(index: Int): Option[Mapping] = {
    if(returnNone)
      colMapping.find(_.in == index)
    else
      scala.None
  }
}

class StatementReaderTest  extends FunSuite{

  var testFile:File = new File("")
  val mockConfig = new MockConfigObject
  var excelReader: StatementReader = new StatementReader(testFile,mockConfig)

  test ("Exception thrown when file does not exist") {
    val thrown = intercept[FileNotFoundException] {
      excelReader.translateStatement
    }
  }

  test ("Statement contains empty entries when no mapping found") {

    testFile = new File("test\\resources\\test-files\\valid-file-single-row.xlsx")
    excelReader = new StatementReader(testFile, mockConfig)
    mockConfig.returnNone = true
    val statementList = excelReader.translateStatement.toList
    assert(statementList.length == 1
      && statementList(0).amount == 0.00
      && statementList(0).description == ""
    && statementList(0).debitType == DebitType.None)
  }


  test ("Statement contains empty entries when mapping is wrong") {

    testFile = new File("test\\resources\\test-files\\valid-file-single-row.xlsx")
    excelReader = new StatementReader(testFile, mockConfig)
    val statementList = excelReader.translateStatement.toList
    assert(statementList.length == 1
      && statementList(0).amount == 0.00
      && statementList(0).description == ""
      && statementList(0).debitType == DebitType.None)
  }

  test ("Statement contains correct entries when with valid file and valid mapping - single row") {

    testFile = new File("test\\resources\\test-files\\valid-file-single-row.xlsx")
    mockConfig.resetMapping(0,1,2,3)
    excelReader = new StatementReader(testFile, mockConfig)
    val statementList = excelReader.translateStatement.toList
    assert(statementList.length == 1
      && statementList(0).amount == 5.99
      && statementList(0).description == "WOODGRANGE"
      && statementList(0).debitType == DebitType.PointOfSale)
  }

  test ("Statement contains correct entries when with valid file and valid mapping - multiple rows") {

    testFile = new File("test\\resources\\test-files\\valid-file-multiple-rows.xlsx")
    mockConfig.resetMapping(0,1,2,3)
    excelReader = new StatementReader(testFile, mockConfig)
    val statementList = excelReader.translateStatement.toList
    assert(statementList.length == 2
      && statementList(0).amount == 5.99
      && statementList(0).description == "WOODGRANGE"
      && statementList(0).debitType == DebitType.PointOfSale
      && statementList(1).amount == 18.23
      && statementList(1).description == "BOOTS"
      && statementList(1).debitType == DebitType.PointOfSale)
  }

  test ("getData returns correct string value when the cell type is STRING") {
    val mockCell: MockCell = new MockCell
    mockCell.stringData = "test"
    mockCell.cellType = Cell.CELL_TYPE_STRING
    assert( excelReader.getData(mockCell) == "test")
  }

  test ("getData returns correct date value when type and format indicate a date") {
    val mockCell: MockCell = new MockCell
    val now = Calendar.getInstance.getTime
    mockCell.dateData = now
    mockCell.cellType = Cell.CELL_TYPE_NUMERIC
    mockCell.cellStyle = new MockCellStyle(14)
    assert( excelReader.getData(mockCell) == now)
  }

  test ("getData returns correct number value when type and format indicate a number") {
    val mockCell: MockCell = new MockCell
    mockCell.numberData = 5.23
    mockCell.cellType = Cell.CELL_TYPE_NUMERIC
    mockCell.cellStyle = new MockCellStyle(0)
    assert( excelReader.getData(mockCell) == 5.23)
  }

  test ("getData returns a blank string when type is not numeric or string") {
    val mockCell: MockCell = new MockCell
    mockCell.numberData = 5.23
    mockCell.cellType = Cell.CELL_TYPE_BLANK
    assert( excelReader.getData(mockCell) == "")
  }
}
