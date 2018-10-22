package actorsFSM

import actorsFSM.CheckoutFSM.{CheckoutData, CheckoutState}
import akka.actor.FSM

object CheckoutFSM {

  sealed trait CheckoutState

  case object SelectingDelivery extends CheckoutState

  case object SelectingPaymentMethod extends CheckoutState

  case object ProcessingPayment extends CheckoutState


  sealed trait CheckoutData

  case object Uninitialized extends CheckoutData

  case object Delivering extends CheckoutData

  case object Paying extends CheckoutData

}

class CheckoutAggregatorFSM extends FSM[CheckoutState, CheckoutData] {

}


