package uk.co.finanlysis

import java.io.{FileInputStream, File, FileOutputStream}

import org.apache.poi.ss.usermodel.charts._
import org.apache.poi.ss.usermodel._
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.{XSSFChart, XSSFWorkbook}
import org.openxmlformats.schemas.drawingml.x2006.chart.{CTBoolean, CTPlotArea}


object ExcelWriter {

  private def findColumnIndex(sheet: Sheet): Int = {
    var columnIndex = 0

    var row = sheet.getRow(columnIndex)

    while (row.cellIterator.hasNext) {
      val cell : Cell = row.cellIterator.next
      if( cell.getCellType == Cell.CELL_TYPE_BLANK || cell == null)
        return columnIndex
      columnIndex+=1
      row = sheet.getRow(columnIndex)
    }
    columnIndex
  }


  private def addStatementAnalysis(analysisMap: Map[String, Double], month: Int) = {

    val workbook = WorkbookFactory.create(new FileInputStream(FinanalysisConfig.exportFileLocation))
    val sheet = workbook.getSheetAt(0)

    // find non-empty column
    var colIndex = 0

    val row = sheet.getRow(0)
    val cellIterator = row.cellIterator

    while (cellIterator.hasNext) {
      colIndex+=1
      cellIterator.next
    }

    var rowIndex = 0
    row.createCell(colIndex).setCellValue(FinanalysisConfig.monthNumbers(month))
    rowIndex+=1

    analysisMap.foreach(x => {
      val row = sheet.getRow(rowIndex)
      row.createCell(colIndex).setCellValue(x._2)
      rowIndex+=1
    })

    workbook.write(new FileOutputStream(FinanalysisConfig.exportFileLocation))

  }

  private def writeStatementAnalysis(analysisMap: Map[String, Double], month: Int) = {

    val workbook = new XSSFWorkbook()
    val sheet = workbook.createSheet("Finanalysis")
    var columnIndex = 0
    var rowIndex = 0

    //add Month
    val row = sheet.createRow(rowIndex)
    row.createCell(0).setCellValue("Month")
    row.createCell(1).setCellValue(FinanalysisConfig.monthNumbers(month))
    rowIndex+=1

    analysisMap.foreach(x => {
      val row = sheet.createRow(rowIndex)
      row.createCell(0).setCellValue(transformCategoryName(x._1))
      row.createCell(1).setCellValue(x._2)
      rowIndex+=1
    })

    workbook.write(new FileOutputStream(FinanalysisConfig.exportFileLocation))
  }


  def writeMonthlyStatementAnalysis(analysisMap: Map[String, Double], month:Int) = {

    if(new File(FinanalysisConfig.exportFileLocation).exists ){
      addStatementAnalysis(analysisMap, month)
    }
    else{
      writeStatementAnalysis(analysisMap, month)
    }
  }

  private def transformCategoryName(categoryName: String) : String = {
    categoryName match {
      case "moneyin" => "Money In"
      case "goingout" => "Going Out"
      case  "standingorders" => "Standing Orders"
      case _ => categoryName.capitalize
    }
  }
}


/*
   val drawing: Drawing = sheet.createDrawingPatriarch()
   val anchor: ClientAnchor = drawing.createAnchor(0,0,0,0,0,5,10,15)

   val chart: XSSFChart = drawing.createChart(anchor).asInstanceOf[XSSFChart]
   val legend : ChartLegend = chart.getOrCreateLegend
   legend.setPosition(LegendPosition.TOP_RIGHT)


   val data : LineChartData = chart.getChartDataFactory.createLineChartData()

   val bottomAxis: ChartAxis = chart.getChartAxisFactory.createCategoryAxis(AxisPosition.BOTTOM)
   val leftAxis = chart.getChartAxisFactory.createValueAxis(AxisPosition.LEFT)

   val xs : ChartDataSource[Number] = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(0,analysisMap.size-1,0,0))
   val ys1 : ChartDataSource[Number] = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(0,analysisMap.size-1,1,1))

   data.addSeries(xs, ys1)

   chart.plot(data, bottomAxis, leftAxis)
   */
