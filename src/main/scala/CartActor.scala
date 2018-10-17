import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

sealed trait Command

case class AddItem(id: String) extends Command

case class RemoveItem(id: String) extends Command

case class StartCheckout() extends Command

case class CancelCheckout() extends Command

case class CloseCheckout() extends Command


sealed trait Event

case class ItemAdded(id: String) extends Event

case class ItemRemoved(id: String) extends Event

case class CheckoutStarted() extends Event

case class CheckoutCanceled() extends Event

case class CheckoutClosed() extends Event


case class CartItems(var items: Set[String]) {

  def isEmpty: Boolean = items.isEmpty

  def addItem(item: String): Unit = items = items + item

  def removeItem(item: String): Unit = items -= item
}

class CartAggregator extends Actor {

  val cartItems = CartItems(Set())

  override def receive: Receive = emptyStage

  def emptyStage: Receive = LoggingReceive {
    case AddItem(id) =>
      cartItems.addItem(id)
      println("Added item \"" + id + "\" to cart: " + cartItems.items.toString())
      //sender ! ItemAdded(id)
      context become nonEmptyStage
    case _ =>
      println("Unsupported operation on Empty stage!")
  }

  def nonEmptyStage: Receive = LoggingReceive {
    case AddItem(id) =>
      cartItems.addItem(id)
      println("Added item \"" + id + "\" to cart: " + cartItems.items.toString())
      //sender ! ItemAdded(id)
    case RemoveItem(id) =>
      cartItems.removeItem(id)
      println("Removed item \"" + id + "\" from cart: " + cartItems.items.toString())
      //sender ! ItemRemoved(id)
      if (cartItems.isEmpty) context become emptyStage
    case StartCheckout =>
      //sender ! CheckoutStarted
      println("Starting checkout for items:" + cartItems.items.toString())
      context become inCheckoutStage
    case _ =>
      println("Unsupported operation on NonEmpty stage!")
  }

  def inCheckoutStage: Receive = LoggingReceive {
    case CancelCheckout =>
      //sender ! CheckoutCanceled
      println("Canceling checkout...")
      context become nonEmptyStage
    case CloseCheckout =>
      //sender ! CheckoutClosed
      println("Closing checkout...")
      context become emptyStage
    case _ =>
      println("Unsupported operation on InCheckout stage!")
  }
}