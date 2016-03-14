package uk.co.finanlysis

import java.io.File

import scala.collection.JavaConversions._

// The main application
object Finanalysis {


  def start(file: File): MonthlyStatementAnalysis = {

    try {
      val statement = ExcelReader.createStatement(file)
      StatementAnalyser.analyseStatement(statement)
    }
    catch {
      case e: Throwable => {
        FinanalysisLogger.error(e.getMessage)
        e.printStackTrace()
        new MonthlyStatementAnalysis()
      }
    } finally {


    }
  }
}