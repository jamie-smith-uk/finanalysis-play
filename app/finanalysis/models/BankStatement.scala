package finanalysis.models

import java.util.{Calendar, Date}
import finanalysis.controllers._
import finanalysis.models.DebitType.DebitType
import finanalysis.models.StatementInfoType.StatementCellType
import scala.collection.mutable.ArrayBuffer

object StatementInfoType extends Enumeration {
  type StatementCellType = Value
  val Description, Amount, Type, Date, None = Value
}

object AnalysisCategoryType extends Enumeration {
  type AnalysisCategoryType = Value
  val Travel= Value("Travel")
  val Food = Value("Food")
  val Bills = Value("Bills")
  val GoingOut = Value("Going Out")
  val GeneralExpense = Value("General Expense")
  val MoneyIn = Value("Money In")
  val Childcare = Value("Childcare")
  val Savings = Value("Savings")
  val StandingOrder = Value("Standing Order")
  val None = Value("Unknown")
}

object DebitType extends Enumeration {
  type DebitType = Value
  val PointOfSale = Value("POS")
  val DirectDebit = Value("D/D")
  val CashMachine  = Value("C/L")
  val StandingOrder = Value("S/O")
  val FinanceChange = Value("CHG")
  val Interest = Value("INT")
  val BACSPayment = Value("BAC")
  val DirectPayment = Value("DPC")
  val InternationalTransfer = Value("ITL")
  val internationalPayment = Value("IBP")
  val None = Value("")
}