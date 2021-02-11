import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.util.Yaml;
// import java.io.File
// import java.io.BufferedWriter
import java.io.FileReader
import io.swagger.v3.oas.models.OpenAPI

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import scala.collection.JavaConverters._


object Main extends App {
  private def writeToFile(filename: String, content: String): Unit = {
    import java.io.File
    import java.io.BufferedWriter
    import java.io.FileWriter

    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
  }

  def nullableString(v: String) : String = Option(v).getOrElse("")

  // CsvToOas.go()
  val in = new FileReader("input/API Library 2021-02-01 - Integration Catalogue Export.csv");

  val rows = org.apache.commons.csv.CSVFormat.EXCEL
    .withFirstRecordAsHeader()
    .parse(in).getRecords().asScala.toSeq
    .map({record : CSVRecord => {
      val expectedValues = 6
      // TODO : Handle without exception?
      if (record.size() < 6) throw new RuntimeException(s"Expected $expectedValues values on row ${record.getRecordNumber()}")

      BasicApi(
        nullableString(record.get(0)),
        nullableString(record.get(1)),
        nullableString(record.get(2)),
        nullableString(record.get(3)),
        nullableString(record.get(4)),
        nullableString(record.get(5)) )
      }
    })

  rows
    .map(basicApi => (basicApi, GenerateOpenApi.generateOasContent(basicApi)) )
    .map{ case (basicApi, oasText) => {
      val filename = s"output/${basicApi.publisherReference}.yaml"
      writeToFile(filename, oasText)
    }}
}
