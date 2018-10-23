import actors.CartItems
import actorsFSM.{CartAggregatorFSM, CartFSM, CheckoutAggregatorFSM, CheckoutFSM}
import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object OnlineShopAppFSM extends App {

  import CartFSM._
  import CheckoutFSM._

  val system = ActorSystem("OnlineShop")
  val cartActor = system.actorOf(Props[CartAggregatorFSM], "cartActor")
  val checkoutActor = system.actorOf(Props[CheckoutAggregatorFSM], "checkoutActor")
  val checkoutActor1 = system.actorOf(Props[CheckoutAggregatorFSM], "checkoutActor1")
  val checkoutActor2 = system.actorOf(Props[CheckoutAggregatorFSM], "checkoutActor2")

  println("---------------------CART-----------------------")
  cartActor ! ItemAdded("a")
  Thread.sleep(1100)

  cartActor ! ItemAdded("a")
  cartActor ! ItemRemoved("a")
  cartActor ! CheckoutStarted
  cartActor ! ItemAdded("b")
  cartActor ! CheckoutStarted
  cartActor ! CheckoutClosed
  cartActor ! ItemRemoved("b")

  Thread.sleep(200)

  println("---------------------CHECKOUT-----------------------")
  checkoutActor ! CheckoutStarted(CartItems(Set("a")))
  checkoutActor ! DeliveryTypeSelected
  checkoutActor ! PaymentReceived
  checkoutActor ! PaymentSelected
  checkoutActor ! PaymentReceived

  Thread.sleep(500)

  println("-----------------Checkout timeout-----------------------")
  checkoutActor1 ! CheckoutStarted(CartItems(Set("a")))
  checkoutActor1 ! DeliveryTypeSelected
  Thread.sleep(1100)

  println("------------------Payment timeout-----------------------")
  checkoutActor2 ! CheckoutStarted(CartItems(Set("a")))
  checkoutActor2 ! DeliveryTypeSelected
  checkoutActor2 ! PaymentSelected
  Thread.sleep(1100)

  system.terminate()

  Await.result(system.whenTerminated, Duration.Inf)
}
