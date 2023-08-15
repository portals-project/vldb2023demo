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
  * Two query workflow send queries to the same table.
  *
  * The query is transactional, but not interactive, it may happen that one
  * transaction fails, but still proceed to commit. Such behavior is undefined
  * (and should not be considered).
  *
  * @example
  *   Submitting this example to a Portals Server (requires having a Portals
  *   Server running on localhost port 8080).
  *   {{{
  * // Submit the class files
  * sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes"
  * // Launch the application
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.sqltodataflow.SQLToDataflowTxn$"
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
object SQLToDataflowTxn extends SubmittableApplication:
  override def apply(): Application =
    /** Portals application which runs the queriable KV Table. */
    val tableApp = PortalsApp("SQLToDataflowTable"):

      /** A Table Workflow which serves SQL queries for the table of type KV. */
      val table = TableWorkflow[Types.KV]("KVTable", "k", true)

      /** Transactional input queries for the Query task to execute. */
      val generator1 = Generators.generator[TxnQuery](Data.transactionalQueriesGenerator)

      /** More input queries for another query task. */
      val generator2 = Generators.generator[TxnQuery](Data.transactionalQueriesGenerator)

      /** Workflow which consumes the generated queries and runs query task. */
      val queryWorkflow1 = Workflows[TxnQuery, String]("queryWorkflow1")
        .source(generator1.stream)
        /** A query task which connects to the `table`. */
        .logger("Query  1: ")
        .queryTxn(table)
        .logger("Result 1: ")
        .sink()
        .freeze()

      /** Workflow which consumes the generated queries and runs query task. */
      val queryWorkflow2 = Workflows[TxnQuery, String]("queryWorkflow2")
        .source(generator2.stream)
        /** Another query task which connects to the `table`. */
        .logger("Query  2: ")
        .queryTxn(table)
        .logger("Result 2: ")
        .sink()
        .freeze()

    /** Return Portals App. */
    tableApp
end SQLToDataflowTxn

/** An example with a queryable Key-Value table using the sql library.
  *
  * @example
  *   {{{
  *  sbt "runMain portals.vldb2023demo.sqltodataflow.SQLToDataflowTxnMain"
  *   }}}
  */
object SQLToDataflowTxnMain:
  def main(args: Array[String]): Unit =
    /** Launch the application. */
    val system = Systems.test()
    system.launch(SQLToDataflowTxn.apply())

    /** Run for 10 seconds */
    system.stepFor(10_000)
    system.shutdown()

    // force quit for IDE, as otherwise other Threads might keep the program alive
    System.exit(0)
