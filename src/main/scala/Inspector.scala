import java.io.File
import java.io.FileReader

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import org.agmip.ace._
import org.agmip.ace.io.AceParser
import org.agmip.ace.util._

import au.com.bytecode.opencsv.CSVReader

import java.io.File
import java.io.FileInputStream

import java.util.zip.GZIPInputStream

import com.fasterxml.jackson.core.{JsonFactory, JsonParser, JsonToken}


object Inspector {
  def main(args: Array[String]) {
    args(0) match {
      case "-c" => {
        args(1) match {
          case "aceb" => {
            val f1 = loadACEBFile(new File(args(2)))
            val f2 = loadACEBFile(new File(args(3)))

            // Now compare the basics
            println("Number of experiments: ", f1.getExperiments.size, f2.getExperiments.size)
            println("Number of soils:", f1.getSoils.size, f2.getSoils.size)
            println("Number of weathers:", f1.getWeathers.size, f2.getWeathers.size)

            if(idsDifferent(f1.getExperiments.toList, f2.getExperiments.toList))
              println("Experiments are different")

            if(idsDifferent(f1.getSoils.toList, f2.getSoils.toList))
              println("Soils are different")

            if(idsDifferent(f1.getWeathers.toList, f2.getWeathers.toList))
              println("Weathers are different")

          }
          case _ => {
          }
        }
      }
      case "-v" => {
        val acebFile = loadACEBFile(new File(args(1)))
        val domeFile = loadDOMEFile(new File(args(2)))
        val acmoFile = loadACMOFile(new File(args(3)))
        val eids = acebFile.getExperiments.toList.map(e => e.getId(false))
        val wids = acebFile.getWeathers.toList.map(w => w.getId(false))
        val sids = acebFile.getSoils.toList.map(s => s.getId(false))



        if(validateACMOS(acmoFile, eids, sids, wids, domeFile)) {
          println("ACMO's are good")
        } else {
          println("Errors found")
        }
      }
      case _ => {
        println("No arguments provided")
      }
    }
  }


  def loadACEBFile(file: File):AceDataset = {
    if (! file.canRead()) {
      println("Unable to open file for reading: {}", file.toString)
      System.exit(1)
    }
    // Yes, crash intentionally if this fails
    val ds: AceDataset = AceParser.parseACEB(file)
    ds.linkDataset
    ds
  }

  def loadDOMEFile(file: File):List[String] = {
    if (! file.canRead()) {
      println("Unable to open file for reading: ", file.toString)
      System.exit(1)
    }

    val fis = new FileInputStream(file)
    val gis = new GZIPInputStream(fis)
    val jp = new JsonFactory().createParser(gis)

    var domes:ListBuffer[String] = ListBuffer()

    try {
      var level: Boolean = false
      var currentDome: String = ""
      // Seek to info
      
      while(Option(jp.nextToken()).isDefined) {
        jp.getCurrentToken match {
          case JsonToken.START_OBJECT => {
            level match {
              case true => {
                jp.skipChildren
                level = false
              }
              case false => {
                jp.nextToken
                currentDome = jp.getCurrentName
                domes = domes :+ currentDome
                level = true
              }
            }
          }
          case JsonToken.FIELD_NAME => {
            level match {
              case true => {}
              case false => {
                domes = domes :+ jp.getCurrentName
                level = true
              }
            }
          }
          case JsonToken.END_OBJECT => {
            level = false
          }
          case _ => {}
        }
      }
    } catch {
      case _ : Throwable => {
        val t:List[List[String]]  = List(List())
      }
    } finally {
      jp.close
      gis.close
      fis.close
    }
    //println(domes.toList)
    domes.toList
  }

  def loadACMOFile(file: File):List[List[String]] = {
    if (!file.canRead()) {
      println("Unable to open file for reading: ", file.toString)
      System.exit(1)
    }
    val r = new FileReader(file)
    val c = new CSVReader(r)
    var l = c.readNext
    var acmos:ListBuffer[List[String]] = ListBuffer(List())

    while (Option(l).isDefined) {
      val line = l.toList
      line.head match {
        case "*" => {
          acmos = acmos :+ line.slice(38,44)
        }
        case _ => {} // Do Nothing
      }
      l = c.readNext
    }
    acmos.toList
  }


  def idsDifferent(left: List[IAceBaseComponent], right: List[IAceBaseComponent]):Boolean = {
    val l = left.map(l => l.getId(false))
    val r = right.map(r => r.getId(false))
    val has = l.map(e => r.contains(e))
    println(has)
    has.contains(false)
  }

  def validateACMOS(acmos: List[List[String]], eids: List[String], sids: List[String],
    wids: List[String], domes: List[String]): Boolean = {
    val tf = acmos.tail.map(a => {
      //println(a)
      a.size match {
        case 0 => { None }
        case _ => {
          val l = List(Some(eids.contains(a(0))),
            Some(wids.contains(a(1))),
            Some(sids.contains(a(2))),
            if("" != a(3)) {
              Some(domeCheck(a(3), domes))
            } else {
              None
            },
            if("" != a(4)) {
              Some(domeCheck(a(4), domes))
            } else {
              None
            },
            if("" != a(5)) {
              Some(domeCheck(a(5), domes))
            } else {
              None
            })
          println(l)
          ! l.contains(Some(false))
        }
      }
    })
    //println(tf)
    tf.contains(true)
  }

  def domeCheck(domeId: String, domes: List[String]):Boolean = {
    val dids = domeId.split('|').toList
    println(s"DomeIDS: $dids")
    val tf = dids.map(x => domes.contains(x))
    ! tf.contains(false)
  }
}
