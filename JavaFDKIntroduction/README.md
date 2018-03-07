# Introduction

This tutorial introduces the
[Fn Java FDK (Function Development Kit)](https://github.com/fnproject/fdk-java).
If you haven't completed the [Introduction to Fn](../Introduction/README.md)
tutorial you should head over there before you proceed.

This tutorial takes you through the Fn developer experience for building
Java functions. It shows how easy it is to build, deploy and test
functions written in Java.

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Prerequisites

This tutorial requires you to have both Docker and Fn installed and an
Fn server running locally.  If you need help with Fn installation you
can find instructions in the
[Introduction to Fn](../Introduction/README.md) tutorial.

## Getting Started

Let's start by creating a new function.  In a terminal type the following:

![](images/userinput.png)
>`fn init --runtime java javafn`

The output will be:

```sh
Creating function at: /javafn
Runtime: java
Function boilerplate generated.
func.yaml created.
```

![](images/userinput.png)
>```sh
>cd javafn
>```

The `fn init` command creates an simple function with a bit of boilerplate to get you
started. The `--runtime` option is used to indicate that the function
we're going to develop will be written in Java 9, the default version
as of this writing. A number of other runtimes are also supported.  

__If__ you have the `tree` utility installed
you can see the directory structure that the `init` command has created.

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
>```sh
>cat func.yaml
>```

```yaml
version: 0.0.1
runtime: java
cmd: com.example.fn.HelloFunction::handleRequest
build_image: fnproject/fn-java-fdk-build:jdk9-1.0.56
run_image: fnproject/fn-java-fdk:jdk9-1.0.56
format: http
```

In the case of a Java function, the following properties are created:

* **version:** the version of the function.
* **runtime:** the language used for this function.
* **cmd:** the `cmd` property is set to the fully
qualified name of the function class and the method that should be invoked when your `javafn` function is called.
* **build_image:** the image used to build your function's image.
* **run_image:** the image your function runs in.
* **format:** the input/output formate used by the function to communication. The default is to use `http` headers.

The Java function init also generates a Maven `pom.xml` file to build and test your function.  The pom includes the Fn Java FDK runtime and test libraries your function needs.

## Running your Function

Let's build and run the generated function.  We're working locally and
won't be pushing our function images to a Docker registry like Docker
Hub. So before we build let's set `FN_REGISTRY` to a local-only registry
username like `fndemouser`.

![](images/userinput.png)
>```sh
> export FN_REGISTRY=fndemouser
>```

Now we're ready to run.  Depending on whether this is your first time
developing a Java function you may or may not see Docker images being
pulled from Docker Hub.  Once the necessary base images are downloaded
subsequent operations will be faster.

As the function is built using Maven you may also see a number of Java
packages being downloaded.  This is also expected the first time you
run a function and trigger a build.

![](images/userinput.png)
>`fn run`

Here's what the output looks like:

```sh
Building image fndemouser/javafn:0.0.1 ....................
Hello, world!
```

In the background, Maven compiles the code and runs any
tests, the function is packaged into a container, and then the function is run locally to produce the output "Hello, world!".

Let's try one more thing and pipe some input into the function.  In your
terminal type:

![](images/userinput.png)
>```sh
> echo -n "Bob" | fn run
>```

returns:

```sh
Hello, Bob!
```

Instead of "Hello, world!" the function has read the input string "Bob" from standard input and returned "Hello, Bob!".

## Exploring the Code

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
is provided in which case it returns "Hello, &lt;input string&gt;!".  We saw
this previously when we piped "Bob" into the function.   Notice that
the Java FDK reads from standard input and automatically puts the
content into the string passed to the function.  This greatly simplifies
the function code.

## Testing with JUnit

The `fn init` command also generated a JUnit test for the function which uses the
Java FDK's function test framework.  With this framework you can setup
test fixtures with various function input values and verify the results.

The generated test confirms that when no input is provided the function returns "Hello, world!".

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
Building image fndemouser/javafn:0.0.1 .......
Function fndemouser/javafn:0.0.1 built successfully.
```

## Accepting JSON Input

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
is extensible and you could plug in marshallers for other
data formats like protobuf, avro or xml.

Let's build the updated function:

![](images/userinput.png)
>`fn build`

returns:

```sh
Building image fndemouser/javafn:0.0.1 .......
Error during build. Run with `--verbose` flag to see what went wrong. eg: `fn --verbose CMD`
ERROR: error running docker build: exit status 1
```

To find out what happened rerun build with the verbose switch:

![](images/userinput.png)
>`fn --verbose build`

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

Oops! as we can see this function build has failed due to test failures--we
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

```js
{
    "name": "Bob"
}
```
and expecting a result of
```js
{
    "salutation": "Hello Bob"
}
```

If you re-run the test via `fn -verbose build` we can see that it now passes:

![](images/userinput.png)
>`fn --verbose build`


## Deploying your Java Function

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
Updating route /javafn using image fndemouser/javafn:0.0.2...
```

Review the last line of the deploy output.  When deployed, a function's
Docker image is associated with a route which is the function's name and
the function's name is taken from the containing directory.  In this
case the route is `/javafn`.

We can use the route to invoke the function via curl and passing the
JSON input as the body of the call.

![](images/userinput.png)
>```sh
> curl --data '{"name": "Bob"}' http://localhost:8080/r/myapp/javafn
>```

returns:

```js
{"salutation":"Hello Bob"}
```

Success!

## Wrapping Up

Congratulations! You've just completed an introduction to the Fn Java
FDK.  There's so much more in the FDK than we can cover in a brief
introduction but we'll go deeper in subsequent tutorials.

**Go:** [Back to Contents](../README.md)
