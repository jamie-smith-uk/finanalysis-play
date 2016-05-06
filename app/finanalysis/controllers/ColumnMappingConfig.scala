package finanalysis.controllers

import java.time.Year

import finanalysis.models.{AnalysisCategoryType, StatementInfoType}
import AnalysisCategoryType.AnalysisCategoryType
import StatementInfoType.StatementCellType
import com.typesafe.config.{ConfigObject, ConfigValue, ConfigFactory, Config}
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import java.util.Map.Entry

object FinanalysisConfig{
  val config = ConfigFactory.load()
  val mappingConfig = new ColumnMappingConfig(config)
  val exportFileLocation = config.getString("export-file-location")
  val categoryMapping = new FinanalysisCategoryMapping(config)
  var monthsToAnalayse = config.getIntList("months-to-analyse").asScala

}

object Month {

  private val currentMonth  = java.time.LocalDate.now().getMonth.getValue
  private val currentYear = Year.now().getValue

  def toName(month: Int): String = {
    month match {
      case 1 => "January"
      case 2 => "February"
      case 3 => "March"
      case 4 => "April"
      case 5 => "May"
      case 6 => "June"
      case 7 => "July"
      case 8 => "August"
      case 9 => "September"
      case 10 => "October"
      case 11 => "November"
      case 12 => "December"
    }
  }

  def toInt(month: String): Int ={
    month.toLowerCase match {

      case "january" | "jan" => 1
      case "february" | "feb" => 2
      case "march" | "mar" => 3
      case "april" | "apr" => 4
      case "may" => 5
      case "june" | "jun" => 6
      case "july" | "jul" => 7
      case "august" | "aug" => 8
      case "september" | "sep" => 9
      case "october" | "oct" => 10
      case "november" | "nov" => 11
      case "december" | "dec" => 12
    }
  }

   private def padMonth(month: Int) : String = {
     if(month<10) "0" + month else month.toString
   }

  def comparisonInt(month: String) : Int = {

    if (this.toInt(month) > currentMonth)
      ((currentYear-1).toString +  padMonth(this.toInt(month))).toInt
    else
      (currentYear.toString +  padMonth(this.toInt(month))).toInt
  }

  def months : List[String] = {
    List(
      "January",
      "February",
      "March",
      "April",
      "May",
      "June",
      "July",
      "August",
      "September",
      "October",
      "November",
      "December"
    )
  }

}

trait MappingConfig {

  def mapping: Array[Mapping]
  def mappingAtIndex(index: Int): Option[Mapping]
}

case class ColumnMappingConfig(config: Config) extends MappingConfig{

  lazy val colMapping : Array[Mapping] = {

    val list : Iterable[ConfigObject] = config.getObjectList("column-mappings").asScala
    (for {
      item : ConfigObject <- list
      entry : Entry[String, ConfigValue] <- item.entrySet().asScala
      key = {
        entry.getKey match {
          case "description" => StatementInfoType.Description
          case "value" => StatementInfoType.Amount
          case "date" => StatementInfoType.Date
          case "type" => StatementInfoType.Type
          case _ => StatementInfoType.None
        }
      }
      value = entry.getValue.unwrapped.asInstanceOf[Int]
    } yield new Mapping(value, key)).toArray
  }

  def mapping:Array[Mapping] = colMapping

  def mappingAtIndex(index:Int) : Option[Mapping] ={
    mapping.find(_.in == index)
  }
}

class Mapping(index:Int, mapType: StatementCellType) {
  var in:Int = index
  var mappingType:StatementCellType = mapType
}

trait CategoryMapping {
  def addMapForCategory(categoryName: String)
  def CategoryMap:scala.collection.mutable.Map[String, String]
}

case class FinanalysisCategoryMapping(config: Config) extends CategoryMapping {
  private val categoryMap : scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map.empty[String, String]

  AnalysisCategoryType.values.foreach(v => addMapForCategory(v.toString.trim.replace(" ", "").toLowerCase))

  def CategoryMap:scala.collection.mutable.Map[String, String] = categoryMap

  def addMapForCategory(categoryName: String) = {
    try {
      config.getStringList("type-mappings." + categoryName).toArray.map(x => x.toString).foreach(categoryMap += _ -> categoryName)
    }
    catch {
      case e: Throwable => {
        FinanalysisLogger.error(e.getMessage)
      }
    }
  }

}




