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

/** Cart for the Shopping Cart example.
  *
  * @see
  *   for more information on how to run this example:
  *   [[portals.vldb2023demo.shoppingcart.ShoppingCartMain]]
  *
  * @see
  *   [[portals.vldb2023demo.shoppingcart.ShoppingCartMain]]
  */
object Cart extends SubmittableApplication:
  override def apply(): Application =
    PortalsApp("Cart"):
      val cartOpsGenerator = Generators.generator(ShoppingCartData.cartOpsGenerator)

      val portal = Registry.portals.get[InventoryReqs, InventoryReps]("/Inventory/portals/inventory")

      val cart = Workflows[CartOps, OrderOps]("cart")
        .source(cartOpsGenerator.stream)
        .key(keyFrom(_))
        .task(CartTask(portal))
        .withName("cart")
        .sink()
        .freeze()
