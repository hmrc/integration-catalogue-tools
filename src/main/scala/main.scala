import java.io.FileReader

object Main extends App {

  def printUsage() = {
     println("""
  Usage: sbt 'run --csvToOas "<inputCsvFile>" "<outputPath>"'
""")
  }
  
  val result : Either[String, Unit] = args.toList match {
    case Nil | "--help" :: Nil | "-h" :: Nil => {
      printUsage()
      Right()
    }
    case "--csvToOas" :: inputCsvFile :: outputPath :: Nil => {
      println(s"Exporting CSV to OAS Files:\nInput file: ${inputCsvFile}\noutput path: ${outputPath}")
      val rowsProcessed = ProcessCsvFile.process(inputCsvFile, outputPath)
      println(s"Exported $rowsProcessed OAS files to:\n${outputPath}")
      Right()
    }
    case options => Left(s"Unknown options or arguments : ${options}\nArgs:${args.toList}")
  }

  result match{
    case Left(error) => {
      println(s"Error: $error")
      java.lang.System.exit(1)
    }
    case Right(_) => ();
  }
}
