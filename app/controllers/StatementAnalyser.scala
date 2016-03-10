package  uk.co.finanlysis

import java.util.{Calendar, Date}

object StatementAnalyser {
  var categoryMap:Map[String, Double] = Map()

  def analysisForMonth(statement:BankStatement, month: Int): Map[String, Double] = {
    categoryMap = setUpMap(List("travel", "food", "bills", "expenses", "unknown", "moneyin", "childcare", "goingout", "savings", "standingorders"))
    updateMapForMonth(analyseStatementWithMonths(statement), month)
    categoryMap
  }

  private def updateMapForMonth(statements:List[(String, Double, Int)], month: Int) = statements.filter(x => x._3 == month).map(x => changeMap(x._1, x._2))

  private def analyseStatementWithMonths(statement : BankStatement) : List[(String, Double, Int)] = statement.entries.map( x => analyseEntryForMonth(x)).toList

  private def analyseEntryForMonth(statementEntry: StatementEntry): (String, Double, Int) = {

    // (category, amount, month)
    val cal = Calendar.getInstance()
    cal.setTime(statementEntry.date)
    val month = cal.get(Calendar.MONTH) + 1

    statementEntry.debitType match {
      case DebitType.PointOfSale => {
        val matchingCategory = findMatchingCategory(statementEntry.description)
        (if (matchingCategory.isDefined) matchingCategory.get else "expenses", statementEntry.amount, month)
      }
      case DebitType.StandingOrder => {
        val matchingCategory = findMatchingCategory(statementEntry.description)
        (if (matchingCategory.isDefined) matchingCategory.get else "savings", statementEntry.amount, month)
      }
      case DebitType.DirectDebit => ("bills", statementEntry.amount, month)
      case DebitType.CashMachine => ("expenses", statementEntry.amount, month)
      case DebitType.BACSPayment | DebitType.InternationalTransfer => ("moneyin", statementEntry.amount, month)
      case _ => ("unknown", statementEntry.amount, month)
    }
  }

  private def findMatchingCategory(description: String) : Option[String] = {
    var category:Option[String] = None
    for ((key, value) <- FinanalysisConfig.categoryMapping.categoryMap) {
      if (description.toLowerCase.contains(key.toLowerCase)) category = Some(value.toLowerCase)
    }
    category
  }


  private def setUpMap(categories: List[String]): Map[String, Double] = categories map (c => c -> 0.00) toMap

  private def adjustCategory[A, B](m: Map[A, B], newValue: A)(f: B => B) = m.updated(newValue, f(m(newValue)))

  private def changeMap(category: String, amount: Double) = categoryMap = adjustCategory(categoryMap, category)(_ + amount)
}
