import akka.actor.Actor
import akka.event.LoggingReceive

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


class CheckoutAggregator extends Actor {
  override def receive: Receive = selectingDelivery

  def selectingDelivery: Receive = LoggingReceive {
    case SelectDeliveryType =>
      println("Delivery type selected")
      context become selectingPaymentMethod
    case Cancel =>
      println("Cancelled!")
      context stop self
    case _ =>
      println("Unsupported operation on SelectingDelivery state!")
  }

  def selectingPaymentMethod: Receive = LoggingReceive {
    case SelectPayment =>
      println("Payment method selected")
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
    case Cancel =>
      println("Cancelled!")
      context stop self
    case _ =>
      println("Unsupported operation on ProcessingPayment state!")
  }
}
