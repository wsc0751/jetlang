package scala.example


import actors.Actor
import java.util.concurrent.CountDownLatch

object ScalaPing {
  val latch = new CountDownLatch(1)


  def decrementLatch(): Unit = {
    latch.countDown
  }

  def main(args: Array[String]): Unit = {
    // start the actors
    val start = System.currentTimeMillis;
    PingActor.start
    PongActor.start
    PingActor ! Ping
    latch.await
    println("elapsed = " + (System.currentTimeMillis - start))
    PingActor ! Stop
    PongActor ! Stop
  }
}

case class Ping()
case class Pong()
case class Stop()

object PingActor extends Actor {
  def act() {
    while (true) {
      receive {
        case Ping => {
          PongActor ! Pong
        }
        case Stop => {
          exit
        }
      }
    }
  }
}

object PongActor extends Actor {
  var count = 0

  def act() {
    while (true) {
      receive {
        case Pong => {
          count = count + 1;
          if (count == 1000000)
            ScalaPing.decrementLatch
          else
            PingActor ! Ping
        }
        case Stop => {
          exit
        }
      }
    }
  }
}
