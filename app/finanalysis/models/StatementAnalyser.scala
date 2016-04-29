package finanalysis.models

import java.util.{Calendar, Date}
import finanalysis.controllers.{FinanalysisLogger, FinanalysisConfig}
import finanalysis.dal.Statement
import finanalysis.models.AnalysisCategoryType.AnalysisCategoryType
import finanalysis.models.DebitType.DebitType
import scala.collection.mutable.ListBuffer

case class AnalysisCategory(category: AnalysisCategoryType) {

  private var expenditure:Double = 0.00

  def add(more: Double) = expenditure+=more

  def amount = expenditure
  def categoryName = category.toString

  override def equals(that: Any): Boolean =
  that match {
    case that: AnalysisCategory => this.category == that.category
    case _ => false
  }
}

class AnalysisCategories(){

  private var categories:ListBuffer[AnalysisCategory] = ListBuffer()

  def addCategory(category: AnalysisCategoryType) = {
    if (!containsCategory(category))
      categories += new AnalysisCategory(category)
  }

  def addAmount(category: AnalysisCategoryType, amount: Double) = {
    if(!containsCategory(category))
      categories(indexOfCategory(category)).add(amount)
  }

  def toList: List[AnalysisCategory] = categories.toList.sortWith(_.categoryName < _.categoryName)

  private def indexOfCategory(category: AnalysisCategoryType): Int = {
    categories.indexOf(new AnalysisCategory(category))
  }

  private def containsCategory(category: AnalysisCategoryType): Boolean = {
    categories.contains(new AnalysisCategory(category))
    false
  }

}

case class MonthlyExpenditure(month: Int) {
  private var expenditure:AnalysisCategories = new AnalysisCategories()

  AnalysisCategoryType.values.foreach(v => expenditure.addCategory(v))

  def addAmount(category: AnalysisCategoryType, amount: Double) = {
    expenditure.addAmount(category, amount)
  }

  def monthName:String = FinanalysisConfig.monthNumbers(month)

  def toList : List[AnalysisCategory] = expenditure.toList

}


/**
 *
 * @param month
 */
case class MonthlyStatement(month: Int) {
  private val statementList: ListBuffer[Statement] = ListBuffer()

  def add(statement: Statement) = statementList += statement

  def add(statements: Seq[Statement]) = statements.foreach(s => statementList += s)

  def monthName:String = FinanalysisConfig.monthNumbers(month)

  def toList : List[Statement] = statementList.toList

  def toList(categoryTypeFilter: AnalysisCategoryType) : List[Statement] = statementList.toList.filter(_.categoryType == categoryTypeFilter)

  def cost(categoryTypeFilter: AnalysisCategoryType): Double = {
    var cost: Double = 0.00
    statementList.filter(_.categoryType == categoryTypeFilter).foreach(s => cost += s.value)
    cost
  }

  def allCosts: List[(String, Double)]= {
    val categoriesAndCosts: ListBuffer[(String, Double)] = ListBuffer()
    AnalysisCategoryType.values.foreach( category => categoriesAndCosts += ((category.toString, this.cost(category))))
    categoriesAndCosts.toList
  }



}




case class MonthlyStatementAnalysis(){
  private var analysis:ListBuffer[MonthlyExpenditure] = ListBuffer()

  (1 to 12).foreach(m => analysis+=new MonthlyExpenditure(m))

  def addToMonth(month: Int, category: AnalysisCategoryType, amount: Double) = {
    analysis(month-1).addAmount(category, amount)
  }

  def toList: List[MonthlyExpenditure] = analysis.toList

}


object StatementAnalyser {


  def mapCategory (statementEntry: BankStatementEntry): AnalysisCategoryType = {
    statementEntry.debitType match {
      case DebitType.PointOfSale => {
        val analysisCategory = getMatchingCategory(statementEntry.description)
        analysisCategory.getOrElse(AnalysisCategoryType.GeneralExpense)
      }
      case DebitType.StandingOrder => {
        val analysisCategory = getMatchingCategory(statementEntry.description)
        analysisCategory.getOrElse(AnalysisCategoryType.Savings)
      }
      case DebitType.DirectDebit => AnalysisCategoryType.Bills
      case DebitType.CashMachine => AnalysisCategoryType.GeneralExpense
      case DebitType.BACSPayment | DebitType.InternationalTransfer => AnalysisCategoryType.MoneyIn
      case _ => AnalysisCategoryType.None
    }
  }

   def analyseStatement(statement:BankStatement) : MonthlyStatementAnalysis = {
    val monthlyExpenditure:MonthlyStatementAnalysis = new MonthlyStatementAnalysis
    statement.toList.foreach( x => analyseEntryInMonths(x, monthlyExpenditure))
    monthlyExpenditure
  }

  private def analyseEntryInMonths(statementEntry: BankStatementEntry, monthlyExpenditure:MonthlyStatementAnalysis)= {

    val month = getMonth(statementEntry.date)

    statementEntry.debitType match {
      case DebitType.PointOfSale => {
        val analysisCategory = getMatchingCategory(statementEntry.description)
        monthlyExpenditure.addToMonth(month, analysisCategory.getOrElse(AnalysisCategoryType.GeneralExpense), statementEntry.amount)
      }
      case DebitType.StandingOrder => {
        val analysisCategory = getMatchingCategory(statementEntry.description)
        monthlyExpenditure.addToMonth(month, analysisCategory.getOrElse(AnalysisCategoryType.Savings), statementEntry.amount)
      }
      case DebitType.DirectDebit => monthlyExpenditure.addToMonth(month, AnalysisCategoryType.Bills, statementEntry.amount)
      case DebitType.CashMachine =>monthlyExpenditure.addToMonth(month, AnalysisCategoryType.GeneralExpense, statementEntry.amount)
      case DebitType.BACSPayment | DebitType.InternationalTransfer => monthlyExpenditure.addToMonth(month, AnalysisCategoryType.MoneyIn, statementEntry.amount)
      case _ => monthlyExpenditure.addToMonth(month, AnalysisCategoryType.None, statementEntry.amount)
    }
  }

  def getMonth(statmentDate:Date): Int = {
    val cal = Calendar.getInstance()
    cal.setTime(statmentDate)
    cal.get(Calendar.MONTH) + 1
  }

  private def getMatchingCategory(description: String) : Option[AnalysisCategoryType] = {
    var category:Option[AnalysisCategoryType] = None
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
}
