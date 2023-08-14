---
layout: default
title: SQL to Dataflow
---

# Demo 2: SQL to Dataflow



## Preparations

To run this demo, you will have to either have published a local snapshot of Portals to your local Maven repository, or have a local Docker image of Portals.

### Clone the Portals Repository

```bash
git clone https://github.com/portals-project/portals.git
cd portals
```

### Publishing Portals to Your local Maven Repository

To publish Portals to your local Maven repository, run the following command from the root of the Portals repository:

```bash
sbt publishLocal
```

### Building a Local Docker Image of Portals

To build a local Docker image of Portals, run the following command from the root of the Portals repository:

```bash
docker build -f scripts/deployment/docker/Dockerfile . -t portals
```

## Running the Demo

We provide three options for running the demo: locally, or with a client/server using SBT or using Docker.

### Local Execution with SBT

The local execution option is the simplest, and we can execute each of our examples using the following commands with SBT.

To start, naviage to the source code of this demonstration:

```bash
cd code
```

Then, run the examples. 

```bash
sbt "runMain portals.vldb2023demo.sqltodataflow.SQLToDataflowMain"
sbt "runMain portals.vldb2023demo.sqltodataflow.SQLToDataflowTxnMain"
sbt "runMain portals.vldb2023demo.sqltodataflow.SQLToRemoteDataflowMain"
```

### Server/Client Execution Using SBT

We can also run the demo using a client/server model.

To start, naviage to the source code of this demonstration:
  
```bash
cd code
```

First, start the Portals server:

```bash
# Start the Portals server
sbt "runMain portals.vldb2023demo.ServerMain localhost 8080"
```

Then, connect to the server using the Portals CLI:

```bash
# Submit the class files to the server
sbt "runMain portals.vldb2023demo.ClientMain submitDir --directory target/scala-3.3.0/classes"
# Launch the application
sbt "runMain portals.vldb2023demo.ClientMain launch --application portals.vldb2023demo.sqltodataflow.SQLToDataflow$"
```

### Server/Client Execution Using Docker

To start, naviage to the source code of this demonstration:

```bash
cd code
```

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