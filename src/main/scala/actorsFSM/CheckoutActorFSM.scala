package actorsFSM

import actors.CartItems
import actorsFSM.CartFSM.CheckoutStarted
import actorsFSM.CheckoutFSM.{CheckoutData, CheckoutState}
import akka.actor.FSM

import scala.concurrent.duration._

object CheckoutFSM {

  sealed trait CheckoutState

  case object Starting extends CheckoutState

  case object SelectingDelivery extends CheckoutState

  case object SelectingPaymentMethod extends CheckoutState

  case object ProcessingPayment extends CheckoutState


  sealed trait CheckoutData

  case object Uninitialized extends CheckoutData

  case class Active(items: CartItems) extends CheckoutData

  case object Delivering extends CheckoutData

  case object Paying extends CheckoutData


  sealed trait CheckoutEvent

  case object DeliveryTypeSelected extends CheckoutEvent

  case object PaymentSelected extends CheckoutEvent

  case object PaymentReceived extends CheckoutEvent

  case object Cancelled extends CheckoutEvent

}

class CheckoutAggregatorFSM extends FSM[CheckoutState, CheckoutData] {

  import CheckoutFSM._

  startWith(Starting, Uninitialized)

  when(Starting) {
    case Event(CheckoutStarted(items), Uninitialized) =>
      println("Checkout was started...")
      goto(SelectingDelivery) using Active(items)
  }

  when(SelectingDelivery, stateTimeout = 1.second) {
    case Event(DeliveryTypeSelected, Active(_)) =>
      println("Delivery type selected")
      goto(SelectingPaymentMethod) using Delivering
  }

  when(SelectingPaymentMethod, stateTimeout = 1.second) {
    case Event(PaymentSelected, Delivering) =>
      println("Payment method selected")
      goto(ProcessingPayment) using Paying
  }

  when(ProcessingPayment, stateTimeout = 1.second) {
    case Event(PaymentReceived, Paying) =>
      println("Closing checkout process...")
      stop
  }

  whenUnhandled {
    case Event(StateTimeout, s) =>
      println("Too long in state: %s!".format(s))
      stop
    case Event(Cancelled, _) =>
      println("Cancelled!")
      stop
    case Event(e, s) =>
      println("Unsupported request %s on %s/%s state!".format(e, stateName, s))
      stay
  }

  initialize()
}


