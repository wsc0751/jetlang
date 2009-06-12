package scala.example

import actors.Actor
import java.lang._
import java.util.concurrent.CountDownLatch
import scala.actors._

object ScalaMain {

  val latch = new CountDownLatch(3)
  def decrementLatch(): Unit = {
    latch.countDown
  }

  def main(args: Array[String]): Unit = {
    // start the actors
    DownloadActor.start
    IndexActor.start
    WriteActor.start
    // seed the download actor with requests
    val start = System.currentTimeMillis
    for (i <- 1 until 500000) {
      DownloadActor ! Message(i, "Requested " + i)
    }
    // ask them to stop
    DownloadActor ! StopMessage
    // wait for actors to stop
    latch.await
    println("elapsed = " + (System.currentTimeMillis - start))
  }
}

case class Message(id:Int, payload:String)
case class StopMessage()

object DownloadActor extends Actor {
  def act() {
    while (true) {
      receive {
        case Message(id, payload) => {
          IndexActor !
            Message(id, payload.replaceFirst("Requested ", "Downloaded "))
        }
        case StopMessage => {
          IndexActor ! StopMessage
          ScalaMain.decrementLatch
          exit
        }
      }
    }
  }
}

object IndexActor extends Actor {
  def act() {
    while (true) {
      receive {
        case Message(id, payload) => {
          //println("Indexed " + id)
          WriteActor !
            Message(id, payload.replaceFirst("Downloaded ", "Indexed "))
        }
        case StopMessage => {
          //println("Stopping Index")
          WriteActor ! StopMessage
          ScalaMain.decrementLatch
          exit
        }
      }
    }
  }
}

object WriteActor extends Actor {
  def act() {
    while (true) {
      receive {
        case Message(id, payload) => {
          //println("Wrote " + id)
        }
        case StopMessage => {
          println("Stopping Write")
          ScalaMain.decrementLatch
          exit
        }
      }
    }
  }
}