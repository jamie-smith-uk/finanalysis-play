package finanalysis.controllers

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

  val monthNames:Map[String, Int] = Map (
    "JAN" -> 1,
    "FEB" -> 2,
    "MAR" -> 3,
    "APR" -> 4,
    "MAY" -> 5,
    "JUN" -> 6,
    "JUL" -> 7,
    "AUG" -> 8,
    "SEP" -> 9,
    "OCT" -> 10,
    "NOV" -> 11,
    "DEV" -> 12
  )
  
  val monthNumbers:Map[Int, String] = Map (
    1 -> "January",
    2 -> "February",
    3 -> "March",
    4 -> "April",
    5 -> "May",
    6 -> "June",
    7 -> "July",
    8 -> "August",
    9 -> "September",
    10 -> "October",
    11 -> "November",
    12 -> "December"
  )
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




