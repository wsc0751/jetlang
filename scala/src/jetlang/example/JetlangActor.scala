package jetlang.example


import java.lang.Runnable
import java.util.ArrayList
import java.util.concurrent.Executors
import org.jetlang.core.{RunnableExecutorImpl, RunnableExecutor, BatchExecutor}
import org.jetlang.fibers.{PoolFiberFactory, Fiber, ThreadFiber}

trait JetlangThread {
  def createFiber(callback: Any => Unit): Fiber = {
    new ThreadFiber(new RunnableExecutorImpl(new ActorExecutor(callback)), null, true)
  }

}

object ActorState {
  val current = new ThreadLocal[ReplyTo]()
}

class ReplyTo(actor: Any => Unit) {
  def !(msg: Any): Unit = actor(msg)
}

class ActorExecutor(actor: Any => Unit) extends BatchExecutor {
  val replyTo = new ReplyTo(actor)

  def execute(commands: Array[Runnable]) = {
    ActorState.current.set(replyTo)
    for (index <- 0 to commands.length - 1)
      commands(index).run
    ActorState.current.set(null)
  }

}

object Pool {
  val executors = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors);
  private val fiberFactory = new PoolFiberFactory(executors)

  def create(callback: Any => Unit): Fiber = {
    val e = new ActorExecutor(callback)
    fiberFactory.create(e);
  }

  def shutdown(): Unit = executors.shutdown
}

trait JetlangPooled {
  def createFiber(callback: Any => Unit) = Pool.create(callback)
}

class Pending(msg: Any, sender: ReplyTo) {
  val message = msg
  val replyTo = sender

  override def toString(): String = msg.toString
}

trait JetlangActor {
  val root = act();
  var target = root
  val fiber = createFiber(receiveMsg)
  val pending = new ArrayList[Pending]
  var sender: ReplyTo = null

  def createFiber(callback: Any => Unit): Fiber

  def act(): PartialFunction[Any, Unit]

  def receive(newReact: PartialFunction[Any, Unit]): Unit = target = newReact

  def !(msg: Any): Unit = receiveMsg(msg)

  def receiveMsg(msg: Any): Unit = {
    val sentFrom = ActorState.current.get
    val runner = new Runnable() {
      def run() = {
        if (applyMsg(msg, sentFrom)) {
          var found = -1
          do {
            found = pendingMatch
            if (found >= 0)
              pending.remove(found)
          } while (found >= 0)
        }
        else {
          pending.add(new Pending(msg, sentFrom))
        }
      }
    }
    fiber.execute(runner)
  }

  def applyMsg(msg: Any, replyTo: ReplyTo): Boolean = {
    if (target.isDefinedAt(msg)) {
      sender = replyTo
      val current = target;
      target = root;
      current(msg)
      sender = null;
      return true
    }
    return false
  }

  def pendingMatch(): Int = {
    for (index <- 0 to pending.size - 1) {
      val p = pending.get(index)
      if (applyMsg(p.message, p.replyTo))
        return index
    }
    return -1
  }

  def start() = fiber.start

  def exit() = fiber.dispose
}