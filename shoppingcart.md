---
layout: default
title: "Demo 1: Shopping Cart"
---

# Demo 1: Shopping Cart

The shopping cart scenario consists of four services: an inventory; cart; order; and an analytics service. The application shows how services can be dynamically composed together with inter-dependencies between them. Additionally, the application shows how the Portal service can be used as a core component for messaging between services.

The shopping cart's inventory manages the inventory state in an operator, and exposes this state as a portal service. It can handle either GetItem (take an item from the inventory) or PutItem (put the item back) requests. The user cart interacts with the inventory from one of its operators. For example, to add an item to the cart, it will have to request to get the item (GetItem) from the inventory, by calling the inventory portal, and await the response from the call. The successfully checked-out carts will be consumed by the order service. Lastly, there is an analytics service, that consumes the order history in order to provide real-time recommendations. In our example, this produces a top-100 list of item purchases, accessible as a portal service.

## Demo Experience

The demonstration will show, step-by-step, how each service is launched onto the Portals serverless platform, whilst explaining the example code. A data generator is used for simulating data, which will allow the participants to interactively inspect the various services through ad hoc querying of the portals. For example, sending custom GetItem and PutItem queries to the inventory portal, or querying the analytics dataflow for the most purchased items.

> **Note**
> To execute the demo yourself, check out the instructions in the code directory of this repository: [https://github.com/portals-project/vldb2023demo/tree/main/code](https://github.com/portals-project/vldb2023demo/tree/main/code).

> **Note**
> To executable code for this demo can be found in the code directory of this repository: [https://github.com/portals-project/vldb2023demo/tree/main/code](https://github.com/portals-project/vldb2023demo/tree/main/code).

#### The Inventory Task and Workflow

The Inventory Task holds the inventory state in its contextual `state` variable. The task handles either `onNext` events, these are events that are consumed as a stream, that may either add or remove items to the inventory. Additionally, it handles `onAsk` events, these are events that come from a Portal which this task connects to and handles. This way, other tasks (such as the Cart), connect to the inventory, in order to take an item from the inventory and put it in the clients cart. Here, note the use of `reply`, which is enabled under the ReplyContext and replies back to the sender of the request.

```scala
object InventoryTask:
  type I = InventoryReqs
  type O = Nothing
  type Req = InventoryReqs
  type Res = InventoryReps
  type Context = ProcessorTaskContext[I, O]
  type RepContext = ReplierTaskContext[I, O, Req, Res]
  type PortalRef = AtomicPortalRefKind[Req, Res]
  type Task = GenericTask[I, O, Req, Res]

  private final val state: PerKeyState[Int] =
    PerKeyState[Int]("state", 0)

  private def get(e: Get)(using Context): Unit =
    if ShoppingCartConfig.LOGGING then ctx.log.info(s"Taking ${e.item} from inventory")
    state.get() match
      case x if x > 0 => state.set(x - 1)
      case _ => ???

  private def put(e: Put)(using Context): Unit =
    if ShoppingCartConfig.LOGGING then ctx.log.info(s"Putting ${e.item} in inventory")
    state.set(state.get() + 1)

  private def get_req(e: Get)(using RepContext): Unit =
    if ShoppingCartConfig.LOGGING then ctx.log.info(s"Checking if ${e.item} is in inventory")
    state.get() match
      case x if x > 0 =>
        reply(GetReply(e.item, true))
        state.set(x - 1)
      case _ =>
        reply(GetReply(e.item, false))

  private def put_req(e: Put)(using RepContext): Unit =
    if ShoppingCartConfig.LOGGING then ctx.log.info(s"Putting ${e.item} in inventory")
    state.set(state.get() + 1)
    reply(PutReply(e.item, true))

  private def onNext(event: InventoryReqs)(using Context): Unit = event match
    case e: Get => get(e)
    case e: Put => put(e)

  private def onAsk(event: InventoryReqs)(using RepContext): Unit = event match
    case e: Get => get_req(e)
    case e: Put => put_req(e)

  def apply(portal: PortalRef): Task =
    Tasks.replier(portal)(onNext)(onAsk)

end InventoryTask
```

In order to run the Inventory Task, we execute it in a workflow in its own application. Here, the inventory consumes a generator of inventory operations (for filling the cart with inventory items), and it also creates a `portal`, which exposes the inventory as a service, for which it handles the requests of the form `InventoryReqs` and `InventoryReps`. The `keyFrom` function is used to extract the key from the request, which is used to partition the state of the inventory. Lastly, the inventory task is added to the workflow.

```scala
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
```

Once this application is launched, then the inventory will be executing, and accessible as a service from other applications (both in a local deployment, but also in a remote runtime if it has valid access). Other applications can access both the streams of this application (`inventory.stream`), but also the exposed portal service as in the following section.

### The Cart Task and Workflow

The Cart Task handles client requests with its `onNext` handler: these are either `AddToCart`, `RemoveFromCart`, or `Checkout` requests. To handle the requests, the Cart connects to the Portal of the inventory (see `Tasks.asker(portal)(onNext(portal))`). Here, `portal` is a reference of type `PortalRef`, and can be used for sending requests to the portal (the inventory).

```scala
object CartTask:
  type I = CartOps
  type O = OrderOps
  type Req = InventoryReqs
  type Res = InventoryReps
  type Context = AskerTaskContext[I, O, Req, Res]
  type PortalRef = AtomicPortalRefKind[Req, Res]
  type Task = GenericTask[I, O, Req, Res]

  private final val state: PerKeyState[CartState] =
    PerKeyState[CartState]("state", CartState.zero)

  private def addToCart(event: AddToCart, portal: PortalRef)(using Context): Unit =
    val req = Get(event.item)
    val resp = ask(portal)(req)
    await(resp):
      resp.value match
        case Some(GetReply(item, true)) =>
          if ShoppingCartConfig.LOGGING then ctx.log.info(s"User ${event.user} added $item to cart")
          state.set(state.get().add(item))
        case Some(GetReply(item, false)) =>
          if ShoppingCartConfig.LOGGING then
            ctx.log.info(s"User ${event.user} tried to add $item to cart, but it was not in inventory")
        case _ =>
          if ShoppingCartConfig.LOGGING then ctx.log.info("Unexpected response")
          ???

  private def removeFromCart(event: RemoveFromCart, portal: PortalRef)(using Context): Unit =
    if ShoppingCartConfig.LOGGING then ctx.log.info(s"User ${event.user} removed ${event.item} from cart")
    val req = Put(event.item)
    val _ = ask(portal)(req)

  private def checkout(event: Checkout)(using Context): Unit =
    if ShoppingCartConfig.LOGGING then ctx.log.info(s"Checking out ${event.user}")
    val cart = state.get()
    ctx.emit(Order(event.user, cart))
    state.del()

  private def onNext(portal: PortalRef)(event: CartOps)(using Context): Unit =
    event match
      case event: AddToCart => addToCart(event, portal)
      case event: RemoveFromCart => removeFromCart(event, portal)
      case event: Checkout => checkout(event)

  def apply(portal: PortalRef): Task =
    Tasks.asker(portal)(onNext(portal))

end CartTask
```

The most interesting case is the `AddToCart` event. This triggers the method `addToCart`, which sends a `Get(event.item)` request to the inventory. The task, then, awaits the response of this request (`await(resp)`). When the response arrives, the task will either update its internal state (if it was successfull in getting the item), or otherwise ignore it. We could also add the option here of sending a reply to the client, but we omit this for simplicity.

Similarly to the inventory, in order to run the Cart Task, we execute it in a workflow in its own application. Here, the cart consumes a generator of cart operations (for generating cleint events such as AddToCart, RemoveFromCart, Checkout), and it connects to the remote portal using the `Registry`. The Cart workflow creates a task with the Cart Task, and connects to the Inventory's portal.

```scala
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
```

Now, the cart application can be launched, and it will automatically connect to the inventory if they are launched within the same runtime. If launched in a different/remote runtime, the cart would need to use a Remote Registry instead, in order to connect to a remote runtime's portal.

### Orders and Analytics

For the remaining tasks, we will not go into detail; they are available in the `code` directory in this repo for reference.