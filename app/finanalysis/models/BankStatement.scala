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
  val PointOfSale, DirectDebit, CashMachine, StandingOrder, FinanceChange, Interest, BACSPayment, DirectPayment, InternationalTransfer, None = Value

  def convertToDebitType(x : String):DebitType = {
    x match {
      case "POS" => DebitType.PointOfSale
      case "D/D" => DebitType.DirectDebit
      case "C/L" => DebitType.CashMachine
      case "S/O" => DebitType.StandingOrder
      case "CHG" => DebitType.FinanceChange
      case "INT" => DebitType.Interest
      case "BAC" => DebitType.BACSPayment
      case "DPC" => DebitType.DirectPayment
      case "ITL" => DebitType.InternationalTransfer
      case _ => DebitType.None
    }
  }
}

class BankStatementEntry {

  var description: String = ""
  var amount: Double = 0.00
  var debitType: DebitType = DebitType.None
  var date: Date = Calendar.getInstance.getTime

  def update(infoValue: A forSome {type A}, infoType: StatementCellType) = {

    infoType match {
      case StatementInfoType.Description => {
        if(debitType == DebitType.PointOfSale)
          description = infoValue.toString.split(",")(1).trim
        else
          description = infoValue.toString
      }
      case StatementInfoType.Amount => amount = math.abs(infoValue.asInstanceOf[Double])
      case StatementInfoType.Type => debitType = DebitType.convertToDebitType(infoValue.toString)
      case StatementInfoType.Date => date = infoValue.asInstanceOf[Date]
      case _ => FinanalysisLogger.error("Map incorrectly requested for: " + infoValue + ", for type: " + infoType.toString)
    }
  }
}

class BankStatement {
  private var entries = ArrayBuffer.empty[BankStatementEntry]

  def add(bankStatementEntry: BankStatementEntry) = {
    entries.+=(bankStatementEntry)
  }

  def toList = entries.toList
}
