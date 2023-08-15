---
layout: default
title: VLDB 2023 Demo
---

# Portals VLDB 2023 Demo

Welcome to the accompanying website for our submission to the VLDB 2023 Demonstrations track. 

The demonstration consists of three scenarios.

## **Scenario 1: Shopping Cart** 
The [Shopping Cart](shoppingcart) consists of four services: an inventory, cart, orders, and analytics service. The application shows how services can be launched dynamically, and how `portal` services are exposed and used.

## **Scenario 2: SQL to Dataflow**
The [SQL to Dataflow](sql-to-dataflow) example showcases how complex communication patterns and libraries can be built on top of the existing portal service primitive. It provides an SQL interface for running SQL queries agtainst tables in a convenient API; implemented using Apache Calcite and the Portal abstraction.

## **Scenario 3: Playground** 

The [Portals Playground]({{page.baseurl}}/playground) is a JavaScript-based sandbox capable of running Portals applications. It comes with a JavaScript API, and the runtime is executed in the browser. [Read more about the playground]({{page.baseurl}}/playground) or try it out at [https://portals-project.org/playground/](https://portals-project.org/playground/).
