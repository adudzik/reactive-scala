import actors.CartItems
import actorsFSM.{CartAggregatorFSM, CartFSM, CheckoutAggregatorFSM, CheckoutFSM}
import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object OnlineShopAppFSM extends App{
  import CartFSM._
  import CheckoutFSM._

  val system = ActorSystem("OnlineShop")
  val cartActor = system.actorOf(Props[CartAggregatorFSM], "cartActor")
  val checkoutActor = system.actorOf(Props[CheckoutAggregatorFSM], "checkoutActor")

  cartActor ! ItemAdded("a")
  Thread.sleep(2100)

  cartActor ! ItemAdded("a")
  cartActor ! ItemRemoved("a")
  cartActor ! CheckoutStarted
  cartActor ! ItemAdded("b")
  cartActor ! CheckoutStarted
  cartActor ! CheckoutClosed
  cartActor ! ItemRemoved("b")

  Thread.sleep(200)

  checkoutActor ! CheckoutStarted(CartItems(Set("a")))
  checkoutActor ! DeliveryTypeSelected
  checkoutActor ! PaymentReceived
  checkoutActor ! PaymentSelected
  checkoutActor ! PaymentReceived

  Await.result(system.whenTerminated, Duration.Inf)
}
