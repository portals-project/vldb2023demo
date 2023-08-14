package portals.vldb2023demo.sqltodataflow

import portals.api.dsl.DSL.*
import portals.api.dsl.ExperimentalDSL.*
import portals.application.*
import portals.distributed.SubmittableApplication
import portals.libraries.sql.*
import portals.libraries.sql.internals.*
import portals.libraries.sql.sqlDSL.*
import portals.system.Systems
import portals.vldb2023demo.sqltodataflow.Data.*
import portals.vldb2023demo.sqltodataflow.Types
import portals.vldb2023demo.sqltodataflow.Types.given
import portals.vldb2023demo.Util.*

/** An example with a queryable Key-Value table using the sql library.
  *
  * Uses the TableWorkflow and Query task.
  *
  * @example
  *   Submitting this example to a Portals Server (requires having a Portals
  *   Server running on localhost port 8080).
  *   {{{
  * // Submit the class files
  * sbt "runMain portals.distributed.ClientCLI submitDir --directory target/scala-3.3.0/classes"
  * // Launch the application
  * sbt "runMain portals.distributed.ClientCLI launch --application portals.vldb2023demo.sqltodataflow.SQLToDataflow$"
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
object SQLToDataflow extends SubmittableApplication:
  override def apply(): Application =
    /** Portals application which runs the queriable KV Table. */
    val tableApp = PortalsApp("SQLToDataflowTable"):

      /** A Table Workflow which serves SQL queries for the table of type KV. */
      val table = TableWorkflow[Types.KV]("KVTable", "k")

      /** Input queries for the Query task to execute. */
      val generator = Generators.generator[String](queriesGenerator)

      /** Workflow which consumes the generated queries and runs query task. */
      val queryWorkflow = Workflows[String, String]("queryWorkflow")
        .source(generator.stream)
        .logger("Query:   ")
        /** A query task which connects to the `table`. */
        .query(table)
        .logger("Results: ")
        .sink()
        .freeze()

    /** Return Portals App. */
    tableApp
end SQLToDataflow

/** An example with a queryable Key-Value table using the sql library.
  *
  * @example
  *   {{{
  *  sbt "libraries/runMain portals.vldb2023demo.sqltodataflow.SQLToDataflowMain"
  *   }}}
  */
object SQLToDataflowMain:
  def main(args: Array[String]): Unit =
    /** Launch the application. */
    val system = Systems.test()
    system.launch(SQLToDataflow.apply())

    /** Run for 10 seconds */
    system.stepFor(10_000)
    system.shutdown()

    // force quit for IDE, as otherwise other Threads might keep the program alive
    System.exit(0)
