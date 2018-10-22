package actorsFSM

import actorsFSM.CartFSM.{CartData, CartState}

object CartFSM {

  sealed trait CartState

  case object Empty extends CartState

  case object NonEmpty extends CartState

  case object InCheckout extends CartState


  sealed trait CartData

  case object Buying extends CartData

  case object Checkouting extends CartData

}

class CartAggregatorFSM extends FSM[CartState, CartData] {

}
