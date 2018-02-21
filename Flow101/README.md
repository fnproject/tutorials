# Flow 101

This tutorial is based on [Matthew Gilliard's "Flow 101" blog post](https://mjg123.github.io/2017/10/10/FnProject-Flow-101.html).

[Fn Project](http://fnproject.io/) was released in October 2017. [Chad Arimura](https://twitter.com/chadarimura/) explained the motivation and structure of the project in a good amount of detail in his post ["8 Reasons why we built the Fn Project"](https://medium.com/fnproject/8-reasons-why-we-built-the-fn-project-bcfe45c5ae63), with one of the major components being **Fn Flow**. Flow allows developers to build high-level workflows of functions with some notable features:

  - Flexible model of function composition. Sequencing, fan out/in, retries, error-handling and more.
  - Code-driven. Flow does not rely on massive yaml or json descriptors or visual graph-designers. Workflows are defined *in code*, expressed as a function, naturally.
  - Inspectable. Flow shows you the current state of your workflow, allows you to drill into each stage to see logs, stack traces and more.
  - Language agnostic. The initial Flow implementation which this post will use is in Java but support for other langauges has already started including Python, [Go](https://github.com/fnproject/flow-lib-go) and JS.

## What is a Flow?

A Flow is a way of linking together functions, and incidentally provides a way to define those functions inline if you need to. It's a FaaS-friendly way of saying stuff like

> Start with **this**, then do **that**, then take the result and do **these things** in parallel then when they've all finished do **this one last thing**, and if there's any errors then do **this** to recover

Where all the *this* and *that* are FaaS functions.


A simple Flow function looks like this:

```java

    public String handleRequest(int x) {

        Flow flow = Flows.currentFlow();

        return flow.completedValue(x)
                   .thenApply(i -> i+1)
                   .thenCompose( s -> Flow.invokeFunction("./isPrime", s) )
                   .get();
    }

```

If you've used a promises-style API before then this will be familiar. The closest analogue in core Java is the [CompletionStage API](http://download.java.net/java/jdk9/docs/api/java/util/concurrent/CompletionStage.html) which was even called [`Promise`](http://cs.oswego.edu/pipermail/concurrency-interest/2012-December/010423.html) in a pre-release draft.

Anyway it's easy to tell the stages of what's going to happen:

  - Start with a value provided by the user
  - Apply some transformation `i -> i+1`
  - Pass that to an external function called `./isPrime`
  - Then return get the result and return it

Internally the `Flow` class submits each stage in this workflow to the Flow Server. You'll meet it soon. The Flow Server will then orchestrate each stage as an individual call to Fn. Flow Server is responsible for working out which stages are ready to be called, calling them, handling the results and triggering any following stages until you reach the point where there's no more work to do.

This example could easily be written without Flow but it's good to start simple.

## Running your first Flow

Currently FnProject is available to download, to experiment with, and to run on your private cloud. A managed service by Oracle is in the works. To play with Flow at the moment you will need to run everything locally, but it's not hard. We need **`fn`**, the **Fn server**, the **Flow Server** and not necessary but nice-to-have is the Flow Server **UI**. These run on ports 8080, 8081 and 3002 respectively so you might need to configure firewalls to allow access.

> As you make your way through this tutorial, look out for this icon ![](../images/userinput.png). Whenever you see it, it's time for you to perform an action.

### Setting up

Install the **`fn`** CLI tool:

>![user input](../images/userinput.png)
>```shell
>curl -LSs https://raw.githubusercontent.com/fnproject/cli/master/install | sh
>```

Then start the **Fn server**:

>![user input](../images/userinput.png)
>```shell
>fn start
>```

```
...
time="2017-10-11T13:12:44Z" level=info msg="Serving Functions API on address `:8080`"
        ______
       / ____/___
      / /_  / __ \
     / __/ / / / /
    /_/   /_/ /_/
        v0.3.119
```

The **Flow Server** needs to know how to call the Fn server, so ask Docker which IP address to use.

![user input](../images/userinput.png)
>```shell
>DOCKER_LOCALHOST=$(docker network inspect bridge -f '{{range .IPAM.Config}}{{.Gateway}}{{end}}')
>```

Start the **Flow Server**:

![user input](../images/userinput.png)
>```shell
>docker run --rm -d \
>       -p 8081:8081 \
>       -e API_URL="http://$DOCKER_LOCALHOST:8080/r" \
>       -e no_proxy=$DOCKER_LOCALHOST \
>       --name completer \
>       fnproject/flow:latest
>```

Then start the Flow **UI**:

![user input](../images/userinput.png)
>```shell
>docker run --rm -d \
>       -p 3002:3000 \
>       --name flowui \
>       -e API_URL=http://$DOCKER_LOCALHOST:8080 \
>       -e COMPLETER_BASE_URL=http://$DOCKER_LOCALHOST:8081 \
>       fnproject/flow:ui
>```

Now, everything's set so lets crack on!

### A simple Flow function

Create a new function:

```shell
⇒ fn init --runtime=java simple-flow
⇒ cd simple-flow
```

Flow has a comprehensive test framework, but lets concentrate on playing with the code for the time being:

```shell
⇒ rm -rf src/test   ## yolo
```

Make peace with yourself after that, then let's get the code in shape.

Change `HelloFunction.java` to look like this:

```java
package com.example.fn;

import com.fnproject.fn.api.flow.Flow;
import com.fnproject.fn.api.flow.Flows;

public class HelloFunction {

    public String handleRequest(int x) {

	Flow fl = Flows.currentFlow();

	return fl.completedValue(x)
                 .thenApply( i -> i*2 )
	         .thenApply( i -> "Your number is " + i )
	         .get();	
    }
}
```

Then deploy this to an app which we call `flow101` on the local Fn server, and configure the function to talk to the Flow Server.

```shell
⇒ fn deploy --app flow101 --local
⇒ fn apps config set flow101 COMPLETER_BASE_URL "http://$DOCKER_LOCALHOST:8081"
```

You can now invoke the function using `fn call`:

```shell
⇒ echo 2 | fn call flow101 /simple-flow
Your number is 4
```

or equivalently with `curl`:

```shell
⇒ curl -d "2" http://localhost:8080/r/flow101/simple-flow
Your number is 4
```

### Exploring the UI

Browsing to [http://localhost:3002](http://localhost:3002) you should see something like this:

![flow-ui]({{ "assets/simple-flow-ui.png" | relative_url }})

Which is showing us 3 function invocations:

  * The main flow function, in blue
  * `.thenApply` for the code `i -> i*2`
  * `.thenApply` for the code `i -> "Your number is " + i`
  
Click on any of these and see the detail for each one expanded at the bottom of the screen.

The blue function is shown as running for the whole time that the `thenApply` stages are. Why? Because we are calling `.get()` at the end, so this is synchronously waiting for the final result of the chain. Exercise: Try removing the `.get()` from the code (you'll need to return a different String, and don't forget to re-deploy). Now it will look like:

![flow-ui]({{ "assets/simple-flow-ui-async.png" | relative_url }})

This shows that Flow is well-suited for asynchronous functions which result in a side-effect (posting to slack, for example).

## Why we made Flow

Consider how else you could have achieved what we did in 4 lines of Java above. First of all there are actually 3 different functions at play, each of which would need its own codebase and entrypoint. Then, consider that Flow chains values through them while *preserving type information*. We could have written an overarching "Call function A using an HTTP client, call function B the same" type orchestrator function (ugh), or even hard-coded each function to call the next one in the chain (ugh ugh). Perhaps we could have used an Enterprise Service Bus to glue it all together (multiple ughs).

We think Flow hits a very sweet spot of allowing sophisticated stateful apps defined *in code*, and maintaining the promised benefits of FaaS a la [Serverless Manifesto](http://blog.rowanudell.com/the-serverless-compute-manifesto/). We think you'll like it too.


## Summary

So, congratulations - we've covered a lot! You've got a Flow function running, seen how to use the API to compose simple transformations and run things in parallel. Head to the [Flow 102](/2017/10/11/FnProject-Flow-102.html) post to take your Flows to the next level.

Any questions or comments? There is [#fn-flow](https://join.slack.com/t/fnproject/shared_invite/enQtMjIwNzc5MTE4ODg3LTdlYjE2YzU1MjAxODNhNGUzOGNhMmU2OTNhZmEwOTcxZDQxNGJiZmFiMzNiMTk0NjU2NTIxZGEyNjI0YmY4NTA) on the FnProject slack, and [our github](https://github.com/fnproject/). Or hit me up on Twitter as [@MaximumGilliard](https://twitter.com/maximumgilliard).
