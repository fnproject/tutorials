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

### Before you Begin
* Set aside about 30 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../install/README.md).
    * Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". See [here](../install/README.md).)


## Your First Function

Let's start by creating a new function.  In a terminal type the following:

![](images/userinput.png)
>`fn init --runtime java --trigger http javafn`

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
schema_version: 20180708
name: javafn
version: 0.0.1
runtime: java
build_image: fnproject/fn-java-fdk-build:jdk9-1.0.64
run_image: fnproject/fn-java-fdk:jdk9-1.0.64
cmd: com.example.fn.HelloFunction::handleRequest
format: http
triggers:
- name: javafn-trigger
  type: http
  source: /javafn-trigger
format: http
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* schema_version--identifies the version of the schema for this function file. * version--the version of the function.
* runtime--the language used for this function.
* cmd--the `cmd` property is set to the fully qualified name of the function
class and the method that should be invoked when your `javafn` function is
called.
* build_image--the image used to build your function's image.
* run_image--the image your function runs in.
* format--the function uses JSON as its input/output method ([see: Open
Function Format](https://github.com/fnproject/fn/blob/master/docs/developers/function-format.md)).
* triggers--identifies the automatically generated trigger name and source. For
example, this function would be executed from the URL
<http://localhost:8080/t/appname/gofn-trigger>. Where appname is the name of
the app chosen for your function when it is deployed.

The Java function init also generates a Maven `pom.xml` file to build and test your function.  The pom includes the Fn Java FDK runtime and test libraries your function needs.


## Deploy your Java Function

As we're running the server on the local machine we can save time by not pushing
the generated image out to a remote Docker repository by using the `--local`
option.

![](images/userinput.png)
>`fn deploy --app myapp --local`

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


## Invoke your Deployed Function

Use the the `fn invoke` command to call your function from the command line.

### Invoke with the CLI

The first is using the Fn CLI which makes invoking your function relatively
easy.  Type the following:

![user input](images/userinput.png)
>```sh
> fn invoke myapp javafn
>```

which results in:

```txt
Hello, World!
```

In the background, Maven compiles the code and runs any tests, the function is
packaged into a container, and then the function is run  to produce the output
"Hello, world!".

You can also pass data to the invoke command. For example:

![user input](images/userinput.png)
>```sh
> echo -n 'Bob' | fn invoke myapp javafn
>```

```txt
Hello, Bob!
```

"Bob" was passed to the function where it is processed and returned in the output.


## Exploring the Code

We've generated, compiled, deployed, and invoked the Java function so let's take
a look at the code.  You may want to open the code in your favorite IDE or
editor.

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

The `fn init` command also generated a JUnit test for the function which uses
the Java FDK's function test framework.  With this framework you can setup test
fixtures with various function input values and verify the results.

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
will cause Maven to compile and run the updated test class.  You can also invoke your tests directly from Maven using `mvn test` or from your IDE.

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


## Invoke with Curl

The other way to invoke your function is via HTTP. With the changes to the code,
we can pass JSON and return JSON from the the function.  The Fn server exposes
our deployed function at `http://localhost:8080/t/myapp/javafn-trigger`, a URL
that incorporates our application and function trigger as path elements.

Redeploy your updated Java function

![user input](images/userinput.png)
>```sh
> fn deploy --app myapp --local
>```

Use `curl` to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -H "Content-Type: application/json" http://localhost:8080/t/myapp/javafn-trigger
>```

The result is now in a JSON format.

```js
{"salutation":"Hello World"}
```

We can pass JSON data to our function and get the value of name passed to
the function back.

![user input](images/userinput.png)
>```
> curl -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/t/myapp/javafn-trigger
>```

The result is now in JSON format with the passed value returned.

```js
{"salutation":"Hello Bob"}
```



## Wrap Up

Congratulations! You've just completed an introduction to the Fn Java
FDK.  There's so much more in the FDK than we can cover in a brief
introduction but we'll go deeper in subsequent tutorials.

**Go:** [Back to Contents](../README.md)
