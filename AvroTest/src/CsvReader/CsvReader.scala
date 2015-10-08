package CsvReader

/**
 * @author simonshapiro
 * 
 * This has been configured for github local
 */
import scala.collection.mutable.ListBuffer
import java.io.File

import org.apache.avro.Schema
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.apache.avro.file.FileReader
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io._
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.avro.util.Utf8


case class CsvReader(fName:String,firstLineContainsLabels:Boolean) {
  
  // TODO Consider writing a csv2arrayset and csv2avro here
  
  def toBuffer:(String,String,ListBuffer[String]) = {
    val returnCode = "success"
    val allLines = new ListBuffer[String]
    val bufferedSource = io.Source.fromFile(fName)
    for (line <- bufferedSource.getLines) {
      allLines.append(line)
    }
    bufferedSource.close
// deal with firstLineContainsLabels
    val labels = if(!firstLineContainsLabels) {
        ""
        }
      else {
        allLines(0)
      }
    if (firstLineContainsLabels) allLines.remove(0)
    (returnCode,labels,allLines)    
  }
  
  def toAvro(avroSchemaFname:String, avroFname:String): (String,String,String) = {
    val (status,labelLine,allLines) = toBuffer
    println(allLines.length)
    println(labelLine)
    
    val schema = new Schema.Parser().parse(new File(avroSchemaFname))
    println(schema)
    val inFile = new File(fName)
    val lastModified = new java.util.Date(inFile.lastModified)
    println(">>>>",fName,lastModified,inFile.getCanonicalFile)
    // establish dnaProfile
    val dnaProfile = new GenericData.Record(schema.getField("dnaProfile").schema)
    dnaProfile.put("dqPoints", 0)
    dnaProfile.put("underlyingFile", fName)
    dnaProfile.put("lastModified", lastModified.toString)
    
    val file = new File(avroFname)
    val datumWriter = new GenericDatumWriter[GenericData.Record](schema)
    val dataFileWriter = new DataFileWriter(datumWriter)
    dataFileWriter.create(schema, file)
    
    val labels = labelLine.split(",").map(_.trim)
    println(labels.length)
    allLines.foreach(line=>{
      val user1 = new GenericData.Record(schema)  //strangely this schema only checks for valid fields NOT types.
      val cols = line.split(",").map(_.trim)
      for(i <- Range(0,cols.length)) {  // interpret schema primitives here
//        println(cols.length,labels(i),cols(i))
        if (schema.getField(labels(i)) == null) throw new IllegalArgumentException("csv labels do not match schema file in position "+i+": expecting "+schema.getFields)
        schema.getField(labels(i)).schema.getType match {  //what happens if labels(i) is NOT in schema
          case Schema.Type.INT     => user1.put(labels(i),cols(i).toInt)
          case Schema.Type.BOOLEAN => user1.put(labels(i),cols(i).toBoolean)
          case Schema.Type.STRING  => user1.put(labels(i),cols(i))
          case _ => {println("SCHEMA CONVERSION ERROR - csv can only contain prmitives")}
        }
//        println("out",cols.length,labels(i),cols(i))
      }
//      println(schema.getField("dnaProfile").schema)
      user1.put("dnaProfile", dnaProfile)
      dataFileWriter.append(user1)
      })
    dataFileWriter.close()
    ("success","",avroFname)
  }

}