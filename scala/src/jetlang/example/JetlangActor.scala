package jetlang.example


import java.util.concurrent.Executors
import org.jetlang.fibers.{PoolFiberFactory, Fiber, ThreadFiber}
trait JetlangThread {
  val f = new ThreadFiber()

  def fiber(): Fiber = f

}

object Pool {
  val executors = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors);
  val fiberFactory = new PoolFiberFactory(executors)
}

trait JetlangPooled {
  val f = Pool.fiberFactory.create

  def fiber(): Fiber = f
}


trait JetlangActor {
  val root = react();
  var target = root

  def fiber() : Fiber

  def react(): PartialFunction[Any, Unit]

  def react(newReact: PartialFunction[Any, Unit]): Unit = target = newReact

  def !(msg: Any): Unit = {
    val runner = new Runnable() {
      def run() = {
        if (target.isDefinedAt(msg)) {
          val current = target;
          target = root;
          current(msg)
        }
      }
    }
    fiber.execute(runner)
  }

  def start() = fiber.start

  def exit() = fiber.dispose
}