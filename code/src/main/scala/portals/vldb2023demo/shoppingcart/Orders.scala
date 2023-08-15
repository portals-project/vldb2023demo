package portals.vldb2023demo.shoppingcart

import portals.api.dsl.DSL
import portals.api.dsl.DSL.*
import portals.api.dsl.DSL.PortalsApp
import portals.api.dsl.ExperimentalDSL.*
import portals.application.Application
import portals.distributed.SubmittableApplication
import portals.vldb2023demo.shoppingcart.tasks.*
import portals.vldb2023demo.shoppingcart.ShoppingCartData
import portals.vldb2023demo.shoppingcart.ShoppingCartEvents.*

/** Orders for the Shopping Cart example.
  *
  * @see
  *   for more information on how to run this example:
  *   [[portals.vldb2023demo.shoppingcart.ShoppingCartMain]]
  *
  * @see
  *   [[portals.vldb2023demo.shoppingcart.ShoppingCartMain]]
  */
object Orders extends SubmittableApplication:
  override def apply(): Application =
    PortalsApp("Orders"):
      val cartStream = Registry.streams.get[OrderOps]("/Cart/workflows/cart/stream")

      val orders = Workflows[OrderOps, OrderOps]("orders")
        .source(cartStream)
        .key(keyFrom(_))
        .task(OrdersTask())
        .withName("orders")
        .sink()
        .freeze()
