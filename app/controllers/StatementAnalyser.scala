package  uk.co.finanlysis

import java.util
import java.util.{Calendar, Date}

import uk.co.finanlysis.AnalysisCategoryType
import uk.co.finanlysis.AnalysisCategoryType.AnalysisCategoryType

import scala.collection.mutable.ListBuffer

case class StatementCategory(category: AnalysisCategoryType) {

  private var expenditure:Double = 0.00

  def add(more: Double) = expenditure+=more

  def amount = expenditure
  def categoryName = category.toString

  override def equals(that: Any): Boolean =
  that match {
    case that: StatementCategory => this.category == that.category
    case _ => false
  }
}

class StatementCategories(){

  private var categories:ListBuffer[StatementCategory] = ListBuffer()

  def addCategory(category: AnalysisCategoryType) = {
    if(!containsCategory(category))
      categories+=new StatementCategory(category)
}

  def addAmount(category: AnalysisCategoryType, amount: Double) = {
    if(!containsCategory(category))
      categories(indexOfCategory(category)).add(amount)
  }

  def toList: List[StatementCategory] = categories.toList.sortWith(_.categoryName < _.categoryName)

  private def indexOfCategory(category: AnalysisCategoryType): Int = {
    categories.indexOf(new StatementCategory(category))
  }

  private def containsCategory(category: AnalysisCategoryType): Boolean = {
    categories.contains(new StatementCategory(category))
    false
  }

}

case class MonthlyExpenditure(month: Int) {
  private var expenditure:StatementCategories = new StatementCategories()

  AnalysisCategoryType.values.foreach(v => expenditure.addCategory(v))

  def addAmount(category: AnalysisCategoryType, amount: Double) = {
    expenditure.addAmount(category, amount)
  }

  def monthName:String = FinanalysisConfig.monthNumbers(month)

  def toList : List[StatementCategory] = expenditure.toList

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

   def analyseStatement(statement:BankStatement) : MonthlyStatementAnalysis = {
    var monthlyExpenditure:MonthlyStatementAnalysis = new MonthlyStatementAnalysis
    statement.entries.foreach( x => analyseEntryInMonths(x, monthlyExpenditure))
    monthlyExpenditure
  }

  private def analyseEntryInMonths(statementEntry: StatementEntry, monthlyExpenditure:MonthlyStatementAnalysis)= {

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

  private def getMonth(statmentDate:Date): Int = {
    val cal = Calendar.getInstance()
    cal.setTime(statmentDate)
    cal.get(Calendar.MONTH) + 1
  }

  private def getMatchingCategory(description: String) : Option[AnalysisCategoryType] = {
    var category:Option[AnalysisCategoryType] = None
    for ((key, value) <- FinanalysisConfig.categoryMapping.categoryMap) {
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
