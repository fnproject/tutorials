# Introduction

This tutorial introduces the Fn Java FDK (Function Development Kit). If
you haven't completed the [Introduction to Fn](../Introduction/README.md)
tutorial you should head over there before you proceed.

This tutorial takes you through the Fn developer experience for building
Java functions. It shows how easy it is to build, deploy and test
functions written in Java.

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Prequisites

This tutorial requires you to have both Docker and Fn installed and an
Fn server running locally.  If you need help with Fn installation you
can find instructions in the
[Introduction to Fn](../Introduction/README.md) tutorial.

# Getting Started

Let's start by creating a new function.  In a terminal type the
following:

![](images/userinput.png)
>`mkdir javafn`

> `cd javafn`

>`fn init --runtime java8`

The output will be:
```sh
Runtime: java
function boilerplate generated.
func.yaml created
```

`fn init` creates an simple function with a bit of boilerplate to get you
started. The `--runtime` option is used to indicate that the function
we're going to develop will be written in Java.  A number of other
runtimes are also supported.  If you have the `tree` utility installed
you can see the directory structure that the init command has created.

![](images/userinput.png)
>`tree`

```sh
.
├── func.yaml
├── pom.xml
└── src
    ├── main
    │   └── java
    │       └── com
    │           └── example
    │               └── fn
    │                   └── HelloFunction.java
    └── test
        └── java
            └── com
                └── example
                    └── fn
                        └── HelloFunctionTest.java

11 directories, 4 files
```


As usual, the init command has created a `func.yaml` file for your
function but in the case of Java it also creates a Maven `pom.xml` file
as well as a function class and function test class.

Take a look at the contents of the generated func.yaml file.

![](images/userinput.png)
>`cat func.yaml`

```sh
name: javafn
version: 0.0.1
runtime: java
cmd: com.example.fn.HelloFunction::handleRequest
```

In the case of a Java function, the `cmd` property is set to the fully
qualified name of the function class and the method that should be
invoked when your `javafn` function is called.

The Java function init also generates a Maven `pom.xml` file to build
and test your function.  The pom includes the Fn Java FDK runtime
and test libraries your function needs.

# Running your Function

Let's build and run the generated function.  We're working locally and
won't be pushing our function images to a Docker registry like Docker
Hub. So before we build let's set `FN_REGISTRY` to a local-only registry
username like `fndemouser`.

![](images/userinput.png)
>`export FN_REGISTRY=fndemouser`

Now we're ready to run.  Depending on whether this is your first time
developing a Java function you may or may not see Docker images being
pulled from Docker Hub.  Once the necessary base images are downloaded
subsequent operations will be faster.

As the function is built using Maven you may also see a number of Java
packages being downloaded.  This is also expected the first time you
run a function and trigger a build.

![](images/userinput.png)
>`fn run`

Here's what the abbreviated output will look like:

```sh
Building image fndemouser/javafn:0.0.1
Sending build context to Docker daemon  28.67kB
Step 1/11 : FROM fnproject/fn-java-fdk-build:latest as build-stage
latest: Pulling from fnproject/fn-java-fdk-build
...
Step 2/11 : WORKDIR /function
 ---> 8ed38772a9e4
Removing intermediate container 9c3957272448
Step 3/11 : ENV MAVEN_OPTS -Dhttp.proxyHost= -Dhttp.proxyPort= -Dhttps.proxyHost= -Dhttps.proxyPort= -Dhttp.nonProxyHosts= -Dmaven.repo.local=/usr/share/maven/ref/repository
 ---> Running in 7a2e1ec6d8a5
 ---> 345e102442d0
Removing intermediate container 7a2e1ec6d8a5
Step 4/11 : ADD pom.xml /function/pom.xml
 ---> 7bd708b005e9
Step 5/11 : RUN mvn package dependency:copy-dependencies -DincludeScope=runtime -DskipTests=true -Dmdep.prependGroupId=true -DoutputDirectory=target --fail-never
 ---> Running in 51427fd1c021
[INFO] Scanning for projects...
Downloading: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-deploy-plugin/2.7/maven-deploy-plugin-2.7.pom
Downloaded: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-deploy-plugin/2.7/maven-deploy-plugin-2.7.pom (5.6 kB at 8.4 kB/s)
Downloading: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-deploy-plugin/2.7/maven-deploy-plugin-2.7.jar
Downloaded: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-deploy-plugin/2.7/maven-deploy-plugin-2.7.jar (27 kB at 188 kB/s)
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building hello 1.0.0
[INFO] ------------------------------------------------------------------------
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 7.853 s
[INFO] Finished at: 2017-09-20T13:50:55Z
[INFO] Final Memory: 19M/121M
[INFO] ------------------------------------------------------------------------
 ---> c010a22244a1
Removing intermediate container 51427fd1c021
Step 6/11 : ADD src /function/src
 ---> e9dd4ad1fb0c
Step 7/11 : RUN mvn package
 ---> Running in 74da2bfc5f1b
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building hello 1.0.0
[INFO] ------------------------------------------------------------------------
...
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.example.fn.HelloFunctionTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.371 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ hello ---
[INFO] Building jar: /function/target/hello-1.0.0.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
...
Removing intermediate container 74da2bfc5f1b
Step 8/11 : FROM fnproject/fn-java-fdk:latest
 ---> 3518e302e29e
Step 9/11 : WORKDIR /function
 ---> Using cache
 ---> 2d31a347b567
Step 10/11 : COPY --from=build-stage /function/target/*.jar /function/app/
 ---> 28f86279daf1
Step 11/11 : CMD com.example.fn.HelloFunction::handleRequest
 ---> Running in 12dd9351221a
 ---> 7617229106a0
Removing intermediate container 12dd9351221a
Successfully built 7617229106a0
Successfully tagged fndemouser/javafn:0.0.1
Hello, world!

```

