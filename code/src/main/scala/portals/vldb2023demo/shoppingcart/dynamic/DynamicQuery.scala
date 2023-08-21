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

/** Example showcasing the dynamic remote composition using remote Portals.
  *
  * This example uses the `Remote` extension, a current work in progress, to
  * send dynamic queries to the shopping cart app.
  *
  * To run the example:
  *   - Option 1: start the server (in a different terminal) on localhost 8080,
  *     and launch the shopping cart app, and run the main method of this class.
  *   - Option 2: follow the instructions below. See the code examples for how
  *     to launch everything.
  *
  * **Note:** at the moment, this example is limited to queries of type
  * `String`, this is due to a restriction in the remote library for now.
  *
  * @example
  *   {{{
  * sbt "runMain portals.vldb2023demo.shoppingcart.dynamic.DynamicQuery"
  *   }}}
  *
  * @example
  *   {{{
  * // start the server (in a different terminal)
  * sbt "distributed/runMain portals.distributed.remote.RemoteSBTRunServer localhost 8080"
  *
  * // start the shopping cart app
  * sbt "distributed/runMain portals.distributed.ClientCLI submitDir --directory portals-distributed/target/scala-3.3.0/classes --ip localhost --port 8080"
  * sbt "distributed/runMain portals.distributed.ClientCLI launch --application portals.vldb2023demo.shoppingcart.Inventory$  --ip localhost --port 8080"
  * sbt "distributed/runMain portals.distributed.ClientCLI launch --application portals.vldb2023demo.shoppingcart.Cart$  --ip localhost --port 8080"
  * sbt "distributed/runMain portals.distributed.ClientCLI launch --application portals.vldb2023demo.shoppingcart.Orders$  --ip localhost --port 8080"
  * sbt "distributed/runMain portals.distributed.ClientCLI launch --application portals.vldb2023demo.shoppingcart.Analytics$  --ip localhost --port 8080"
  * sbt "distributed/runMain portals.distributed.ClientCLI launch --application portals.vldb2023demo.shoppingcart.dynamic.ShoppingCartProxy$ --ip localhost --port 8080"
  *
  * // start a new server for the dynamic query app
  * sbt "distributed/runMain portals.distributed.remote.RemoteSBTRunServer localhost 8081"
  *
  * // start the dynamic query app
  * sbt "distributed/runMain portals.distributed.ClientCLI submitDir --directory portals-distributed/target/scala-3.3.0/classes --ip localhost --port 8081"
  * sbt "distributed/runMain portals.distributed.ClientCLI launch --application portals.vldb2023demo.shoppingcart.dynamic.DynamicQuery$ --ip localhost --port 8081"
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
    RemoteSBTRunServer.InternalRemoteSBTRunServer.main(Array(host, port))
    Client.submitObjectWithDependencies(this, host, port.toInt)
    Client.launchObject(this, host, port.toInt)
    Thread.sleep(Long.MaxValue)

  // THE PORTALS APPLICATION
  override def apply(): Application =
    PortalsApp("DynamicQueryApp"):
      //////////////////////////////////////////////////////////////////////////
      // QUERY THE INVENTORY SERVICE
      //////////////////////////////////////////////////////////////////////////

      // A remote reference to the inventory service
      val inventory = RemoteRegistry.portals.get[String, String](
        s"http://$remoteHost:$remotePort",
        "/ProxyServices/portals/inventory",
      )

      // A stream with a single element to trigger the query
      val inventoryQueryStream = Generators.signal("Get 1").stream

      // The workflow which queries the services
      val _ = Workflows[String, String]()
        .source(inventoryQueryStream)
        .asker(inventory): x =>
          // Send the request to the inventory service
          ask(inventory)(x).onComplete:
            case Success(r) =>
              // Log and emit successfull results
              log.info(s"Got result: $r")
              emit(r)
            case Failure(e) =>
              log.error(s"Error: $e")
              emit(s"Error: $e")
        .sink()
        .freeze()

      //////////////////////////////////////////////////////////////////////////
      // QUERY THE ANALYTICS SERVICE
      //////////////////////////////////////////////////////////////////////////

      val analytics = RemoteRegistry.portals.get[String, String](
        s"http://$remoteHost:$remotePort",
        "/ProxyServices/portals/analytics",
      )

      // A stream with a single element to trigger the query
      val analyticsQueryStream = Generators.signal("Top100").stream

      // The workflow which queries the services
      val _ = Workflows[String, String]()
        .source(analyticsQueryStream)
        .asker(analytics): x =>
          // Send the request `x` to the analytics service
          ask(analytics)(x).onComplete:
            case Success(r) =>
              // Log and emit successfull results
              log.info(s"Got result: $r")
              emit(r)
            case Failure(e) =>
              log.error(s"Error: $e")
              emit(s"Error: $e")
        .sink()
        .freeze()
  end apply
end DynamicQuery
