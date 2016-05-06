package finanalysis.models

import java.util.{Calendar, Date}
import finanalysis.controllers.{Month, FinanalysisLogger, FinanalysisConfig}
import finanalysis.dal.Statement
import finanalysis.models.AnalysisCategoryType.AnalysisCategoryType
import finanalysis.models.DebitType.DebitType
import scala.collection.mutable.ListBuffer


class StatementAnalysis {

  private val statementList: ListBuffer[Statement] = ListBuffer()

  def add(statement: Statement) = statementList += statement

  def add(statements: Seq[Statement]) = statements.foreach(s => statementList += s)

  def toList : List[Statement] = statementList.toList

  def toList(categoryTypeFilter: AnalysisCategoryType) : List[Statement] = statementList.toList.filter(_.categoryType == categoryTypeFilter)

  def costsByCategory: List[(String, Double)]= {
    val categoriesAndCosts: ListBuffer[(String, Double)] = ListBuffer()
    AnalysisCategoryType.values.foreach( category => categoriesAndCosts += ((category.toString, this.cost(category))))
    categoriesAndCosts.toList
  }

  def costsByMonth: List[(String, Double)] = {
    val categoriesAndCosts: ListBuffer[(String, Double)] = ListBuffer()
    Month.months.foreach(month => categoriesAndCosts += ((month, this.cost(month))))
    categoriesAndCosts.toList.sortBy(s => Month.comparisonInt(s._1))
  }


  private def cost(categoryTypeFilter: AnalysisCategoryType): Double = {
    var cost: Double = 0.00
    statementList.filter(_.categoryType == categoryTypeFilter).foreach(s => cost += s.value)
    cost
  }

  private def cost(month: String) : Double = {
    var cost: Double = 0.00
    statementList.filter(extractMonth(_) == month).foreach(s => cost += s.value)
    cost
  }

  private def extractMonth(statement: Statement) : String = {
    val dateStrings = statement.date.toString.split("-")
    Month.toName(dateStrings(1).toInt)
  }
}