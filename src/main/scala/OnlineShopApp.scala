import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object OnlineShopApp extends App {
  val system = ActorSystem("OnlineShop")
  val cartActor = system.actorOf(Props[CartAggregator], "cartActor")
  val checkoutActor = system.actorOf(Props(new CheckoutAggregator(CartItems(Set("a")))), "checkoutActor")

  cartActor ! AddItem("a")
  cartActor ! RemoveItem("a")
  cartActor ! StartCheckout
  cartActor ! AddItem("b")
  cartActor ! StartCheckout
  cartActor ! CloseCheckout
  cartActor ! RemoveItem("b")

  checkoutActor ! StartCheckout
  checkoutActor ! SelectDeliveryType
  checkoutActor ! ReceivePayment
  checkoutActor ! SelectPayment
  checkoutActor ! ReceivePayment

  Await.result(system.whenTerminated, Duration.Inf)
}
