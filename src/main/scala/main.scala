import java.io.FileReader

object Main extends App {

  def printUsage() = {
     println("""
  Usage: sbt "run -csvToOas <inputCsvFile> <outputPath>"
""")
  }
  
  val result = args.toList match {
    case Nil => printUsage()
    case "--csvToOas" :: inputCsvFile :: outputPath :: Nil => {
      println(s"Exporting CSV to OAS Files:\nInput file: ${inputCsvFile}\noutput path: ${outputPath}")
      val rowsProcessed = ProcessCsvFile.process(inputCsvFile, outputPath)
      println(s"Exported $rowsProcessed OAS files to:\n${outputPath}")
      Right()
    }
    case "--help" :: Nil | "-h" :: Nil => printUsage()
    case option :: tail => Left(s"Unknown option or arguments : ${option}\nArgs:${args.toList}")
    case _ => printUsage()
  }

  val exitCode = result match{
     case Left(error) => println(s"Error: $error"); 1
     case Right(_) => (); 0
  }
  
  // sys.exit(exitCode)
}
