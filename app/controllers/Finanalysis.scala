package uk.co.finanlysis

import java.io.File

import scala.collection.JavaConversions._

// The main application
object Finanalysis {


  def start(file: File): Map[String, Double] = {

    try {
      val statement = ExcelReader.createStatement(file)
      StatementAnalyser.analysisForMonth(statement, 1)
      //FinanalysisConfig.monthsToAnalayse.foreach(x => ExcelWriter.writeMonthlyStatementAnalysis(StatementAnalyser.analysisForMonth(statement, x), x))
    }
    catch {
      case e: Throwable => {
        FinanalysisLogger.error(e.getMessage)
        e.printStackTrace()
        Map("" -> 0.00)
      }
    } finally {


    }
  }
}