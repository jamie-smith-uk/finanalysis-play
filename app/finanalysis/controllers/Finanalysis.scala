package finanalysis.controllers

import java.io.File

import finanalysis.dal.StatementDAO
import finanalysis.models.{StatementAnalyser, MonthlyStatementAnalysis}

import scala.collection.JavaConversions._

// The main application
object Finanalysis {


  def start(statementDAO : StatementDAO,  file: File): MonthlyStatementAnalysis = {

    try {
      val excelReader = new StatementReader(file, statementDAO, FinanalysisConfig.mappingConfig)
      val statement = excelReader.translateStatement
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