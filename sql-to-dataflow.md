---
layout: default
title: "Demo 2: SQL to Dataflow"
---

# Demo 2: SQL to Dataflow
The second scenario demonstrates a serverless application that runs a key-value store dataflow accessible through a portal with an SQL interface. The application shows how interesting abstractions (the SQL interface) can be built on top of the portal abstraction, leveraging the underlying guarantees and execution runtime. The example both exposes the SQL interface as a service using the Portal service abstraction, and uses portal services internally for implementation of the SQL evaluation strategy and key-value stores.

The demo consists of two main portals applications: a key-value store dataflow; and a query dataflow. 
The key-value store dataflow is also called the `Table Workflow`. It implements a key-value store that is exposed through a Portal service. The table workflow needs to manage the state transactionally; we have implement a simple 2PL for transactional workloads. 
The query dataflow, also called the `Query Workflow`, implements a simple SQL interface, and translates SQL queries into requests to the table workflow. That is, the query workflow consumes SQL queries (either as a stream or through portal requests), and sends corresponding requests to the table workflows to retrieve the data and produce the result.

The queries, support queries over multiple tables. However, they do not support range queries (for the moment). The SQL engine used by the SQL library is **[Apache Calcite](https://calcite.apache.org/)**.

The SQL interface can be leveraged for decentralized executions, in which edge Portals applications may connect to cloud-based key-value stores through the Portal service interface (for example, as an SQL interface), to query and update the state of the key-value store. This way, many edge runtimes could conveniently connect to the same key-value store. Due to the underlying guarantees, this would still guarantee exactly-once processing semantics, spanning over edge devices to serverless cloud instances.

More information on the SQL interface is available in a student thesis (the presented work in this demo is a continuation of this thesis):
* Huang C. Queryable Workflows: Extending Dataflow Streaming with Dynamic Request/Reply Communication [Internet] [Dissertation]. 2023. (TRITA-EECS-EX). Available from: [https://urn.kb.se/resolve?urn=urn:nbn:se:kth:diva-329594](https://urn.kb.se/resolve?urn=urn:nbn:se:kth:diva-329594)

### Demo Experience

This demo comes in three flavours: `SQLToDataflow.scala`, `SQLToDataflowTxn.scala`, and `SQLToRemoteDataflow.scala`.

#### SQLToDataflow

The simplest version is `SQLToDataflow.scala`, which runs both the table and the query workflow in the same Portals Application.

```scala
/** Portals application which runs the queriable KV Table. */
val tableApp = PortalsApp("SQLToDataflowTable"):

  /** A Table Workflow which serves SQL queries for the table of type KV. */
  val table = TableWorkflow[Types.KV]("KVTable", "k")

  /** Input queries for the Query task to execute. */
  val generator = Generators.generator[String](queriesGenerator)

  /** Workflow which consumes the generated queries and runs query task. */
  val queryWorkflow = Workflows[String, String]("queryWorkflow")
    .source(generator.stream)
    .logger("Query:   ")
    /** A query task which connects to the `table`. */
    .query(table)
    .logger("Results: ")
    .sink()
    .freeze()
```

The example consists of a table workflow, a generated stream of queries, and the query workflow. The query workflow consumes this generated stream of queries (these are insert and select queries), and in turn sends corresponding requests to the table workflow. In essence, the query workflow fetches the data from the table workflow, and processes the data in order to produce the query result.

#### SQLToDataflowTxn

In addition to this, we show how to also perform transactional queries (SQLToDataflowTxn), and how to perform queries to remote tables (SQLToRemoteDataflow, advanced). Not shown in the examples, is how to connect to multiple tables, in order to join inforation from multiple tables.

```scala
/** Portals application which runs the queriable KV Table. */
val tableApp = PortalsApp("SQLToDataflowTable"):

  /** A Table Workflow which serves SQL queries for the table of type KV. */
  val table = TableWorkflow[Types.KV]("KVTable", "k", true)

  /** Transactional input queries for the Query task to execute. */
  val generator1 = Generators.generator[TxnQuery](Data.transactionalQueriesGenerator)

  /** More input queries for another query task. */
  val generator2 = Generators.generator[TxnQuery](Data.transactionalQueriesGenerator)

  /** Workflow which consumes the generated queries and runs query task. */
  val queryWorkflow1 = Workflows[TxnQuery, String]("queryWorkflow1")
    .source(generator1.stream)
    /** A query task which connects to the `table`. */
    .logger("Query  1: ")
    .queryTxn(table)
    .logger("Result 1: ")
    .sink()
    .freeze()

  /** Workflow which consumes the generated queries and runs query task. */
  val queryWorkflow2 = Workflows[TxnQuery, String]("queryWorkflow2")
    .source(generator2.stream)
    /** Another query task which connects to the `table`. */
    .logger("Query  2: ")
    .queryTxn(table)
    .logger("Result 2: ")
    .sink()
    .freeze()
```

The transactional example only has a slight change: it now has two query workflows connecting to the table workflow with the transactional mode enabled (`queryTxn`).

#### SQLToRemoteDataflow

```scala
/** Portals application which runs the queriable KV Table. */
PortalsApp("SQLToDataflowTable"):
  /** A Table (Portal) for queries of type `KV`. */
  val table = Table[Types.KV]("KVTable", "k")

  /** A Table Workflow which serves the queries of the Table Portal. */
  val tableWorkflow = Workflows[Nothing, Nothing]()
    .source(Generators.empty[Nothing].stream)
    .table[Types.KV](table)
    .sink()
    .freeze()

  /** A Query Portal which exposes the Table Portal. */
  val queryPortal = QueryPortal("queryPortal", table.ref)

/** Remote Portals applications which queries the Query Portal. */
PortalsApp("SQLToDataflowRemote"):
  /** Get a reference to the Query Portal from the Registry. */
  val queryPortal = Registry.portals.get[String, String]("/SQLToDataflowTable/portals/queryPortal")

  /** Input queries for the Query task to execute. */
  val generator = Generators.generator[String](Data.queriesGenerator)

  /** Workflow which sends the consumed SQL requests to the query portal.
    */
  val queryWorkflow = Workflows[String, String]("queryWorkflow")
    .source(generator.stream)
    .logger("Query:  ")
    .asker(queryPortal) { x =>
      // ask the query portal to execute the query
      val f = ask(queryPortal)(x)
      await(f) {
        // log the results
        ctx.log.info(s"== Query: ${x}; Result: ${f.value.get} ==")
        // emit the result of the query
        emit(f.value.get)
      }
    }
    .logger("Results: ")
    .sink()
    .freeze()
```

The remote example shows how to connect to a remote Table Workflow, as well as a remote Query workflow. This example has a query portal which manages the query workflow. In addition to this, it ahs a `queryWorkflow`, which connects to the query portal. This lets us inspect how an application would connect to the query portal: we can send a query to the query portal (`ask(queryPortal)(x)`), and await its result to, here the result is printed (`ctx.log.info(s"== Query: ${x}; Result: ${f.value.get} ==")`) and emitted.

## Executing the Demo

Follow these steps to run the demo.

### Preparations

To run this demo, you will have to either have published a local snapshot of Portals to your local Maven repository, or have a local Docker image of Portals.

#### Clone the Portals Repository

```bash
git clone https://github.com/portals-project/portals.git
cd portals
```

#### Publishing Portals to Your local Maven Repository

To publish Portals to your local Maven repository, run the following command from the root of the Portals repository:

```bash
sbt publishLocal
```

#### Building a Local Docker Image of Portals

To build a local Docker image of Portals, run the following command from the root of the Portals repository:

```bash
docker build -f scripts/deployment/docker/Dockerfile . -t portals
```

### Running the Demo

We provide three options for running the demo: locally, or with a client/server using SBT or using Docker.

#### Local Execution with SBT

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

#### Server/Client Execution Using SBT

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

#### Server/Client Execution Using Docker

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