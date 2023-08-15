---
layout: default
title: Project Info
---

# Project Info

Portals is a framework written in Scala under the **Apache 2.0 license** for stateful serverless applications. It provides a Scala and a JavaScript API, its source code is available on [GitHub](https://github.com/portals-project/portals). 

At its core, Portals unifies the Distributed Dataflow Streaming Model and the Actor Model, providing unparalleled flexibility and data-parallel processing capabilities with strong guarantees. The framework's programming model is tailored for edge stateful serverless processing, and provides the following key features:

1. **Multi-Dataflow Applications.** Multiple stateful dataflow streaming pipelines can dynamically be composed together on top of atomic streams, a transactional type of data streams.
2. **Inter-Dataflow Services.** The Portal abstraction binds dataflow pipelines together to create and expose reusable services. This enables a request-reply type of communication between pipelines by providing serverless access to remote operator states on top of a continuation-style execution.
3. **Decentralized Cloud and Local Execution.** The decentralized runtime can be executed on cloud and edge devices, whilst still providing end-to-end exactly-once processing guarantees.

Portals is a research project developed at the [KTH Royal Institute of Technology](https://www.kth.se/en) and [RISE Research Institutes of Sweden](https://www.ri.se/en) in Stockholm, Sweden.

For more information on the project, please visit the [project website](https://portals-project.org/) or the [GitHub repository](https://github.com/portals-project/portals).