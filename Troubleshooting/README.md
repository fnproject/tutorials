# Troubleshooting and Logging with Fn

Even if you've got excellent unit tests (e.g., using the
[Fn Java JUnit support](https://github.com/fnproject/fdk-java/blob/master/docs/TestingFunctions.md))
things can still go wrong.  Your function may throw an exception,
or you may be getting back unexpected results? So what can you do to troubleshoot
your functions?  In this tutorial we'll look at a number of techniques and Fn 
features you can use to get to the root cause of your problem.

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

When you run commands like `fn build` or `fn deploy` you typically see "progress
dots" (i.e., `...`) that let's you know some action is taking place.  Let's
build our function and observe the output.

![user input](images/userinput.png)
>```sh
> fn build
>```

You should see something like:

```sh
Building image trouble:0.0.1 ........
Function trouble:0.0.1 built successfully.
```

Perfect!  But if your code can't be built successfully, either not compiling or
failing unit tests, then you get a helpful error message suggesting you rerun
your command with the `--verbose`/`-v` flag.

To see this let's
break the function so it won't compile. Comment out the return statement in the
`HelloFunction` class' `handleRequest` function by putting `//` in front of the
`return` statement so it looks like:

```java
package com.example.fn;

public class HelloFunction {

    public String handleRequest(String input) {
        String name = (input == null || input.isEmpty()) ? "world"  : input;

        //return "Hello, " + name + "!";
    }

}
```

Let's build again and checkout the error message.

![user input](images/userinput.png)
>```sh
> fn build
>```

Results in:

```sh
Building image trouble:0.0.1 .....
Error during build. Run with `--verbose` flag to see what went wrong. eg: `fn --verbose CMD`

Fn: error running docker build: exit status 1

See 'fn <command> --help' for more information. Client version: 0.5.86
```

Now let's try the build with the `--verbose` flag, which you need to put 
*immediately* after `fn`:

![user input](images/userinput.png)
>```sh
> fn --verbose build
>```

Now we see details of the build and the failure (output abbreviated):

```sh
Building image trouble:0.0.1
Sending build context to Docker daemon  10.24kB
Step 1/11 : FROM fnproject/fn-java-fdk-build:jdk11-1.0.102 as build-stage
 ---> cc41c56dd693
...
[INFO] --- maven-compiler-plugin:3.3:compile (default-compile) @ hello ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /function/target/classes
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR :
[INFO] -------------------------------------------------------------
[ERROR] /function/src/main/java/com/example/fn/HelloFunction.java:[9,5] missing return statement
[INFO] 1 error
...
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.3:compile (default-compile) on project hello: Compilation failure
[ERROR] /function/src/main/java/com/example/fn/HelloFunction.java:[9,5] missing return statement
...
The command 'mvn package' returned a non-zero code: 1
...
ERROR: error running docker build: exit status 1
```

With verbose output we see the entirety of the Maven build which includes an
error message telling us we're missing a return statement--as we expected.

When an unexpected error happens, verbose output is the first thing you need to
enable to diagnose the issue.


## Cause a Runtime Error
Let's update our sample function to throw a Runtime exception. Then we can explore the options for getting details of what happened.

Create the `tutorial` application to store our test function.

![user input](images/userinput.png)
>```sh
> fn create app tutorial
>```

Let's update our HelloFunction so that it writes an error message and then
throws an exception in the `handleRequst` method.  Replace the definition of
HelloFunction with the following:

```java
package com.example.fn;

public class HelloFunction {

    public String handleRequest(String input) {
        System.err.println("Something wrong is going to happen");
        throw new RuntimeException("Something went horribly wrong!");
    }

}
```

With this change let's deploy the function and invoke it. If you haven't got an
Fn server running locally you can follow the
[Install and Start Fn](../install/README.md) tutorial to get setup.

![user input](images/userinput.png)
>```sh
> fn deploy --app tutorial --local
>```

```sh
Deploying trouble to app: tutorial
Bumped to version 0.0.2
Building image trouble:0.0.2
Updating function trouble using image trouble:0.0.2...
Successfully created app:  tutorial
Successfully created function: trouble with trouble:0.0.2
```

You can verified the function is deployed successfully by listing
the functions of the 'tutorial' app:

![user input](images/userinput.png)
>```sh
> fn ls functions tutorial
>```

Or the slightly more economical:

![user input](images/userinput.png)
>```sh
> fn ls f tutorial
>```

```sh
NAME     IMAGE          ID
trouble  trouble:0.0.1  01CT1QZFJTNG8G00GZJ0000002
```

With the function defined let's invoke it and see what happens when if fails:

![user input](images/userinput.png)
>```sh
> fn invoke tutorial trouble
>```

```sh
Error invoking function. status: 502 message: function failed
```

This is not much information to go on to debug the problem.  What we need to
do is look at the logs!


## Log to Terminal Window with DEBUG
When working with Fn locally, you have the option to turn on DEBUG logging using the `fn start` command. This causes detailed information about functions to be output to the terminal where Fn server was started.

To enable DEBUG logging for Fn server, restart the server with the following command:

![user input](images/userinput.png)
>```sh
> fn start --log-level DEBUG
>```

```sh
2019/12/19 09:26:27 ¡¡¡ 'fn start' should NOT be used for PRODUCTION !!! see https://github.com/fnproject/fn-helm/
time="2019-12-19T16:26:28Z" level=info msg="Setting log level to" fields.level=DEBUG
...
```
Notice in the first couple of messages state that the log level is set to debug.

Now invoke the function again. This time, looks for out put in the terminal window where the server was started.

![user input](images/userinput.png)
>```sh
> fn invoke tutorial trouble
>```

Here is the log output for Fn server:
```sh
time="2019-12-19T16:27:55Z" level=info msg="starting call" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 container_id=01DWFFS7QZNG8G00GZJ0000004 fn_id=01DWFFRQVQNG8G00GZJ0000002
time="2019-12-19T16:27:55Z" level=debug msg="Something wrong is going to happen\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
time="2019-12-19T16:27:55Z" level=debug msg="An error occurred in function: Something went horribly wrong!\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
time="2019-12-19T16:27:55Z" level=debug msg="Caused by: java.lang.RuntimeException: Something went horribly wrong! ...\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
time="2019-12-19T16:27:55Z" level=debug msg="    at com.example.fn.HelloFunction.handleRequest(HelloFunction.java:7)\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
time="2019-12-19T16:27:55Z" level=debug msg="    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
time="2019-12-19T16:27:55Z" level=debug msg="    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
time="2019-12-19T16:27:55Z" level=debug msg="    at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
time="2019-12-19T16:27:55Z" level=debug msg="    at java.base/java.lang.reflect.Method.invoke(Unknown Source)\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
time="2019-12-19T16:27:55Z" level=debug msg="\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
time="2019-12-19T16:27:55Z" level=debug msg="Got resp from UDS socket" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 resp="&{502 FunctionError 502 HTTP/1.1 1 1 map[Content-Type:[application/octet-stream]] {0xc420183260} -1 [] true false map[] 0xc42029b700 <nil>}"
time="2019-12-19T16:27:55Z" level=error msg="api error" action="server.handleFnInvokeCall)-fm" code=502 error="function failed" fn_id=01DWFFRQVQNG8G00GZJ0000002
time="2019-12-19T16:27:55Z" level=debug msg="docker pause" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000004 container_id=01DWFFS7QZNG8G00GZJ0000004 cpus= fn_id=01DWFFRQVQNG8G00GZJ0000002 idle_timeout=30 image="fndemouser/trouble:0.0.2" memory=128 stack=Freeze
```
These key lines shows us what went wrong.
```sh
time="2019-12-19T16:27:55Z" level=debug msg="Caused by: java.lang.RuntimeException: Something went horribly wrong! ...\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
time="2019-12-19T16:27:55Z" level=debug msg="    at com.example.fn.HelloFunction.handleRequest(HelloFunction.java:7)\n" action="server.handleFnInvokeCall)-fm" app_id=01DWFFR290NG8G00GZJ0000001 call_id=01DWFFS7QZNG8G00GZJ0000003 fn_id=01DWFFRQVQNG8G00GZJ0000002 image="fndemouser/trouble:0.0.2" user_log=true
```
A Runtime Exception was thrown on line 7 of the HelloFunction.

Running the Fn server with the DEBUG log level is a great way to track down any issues you are having with your functions.


## Log Capture to a Logging Service
When calling a deployed function, Fn captures all standard error output and sends it to a syslog server, if configured. So if you have a
function throwing an exception and the stack trace is being written to standard
error it's straightforward to get that stack trace via syslog.

We need to capture the logs for the function so that we can see what happens
when it fails.  To capture logs you need to configure the `tutorial` application
with the URL of a syslog server.  You can do this either when you create an
app or after it's been created.  

When creating a new app you can specify the URL using the `--syslog-url` option
as in:

![user input](images/userinput.png)
>```sh
>fn create app tutorial --syslog-url tcp://mysyslogserver.com
>```

**Note:** As of the time of writing, Podman Desktop does not support syslog log driver. The syslog option is only supported when you are running Fn Server in Docker Desktop and Rancher Desktop.


If you have already created an app, you will have to update it using
`fn update app`.  But before you do that you will need a syslog server ready to receive log data.

There are multiple options to setup a syslog server for testing. The easiest way is to start up a local syslog server.

### Run local syslog docker container

The steps are for Linux/MacOS but the steps on Windows should be similar. The syslog container works for both.

**Note:** We are using rsyslog/rsyslog container here. For detailed documentation, please check here: https://www.rsyslog.com/doc/getting_started/index.html

First, create a local directory to host the config and log files

![user input](images/userinput.png)
>```
>mkdir mysyslog
>cd mysyslog
>mkdir logs
>```

Then prepare the syslog config file. Create a file called `my-syslog.conf` under `mysyslog` with the following content

~~~
# Load module
module(load="imtcp")
input(type="imtcp" port="601")

# Ensure directory exists
$WorkDirectory /var/log

# Log all UDP messages to /var/log/syslog/syslog.log
action(type="omfile" file="/var/log/syslog")
~~~

Now you can run the rsyslog docker container

![user input](images/userinput.png)
>```sh
>docker run --rm -it -v /<your path to mysyslog>/my-syslog.conf:/etc/rsyslog.d/my-syslog.conf -v /<your path to mysyslog>/logs:/var/log -p 601:601 --name=syslogng rsyslog/rsyslog:latest
>```

You should have the container up and running
![Syslog container](images/syslogcontainer.jpg)

Then update app with the syslog url. You need to check your host IP and then run the following.

![user input](images/userinput.png)
>```sh
>fn update app tutorial --syslog-url tcp://<your host ip>:601
>```

You could see that the app is updated.
```sh
app tutorial updated
```

You can confirm that the syslog URL is set correctly by inspecting your
application:

![user input](images/userinput.png)
>```sh
> fn inspect app tutorial
>```

Which will return JSON looking something like:

```sh
{
	"created_at": "2019-10-13T14:54:45.459Z",
	"id": "01CT1QZFJ7NG8G00GZJ0000001",
	"name": "tutorial",
	"syslog_url": "tcp://<your host ip>:601",
	"updated_at": "2019-10-13T15:55:50.628Z"
}
```

`syslog_url` is now pointing to our syslog container so let's rerun our failing function:

![user input](images/userinput.png)
>```sh
> fn invoke tutorial trouble
>```

Of course it still fails. Let's check the syslog container log under `/<your path to mysyslog>/logs`.

![Log](images/log.jpg)


## Viewing HTTP Headers with DEBUG=1
If you're interacting with functions via the `fn` CLI, you can enable debug
mode to see the full details of the HTTP requests going to the Fn server and
the responses. The `fn` CLI simply wraps the Fn API to make it easier to
manage your applications and functions. You can always use `curl` but the CLI 
is much more convenient!

You enable debug mode by adding `DEBUG=1` before `fn` on each command.  For
example try the following:

![user input](images/userinput.png)
>```sh
> DEBUG=1 fn ls apps
>```

Which, with debugging turn on, returns the following:

```sh
GET /v2/apps HTTP/1.1
Host: localhost:8080
User-Agent: Go-http-client/1.1
Accept: application/json
Accept-Encoding: gzip


HTTP/1.1 200 OK
Content-Length: 977
Content-Type: application/json; charset=utf-8
Date: Sun, 13 Oct 2019 16:45:56 GMT


{"items":[{"id":"01DQ2STN6KNG8G00GZJ000001Q","name":"tutorial","syslog_url":"tcp://xxx.xxx.xxx.xxx:NNNN","created_at":"2019-10-13T14:54:45.459Z","updated_at":"2019-10-13T15:55:50.628Z"}]}
NAME		ID
tutorial	01DQ2STN6KNG8G00GZJ000001Q
```

All debug output is written to stderr while the normal response is written
to stdout so it's easy to capture or pipe either for processing.

## Wrapping Up

That's brief intro to troubleshooting techniques for Fn today.  We'll update
this tutorial should new features become available.

**Go:** [Back to Contents](../README.md)
