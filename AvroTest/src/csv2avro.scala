

/**
 * @author simonshapiro
 */
import scala.collection.mutable.StringBuilder
import scala.collection.mutable.HashMap
import YedIntegration.Node
import YedIntegration.Edge
import YedIntegration.Graph
import CsvReader.CsvReader
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

//    val (status,labels,allLines) = CsvReader(args(0),true).csv2Buffer
//    val g=Graph(allLines)
//    println(g.asXML)


object csv2avro {
  def processArgs(args:Array[String]):HashMap[String,String] = {
    val argHash = new HashMap[String,String]
    argHash.put("input","data/input.csv")
    argHash.put("output","data/output.avro")
    argHash.put("schema","data/csv.schema")
    args.foreach(l=>argHash.put(l.split("=")(0),l.split("=")(1)))
    println(argHash)
    argHash
  }
  
  def main(args:Array[String]){
    println(args(0))
    val argHash = processArgs(args)
    val (status,msg,avroFname) = new CsvReader(argHash("input"),true).toAvro(argHash("schema"),argHash("output"))
  
    // Deserialize users from disk
    val file = new File(argHash("output"))
    val schemaParser = new Schema.Parser()
    val schema = schemaParser.parse(new File(argHash("schema")))
    println(schema)
    val datumReader = new GenericDatumReader[GenericData.Record](schema)
    println(datumReader.getSchema)
    val dataFileReader = new DataFileReader[GenericData.Record](file, datumReader)
    var user:GenericData.Record = null
    while (dataFileReader.hasNext()) {
      // Reuse user object by passing it to next(). This saves us from
      // allocating and garbage collecting many objects for files with
      // many items.
      user = dataFileReader.next(user);
//      println(user);
//      println(user.get("name"))
    }
    println(dataFileReader.getSchema)
  }
}