In the output you can see Maven compiling the code and running
the test, the function packaged into a container, and then run locally
to produce the output "Hello, world!".

Let's try one more thing and pipe some input into the function.  In your
terminal type:

![](images/userinput.png)
>`echo -n "Bob" | fn run`

```sh
Hello, Bob!
```

Instead of "Hello, world!" the function has read the input string "Bob"
from standard input and returned "Hello, Bob!".

# Exploring the Code

We've generated, compiled, and run the Java function so let's take a
look at the code.  You may want to open the code in your favorite IDE or editor.

Below is the generated `com.example.fn.HelloFunction` class.  As you can
see the function is just a method on a POJO that takes a string value
and returns another string value, but the Java FDK also supports binding
input parameters to streams, primitive types, byte arrays and Java POJOs
unmarshalled from JSON.  Functions can also be static or instance
methods.

```java
package com.example.fn;

public class HelloFunction {

    public String handleRequest(String input) {
        String name = (input == null || input.isEmpty()) ? "world"  : input;

        return "Hello, " + name + "!";
    }

}
```

This function returns the string "Hello, world!" unless an input string
is provided in which case it returns "Hello, \<input string\>!".  We saw
this previously when we piped "Bob" into the function.   Notice that
the Java FDK reads from standard input and automatically puts the
content into the string passed to the function.  This greatly simplifies
the function code.

# Testing with JUnit

`fn init` also generated a JUnit test for the function which uses the
Java FDK's function test framework.  With this framework you can setup
test fixtures with various function input values and verify the results.

The generated test confirms that when no input is provided the function
returns "Hello, world!".

```java
package com.example.fn;

import com.fnproject.fn.testing.*;
import org.junit.*;

import static org.junit.Assert.*;

public class HelloFunctionTest {

    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();

    @Test
    public void shouldReturnGreeting() {
        testing.givenEvent().enqueue();
        testing.thenRun(HelloFunction.class, "handleRequest");

        FnResult result = testing.getOnlyResult();
        assertEquals("Hello, world!", result.getBodyAsString());
    }

}
```

Let's add a test that confirms that when an input string like "Bob" is
provided we get the expected result.

Add the following method to `HelloFunctionTest`:

```java
    @Test
    public void shouldReturnWithInput() {
        testing.givenEvent().withBody("Bob").enqueue();
        testing.thenRun(HelloFunction.class, "handleRequest");

        FnResult result = testing.getOnlyResult();
        assertEquals("Hello, Bob!", result.getBodyAsString());
    }
```

You can see the `withBody()` method used to specify the value of the
function input.

You can run the tests by building your function with `fn build`.  This
will cause Maven to compile and run the updated test class.  If you
opened the code in an IDE you can run the tests directly from there.

![](images/userinput.png)
>`fn build`

```sh
...
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.example.fn.HelloFunctionTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.508 sec

Results :

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
...
Successfully built 85fb9b00348e
Successfully tagged fndemouser/javafn:0.0.1
Function fndemouser/javafn:0.0.1 built successfully.
```

# Accepting JSON Input

Let's convert this function to use JSON for its input and output.
Replace the definition of `HelloFunction` with the following:

```java
package com.example.fn;

public class HelloFunction {

    public static class Input {
        public String name;
    }

    public static class Result {
        public String salutation;
    }

    public Result handleRequest(Input input) {
        Result result = new Result();
        result.salutation = "Hello " + input.name;
        return result;
    }

}
```

We've created a couple of simple Pojos to bind the JSON input and output
to and changed the function signature to use these Pojos.  The
Java FDK will automatically bind input data based on the Java arguments
to the function. JSON support is built-in but input and output binding
is extensible and you could (for instance) plug in marshallers for other
data formats like protobuf, avro or xml.

