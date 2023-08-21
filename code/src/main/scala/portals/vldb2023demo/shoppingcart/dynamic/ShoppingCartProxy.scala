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

/** A proxy for the shopping cart portals, to circumvent the restriction on the
  * request/reply type.
  *
  * @see
  *   [[portals.vldb2023demo.shoppingcart.dynamic.DynamicQuery]]
  */
object ShoppingCartProxy extends SubmittableApplication:
  override def apply(): Application =
    // Note: we are restricted to only supporting requests/replies of type
    // `String` here at the moment, this is only a temporary restriction.
    PortalsApp("ProxyServices"):
      //////////////////////////////////////////////////////////////////////////
      // Analytics PROXY
      //////////////////////////////////////////////////////////////////////////

      // Note: we are restricted to only supporting requests/replies of type
      // `String` here at the moment, this is only a temporary restriction.
      // Here we use `Any` just temporarily.
      val anaProxy = Portal[Any, Any]("analytics")

      // The real analytics service
      val anaServc = Registry.portals.get[Any, Any]("/Analytics/portals/analytics")

      val _ = Workflows[Nothing, Nothing]()
        .source(Generators.empty[Nothing].stream)
        .askerreplier(anaServc)(anaProxy)(_ => ???): //
          // PROXY REQUEST Top100
          case "Top100" =>
            // Send the request "Top100" to the analytics service and reply with
            // the result
            ask(anaServc)(Top100).onComplete:
              case Success(r) =>
                reply(r.toString())
              case Failure(e) =>
                reply(s"Error: $e")
          // NO MATCH
          case unkwn =>
            reply(s"Error: unknown request: $unkwn")
        .empty[Nothing]()
        .sink()
        .freeze()

      //////////////////////////////////////////////////////////////////////////
      // INVENTORY PROXY
      //////////////////////////////////////////////////////////////////////////

      val invProxy = Portal[Any, Any]("inventory")

      val invServc = Registry.portals.get[Any, Any]("/Inventory/portals/inventory")

      val _ = Workflows[Nothing, Nothing]()
        .source(Generators.empty[Nothing].stream)
        .askerreplier(invServc)(invProxy)(_ => ???): //
          // PROXY REQUEST <GET, INT>
          case s: String if s.startsWith("Get") =>
            val splits = s.split(" ")
            val item = splits(1).toInt
            ask(invServc)(Get(item)).onComplete:
              case Success(r) =>
                reply(r.toString())
              case Failure(e) =>
                reply(s"Error: $e")
          // PROXY REQUEST <PUT, INT>
          case s: String if s.startsWith("Put") =>
            val splits = s.split(" ")
            val item = splits(1).toInt
            ask(invServc)(Put(item)).onComplete:
              case Success(r) =>
                reply(r.toString())
              case Failure(e) =>
                reply(s"Error: $e")
          // NO MATCH
          case unkwn =>
            reply(s"Error: unknown request: $unkwn")
        .empty[Nothing]()
        .sink()
        .freeze()
  end apply
end ShoppingCartProxy
