# Creating a Native Java Function using GraalVM

This tutorial walks through how to use Java runtime with 
[GraalVM native-image feature](https://www.graalvm.org/docs/reference-manual/aot-compilation/) 
in order to compile Java code into a native executable and 
package it as a Fn function using a Docker image.

The steps include compiling Java code Ahead-of-Time (AOT) into a 
native executable that does not require a JVM or any extra
dependencies to run inside a Docker container.

As you make your way through this tutorial, look out for this icon.
![](../images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Prerequisites

This tutorial requires you to have both Docker and Fn installed and an
Fn server running locally.  If you need help with Fn installation you
can find instructions in the
[Introduction to Fn](../Introduction/README.md) tutorial.

# Getting Started

## Create a Java function

![](../images/userinput.png) Let's start by creating a new Java function. In a terminal type the following:

>```sh
>fn init --runtime java8 nativejavafn
>```

The output will be:

```
Creating function at: /nativejavafn
Runtime: java8
Function boilerplate generated.
func.yaml created.
```

![](../images/userinput.png) Enter into created directory:
>```sh
>cd nativejavafn
>```

In this tutorial we are using `java8` runtime because current release of 
GraalVM 1.0.0 uses OpenJDK 8 as default JDK.

## reflection.json

GraalVM has some special declaration in order to be able to access Java
classes using reflection. This is true for Java functions entrypoint and
additionally in POJOs used in JSON serialization if present.
More details can be found in GraalVM [documentation](https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.m).

![](../images/userinput.png) Copy/paste the following into a file named `reflection.json`:

```json
[
    {
      "name": "com.example.fn.HelloFunction",
      "allDeclaredMethods": true,
      "methods": [
        { "name": "<init>", "parameterTypes": [] }
      ]
    }
]
```

The above code illustrates an example `reflection.json` for Java Fn entrypoint.

## Dockerfile

The `Dockerfile` for our function uses Docker multi-stage build to build Maven
project and generate a native executable to finally copy it into a new scratch image.

This is required because the AoT compilation needs to be done in the same architecture that will run the
function, in this case, Linux.

_Note: There are comments that explain what `Dockerfile` is doing step-by-step._

![](../images/userinput.png) Copy/paste the following into a file named `Dockerfile`:

```dockerfile
# Apache Maven docker image used for maven build
FROM maven:3-jdk-8 as build-jar

# Add project sources 
ADD src app/src/
ADD pom.xml app/

# Set workdir
WORKDIR /app

# Execute maven build
RUN mvn test package dependency:copy-dependencies

# GraalVM docker image used for AoT compilation
FROM panga/graalvm-ce:latest as build-aot

# Copy application jars from build-jar stage
COPY --from=build-jar /app/target/*.jar /app/
COPY --from=build-jar /app/target/dependency/*.jar /app/

# Add reflection.json from function folder
ADD reflection.json /app/

# Execute native-image tool and custom settings
RUN native-image \
    --no-server \
    --static \
    -H:Name=app/fn \
    -H:ReflectionConfigurationFiles=app/reflection.json \
    -H:+ReportUnsupportedElementsAtRuntime \
    -cp "app/*" \
    com.fnproject.fn.runtime.EntryPoint

# Create new image from scratch
FROM scratch

# Set workdir
WORKDIR /app

# Copy generated native executable from build-aot stage
COPY --from=build-aot /app/fn /app/fn

# Set entrypoint to native executable
ENTRYPOINT [ "./fn" ]

# Set command to function entrypoint
CMD [ "com.example.fn.HelloFunction::handleRequest" ]
```

## func.yaml

As usual, the init command has created a `func.yaml` file for your
function and Java runtime values set.

Although, we need to change the `func.yaml` file to use `Dockerfile`
instead of standard Java runtime and also add `Maven` build commands.
In our case, we need to compile, test and package Java function plus
copy it's dependencies in order to do the AoT compilation.

![](../images/userinput.png) Copy/paste the following into a file named `func.yaml`:

```yaml
name: nativejavafn
version: 0.0.1
runtime: docker
```

## Building the Function Image

Let's build and run the native Java function. We're working locally and
won't be pushing our function images to a Docker registry like Docker
Hub.

![](../images/userinput.png) In a terminal type the following:
>`fn build`

In the background, Maven compiles the code and runs any tests and the function is packaged into a container.

This step can take some time due to Maven may also download dependencies and also
Docker images required by the build.

The output looks like:

```
Building image nativejavafn:0.0.1 ....................
```

![](../images/userinput.png) After the build is finished, in a terminal type the following:
>`docker images -a | grep nativejavafn`

The output looks like:

```
nativejavafn      0.0.1     0ed3ac2ebd99      2 minutes ago      13.5MB
```

The resulting image size is very small compared to default Java ones as it only contains the generated static executable on top of a scratch Docker image.

## Running the Function

![](../images/userinput.png) In a terminal type the following:
>`fn run`

Here's what the output looks like:

```
Building image nativejavafn:0.0.1 ....................
Hello, world!
```

![](../images/userinput.png) Let's try one more thing and pipe some input into the function.
In your terminal type:

>```sh
> echo -n "Joe" | fn run
>```

It returns:

```sh
Hello, Joe!
```

Instead of "Hello, world!" the function has read the input string "Joe" from 
standard input and returned "Hello, Joe!".

## Wrapping Up

Congratulations! You've just completed this tutorial and created your first Native Java Function.  
The resulting Docker image is very small because it contains only the native executable instead of the whole JVM.  
It also has improved cold startup times and reduced memory footprint compared to standard Java functions.  
You can look at [Java FDK](../JavaFDKIntroduction/README.md) for more detailed usage of Java functions inside Fn.  

**Go:** [Back to Contents](../README.md)
