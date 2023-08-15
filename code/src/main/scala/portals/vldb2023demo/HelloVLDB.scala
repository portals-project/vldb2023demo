package portals.vldb2023demo

import portals.api.dsl.DSL.*
import portals.application.Application
import portals.distributed.SubmittableApplication
import portals.system.Systems
import portals.vldb2023demo.Util.*

/** A simple application that prints "Hello VLDB" to the log.
  *
  * Submit this application to a runtime, either locally (see
  * [[HelloVLDBMain]]), or to a distributed/remote execution (see examples
  * below).
  *
  * @example
  *   Run it locally
  *   {{{
  * sbt "runMain portals.vldb2023demo.HelloVLDBMain"
  *   }}}
  *
  * @example
  *   Submit it to a server
  *   {{{
  * // start the server
  * sbt "runMain portals.vldb2023demo.ServerMain"
  * // submit the application classfiles
  * sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes"
  * // launch the application
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.HelloVLDB$"
  *   }}}
  */
object HelloVLDB extends SubmittableApplication:
  override def apply(): Application =
    PortalsApp("Hello VLDB"):
      val message = "Hello VLDB"

      val generator = Generators.fromList(List(message))

      val workflow = Workflows[String, String]("workflow")
        .source(generator.stream)
        .logger("before: ")
        .map(_.reverse)
        .logger("after:  ")
        .sink()
        .freeze()

/** Main method for running the HelloVLDB example locally using the test
  * runtime.
  */
object HelloVLDBMain:
  def main(args: Array[String]): Unit =
    val system = Systems.test()
    system.launch(HelloVLDB())
    system.stepFor(2_000) // step for maximum 2 seconds
    // system.stepUntilComplete() // alternatively, step until complete
    system.shutdown()
