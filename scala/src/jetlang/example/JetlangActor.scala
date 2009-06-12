package jetlang.example


import org.jetlang.fibers.ThreadFiber

trait JetlangActor {
  val fiber = new ThreadFiber()
  val target = react()

  def react(): PartialFunction[Any, Unit]

  def !(msg: Any): Unit = {
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