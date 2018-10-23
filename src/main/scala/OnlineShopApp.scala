import akka.actor.{ActorSystem, Props}
import actors.{Cart, CartAggregator, CartItems, Checkout, CheckoutAggregator}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object OnlineShopApp extends App {

  import Cart._
  import Checkout._

  val system = ActorSystem("OnlineShop")
  val cartActor = system.actorOf(Props[CartAggregator], "cartActor")
  val checkoutActor = system.actorOf(Props[CheckoutAggregator], "checkoutActor")
  val checkoutActor1 = system.actorOf(Props[CheckoutAggregator], "checkoutActor1")
  val checkoutActor2 = system.actorOf(Props[CheckoutAggregator], "checkoutActor2")

  println("---------------------CART-----------------------")
  cartActor ! AddItem("a")
  Thread.sleep(1100)

  cartActor ! AddItem("a")
  cartActor ! RemoveItem("a")
  cartActor ! StartCheckout
  cartActor ! AddItem("b")
  cartActor ! StartCheckout
  cartActor ! CloseCheckout
  cartActor ! RemoveItem("b")

  Thread.sleep(200)

  println("-----------------Checkout timeout-----------------------")
  checkoutActor1 ! StartCheckout(CartItems(Set("a")))
  checkoutActor1 ! SelectDeliveryType
  Thread.sleep(1100)

  println("------------------Payment timeout-----------------------")
  checkoutActor2 ! StartCheckout(CartItems(Set("a")))
  checkoutActor2 ! SelectDeliveryType
  checkoutActor2 ! SelectPayment
  Thread.sleep(1100)

  println("---------------------CHECKOUT-----------------------")
  checkoutActor ! StartCheckout(CartItems(Set("a")))
  checkoutActor ! SelectDeliveryType
  checkoutActor ! ReceivePayment
  checkoutActor ! SelectPayment
  checkoutActor ! ReceivePayment

  Await.result(system.whenTerminated, Duration.Inf)
}
