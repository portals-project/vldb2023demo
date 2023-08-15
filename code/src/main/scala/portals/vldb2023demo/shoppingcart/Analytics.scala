package portals.vldb2023demo.shoppingcart

import portals.api.dsl.DSL
import portals.api.dsl.DSL.*
import portals.api.dsl.ExperimentalDSL.*
import portals.application.Application
import portals.vldb2023demo.shoppingcart.tasks.*
import portals.vldb2023demo.shoppingcart.ShoppingCartEvents.*

/** Analytics for the Shopping Cart example.
  *
  * @see
  *   for more information on how to run this example:
  *   [[portals.vldb2023demo.shoppingcart.ShoppingCartMain]]
  *
  * @see
  *   [[portals.vldb2023demo.shoppingcart.ShoppingCartMain]]
  */
object Analytics extends portals.distributed.SubmittableApplication:
  override def apply(): portals.application.Application =
    PortalsApp("Analytics"):
      val ordersStream = Registry.streams.get[OrderOps]("/Orders/workflows/orders/stream")

      val analyticsportal = Portal[AnalyticsReqs, AnalyticsReps]("analytics", keyFrom)

      val analytics = Workflows[OrderOps, Nothing]("analytics")
        .source(ordersStream)
        .flatMap { case Order(_, CartState(items)) => items }
        .key(keyFrom(_))
        .task(AggregatingTask())
        .key(_ => 0L)
        .task(AnalyticsTask(analyticsportal))
        .logger()
        .nothing()
        .sink()
        .freeze()
