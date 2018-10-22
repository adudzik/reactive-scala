package actors

import akka.actor.{Actor, Timers}
import akka.event.LoggingReceive

import scala.concurrent.duration._

object Cart {

  sealed trait CartCommand

  case class AddItem(id: String) extends CartCommand

  case class RemoveItem(id: String) extends CartCommand

  case class StartCheckout(items: CartItems) extends CartCommand

  case object CancelCheckout extends CartCommand

  case object CloseCheckout extends CartCommand


  sealed trait CartEvent

  case class ItemAdded(id: String) extends CartEvent

  case class ItemRemoved(id: String) extends CartEvent

  case object CheckoutStarted extends CartEvent

  case object CheckoutCanceled extends CartEvent

  case object CheckoutClosed extends CartEvent

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
      timers.startSingleTimer(TimeoutKey, CartTimeout, 2.second)
      context become nonEmptyStage
    case _ =>
      println("Unsupported operation on Empty state!")
  }

  def nonEmptyStage: Receive = LoggingReceive {
    case AddItem(id) =>
      cartItems.addItem(id)
      println("Added item \"" + id + "\" to cart: " + cartItems.items.toString())
    case RemoveItem(id) =>
      cartItems.removeItem(id)
      println("Removed item \"" + id + "\" from cart: " + cartItems.items.toString())
      if (cartItems.isEmpty) context become emptyStage
    case StartCheckout =>
      println("Starting checkout for items:" + cartItems.items.toString())
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