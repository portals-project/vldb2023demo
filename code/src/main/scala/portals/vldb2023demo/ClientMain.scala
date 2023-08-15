package portals.vldb2023demo

/** Command line interface for the client based on the
  * `portals.distributed.ClientCLI`.
  *
  * Subcommands:
  *   - `--help`: Display the help message.
  *   - `submit <classfile> [--directory <directory>] [--ip <ip>] [--port
  *     <port>]`: Submit the classfile.
  *   - `submitDir <directory> [--ip <ip>] [--port <port>]`: Submit all the
  *     classfiles from the directory.
  *   - `launch <application (String)> [--ip <ip>] [--port <port>]`: Launch the
  *     application.
  *
  * Note: connects to the server on the specified ip and port.
  *
  * Note: there have been issues with submitting applications that are nested
  * within other classes/objects.
  *
  * Note: the submitted application must be of type `SubmitttableApplication`.
  *
  * @example
  *   Display the help message to see all available commands.
  *   {{{
  * sbt "runMain portals.vldb2023demo.ClientMain --help"
  *   }}}
  *
  * @example
  *   Submit the classfile `MyApp.class` to the server running on `localhost`.
  *   {{{
  * // Submit the class files
  * sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes --ip localhost --port 8080"
  * // Launch the application
  * sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.sqltodataflow.SQLToDataflow$ --ip localhost --port 8080"
  *   }}}
  */
object ClientMain:
  def main(args: Array[String]): Unit =
    portals.distributed.ClientCLI.main(args)
