package portals.vldb2023demo.sqltodataflow.dynamic

import scala.util.Failure
import scala.util.Success

import portals.api.dsl.DSL.*
import portals.api.dsl.ExperimentalDSL.*
import portals.application.Application
import portals.distributed.remote.*
import portals.distributed.remote.RemoteExtensions.*
import portals.distributed.Client
import portals.distributed.SubmittableApplication
import portals.libraries.sql.sqlDSL.*
import portals.vldb2023demo.sqltodataflow.Data.*
import portals.vldb2023demo.ServerMain

/** Example showcasing the dynamic remote composition using remote Portals.
  *
  * This example uses the `Remote` extension, a current work in progress, to
  * send dynamic queries to the shopping cart app.
  *
  * To run the example:
  *   - Option 1: start the server (in a different terminal) on localhost 8080,
  *     and launch the SQLToRemoteDataflowTableApp app, and run the main method
  *     of this class.
  *   - Option 2: follow the instructions below. See the code examples for how
  *     to launch everything.
  *
  * @example
  *   {{{
  * sbt "runMain portals.vldb2023demo.sqltodataflow.dynamic.DynamicQuery"
  *   }}}
  *
  * @example
  *   {{{
  * # start the server (in a different terminal)
  * sbt "runMain portals.vldb2023demo.ServerMain localhost 8080";
  *
  * # start the SQLToRemoteDataflowTableApp app
  * sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes --ip localhost --port 8080";
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.sqltodataflow.SQLToRemoteDataflowTableApp$  --ip localhost --port 8080";
  *
  * # start a new server for the dynamic query app
  * sbt "runMain portals.distributed.remote.RemoteSBTRunServer localhost 8081";
  *
  * # start the dynamic query app
  * sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory portals-distributed/target/scala-3.3.0/classes --ip localhost --port 8081";
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.sqltodataflow.dynamic.DynamicQuery$ --ip localhost --port 8081";
  *   }}}
  */
object DynamicQuery extends SubmittableApplication:
  // CONFIG
  val host = "localhost"
  val port = "8081"
  val remoteHost = "localhost"
  val remotePort = "8080"

  // Launch the server and app using this main method
  def main(args: Array[String]): Unit =
    portals.distributed.remote.RemoteSBTRunServer.InternalRemoteSBTRunServer.main(Array(host, port))
    Client.submitObjectWithDependencies(this, host, port.toInt)
    Client.launchObject(this, host, port.toInt)
    Thread.sleep(Long.MaxValue)

  // THE PORTALS APP
  override def apply(): Application =
    PortalsApp("DynamicQueryApp"):
      //////////////////////////////////////////////////////////////////////////
      // QUERY THE SQL Service
      //////////////////////////////////////////////////////////////////////////

      // A remote reference to the sql table service
      val queryPortal = RemoteRegistry.portals.get[String, String](
        s"http://$remoteHost:$remotePort",
        "/SQLToDataflowTable/portals/queryPortal",
      )

      // // Select some queries to execute
      // inline def QUERIES =
      //   select_from("KVTable", 0) ::
      //     select_from("KVTable", 1) ::
      //     Nil
      //   // insert_into("KVTable", 0, 10) ::
      //   //   insert_into("KVTable", 1, 11) ::
      //   //   Nil
      //   // select_from_where("KVTable", 10, List(0, 1, 2)) ::
      //   //   select_from_where("KVTable", 11, List(0, 1, 2)) ::
      //   //   Nil

      // // A stream with a single element to trigger the query
      // val queryStream = Generators.fromList(QUERIES).stream

      // Alternatively, use a throttled query generator
      val queryStream =
        Generators.generator(queriesGenerator).stream

      // The workflow which queries the services
      val _ = Workflows[String, String]()
        .source(queryStream)
        .asker(queryPortal): q =>
          ask(queryPortal)(q).onComplete:
            case Success(r) =>
              ctx.log.info(s"== Query : ${q} ==")
              // Log and emit successfull results
              ctx.log.info(s"== Result: ${r} ==")
              emit(r)
            case Failure(e) =>
              ctx.log.error(s"== Error : ${e} ==")
              emit(s"Error: $e")
        .sink()
        .freeze()
  end apply
end DynamicQuery
