package portals.vldb2023demo

/** Portals Server for submitting and launching applications remotely.
  *
  * The server will accept `submit`, `submitDir`, and `launch` requests. To
  * issue these requests, either use the [[ClientMain]] CLI interface, or follow
  * the example from [[ShoppingCartMain]] to use the programmatic interface.
  *
  * Note: The server takes two arguments: host and port. These may also be
  * omitted, in which case it will default to localhost:8080.
  *
  * Note: The server should be run in a separate process or thread.
  *
  * @example
  *   Run the server on localhost:8080.
  *   {{{
  * sbt "runMain portals.vldb2023demo.ServerMain localhost 8080"
  *   }}}
  */
object ServerMain:
  def main(args: Array[String]): Unit =
    val host = if args.length > 0 then Some(args(0).toString) else Some("localhost")
    val port = if args.length > 1 then Some(args(1).toString) else Some("8080")
    // // swap if remote not accessible
    // portals.distributed.SBTRunServer.main(Array(host.get, port.get))
    portals.distributed.remote.RemoteSBTRunServer.main(Array(host.get, port.get))
