---
layout: default
title: "Demo 2: SQL to Dataflow"
---

# Demo 2: SQL to Dataflow
The second scenario demonstrates a serverless application that runs a key-value store dataflow accessible through a portal with an SQL interface. The application shows how interesting abstractions (the SQL interface) can be built on top of the portal abstraction, leveraging the underlying guarantees and execution runtime. The example both exposes the SQL interface as a service using the Portal service abstraction, and uses portal services internally for implementation of the SQL evaluation strategy and key-value stores.

The interface supports queries over multiple tables. However, it does not support range queries (for the moment). The SQL engine used by the SQL library is **[Apache Calcite](https://calcite.apache.org/)**.

The SQL interface consists of two sides: a key-value store dataflow and a query dataflow. 
The key-value store dataflow is also called the `Table Workflow`. It implements a key-value store that is exposed through a Portal service, and implements transactional state manageement using 2PL.
The query dataflow, also called the `Query Workflow`, implements a simple SQL interface, and translates SQL queries into requests to the table workflow. That is, the query workflow consumes SQL queries (either as a stream or through portal requests), and sends corresponding requests to the table workflows to retrieve the data and produce the result. Internally, the query dataflow runs Apache Calcite to parse and evaluate the SQL queries.

More information on the SQL interface is available in a student thesis (the presented work in this demo is a continuation of this thesis):
* Huang C. Queryable Workflows: Extending Dataflow Streaming with Dynamic Request/Reply Communication [Internet] [Dissertation]. 2023. (TRITA-EECS-EX). Available from: [https://urn.kb.se/resolve?urn=urn:nbn:se:kth:diva-329594](https://urn.kb.se/resolve?urn=urn:nbn:se:kth:diva-329594)

## Demo Experience

The demo shows how to run SQL queries over a key-value store. It also shows how to connect multiple querying applications to the same key-value store, leveraging the decentralized execution model. A deep-dive into the source code will reveal that this is all implemented using the `workflow` and `portal` primitives. This demo comes in three flavours: `SQLToDataflow.scala`, `SQLToDataflowTxn.scala`, and `SQLToRemoteDataflow.scala`. 

> **Note**
> To execute the demo yourself, check out the instructions in the code directory of this repository: [https://github.com/portals-project/vldb2023demo/tree/main/code](https://github.com/portals-project/vldb2023demo/tree/main/code).

> **Note**
> The executable code for this demo can be found in the code directory of this repository: [https://github.com/portals-project/vldb2023demo/tree/main/code](https://github.com/portals-project/vldb2023demo/tree/main/code).

## Demo Overview

#### SQLToDataflow

The simplest version is `SQLToDataflow.scala`, which runs both the table and the query workflow in the same Portals Application.

```scala
/** Portals application which runs the queriable KV Table. */
PortalsApp("SQLToDataflowTable"):

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

In addition to this, we show how to also perform transactional queries (SQLToDataflowTxn), and how to perform queries to remote tables (SQLToRemoteDataflow, advanced). Not shown in the examples, is how to connect to multiple tables, in order to join information from multiple tables.

```scala
/** Portals application which runs the queriable KV Table. */
PortalsApp("SQLToDataflowTable"):

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

The remote example shows how to connect to a remote Table Workflow, as well as a remote Query workflow. This example has a query portal which manages the query workflow. In addition to this, it has a `queryWorkflow`, which connects to the query portal. This lets us inspect how an application would connect to the query portal: we can send a query to the query portal (`ask(queryPortal)(x)`), and await its result to, here the result is printed (`ctx.log.info(s"== Query: ${x}; Result: ${f.value.get} ==")`) and emitted.