Let's build the updated function.

![](images/userinput.png)
>`fn build`

```sh
...
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.example.fn.HelloFunctionTest
An exception was thrown during Input Coercion: Failed to coerce event to user function parameter type class com.example.fn.HelloFunction$Input
...
An exception was thrown during Input Coercion: Failed to coerce event to user function parameter type class com.example.fn.HelloFunction$Input
...
Tests run: 2, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 0.893 sec <<< FAILURE!
...
Results :

Tests in error:
  shouldReturnGreeting(com.example.fn.HelloFunctionTest): One and only one response expected, but 0 responses were generated.
  shouldReturnWithInput(com.example.fn.HelloFunctionTest): One and only one response expected, but 0 responses were generated.

Tests run: 2, Failures: 0, Errors: 2, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.477 s
[INFO] Finished at: 2017-09-21T14:59:21Z
[INFO] Final Memory: 16M/128M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.12.4:test (default-test) on project hello: There are test failures.
```

Oops! as we can see this function build has fail due to test failures--we
changed the code significantly but didn't update our tests!  We really
should be doing test driven development and updating the test first but
at least our bad behavior has been caught.  Let's update the tests
to reflect our new expected results.  Replace the definition of
`HelloFunctionTest` with:

```java

package com.example.fn;

import com.fnproject.fn.testing.*;
import org.junit.*;

import static org.junit.Assert.*;

public class HelloFunctionTest {

    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();

    @Test
    public void shouldReturnGreeting(){
        testing.givenEvent().withBody("{\"name\":\"Bob\"}").enqueue();
        testing.thenRun(HelloFunction.class,"handleRequest");

        FnResult result = testing.getOnlyResult();
        assertEquals("{\"salutation\":\"Hello Bob\"}", result.getBodyAsString());
    }
}

```

In the new `shouldReturnGreeting()` test method we're passing in the
JSON document

```json
{
    "name": "Bob"
}
```
and expecting a result of
```json
{
    "salutation": "Hello Bob"
}
```

If you re-run the test via `fn build` we can see that it now passes.

# Deploying your Java Function

Now that we have our Java function updated and passing our JUnit tests
we can move onto deploying it to the Fn server.  As we're running the
server on the local machine we can save time by not pushing the
generated image out to a remote Docker repository by using the `--local`
option.

![](images/userinput.png)
>`fn deploy --local --app myapp`

```sh
Deploying javafn to app: myapp at path: /javafn
Bumped to version 0.0.2
Building image fndemouser/javafn:0.0.2
...
Successfully built 406b44a45821
Successfully tagged fndemouser/javafn:0.0.2
Updating route /javafn using image fndemouser/javafn:0.0.2...
```

Review the last line of the deploy output.  When deployed a function's
Docker image is associated with the route specified in the
`func.yaml` which defaults to the containing directory name.  In this
case the route is `/javafn`.

We can use the route to invoke the function via curl and passing the
JSON input as the body of the call.

![](images/userinput.png)
> `curl --data '{"name": "Bob"}' http://localhost:8080/r/myapp/javafn`

```sh
{"salutation":"Hello Bob"}
```

Success!

# Improving Performance

Finally you might notice that the function call takes a few hundred
milliseconds.  Try calling the function three times
in a row paying attention to how long it takes to complete each call:

![](images/userinput.png)
>`curl --data '{"name":"Tom"}' http://localhost:8080/r/myapp/javafn`

>`curl --data '{"name":"Tom"}' http://localhost:8080/r/myapp/javafn`

>`curl --data '{"name":"Tom"}' http://localhost:8080/r/myapp/javafn`


By default, fn will start a new container (and therefore
a new JVM) for each invocation. This may be what you want--as each
function call will run in its own
isolated container and process.  But you can
configure the function to re-use the same container and JVM for multiple
invocations, thus reducing latency.  This is called a 'Hot Function'.
We can turn our function into a Hot Function by changing the format on
the route:

![](images/userinput.png)
>`fn routes update myapp /javafn --format http`

Now if we call it again the first call still takes a few hundred
milliseconds to start up the container but subsequent calls are super
fast. Try calling the function repeatedly now that you've made the
format change:


![](images/userinput.png)
>`curl --data '{"name":"Tom"}' http://localhost:8080/r/myapp/javafn`

>`curl --data '{"name":"Tom"}' http://localhost:8080/r/myapp/javafn`

>`curl --data '{"name":"Tom"}' http://localhost:8080/r/myapp/javafn`

# Wrapping Up

Congratulations! You've just completed an introduction to the Fn Java
FDK.  There's so much more in the FDK than we can cover in a brief
introduction but we'll go deeper in subsequent tutorials.
