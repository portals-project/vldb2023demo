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

/** Inventory for the Shopping Cart example.
  *
  * @see
  *   for more information on how to run this example:
  *   [[portals.vldb2023demo.shoppingcart.ShoppingCartMain]]
  *
  * @see
  *   [[portals.vldb2023demo.shoppingcart.ShoppingCartMain]]
  */
object Inventory extends SubmittableApplication:
  override def apply(): Application =
    PortalsApp("Inventory"):
      val inventoryOpsGenerator = Generators.generator(ShoppingCartData.inventoryOpsGenerator)
      
      val portal = Portal[InventoryReqs, InventoryReps]("inventory", keyFrom)
      
      val inventory = Workflows[InventoryReqs, Nothing]("inventory")
        .source(inventoryOpsGenerator.stream)
        .key(keyFrom(_))
        .logger()
        .task(InventoryTask(portal))
        .withName("inventory")
        .sink()
        .freeze()
