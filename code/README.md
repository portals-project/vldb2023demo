# Source Code for the VLDB 2023 Demonstration

This repository contains the source code for the VLDB 2023 demonstration paper. More information can be found at [https://portals-project.github.io/vldb2023demo/](https://portals-project.github.io/vldb2023demo/).

Follow these steps to run the demo.

> > **Note:** 
> The portals dockerfile must be built from the `remote-sql-merge` branch, in order to include the Portals SQL library.

<!--
================================================================================ 
== PREREQUISITES 
================================================================================
-->

## Prerequisites

To run this demo, you will have to either have published a local snapshot of Portals to your local Maven repository, or have a local Docker image of Portals.

##### SBT/Docker

Install `sbt` and/or `docker`.

##### Clone the Portals Repository

```bash
git clone https://github.com/portals-project/portals.git
cd portals
```

##### Publish Portals to Your local Maven Repository

To publish Portals to your local Maven repository, run the following command from the root of the Portals repository:

```bash
sbt publishLocal
```

##### Build a Local Docker Image of Portals

To build a local Docker image of Portals, run the following command from the root of the Portals repository:

```bash
docker build -f scripts/deployment/docker/Dockerfile . -t portals
```

##### Build a Local Docker Image of the VLDB 2023 Demo

In the root of this (`code`) repository, run the following command:

```bash
docker build . -t vldb2023demo
```

##### Navigate to the Source Code of this Demonstration

```bash
cd code
```

<!--
================================================================================ 
== Hello VLDB 
================================================================================
-->

## Hello VLDB

To test that the setup is working, run the `Hello VLDB` example.

```bash
# Run it locally
sbt "runMain portals.vldb2023demo.HelloVLDBMain"
# Or, start a server/client
sbt "runMain portals.vldb2023demo.ServerMain localhost 8080"
sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes --ip localhost --port 8080"
sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.HelloVLDB$ --ip localhost --port 8080"
# Or, run it using Docker
docker run --rm -it -p 8080:8080 vldb2023demo sbt "runMain portals.vldb2023demo.ServerMain 0.0.0.0 8080"
docker run --rm -it vldb2023demo sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes --ip host.docker.internal --port 8080"
docker run --rm -it vldb2023demo sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.HelloVLDB$ --ip host.docker.internal --port 8080"
```

We provide three options for running the demo:
* Locally, with the Test runtime using SBT.
* With a client/server using SBT.
* With a client/server using Docker.

In the following descriptions, we will be running the examples in a client/server model using SBT.

<!--
================================================================================ 
== DEMO SCENARIO 1: SHOPPING CART 
================================================================================
-->

## Demo 1: Shopping Cart


In addition to this, there is an option to run the demo using Kubernetes (see [Tutorial Helper Script](#tutorial-helper-script)).

##### Local Execution with SBT

The local execution option is the simplest option. We can execute each of our examples using the following commands with SBT. We have some additional options detailed at the end of this file, that may not be up to date.

```bash
sbt "runMain portals.vldb2023demo.shoppingcart.ShoppingCartLocalMain";
```

##### Server/Client Execution Using SBT

We can also run the demo using a client/server model.

Start the server.

```bash
sbt "runMain portals.vldb2023demo.ServerMain localhost 8080";
```

Then, connect to the server using the Portals CLI:

```bash
# Submit the class files to the server
sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes --ip localhost --port 8080";
# Launch the application
sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Inventory$ --ip localhost --port 8080";
sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Cart$ --ip localhost --port 8080";
sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Orders$ --ip localhost --port 8080";
sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Analytics$ --ip localhost --port 8080";
sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.dynamic.ShoppingCartProxy$ --ip localhost --port 8080";
```

Then, you can modify the DynamicQuery app, which may access the inventory, or analytics service, from a new runtime.

```bash
# Start the second remote runtime, and send requests to the inventory/analytics service
sbt "runMain portals.vldb2023demo.shoppingcart.dynamic.DynamicQuery";
```

<!-- 
================================================================================ 
== DEMO SCENARIO 2: SQL-TO-DATAFLOW
================================================================================
-->

## Demo 2: SQL-to-Dataflow

##### Local Execution with SBT

The local execution option is the simplest option. We can execute each of our examples using the following commands with SBT.

```bash
sbt "runMain portals.vldb2023demo.sqltodataflow.SQLToDataflowMain";
sbt "runMain portals.vldb2023demo.sqltodataflow.SQLToDataflowTxnMain";
sbt "runMain portals.vldb2023demo.sqltodataflow.SQLToRemoteDataflowMain";
```

##### Server/Client Execution Using SBT

We can also run the demo using a client/server model.


First, start the Portals server:

```bash
# Start the Portals server
sbt "runMain portals.vldb2023demo.ServerMain localhost 8080";
```

Then, connect to the server using the Portals CLI:

```bash
# Submit the class files to the server
sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes --ip localhost --port 8080";
# Launch the application
sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.sqltodataflow.SQLToRemoteDataflowTableApp$ --ip localhost --port 8080";
```

Then, you can modify the DynamicQuery app, which can send SQL queries on-demand to the running KV Tables.

```bash
# Start a new runtime, and send requests to the query engine
sbt "runMain portals.vldb2023demo.sqltodataflow.dynamic.DynamicQuery";
```

<!-- 
================================================================================ 
== DEMO SCENARIO 3: PLAYGROUND
================================================================================
-->

## Demo Scenario 3: Playground

The Playground is available at [https://portals-project.org/playground/](https://portals-project.org/playground/).

<!-- 
================================================================================
== TUTORIAL HELPER SCRIPT
================================================================================
-->

## Tutorial Helper Script

##### Introduction

This script is designed to assist with the installation and setup of the VLDB 2023 tutorial environment. It provides several options to manage the tutorial's installation, configuration, and execution processes.

##### Notes

- The script is tested on Ubuntu 20.04 LTS. Running it on other platforms may require adjustments.
- The installation of required packages is experimental and requires root access. Use it with caution.
- The script uses Minikube as a local Kubernetes replacement.

##### Download

You can find the script at [vldb-helper-shopping-cart.shipt](vldb-helper-shopping-cart.sh).

##### Usage

To use the script, place it into the root directory of the portals repository and execute it from the command line with one of the following options:

./vldb-helper-shopping-cart.sh OPTION


Replace `OPTION` with one of the following:

- `-help` or `-h`: Display help and usage instructions.
- `-install` or `-i`: Install the required packages (requires root access, experimental).
- `-setup` or `-s`: Setup the environment (start Minikube, build and load Docker images).
- `-run_example` or `-r`: Run the example (start the portals server and shopping cart client).
- `-all` or `-a`: Setup the environment and run the example (does not install required packages) and perform the teardown afterward.
- `-teardown` or `-t`: Teardown the example (delete the portals server deployment).

##### Options

###### -help / -h

This option displays help and usage instructions for the script.

###### -install / -i

Use this option to install the required packages. Note that this process requires root access and is experimental. The script will check for and install the following packages if they are not already installed:

- Docker
- Kubernetes (kubectl)
- Minikube
- SBT (Scala Build Tool)
- Scala

###### -setup / -s

This option sets up the environment for the VLDB 2023 tutorial. It includes the following steps:

1. Starting Minikube (if not already running).
2. Setting up the Minikube Docker environment.
3. Building and loading the required Docker images for the tutorial.

###### -run_example / -r

Use this option to run the example of the VLDB 2023 tutorial. It performs the following tasks:

1. Starts the portals server using the Kubernetes deployment defined in `scripts/deployment/Kubernetes/Deployment.yaml`.
2. Waits for the server pod to be ready.
3. Sets up port forwarding to the portals server.
4. Starts the shopping cart client and logs its output to `client_output.log`.
5. Stops the port forwarding after the client completes its execution.
6. Prints the server log to `output_server.log`.

###### -all / -a

This option combines the setup and run_example options without installing the required packages. It sets up the environment and runs the tutorial example. Afterward, it performs the teardown by deleting the portals server deployment.

###### -teardown / -t

Use this option to tear down the tutorial example. It deletes the portals server deployment, effectively removing the running example.

## Additional Options, Potentially Not Up to Date

##### Shopping Cart Server/Client Execution Using Docker

In order to launch this example using Docker, we will need to build a local Docker image of Portals which also contains this app.

```bash
docker build . -t vldb2023demo
```

Then, we can launch the server:

```bash
docker run --rm -it -p 8080:8080 vldb2023demo sbt "runMain portals.vldb2023demo.ServerMain 0.0.0.0 8080"
```

Followed by the clients:

```bash
docker run --rm -it vldb2023demo sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes --ip host.docker.internal --port 8080"
docker run --rm -it vldb2023demo sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Inventory$ --ip host.docker.internal --port 8080"
docker run --rm -it vldb2023demo sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Cart$ --ip host.docker.internal --port 8080"
docker run --rm -it vldb2023demo sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Orders$ --ip host.docker.internal --port 8080"
docker run --rm -it vldb2023demo sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.Analytics$ --ip host.docker.internal --port 8080"
docker run --rm -it vldb2023demo sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.shoppingcart.dynamic.ShoppingCartProxy$ --ip host.docker.internal --port 8080"
```

##### SQL Server/Client Execution Using Docker

In order to launch this example using Docker, we will need to build a local Docker image of Portals which also contains this app.

```bash
docker build . -t vldb2023demo
```

Then, we can launch the server:

```bash
docker run --rm -it -p 8080:8080 vldb2023demo sbt "runMain portals.vldb2023demo.ServerMain 0.0.0.0 8080"
```

And, we can launch the client:

```bash
docker run --rm -it vldb2023demo sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes --ip host.docker.internal --port 8080"
docker run --rm -it vldb2023demo sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.sqltodataflow.SQLToDataflow$ --ip host.docker.internal --port 8080"
```
