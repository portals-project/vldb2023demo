package portals.vldb2023demo.shoppingcart

import portals.distributed.Client
import portals.system.Systems
import portals.vldb2023demo.Util.*

/** Launch the Shopping Cart demo locally on the test runtime.
  *
  * To run this example, simply execute the main method of this object. This
  * will start all services, run for a bit, and terminate after 10 seconds.
  *
  * Note: this uses a local `Test` execution environment, and launches the
  * services one by one onto it.
  *
  * Note: enable logging by setting the flag in object
  * `ShoppingCartConfig.LOGGING` to `true`.
  *
  * @example
  *   {{{
  *  sbt "runMain portals.vldb2023demo.shoppingcart.ShoppingCartLocalMain"
  *   }}}
  */
object ShoppingCartLocalMain:
  def main(args: Array[String]): Unit =
    /** Launch the applications. */
    val system = Systems.test()
    system.launch(Inventory.apply())
    system.launch(Cart.apply())
    system.launch(Orders.apply())
    system.launch(Analytics.apply())

    /** Run for 10 seconds */
    system.stepFor(10_000)
    system.shutdown()

    // Force quit for IDE, as otherwise other Threads might keep the program alive
    System.exit(0)
