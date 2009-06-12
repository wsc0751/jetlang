import actors.Actor
import example.Stop
import java.util.concurrent.{TimeUnit, CountDownLatch}
import jetlang.example.{JetlangPooled, JetlangThread, JetlangActor}
import org.scalatest.FunSuite

class NestedScalaActor extends Actor {
  val latch = new CountDownLatch(2)

  def act() = {
    while (true) {
      println("looping")
      receive {
        case Stop => exit
        case A => {
          println("stufffff ")
          receive {
            case B => {
              println("B sfasfsd")
              receive {
                case C => {
                  println("C")
                    latch.countDown
                }
              }
            }
          }
        }
      }
    }
  }
}

class NestedOutOfOrderScalaTest extends FunSuite {
  test("should handleOutOfOrderNestedReacts") {
    val actor = new NestedScalaActor()
    actor.start
    actor ! A
    actor ! B
    actor ! A
    actor ! B
    actor ! C
    actor ! C

    assert(actor.latch.await(10, TimeUnit.SECONDS))
    actor ! Stop
    println("done")
  }
}