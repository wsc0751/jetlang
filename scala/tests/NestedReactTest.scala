import java.util.concurrent.{TimeUnit, CountDownLatch}
import jetlang.example.{JetlangPooled, JetlangThread, JetlangActor}
import org.scalatest.FunSuite

case class A()
case class B()
case class C()

class NestedActor extends JetlangActor with JetlangThread {
  val latch = new CountDownLatch(1)

  def react() = {
    case A => {
      receive {
        case B => {
          receive {
            case C => latch.countDown
          }
        }
      }
    }
  }
}

class Reply extends JetlangActor with JetlangThread {
  def react() = {
    case B => sender ! C
  }
}

class Starter(replyActor: Reply ) extends JetlangActor with JetlangThread {
  val latch = new CountDownLatch(1)

  def react() = {
    case A => replyActor ! B
    case C => latch.countDown
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

  test("should reply to sender") {
    val reply = new Reply()
    val starter = new Starter(reply)
    reply.start
    starter.start
    starter ! A
    assert(starter.latch.await(10, TimeUnit.SECONDS))
    starter.exit
    reply.exit
  }
}