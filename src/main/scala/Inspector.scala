import java.io.File

import scala.collections.JavaConversions._

import org.agmip.ace._
import org.agmip.ace.io.AceParser
import org.agmip.ace.util._

object Inspector {
  def main(args: Array[String]) = {
    al f1 = loadFile(new File(args(0)))
    val f2 = loadFile(new File(args(1)))

    // Now compare the basics
    println("Number of experiments: ", f1.getExperiments.size, f2.getExperiments.size)
    println("Number of soils:", f1.getSoils.size, f2.getSoils.size)
    println("Number of weathers:", f1.getWeathers.size, f2.getWeathers.size)

    val ex1 = f1.getExperiments.toList
    val ex2 = f2.getExperiments.toList

  }

  def loadFile(file: File):AceDataset = {
    if (! file.canRead()) {
      println("Unable to open file for reading: {}", file.toString)
      System.exit(1)
    }
    // Yes, crash intentionally if this fails
    val ds: AceDataset = AceParser.parseACEB(file)
    ds.linkDataset
    ds
  }

  def idsDifferent(left: List, right: List):Boolean = {

  }
}
