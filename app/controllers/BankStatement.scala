package uk.co.finanlysis

import java.util.{Date, Calendar}

import uk.co.finanlysis.DebitType.DebitType
import uk.co.finanlysis.StatementCellType.StatementCellType
import scala.collection.mutable.ArrayBuffer

class BankStatement {
  var entries = ArrayBuffer.empty[StatementEntry]
}

object StatementCellType extends Enumeration {
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


class StatementEntry {

  var description: String = ""
  var amount: Double = 0.00
  var debitType: DebitType = DebitType.None
  var date: Date = Calendar.getInstance.getTime
  var category: String = ""

  def addInformation(information: A forSome {type A}, informationType: StatementCellType) = {

    informationType match {
      case StatementCellType.Description => {
        if(debitType == DebitType.PointOfSale)
          description = information.toString.split(",")(1)
        else
          description = information.toString
      }
      case StatementCellType.Amount => amount = math.abs(information.asInstanceOf[Double])
      case StatementCellType.Type => debitType = DebitType.convertToDebitType(information.toString)
      case StatementCellType.Date => date = information.asInstanceOf[Date]
      case _ => FinanalysisLogger.error("Map incorrectly requested for: " + information + ", for type: " + informationType.toString)
    }
  }
}
