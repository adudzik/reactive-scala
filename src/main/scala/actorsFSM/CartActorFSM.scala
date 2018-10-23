package actorsFSM

import actorsFSM.CartFSM.{CartData, CartState}
import actors.CartItems
import akka.actor.FSM

import scala.concurrent.duration._

object CartFSM {

  sealed trait CartState

  case object Empty extends CartState

  case object NonEmpty extends CartState

  case object InCheckout extends CartState


  sealed trait CartData

  case class Buying(items: CartItems) extends CartData


  sealed trait CartEvent

  case class ItemAdded(id: String) extends CartEvent

  case class ItemRemoved(id: String) extends CartEvent

  case class CheckoutStarted(items: CartItems) extends CartEvent {
    require(!items.isEmpty)
  }

  case object CheckoutCanceled extends CartEvent

  case object CheckoutClosed extends CartEvent

}

class CartAggregatorFSM extends FSM[CartState, CartData] {

  import CartFSM._

  startWith(Empty, Buying(CartItems(Set())))

  when(Empty) {
    case Event(ItemAdded(id), Buying(items)) =>
      items.addItem(id)
      println("Added item \"" + id + "\" to cart: " + items.toString)
      goto(NonEmpty) using Buying(items)
  }

  when(NonEmpty, stateTimeout = 1.second) {
    case Event(ItemAdded(id), Buying(items)) =>
      items.addItem(id)
      println("Added item \"" + id + "\" to cart: " + items.toString)
      stay using Buying(items)
    case Event(ItemRemoved(id), Buying(items)) =>
      items.removeItem(id)
      println("Removed item \"" + id + "\" from cart: " + items.toString)
      if (items.isEmpty) goto(Empty) using Buying(items) else stay using Buying(items)
    case Event(CheckoutStarted, Buying(items)) =>
      println("Starting checkout for items:" + items.toString)
      goto(InCheckout) using Buying(items)
  }

  when(InCheckout) {
    case Event(CheckoutCanceled, Buying(items)) =>
      println("Canceling checkout...")
      goto(NonEmpty) using Buying(items)
    case Event(CheckoutClosed, Buying(_)) =>
      println("Closing checkout...")
      goto(Empty) using Buying(CartItems(Set()))
  }

  whenUnhandled {
    case Event(StateTimeout, Buying(items)) =>
      items.removeAll()
      println("Too long in state: %s!".format(stateName))
      goto(Empty) using Buying(items)
    case Event(e, s) =>
      println("Unsupported request %s on %s/%s state!".format(e, stateName, s))
      stay
  }

  initialize()
}
