import akka.actor.{Actor, Timers}
import akka.event.LoggingReceive

import scala.concurrent.duration._

sealed trait CheckoutCommand

case class SelectDeliveryType() extends CheckoutCommand

case class SelectPayment() extends CheckoutCommand

case class ReceivePayment() extends CheckoutCommand

case class Cancel() extends CheckoutCommand


sealed trait CheckoutEvent

case class DeliveryTypeSelected() extends CheckoutEvent

case class PaymentSelected() extends CheckoutEvent

case class PaymentReceived() extends CheckoutEvent

case class Cancelled() extends CheckoutEvent


case object CheckoutTimeout

case object PaymentTimeout


class CheckoutAggregator(cart: CartItems) extends Actor with Timers {
  override def receive: Receive = LoggingReceive {
    case StartCheckout =>
      if (!cart.isEmpty) {
        println("Checkout was started...")
        timers.startSingleTimer(TimeoutKey, CheckoutTimeout, 2.second)
        context become selectingDelivery
      }
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
