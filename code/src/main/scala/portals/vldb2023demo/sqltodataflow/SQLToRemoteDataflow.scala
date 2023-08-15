package portals.vldb2023demo.sqltodataflow

import portals.api.dsl.DSL.*
import portals.api.dsl.ExperimentalDSL.*
import portals.application.*
import portals.distributed.SubmittableApplication
import portals.libraries.sql.*
import portals.libraries.sql.internals.*
import portals.libraries.sql.sqlDSL.*
import portals.libraries.sql.sqlDSL.ref
import portals.system.Systems
import portals.vldb2023demo.sqltodataflow.Data.*
import portals.vldb2023demo.sqltodataflow.Types
import portals.vldb2023demo.sqltodataflow.Types.given
import portals.vldb2023demo.Util.*

/** The Table App of an example with a queryable Key-Value table using the sql
  * library.
  *
  * Exposes the Table App through a Query Portal.
  *
  * @example
  *   Submitting this example to a Portals Server (requires having a Portals
  *   Server running on localhost port 8080).
  *   {{{
  * // Submit the class files
  * sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes"
  * // Launch the application
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.sqltodataflow.SQLToRemoteDataflowTableApp$"
  *   }}}
  *
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.Types]]
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.Data]]
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.Config]]
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.SQLToDataflow]]
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.SQLToDataflowTxn]]
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.SQLToRemoteDataFlow]]
  */
object SQLToRemoteDataflowTableApp extends SubmittableApplication:
  /** Portals application which runs the queriable KV Table. */
  override def apply(): Application =
    PortalsApp("SQLToDataflowTable"):

      /** A Table (Portal) for queries of type `KV`. */
      val table = Table[Types.KV]("KVTable", "k")

      /** A Table Workflow which serves the queries of the Table Portal. */
      val tableWorkflow = Workflows[Nothing, Nothing]()
        .source(Generators.empty[Nothing].stream)
        .table[Types.KV](table)
        .sink()
        .freeze()

      /** A Query Portal which exposes the Table Portal. */
      val queryPortal = QueryPortal("queryPortal", table.ref)
end SQLToRemoteDataflowTableApp

/** The Query App of an example with a queryable Key-Value table using the sql
  * library.
  *
  * Connects to the Table App using the Query Portal.
  *
  * @example
  *   Submitting this example to a Portals Server (requires having a Portals
  *   Server running on localhost port 8080).
  *   {{{
  * // Submit the class files
  * sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes"
  * // Launch the application
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.sqltodataflow.SQLToRemoteDataflowQueryApp$"
  *   }}}
  *
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.Types]]
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.Data]]
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.Config]]
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.SQLToDataflow]]
  * @see
  *   [[portals.libraries.sql.examples.sqltodataflow.SQLToDataflowTxn]]
  */
object SQLToRemoteDataflowQueryApp extends SubmittableApplication:
  /** Remote Portals applications which queries the Query Portal. */
  override def apply(): Application =
    PortalsApp("SQLToDataflowRemote"):
      /** Get a reference to the Query Portal from the Registry. */
      val queryPortal = Registry.portals.get[String, String]("/SQLToDataflowTable/portals/queryPortal")

      /** Input queries for the Query task to execute. */
      val generator = Generators.generator[String](Data.queriesGenerator)

      /** Workflow which sends the consumed SQL requests to the query portal.
        */
      val queryWorkflow = Workflows[String, String]("queryWorkflow")
        .source(generator.stream)
        .logger("Query:  ")
        .asker(queryPortal) { x =>
          // ask the query portal to execute the query
          val f = ask(queryPortal)(x)
          await(f) {
            // log the results
            ctx.log.info(s"== Query: ${x}; Result: ${f.value.get} ==")
            // emit the result of the query
            emit(f.value.get)
          }
        }
        .logger("Results: ")
        .sink()
        .freeze()
end SQLToRemoteDataflowQueryApp

/** An example with a queryable Key-Value table using the sql library.
  *
  * @example
  *   {{{
  * sbt "runMain portals.vldb2023demo.sqltodataflow.SQLToRemoteDataflowMain"
  *   }}}
  */
object SQLToRemoteDataflowMain:
  def main(args: Array[String]): Unit =
    /** Launch the applications. */
    val system = Systems.test()
    system.launch(SQLToRemoteDataflowTableApp.apply())
    system.launch(SQLToRemoteDataflowQueryApp.apply())

    /** Run for 10 seconds */
    system.stepFor(10_000)
    system.shutdown()

    // Force quit for IDE, as otherwise other Threads might keep the program alive
    System.exit(0)
