package actors

import akka.actor.{Actor, Timers}
import akka.event.LoggingReceive

import scala.concurrent.duration._

object Cart {

  sealed trait CartCommand

  case class AddItem(id: String) extends CartCommand

  case class RemoveItem(id: String) extends CartCommand

  case class StartCheckout(items: CartItems) extends CartCommand {
    require(!items.isEmpty)
  }

  case object CancelCheckout extends CartCommand

  case object CloseCheckout extends CartCommand

}

case object TimeoutKey

case object CartTimeout


case class CartItems(var items: Set[String]) {

  def isEmpty: Boolean = items.isEmpty

  def addItem(item: String): Unit = items = items + item

  def removeItem(item: String): Unit = items -= item

  def removeAll(): Unit = items = Set()
}

class CartAggregator extends Actor with Timers {

  import Cart._

  val cartItems = CartItems(Set())

  override def receive: Receive = emptyStage

  def emptyStage: Receive = LoggingReceive {
    case AddItem(id) =>
      cartItems.addItem(id)
      println("Added item \"" + id + "\" to cart: " + cartItems.items.toString())
      timers.startSingleTimer(TimeoutKey, CartTimeout, 1.second)
      context become nonEmptyStage
    case s =>
      println("Unsupported operation: %s on Empty state!".format(s))
  }

  def nonEmptyStage: Receive = LoggingReceive {
    case AddItem(id) =>
      cartItems.addItem(id)
      println("Added item \"" + id + "\" to cart: " + cartItems.items.toString())
    case RemoveItem(id) =>
      cartItems.removeItem(id)
      timers.cancel(TimeoutKey)
      println("Removed item \"" + id + "\" from cart: " + cartItems.items.toString())
      if (cartItems.isEmpty) context become emptyStage
    case StartCheckout =>
      println("Starting checkout for items:" + cartItems.items.toString())
      timers.cancel(TimeoutKey)
      context become inCheckoutStage
    case CartTimeout =>
      println("Too long in NonEmpty state!")
      cartItems.removeAll()
      context become emptyStage
    case _ =>
      println("Unsupported operation on NonEmpty state!")
  }

  def inCheckoutStage: Receive = LoggingReceive {
    case CancelCheckout =>
      println("Canceling checkout...")
      context become nonEmptyStage
    case CloseCheckout =>
      println("Closing checkout...")
      context become emptyStage
    case _ =>
      println("Unsupported operation on InCheckout state!")
  }
}