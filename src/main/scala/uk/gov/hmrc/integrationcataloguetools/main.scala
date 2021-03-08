package uk.gov.hmrc.integrationcataloguetools

import java.io.FileReader

import uk.gov.hmrc.integrationcataloguetools.connectors.PublisherConnector

import uk.gov.hmrc.integrationcataloguetools.models._
import org.apache.http.impl.client.HttpClients
object Main extends App {

   val integrationCatalogueTools = new IntegrationCatalogueTools()
  integrationCatalogueTools.runApplication( args.toList) match{
    case Left(error) => {
      println(s"Error: $error")
      java.lang.System.exit(1)
    }
    case Right(_) => ();
  }

}
