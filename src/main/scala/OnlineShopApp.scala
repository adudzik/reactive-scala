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

  cartActor ! AddItem("a")
  cartActor ! RemoveItem("a")
  cartActor ! StartCheckout
  cartActor ! AddItem("b")
  cartActor ! StartCheckout
  cartActor ! CloseCheckout
  cartActor ! RemoveItem("b")

  Thread.sleep(200)

  checkoutActor ! StartCheckout(CartItems(Set("a")))
  checkoutActor ! SelectDeliveryType
  checkoutActor ! ReceivePayment
  checkoutActor ! SelectPayment
  checkoutActor ! ReceivePayment

  Await.result(system.whenTerminated, Duration.Inf)
}
