package portals.vldb2023demo.shoppingcart.tasks

import portals.api.dsl.DSL.*
import portals.application.task.*
import portals.vldb2023demo.shoppingcart.ShoppingCartConfig
import portals.vldb2023demo.shoppingcart.ShoppingCartEvents.*

object AggregatingTask:
  type I = Item
  type O = (Item, Count)
  type Req = Nothing
  type Res = Nothing
  type Context = MapTaskContext[I, O]
  type Task = GenericTask[I, O, Nothing, Nothing]

  private final val state: PerKeyState[Int] =
    PerKeyState[Int]("state", 0)

  private def onNext(item: Item)(using Context): (Item, Count) =
    val count = state.get()
    val newCount = count + 1
    state.set(newCount)
    (item, newCount)

  def apply(): Task =
    Tasks.map(onNext)
