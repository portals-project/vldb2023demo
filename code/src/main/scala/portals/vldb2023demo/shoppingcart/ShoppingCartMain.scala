package portals.vldb2023demo.shoppingcart

import portals.distributed.Client
import portals.system.Systems
import portals.vldb2023demo.Util.*

/** Incrementally launches a shopping cart application using the `Client`.
  *
  * Note: in order to run this example, a corresponding `Server` must be
  * running. @see [[portals.vldb2023demo.ServerMain]].
  *
  * Note: this will use the programmatic `Client` object interface to launch the
  * application.
  *
  * Optionally, to launch the application using the `ClientCLI` see the
  * following example:
  * ```
  * // comment out the files that you want to submit to the server (so that they are not compiled with the server.)
  *
  * // start the server (in a different terminal)
  * sbt "runMain portals.vldb2023demo.ServerMain"
  *
  * // uncomment the files you want to submit
  *
  * // submit the class files with the client
  * sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes"
  *
  * // launch each application with the client
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Inventory$"
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Cart$"
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Orders$"
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Analytics$"
  * ```
  *
  * * Note: enable logging by setting the flag in object
  * `ShoppingCartConfig.LOGGING` to `true`.
  *
  * @example
  *   {{{
  * // start the server
  * sbt "runMain portals.vldb2023demo.ServerMain"
  * // submit application
  * sbt "runMain portals.vldb2023demo.shoppingcart.ShoppingCartMain"
  *   }}}
  */
object ShoppingCartMain extends App:
  // submit all the class files with the client
  Client.submitObjectWithDependencies(this)

  // launch the applications
  Client.launchObject(Inventory)
  Client.launchObject(Cart)
  Client.launchObject(Orders)
  Client.launchObject(Analytics)
