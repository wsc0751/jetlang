package jetlang.example


import org.jetlang.fibers.ThreadFiber

trait JetlangActor {
  val fiber = new ThreadFiber()
  val root = react();
  var target = root

  def react(): PartialFunction[Any, Unit]

  def react(newReact: PartialFunction[Any, Unit]): Unit = {
    target = newReact
  }

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