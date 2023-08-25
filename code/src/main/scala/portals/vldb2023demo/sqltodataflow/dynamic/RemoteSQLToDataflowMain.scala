package portals.vldb2023demo.sqltodataflow.dynamic

import portals.distributed.remote.*
import portals.distributed.remote.RemoteExtensions.*
import portals.distributed.remote.RemoteSBTRunServer
import portals.distributed.Client
import portals.vldb2023demo.sqltodataflow.SQLToDataflow
import portals.vldb2023demo.sqltodataflow.SQLToRemoteDataflowTableApp

/** A runner for running the SQL to Dataflow on the remote runtime, for
  * convenience.
  *
  * @example
  *   {{{
  * sbt "runMain portals.vldb2023demo.sqltodataflow.dynamic.RemoteSQLToDataflowMain"
  *   }}}
  */
object RemoteSQLToDataflowMain:
  val host = "localhost"
  val port = "8080"

  def main(args: Array[String]): Unit =
    RemoteSBTRunServer.InternalRemoteSBTRunServer.main(Array(host, port))
    Client.submitObjectWithDependencies(this, host, port.toInt)
    Client.launchObject(SQLToRemoteDataflowTableApp, host, port.toInt)
    Thread.sleep(Long.MaxValue)
    System.exit(0)
