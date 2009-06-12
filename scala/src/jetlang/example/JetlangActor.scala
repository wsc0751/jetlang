package jetlang.example


import org.jetlang.fibers.ThreadFiber

trait JetlangActor {
  val fiber = new ThreadFiber()

  def react(): PartialFunction[Any, Unit]

  def !(msg: Any): Unit = {
    val target = react();
    if (target.isDefinedAt(msg)) {
      val runner = new Runnable() {
        def run() = target(msg)
      }
      fiber.execute(runner)
    }
  }

  def start() = fiber.start

  def exit() = fiber.dispose
}