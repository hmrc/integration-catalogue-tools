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

  val in = new FileReader("input/API Library 2021-02-01 - Integration Catalogue Export.csv");

  GenerateOpenApi
    .fromCsvToOasContent(in)
    .map { case (publisherReference, oasContent) => {
      val filename = s"output/${publisherReference}.yaml"
      writeToFile(filename, oasContent)
    }}
}
