package portals.vldb2023demo

object ServerMain:
  def main(args: Array[String]): Unit =
    val host = if args.length > 0 then Some(args(0).toString) else Some("localhost")
    val port = if args.length > 1 then Some(args(1).toString) else Some("8080")
    portals.distributed.SBTRunServer.main(Array(host.get, port.get))
