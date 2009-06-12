package jetlang.example


import org.jetlang.fibers.ThreadFiber

trait JetlangActor{

  val fiber = new ThreadFiber()

  def react() : PartialFunction[Any, Unit]

  def !(msg: Any) : Unit =  {
      val runner = new Runnable(){
         def run() = react()(msg)
      }
      fiber.execute(runner)
  }

  def start() = fiber.start
  def exit() = fiber.dispose
}