# Troubleshooting Fn

Even if you've got excellent unit tests (e.g., using the 
[Fn Java JUnit support](https://github.com/fnproject/fdk-java/blob/master/docs/TestingFunctions.md))
things can still go wrong.  Your function may throw an exception, you may be
using [Fn Flow](https://github.com/fnproject/flow) and can't figure out why you
don't see any activity in the [Flow UI](https://github.com/fnproject/flowui),
you may be getting back unexpected results from your function or maybe you can't
even execute your function locally with `fn run`? So what can you do to
troubleshoot your functions?  In this tutorial we'll look at a number of
techniques and Fn features you can use to get to the root cause of your problem.

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Getting Started

First, let's create a simple Java function called `trouble`.  In a new folder
type:

![user input](images/userinput.png)
>```sh
> fn init --runtime java trouble
>```

This will create a boilerplate Java hello world function in the `trouble`
folder.  Let's cd into that folder.

![user input](images/userinput.png)
>```sh
> cd trouble
>```

And let's delete the unit tests so we can concentrate on the troubleshooting
techniques rather than keeping the tests up to date.

![user input](images/userinput.png)
>```sh
> rm -rf src/test
>```

__If__ you have the `tree` utility installed you can verify that your structure
looks like this:

```sh
.
├── func.yaml
├── pom.xml
└── src
    └── main
        └── java
            └── com
                └── example
                    └── fn
                        └── HelloFunction.java
```

Ok, we're ready to begin!

## Verbose Mode

When you run commands like `fn run` or `fn build` you typically see "progress
dots" (i.e., `...`) that let's you know some action is taking place.  Let's
build our function and observe the output.

![user input](images/userinput.png)
>```sh
> fn build
>```

You should see something like:

```sh
Building image trouble:0.0.1
Function trouble:0.0.1 built successfully.
```

Perfect!  But if your code can't be built successfully, either not compiling or
failing unit tests, then you get a helpful error message.  To see this let's
break the function so it won't compile. Comment out the return statement in the
`HelloFunction` class' `handleRequest` function by putting `//` in front of the
`return` statement so it looks like:

![user input](images/userinput.png)
```java
package com.example.fn;

public class HelloFunction {

    public String handleRequest(String input) {
        String name = (input == null || input.isEmpty()) ? "world"  : input;

        //return "Hello, " + name + "!";
    }

}
```

If you build the function you'll see progress dots and then an error message
with the useful suggestion to try running the command again with the
`--verbose` option (which you need to put *immediately* after `fn`) to get more
details on the failure.

Let's build and checkout the error message.

![user input](images/userinput.png)
>```sh
> fn build
>```

Results in:

```sh
Building image trouble:0.0.1 .....
Error during build. Run with `--verbose` flag to see what went wrong. eg: `fn --verbose CMD`
ERROR: error running docker build: exit status 1
```

Now let's try the build with the `--verbose` flag:

![user input](images/userinput.png)
>```sh
> fn --verbose build
>```

Now we see details of the build and the failure (output abbreviated):

```sh
Building image trouble:0.0.1
Sending build context to Docker daemon  10.24kB
Step 1/11 : FROM fnproject/fn-java-fdk-build:jdk9-1.0.56 as build-stage
 ---> dbeadad33cac
...
[INFO] --- maven-compiler-plugin:3.3:compile (default-compile) @ hello ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /function/target/classes
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR :
[INFO] -------------------------------------------------------------
[ERROR] /function/src/main/java/com/example/fn/HelloFunction.java:[13,5] missing return statement
[INFO] 1 error
...
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.3:compile (default-compile) on project hello: Compilation failure
[ERROR] /function/src/main/java/com/example/fn/HelloFunction.java:[13,5] missing return statement
...
The command 'mvn package' returned a non-zero code: 1
...
ERROR: error running docker build: exit status 1
```

With verbose output we see the entirety of the Maven build which includes an
error message telling us we're missing a return statement--as we expected.

This same technique can be used with `fn run` which may also fail due to a
compilation or test failure.  When this happens, verbose output is the first
thing you need to enable to diagnose the issue.

## Logs

When calling a deployed function Fn captures all standard error output and saves
it in a log associated with that specific call instance. So if you have a
function throwing an exception and the stack trace is being written to standard
error it's straight forward to get that stack trace.

Let's update our HelloFunctionn so that it throws an exception in the
`handleRequst` method.  Replace the definition of HelloFunction with the
following:

![user input](images/userinput.png)
```sh
package com.example.fn;

public class HelloFunction {

    public String handleRequest(String input) {
        throw new RuntimeException("Something went horribly wrong!");
    }

}
```

With this change we can run the function locally.

![user input](images/userinput.png)
>```sh
> fn run
>```

The good news is that `fn run` redirects the function's standard error to your
terminal so you can see the failure immediately.

```sh
Building image trouble:0.0.1
An error occurred in function: Something went horribly wrong!
Caused by: java.lang.RuntimeException: Something went horribly wrong!
    at com.example.fn.HelloFunction.handleRequest(HelloFunction.java:6)
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.base/java.lang.reflect.Method.invoke(Method.java:564)

ERROR: exit status 1
```

But things are different when you deploy your function to a (likely) remote Fn
server.  When you do that we have to use the logs to see runtime errors. 

Deploy the function to an Fn server.  If you haven't got a server running
locally you can follow the [Install and Start Fn](../install/README.md) tutorial
to get setup.

![user input](images/userinput.png)
>```sh
> fn deploy --app tutorials --local
>```

```sh
Deploying trouble to app: tutorials at path: /trouble
Bumped to version 0.0.2
Building image trouble:0.0.2
Updating route /trouble using image trouble:0.0.2...
```

If we call the deployed function we get an error message that the call failed,
but no details.

![user input](images/userinput.png)
>```sh
> fn call tutorials /trouble
>```

```sh
ERROR: error calling function: status 500
```

We need to get the log for the failed call to see what happened.  The first
step is to get a list of calls for the `tutorials` application.  You can do
this with the `fn calls` command.  The syntax is `fn calls [l|list]
<app-name>`.  Let's get the calls for our `tutorials` app:

![user input](images/userinput.png)
>```sh
> fn calls list tutorials
>```

This returns a series of call records with the most recent first.

```sh
ID: 01C65C94M747WG600000000000
App: tutorials
Route: /trouble
Created At: 2018-02-12T16:06:33.863Z
Started At: 2018-02-12T16:06:34.458Z
Completed At: 2018-02-12T16:06:34.762Z
Status: success

ID: 01C656NGYX47WG200000000000
App: tutorials
Route: /logs
Created At: 2018-02-12T14:28:28.253Z
Started At: 2018-02-12T14:28:28.839Z
Completed At: 2018-02-12T14:28:29.147Z
Status: success
```

The next step is to get the log for a specific call.  Typically when you're in
the middle of developing and testing a function, that call is the last call.
Copy the call id for the very first call record and use `fn logs get`, e.g.:

![user input](images/userinput.png)
>```sh
> fn logs get tutorials 01C656NGYX47WG200000000000
>```

This returns the captured standard error output:

```sh
An error occurred in function: Something went horribly wrong!
Caused by: java.lang.RuntimeException: Something went horribly wrong!
    at com.example.fn.HelloFunction.handleRequest(HelloFunction.java:6)
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.base/java.lang.reflect.Method.invoke(Method.java:564)
```

Great!  But this two step procedure is a little tedious when you're developing
and you just want the log of the last call.  Fortunately there's a shortcut for
just this case! You can use `last` instead of a call id to get the log of the
most recent function call.

![user input](images/userinput.png)
>```sh
> fn logs get tutorials last
>```

But be careful! The 'last log' feature is ideal when you're working locally
__by yourself__ but when deploying to a shared Fn development server others may
be calling different functions of the same app.  You need to make sure you're
getting the log for the function you called and not the log of an entirely
different function in the same application.

## Coming Soon

More troubleshooting tips coming covering:
* Debugging Configuration
* Debugging Routes
* Using Docker inspect
* Debugging Flow


**Go:** [Back to Contents](../README.md)
