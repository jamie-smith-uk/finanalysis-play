package finanalysis.controllers

import play.api.Logger

/**
 * Created by jsmith on 30/12/2015.
 */
object FinanalysisLogger {

  //private val logger = Logger("Finanalysis")

  def debug ( msg:String ) = {
    //logger.debug(msg)
    Logger.debug(msg)
  }

  def error ( msg:String ) = {
    //logger.error(msg)
    Logger.debug(msg)
  }

}
