package actors

import akka.actor.{Actor, Timers}
import akka.event.LoggingReceive
import actors.Cart.StartCheckout

import scala.concurrent.duration._

object Checkout {

  sealed trait CheckoutCommand

  case object SelectDeliveryType extends CheckoutCommand

  case object SelectPayment extends CheckoutCommand

  case object ReceivePayment extends CheckoutCommand

  case object Cancel extends CheckoutCommand

}

case object CheckoutTimeout

case object PaymentTimeout


class CheckoutAggregator extends Actor with Timers {

  import Checkout._

  private var cart = CartItems(Set())

  override def receive: Receive = LoggingReceive {
    case StartCheckout(items) =>
      cart = items
      println("Checkout was started...")
      timers.startSingleTimer(TimeoutKey, CheckoutTimeout, 2.second)
      context become selectingDelivery
  }

  def selectingDelivery: Receive = LoggingReceive {
    case SelectDeliveryType =>
      println("Delivery type selected")
      context become selectingPaymentMethod
    case CheckoutTimeout =>
      println("Too long in StartCheckout!")
      context stop self
    case Cancel =>
      println("Cancelled!")
      context stop self
    case _ =>
      println("Unsupported operation on SelectingDelivery state!")
  }

  def selectingPaymentMethod: Receive = LoggingReceive {
    case SelectPayment =>
      println("Payment method selected")
      timers.startSingleTimer(TimeoutKey, PaymentTimeout, 2.second)
      context become processingPayment
    case CheckoutTimeout =>
      println("Too long in StartCheckout!")
      context stop self
    case Cancel =>
      println("Cancelled!")
      context stop self
    case _ =>
      println("Unsupported operation on SelectingPaymentMethod state!")
  }

  def processingPayment: Receive = LoggingReceive {
    case ReceivePayment =>
      println("Closing checkout process...")
      context.system.terminate()
    case PaymentTimeout =>
      println("Too long in ProcessingPayment!")
      context stop self
    case Cancel =>
      println("Cancelled!")
      context stop self
    case _ =>
      println("Unsupported operation on ProcessingPayment state!")
  }
}
