package portals.vldb2023demo.shoppingcart.dynamic

import scala.util.Failure
import scala.util.Success

import portals.api.dsl.DSL.*
import portals.api.dsl.ExperimentalDSL.*
import portals.application.*
import portals.distributed.*
import portals.distributed.remote.*
import portals.distributed.remote.RemoteExtensions.*
import portals.vldb2023demo.shoppingcart.*
import portals.vldb2023demo.shoppingcart.ShoppingCartEvents.*

/** A runner for running the shopping cart on the remote runtime, for
  * convenience.
  *
  * @example
  *   {{{
  * sbt "runMain portals.vldb2023demo.shoppingcart.dynamic.RemoteShoppingCartMain"
  *   }}}
  */
object RemoteShoppingCartMain:
  val host = "localhost"
  val port = "8080"

  def main(args: Array[String]): Unit =
    RemoteSBTRunServer.InternalRemoteSBTRunServer.main(Array(host, port))
    Client.submitObjectWithDependencies(this, host, port.toInt)
    Client.launchObject(Inventory, host, port.toInt)
    Client.launchObject(Cart, host, port.toInt)
    Client.launchObject(Orders, host, port.toInt)
    Client.launchObject(Analytics, host, port.toInt)
    Client.launchObject(ShoppingCartProxy, host, port.toInt)
    Thread.sleep(Long.MaxValue)
    System.exit(0)
