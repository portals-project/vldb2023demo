---
layout: default
title: "Demo 3: Playground"
---

# Demo 3: Playground

The Portals playground is a JavaScript-based sandbox capable of running Portals applications that hosts a code editor for writing programs and an output log. It comes with a JavaScript API, and the runtime is executed fully within the browser. The Playground supports most of the core API, and can be used to write multi-dataflow applications and Portal applications, as described in the other two demonstration scenarios. 

The Portals Playground is implemented with Scala using the Scala-JS library. Thus, the core runtime (Scala) and the JavaScript runtime share most of the code-base. This enables Portals to run both on lightweight edge devices with the JS runtime, and on cloud instances with the JVM runtime.

The Playground is available at [https://portals-project.org/playground/](https://portals-project.org/playground/).

## Example 1 -- Hello VLDB!

The Hello VLDB Example is one of many examples that can be found on the playground website. It simply consists of a generator that generators a single event: "Hello VLDB!", and a workflow which consumes this generated stream and logs it to the console. The code for this example is shown below.

```javascript
let builder = PortalsJS.ApplicationBuilder("helloVLDB")
let _ = builder.workflows
  .source(builder.generators.fromArray(["Hello VLDB!"]).stream)
  .logger()
  .sink()
  .freeze()
let helloVLDB = builder.build()
let system = PortalsJS.System()
system.launch(helloVLDB)
system.stepUntilComplete()
```

[Run this example in the playground.](https://www.portals-project.org/playground/?code=let%20builder%20%3D%20PortalsJS.ApplicationBuilder(%22helloVLDB%22)%0Alet%20_%20%3D%20builder.workflows%0A%20%20.source(builder.generators.fromArray(%5B%22Hello%20VLDB!%22%5D).stream)%0A%20%20.logger()%0A%20%20.sink()%0A%20%20.freeze()%0Alet%20helloVLDB%20%3D%20builder.build()%0Alet%20system%20%3D%20PortalsJS.System()%0Asystem.launch(helloVLDB)%0Asystem.stepUntilComplete())

## Example 2 -- Multi Dataflow

The Multi Dataflow example shows how we can compose together multiple dataflows using workflows, sequencers, and splitters.

```javascript
let builder = PortalsJS.ApplicationBuilder("MultiDataflow")
let generator = builder.generators.fromArrayOfArrays([["Hello"], ["World"]])
let wf1 = builder.workflows
  .source(generator.stream)
  .map(ctx => x => x.toUpperCase())
  .logger("from wf1: ")
  .sink()
  .freeze()
let wf2 = builder.workflows
  .source(wf1.stream)
  .map(ctx => x => x.toLowerCase())
  .logger("from wf2: ")
  .sink()
  .freeze()
let sequencer = builder.sequencers.random()
let wf3 = builder.workflows
  .source(sequencer.stream)
  .flatMap(ctx => x => x.length > 0 ? [x.slice(1)] : [])
  .logger("from wf3: ")
  .sink()
  .freeze()
let c1 = builder.connections.connect(wf2.stream, sequencer)
let c2 = builder.connections.connect(wf3.stream, sequencer)
let splitter = builder.splitters.empty(wf3.stream)
let split = builder.splits.split(splitter, x => x.length % 2 == 0)
let wf4 = builder.workflows
  .source(split)
  .logger("from wf4: ")
  .sink()
  .freeze()
let multiDataflow = builder.build()
let system = PortalsJS.System()
system.launch(multiDataflow)
system.stepUntilComplete()
```

## More Examples

There are more examples available at [https://portals-project.org/playground/](https://portals-project.org/playground/).