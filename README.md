# Tutorials

Each of the tutorials below provides a step by step examination and walkthrough of a specific Fn feature or component.  Check back soon as new tutorials are being added regularly.

## Introduction to Fn
Before deploying your first function, you need to [install the Fn cli and start an Fn Server](install/README.md).

Now that the Fn Server is up and running, you can deploy your first function. Select your preferred language:

Official:

* [Go](Introduction/README.md)
* [Java](JavaFDKIntroduction//README.md)
* [Node.js](node/intro/README.md)
* [Ruby](ruby/intro/README.md)
* [Python](python/intro/README.md)

Community Supported:

* [C#](csharp/intro/README.md)

## Explore Fn
* [Create Apps with Fn](Apps/README.md) - Learn how to group your functions into an application/API and deploy them together.
* [Create a Function with a Docker Container](ContainerAsFunction/README.md) - This tutorial provides a simple example of how to define an Fn function using a custom built Docker container.
* [Create a Function with a Linux Command and HotWrap](docker/CustomLinuxContainer/README.md) - This tutorial provides an example of how to define an Fn function using Linux commands, HotWrap, and a custom Docker container.
* [Create a Function from a Docker image that contains a Node.js app with Oracle DB Support](node/custom-db/README.md)

## Test and Monitor Functions

* [Monitor Fn metrics with Grafana and Prometheus](grafana/README.md) - Learn how to view Fn server metrics with Prometheus and Grafana.
* [Troubleshoot and Log functions](Troubleshooting/README.md) - Resolve issues at both development and deployment time.

## Orchestrate with Fn Flow

Fn Flow provides a way to orchestrate functions to build sophisticated applications, initially using Java, and soon with other programming languages.

* [Flow 101](Flow101/README.md)
* [Flow 102](Flow102/README.md)
* [Fn Flow Saga](FlowSaga/README.md) - In this tutorial you will use Fn Flow to implement a Java travel booking system that leverages functions written in a variety of languages.
