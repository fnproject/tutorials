# Introduction to Fn with Java

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
    * Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". [See here](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context).)


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
build_image: fnproject/fn-java-fdk-build:jdk9-1.0.75
run_image: fnproject/fn-java-fdk:jdk9-1.0.75
cmd: com.example.fn.HelloFunction::handleRequest
format: http-stream
triggers:
- name: javafn-trigger
  type: http
  source: /javafn-trigger
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
* format--the function uses `http-stream` as its input/output method ([see: Open
Function Format](https://github.com/fnproject/docs/blob/master/fn/develop/function-format.md)).
* triggers--identifies the automatically generated trigger name and source. For
example, this function would be executed from the URL
<http://localhost:8080/t/appname/javafn-trigger>. Where appname is the name of
the app chosen for your function when it is deployed.

The Java function init also generates a Maven `pom.xml` file to build and test your function.  The pom includes the Fn Java FDK runtime and test libraries your function needs.


## Deploy your Java Function

With the `javafn` directory containing `pom.xml` and `func.yaml` you've got
everything you need to deploy the function to Fn server. This server could be
running in the cloud, in your datacenter, or on your local machine like we're
doing here.

Make sure your context is set to default and you are using a demo user. Use the `fn list contexts` command to check.

![user input](images/userinput.png)
>```sh
> fn list contexts
>```

```cs
CURRENT	NAME	PROVIDER	API URL			        REGISTRY
*       default	default		http://localhost:8080	fndemouser
```

If your context is not configured, please see [the context installation instructions](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context) before proceeding. Your context determines where your function is deployed.

Deploying your function is how you publish your function and make it accessible
to other users and systems. To see the details of what is happening during a
function deploy,  use the `--verbose` switch.  The first time you build a
function of a particular language it takes longer as Fn downloads the necessary
Docker images. The `--verbose` option allows you to see this process.

![](images/userinput.png)
>`fn --verbose deploy --app java-app --local`

```yaml
Deploying javafn to app: java-app
Bumped to version 0.0.2
Building image fndemouser/javafn:0.0.2 
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon  14.34kB
Step 1/11 : FROM fnproject/fn-java-fdk-build:jdk9-1.0.75 as build-stage
jdk9-1.0.75: Pulling from fnproject/fn-java-fdk-build
c2ad77de49ce: Already exists 
d6485a2cca95: Already exists 
4f4ea4e6ab41: Already exists 
649f9534d72b: Already exists 
6e47a95e0938: Already exists 
d46a954202a9: Pull complete 
5a73fd16c382: Pull complete 
6028b8203fcc: Pull complete 
98a6eaf5f83b: Pull complete 
7afb9733d3e4: Pull complete 
107a8e7e5bd9: Pull complete 
384cc00c5a4f: Pull complete 
bb19e03dd551: Pull complete 
b7f4aa3f1f42: Pull complete 
Digest: sha256:5be1aff1f7107b8a1a50e4b906b91fc6487977a9e70639e6133cdaaa8b58d74d
Status: Downloaded newer image for fnproject/fn-java-fdk-build:jdk9-1.0.75
 ---> 10c10a1cd2ae
Step 2/11 : WORKDIR /function
 ---> Running in 68884bc0f125
Removing intermediate container 68884bc0f125
 ---> 44432a740323
Step 3/11 : ENV MAVEN_OPTS -Dhttp.proxyHost= -Dhttp.proxyPort= -Dhttps.proxyHost= -Dhttps.proxyPort= -Dhttp.nonProxyHosts= -Dmaven.repo.local=/usr/share/maven/ref/repository
 ---> Running in b6f5256bc328
Removing intermediate container b6f5256bc328
 ---> 401d12925a1f
Step 4/11 : ADD pom.xml /function/pom.xml
 ---> 92803f3eba9d
Step 5/11 : RUN ["mvn", "package", "dependency:copy-dependencies", "-DincludeScope=runtime", "-DskipTests=true", "-Dmdep.prependGroupId=true", "-DoutputDirectory=target", "--fail-never"]
 ---> Running in 13af70800045
[INFO] Scanning for projects...
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.3/maven-compiler-plugin-3.3.pom
...more maven downloads here, removed for brevity...

[INFO] 
[INFO] ------------------------< com.example.fn:hello >------------------------
[INFO] Building hello 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
Downloading from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/api/1.0.75/api-1.0.75.pom
...more maven downloads here, removed for brevity...

[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ hello ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /function/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.3:compile (default-compile) @ hello ---
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-toolchain/2.2.1/maven-toolchain-2.2.1.pom
...more maven downloads here, removed for brevity...

[INFO] No sources to compile
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ hello ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /function/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.3:testCompile (default-testCompile) @ hello ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ hello ---
[INFO] Tests are skipped.
[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ hello ---
[WARNING] JAR will be empty - no content was marked for inclusion!
[INFO] Building jar: /function/target/hello-1.0.0.jar
[INFO] 
[INFO] --- maven-dependency-plugin:2.8:copy-dependencies (default-cli) @ hello ---
[INFO] Copying api-1.0.75.jar to /function/target/com.fnproject.fn.api-1.0.75.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 5.628 s
[INFO] Finished at: 2018-10-16T22:46:45Z
[INFO] ------------------------------------------------------------------------
Removing intermediate container 13af70800045
 ---> 641e7944d2af
Step 6/11 : ADD src /function/src
 ---> ca6c3f1b91ef
Step 7/11 : RUN ["mvn", "package"]
 ---> Running in 1bb7f99d39f8
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------------< com.example.fn:hello >------------------------
[INFO] Building hello 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ hello ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /function/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.3:compile (default-compile) @ hello ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /function/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ hello ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /function/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.3:testCompile (default-testCompile) @ hello ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /function/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ hello ---
[INFO] Surefire report directory: /function/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.example.fn.HelloFunctionTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.394 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ hello ---
[INFO] Building jar: /function/target/hello-1.0.0.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2.978 s
[INFO] Finished at: 2018-10-16T22:46:51Z
[INFO] ------------------------------------------------------------------------
Removing intermediate container 1bb7f99d39f8
 ---> f927528437d9
Step 8/11 : FROM fnproject/fn-java-fdk:jdk9-1.0.75
jdk9-1.0.75: Pulling from fnproject/fn-java-fdk
c2ad77de49ce: Already exists 
d6485a2cca95: Already exists 
4f4ea4e6ab41: Already exists 
649f9534d72b: Already exists 
6e47a95e0938: Already exists 
8068f9696b91: Pull complete 
6c20847ce4f9: Pull complete 
10e1d186dcc9: Pull complete 
Digest: sha256:a317732d9dd12ae8f9078591e86bba2ed569c7ec823e4c763ff176c07c8add3f
Status: Downloaded newer image for fnproject/fn-java-fdk:jdk9-1.0.75
 ---> 5ca9da5945c4
Step 9/11 : WORKDIR /function
 ---> Running in 0f94631cd8f9
Removing intermediate container 0f94631cd8f9
 ---> a6da0230bf05
Step 10/11 : COPY --from=build-stage /function/target/*.jar /function/app/
 ---> 9fb385022aeb
Step 11/11 : CMD ["com.example.fn.HelloFunction::handleRequest"]
 ---> Running in 8b571cbd24af
Removing intermediate container 8b571cbd24af
 ---> 0cbe66bb9e2b
Successfully built 0cbe66bb9e2b
Successfully tagged fndemouser/javafn:0.0.2

Updating function javafn using image fndemouser/javafn:0.0.2...
Successfully created app:  java-app
Successfully created function: javafn with fndemouser/javafn:0.0.2
Successfully created trigger: javafn-trigger
```

All the steps to load the current language Docker image are displayed.

Functions are grouped into applications so by specifying `--app java-app`
we're implicitly creating the application `java-app` and associating our
function with it.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message
`Updating function javafn using image fndemouser/javafn:0.0.2...`
let's us know that the function is packaged in the image
"fndemouser/javafn:0.0.2".

Note that the containing folder name `javafn` was used as the name of the
generated Docker container and used as the name of the function that
container was bound to. By convention it is also used to create the trigger name
`javafn-trigger`.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.


## Invoke your Deployed Function

Use the the `fn invoke` command to call your function from the command line.

### Invoke with the CLI

The first is using the Fn CLI which makes invoking your function relatively
easy.  Type the following:

![user input](images/userinput.png)
>```sh
> fn invoke java-app javafn
>```

which results in:

```txt
Hello, World!
```

In the background, Maven compiles the code and runs any tests, the function is
packaged into a container, and then the function is run to produce the output
"Hello, world!".

You can also pass data to the invoke command. For example:

![user input](images/userinput.png)
>```sh
> echo -n 'Bob' | fn invoke java-app javafn
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
Building image fndemouser/javafn:0.0.2 .......
Function fndemouser/javafn:0.0.2 built successfully.
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
Building image fndemouser/javafn:0.0.2 .....
Error during build. Run with `--verbose` flag to see what went wrong. eg: `fn --verbose CMD`

Fn: error running docker build: exit status 1

See 'fn <command> --help' for more information. Client version: 0.5.16
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
> fn deploy --app java-app --local
>```

Use `curl` to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -H "Content-Type: application/json" http://localhost:8080/t/java-app/javafn-trigger
>```

The result is now in a JSON format.

```js
{"salutation":"Hello World"}
```

We can pass JSON data to our function and get the value of name passed to
the function back.

![user input](images/userinput.png)
>```
> curl -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/t/java-app/javafn-trigger
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
