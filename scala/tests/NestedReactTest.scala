import java.util.concurrent.{TimeUnit, CountDownLatch}
import jetlang.example.{JetlangThread, JetlangActor}
import org.scalatest.FunSuite

case class A()
case class B()
case class C()

class NestedActor extends JetlangActor with JetlangThread {
  val latch = new CountDownLatch(1)

  def react() = {
    case A => {
      react {
        case B => {
          react {
            case C => latch.countDown
          }
        }
      }
    }
  }
}

class NestedReactTest extends FunSuite {
  test("should handleNestedReacts") {
    val actor = new NestedActor()
    actor.start
    actor ! A
    actor ! B
    actor ! C

    assert(actor.latch.await(10, TimeUnit.SECONDS))
    actor.exit
  }
}