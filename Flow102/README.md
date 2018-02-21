# Flow 102

This tutorial is based on [Matthew Gilliard's "Flow 102" blog post](https://mjg123.github.io/2017/10/11/FnProject-Flow-102.html).

If you haven't read [Flow 101](../Flow101) yet, we recommend you to start there to understand what Flow is, what it's used for and how it works.

In this tutorial we will go through how to build a more complex Flow with parallelism and asynchronous chaining. We will assume you have set up the services as described in the Flow 101.

## The demo Flow

An app which:

  * reads some text
  * greps for a given keyword
  * counts the matching lines
  * prints the count
  * prints the file header
  
In your shell, it might look something like:

```shell
⇒ cat my_file | grep -i love | wc -l | xargs -n1 echo result:
⇒ head -n10 my_file
```

## Installing helper functions

One of the cool things about Fn is that because it's based on Docker, functions can be written in *any* language - even Bash!

Clone this repo of simple Bash functions and deploy them all:

```shell
⇒ git clone https://github.com/mjg123/fnproject-text-functions.git
⇒ cd fnproject-text-functions
⇒ fn deploy --local --all
```

You can test all of these individually, for example:

```shell
⇒ curl -H "Word: bar" -d $' foo \n bar \n baz' http://localhost:8080/r/flow102/grep
 bar
```

## Creating our Flow function

In a new directory called `word-flow`:

```shell
⇒ fn init --runtime=java
⇒ rm -rf src/test  ## yolo, again
```

And, make `HelloFunction.java` look like this:

```java
package com.example.fn;

import com.fnproject.fn.api.flow.Flow;
import com.fnproject.fn.api.flow.FlowFuture;
import com.fnproject.fn.api.flow.Flows;
import com.fnproject.fn.api.flow.HttpResponse;

import static com.fnproject.fn.api.Headers.emptyHeaders;
import static com.fnproject.fn.api.flow.HttpMethod.POST;

public class HelloFunction {

    public String handleRequest(String input) {
        Flow flow = Flows.currentFlow();

        // Get the first ten lines of the file
        FlowFuture<byte[]> headText = flow.invokeFunction( "./head", POST,
                    emptyHeaders().withHeader("LINES", "10"), input.getBytes() )
                .thenApply(HttpResponse::getBodyAsBytes);

        // Grep for "love"
        FlowFuture<byte[]> wordCountResult =
                flow.invokeFunction( "./grep", POST,
                                     emptyHeaders().withHeader("WORD", "love"),
                                     input.getBytes())
                .thenApply(HttpResponse::getBodyAsBytes)

        // and count the hits
                .thenCompose( grepResponse ->
                        flow.invokeFunction("./linecount", POST,
                                            emptyHeaders(),
                                            grepResponse ))
                .thenApply(HttpResponse::getBodyAsBytes);


        return "Number of times I found 'love': " + new String(wordCountResult.get()) + "\n" +
               "The first ten lines are: \n" + new String(headText.get());
    }
}
```

It's worth reading this code carefully, remembering that anything returning a `FlowFuture` object is an asynchronous call, which can be chained with `thenApply`, `thenCompose` and the other [Flow API methods](https://github.com/fnproject/fdk-java/blob/master/api/src/main/java/com/fnproject/fn/api/flow/Flow.java).

We'll want some test data:

```shell
⇒ curl http://www.gutenberg.org/cache/epub/1524/pg1524.txt > hamlet.txt
```

Deploy the function, and remember to configure the app with the location of the completer:

```shell{% raw %}
⇒ export DOCKER_LOCALHOST=$(docker network inspect bridge -f '{{range .IPAM.Config}}{{.Gateway}}{{end}}')
⇒ fn apps config set flow102 COMPLETER_BASE_URL "http://$DOCKER_LOCALHOST:8081"
⇒ fn deploy --app flow102 --local
{% endraw %}```

And... send in the Shakespeare:

```shell
⇒ curl --data-binary @hamlet.txt http://localhost:8080/r/flow102/word-flow
Number of times I found 'love': 76

The first ten lines are: 
...etc etc...
```

## Visualising the Flow

Check the UI on [http://localhost:3002](http://localhost:3002) and you should see something like this:

![flow-ui]({{ "/assets/word-flow.png" | relative_url }})

As you could see from the code above, the `head` and `grep` are executed in parallel, the `linecount` has to wait for the `grep`, and the `main` has to wait till everything else is finished.

## Further reading

For a more thorough treatment of the different operations you can use to create Flows, see the [Fn Flow User Guide](https://github.com/fnproject/fdk-java/blob/master/docs/FnFlowsUserGuide.md). If you're at the top of the class, you can have a look at the [Flow - Advanced Topics](https://github.com/fnproject/fdk-java/blob/master/docs/FnFlowsAdvancedTopics.md) page. And a real example can be found in the [Asynchronous Thumbnails](https://github.com/fnproject/fdk-java/blob/master/examples/async-thumbnails/README.md) project.

Finally, there is an explanation of [testing Fn Java Functions and Flows](https://github.com/fnproject/fdk-java/blob/master/docs/TestingFunctions.md)

Any questions or comments? There is [#fn-flow](https://join.slack.com/t/fnproject/shared_invite/enQtMjIwNzc5MTE4ODg3LTdlYjE2YzU1MjAxODNhNGUzOGNhMmU2OTNhZmEwOTcxZDQxNGJiZmFiMzNiMTk0NjU2NTIxZGEyNjI0YmY4NTA) on the FnProject slack, and [our github](https://github.com/fnproject/). Or hit me up on Twitter as [@MaximumGilliard](https://twitter.com/maximumgilliard).
