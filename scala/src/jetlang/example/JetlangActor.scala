package jetlang.example


import java.lang.Runnable
import java.util.concurrent.Executors
import org.jetlang.core.{RunnableExecutorImpl, RunnableExecutor, BatchExecutor}
import org.jetlang.fibers.{PoolFiberFactory, Fiber, ThreadFiber}
trait JetlangThread {

  def createFiber(callback: Any=> Unit ): Fiber = {
    new ThreadFiber(new RunnableExecutorImpl(new ActorExecutor(callback)), null, true)
  }

}

object ActorState {
  val current = new ThreadLocal[ReplyTo]()
}

class ReplyTo(actor: Any=> Unit) {
    def !(msg: Any): Unit = actor(msg)
}

class ActorExecutor(actor: Any => Unit) extends BatchExecutor {
  val replyTo = new ReplyTo(actor)

  def execute(commands: Array[Runnable]) = {
    ActorState.current.set(replyTo)
    for ( index <- 0 to commands.length -1)
      commands(index).run
    ActorState.current.set(null)
  }

}

object Pool {
  val executors = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors);
  private val fiberFactory = new PoolFiberFactory(executors)

  def create(callback: Any => Unit) : Fiber = {
    val e = new ActorExecutor(callback)
    fiberFactory.create(e);
  }
}

trait JetlangPooled {
  def createFiber(callback: Any => Unit) = Pool.create(callback)
}


trait JetlangActor {
  val root = react();
  var target = root
  val fiber = createFiber(receiveMsg)
  var sender :ReplyTo = null

  def createFiber(callback: Any => Unit) : Fiber

  def react(): PartialFunction[Any, Unit]

  def receive(newReact: PartialFunction[Any, Unit]): Unit = target = newReact

  def !(msg: Any): Unit = receiveMsg(msg)

  def receiveMsg(msg: Any): Unit = {
    val sentFrom = ActorState.current.get
    val runner = new Runnable() {
      def run() = {
        if (target.isDefinedAt(msg)) {
          sender = sentFrom
          val current = target;
          target = root;
          current(msg)
          sender = null;
        }
      }
    }
    fiber.execute(runner)
  }

  def start() = fiber.start

  def exit() = fiber.dispose
}