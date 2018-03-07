# Introduction to Fn

Fn is a lightweight Docker-based serverless functions platform you can
run on your laptop, server, or cloud.  In this introductory tutorial
we'll walk through installing Fn, develop a function using the Go
programming language (without installing any Go tools!) and
deploy them to a local Fn server.  We'll also learn about the core Fn
concepts like applications and routes.

So let's get started!

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.


## Installing Fn

Setting up a working Fn install is a two-step process.  First you need
to ensure you have the necessary prerequisites and then you can install
Fn itself.

### Prerequisites

Before we can install Fn you'll need:

1. A computer running Linux or MacOS.  If you have a Windows machine the
easiest thing to do is install [VirtualBox](https://www.virtualbox.org/)
and run a free Linux virtual machine.
2. [Docker](https://www.docker.com/) 17.05 (or higher) needs to be
installed and running. 

> __NOTE__ In this tutorial we'll work in a purely local development
mode.  However, when deploying functions to a remote Fn server, a Docker
Hub (or other Docker registry) account is required.

That's it.  You can use your favorite IDE for function development.
However, for this tutorial, an IDE isn't necessary.


### Downloading and Installing Fn

From a terminal type the following:


![](images/userinput.png)
>`curl -LSs https://raw.githubusercontent.com/fnproject/cli/master/install | sh`

Once installed you'll see the Fn version printed out.  You should see
something similar to the following displayed (although likely with a later 
version number):

```sh
fn version 0.4.44
```

### Starting Fn Server

The final install step is to start the Fn server.  Since Fn runs on
Docker it'll need to be up and running too.

To start Fn you can use the `fn` command line interface (CLI).  Type the
following but note that the process will run in the foreground so that
it's easy to stop with Ctrl-C:

![user input](images/userinput.png)
>`fn start`

You should see output similar to:

```sh
time="2017-09-18T14:37:13Z" level=info msg="datastore dialed" datastore=sqlite3 max_idle_connections=256
time="2017-09-18T14:37:13Z" level=info msg="available memory" ram=1655975936
time="2017-09-18T14:37:13Z" level=info msg="Serving Functions API on address `:8080`"

      ______
     / ____/___
    / /_  / __ \
   / __/ / / / /
  /_/   /_/ /_/
```

Let's verify everthing is up and running correctly.

**Open a new terminal** and run the following:

![user input](images/userinput.png)
>`fn version`

You should see the version of the fn CLI (client) and server displayed (your version will
likely differ):

```sh
Client version:  0.4.44
Server version:  0.3.335
```

If the server version is "?" then the fn CLI cannot reach the server.  If 
this happens it's likely you have something else running on port 8080. In this
case stop the other process, and stop (ctrl-c) and restart the fn server as
described previously.

## Your First Function

Let's start with a very simple "hello world" function written in
[Go](https://golang.org/). Don't worry, you don't need to know Go!  In
fact you don't even need to have Go installed on your development
machine as Fn provides the necessary Go compiler and tools as a Docker
container.  Let's walk through your first function to become familiar
with the process and how Fn supports development.

Before we start developing we need to set the `FN_REGISTRY`
environment variable.  Normally, it's set to your Docker Hub username.
However in this tutorial we'll work in local development mode so we can set
the `FN_REGISTRY` variable to an arbitrary value. Let's use `fndemouser`.

![user input](images/userinput.png)
>`export FN_REGISTRY=fndemouser`


With that out of the way, create a new directory named "hello" and cd
into it:

![user input](images/userinput.png)
>`mkdir hello`
>
>`cd hello`

Copy and paste the following Go code into a file named `func.go`.

![user input](images/userinput.png)
>```go
>package main
>
>import (
>  "fmt"
>)
>
>func main() {
>  fmt.Println("Hello from Fn!")
>}
>```

This function just prints "Hello from Fn!" to standard output.  It takes
no arguments and returns no results. So it's as simple as possible.  Of
course, you can write functions that accept a number of different types
of arguments and this is explored in other Fn tutorials.


#### Initializing your Function Configuration

Let's use the `fn` CLI to initialize this function's configuration.

![user input](images/userinput.png)
> `fn init --runtime go hello`

```sh
Found go function, assuming go runtime.
func.yaml created.
```

`fn` found your `func.go` file and generated a `func.yaml` function 
configuration file with contents that should look like:

```yaml
name: hello
version: 0.0.1
runtime: go
entrypoint: ./func
```

You can see the `func.yaml` contents by typing:

![user input](images/userinput.png)
>cat func.yaml


#### Understanding func.yaml

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* the version--automatically starting at 0.0.1
* the name of the runtime/language--which was set
automatically based on the presence of `func.go`
* the name of the executable to invoke when your function is called,
in this case `./func` 

There are other user specifiable properties but these will suffice for
this example.  Note that the name of your function is taken from the containing
folder name.  We'll see this come into play later on.


### Running Your First Function

With the `hello` directory containing `func.go` and `func.yaml` you've
got everything you need to run the function.  So let's run it and
observe the output.  Note that the first time you build a
function of a particular language it takes longer as Fn downloads
the necessary Docker images.

![user input](images/userinput.png)
> `fn run`

```sh
Building image fndemouser/hello:0.0.1 ...
Hello from Fn!
```

The last line of output should be "Hello from Fn!" that was produced
by the Go statement `fmt.Println("Hello from Fn!")`.

If you ever want more details on what a given fn command is doing behind the
scenes you can add the `--verbose` switch.  Let's rerun with verbose output
enabled.

![user input](images/userinput.png)
> `fn --verbose run`

```sh
Building image fndemouser/hello:0.0.1
Sending build context to Docker daemon  4.096kB
Step 1/8 : FROM fnproject/go:dev as build-stage
 ---> 57ed8b626466
Step 2/8 : WORKDIR /function
 ---> Using cache
 ---> c83f43743f5c
Step 3/8 : ADD . /go/src/func/
 ---> b0dcb987a280
Step 4/8 : RUN cd /go/src/func/ && go build -o func
 ---> Running in 494a060b5fdb
Removing intermediate container 494a060b5fdb
 ---> 6c89d2cf3a6d
Step 5/8 : FROM fnproject/go
 ---> 10f82b0de851
Step 6/8 : WORKDIR /function
 ---> Using cache
 ---> a46fefe41a6c
Step 7/8 : COPY --from=build-stage /go/src/func/func /function/
 ---> Using cache
 ---> 7bb5e7dadecc
Step 8/8 : ENTRYPOINT ["./func"]
 ---> Using cache
 ---> 4738aa280fad
Successfully built 4738aa280fad
Successfully tagged fndemouser/hello:0.0.1

Hello from Fn!
```

### Understanding fn run

If you have used Docker before the output of `fn --verbose run` should look
familiar--it looks like the output you see when running `docker build`
with a Dockerfile.  Of course this is exactly what's happening!  When
you run a function like this Fn is dynamically generating a Dockerfile
for your function, building a container, and then running it.

> __NOTE__: Fn is actually using two images.  The first contains
the language compiler and is used to generate a binary.  The second
image packages only the generated binary and any necessary language
runtime components. Using this strategy, the final function image size
can be kept as small as possible.  Smaller Docker images are naturally
faster to push and pull from a repository which improves overall
performance.  For more details on this technique see [Multi-Stage Docker
Builds for Creating Tiny Go Images](https://medium.com/travis-on-docker/multi-stage-docker-builds-for-creating-tiny-go-images-e0e1867efe5a).

`fn run` is a local operation.  It builds and packages your function
into a container image which resides on your local machine.  As Fn is
built on Docker you can use the `docker` command to see the local
container image you just generated.

You may have a number of Docker images so use the following command
to see only those created by fndemouser:

![user input](images/userinput.png)
>`docker images | grep fndemouser`

You should see something like:

```sh
fndemouser/hello                               0.0.1               d64b4a1a15b9        2 minutes ago      6.98MB
```

## Deploying Your First Function

When we used `fn run` your function was run in your local environment.
Now let's deploy your function to the Fn server we started previously.
This server could be running in the cloud, in your datacenter, or on
your local machine like we're doing here.

Deploying your function is how you publish your function and make it
accessible to other users and systems.

In your terminal type the following:

![user input](images/userinput.png)
> `fn deploy --app myapp --local`

You should see output similar to:

```sh
Deploying hello to app: myapp at path: /hello
Bumped to version 0.0.2
Building image fndemouser/hello:0.0.2 ..
Updating route /hello using image fndemouser/hello:0.0.2...
```

Functions are grouped into applications so by specifying `--app myapp`
we're implicitly creating the application "myapp" and associating our
function with it.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server. 

The output message
`Updating route /hello using image fndemouser/hello:0.0.2`
let's us know that the function packaged in the image
"fndemouser/hello:0.0.2" has been bound by the Fn server to the route
"/hello".  We'll see how to use the route below.

Note that the containing folder name 'hello' was used as the name of the
generated Docker container and used as the name of the route that
container was bound to.

The fn CLI provides a couple of commands to let us see what we've deployed.
`fn apps list` returns a list of all of the defined applications. 

![user input](images/userinput.png)
> `fn apps list`

Which, in our case, returns the name of the application we created when we
deployed our hello function:

```sh
myapp
```

We can also see the functions that are defined by an application.  Since
functions are exposed via routes, the `fn routes list <appname>` command
is used.  To list the functions included in "myapp" we can type:

![user input](images/userinput.png)
>`fn routes list myapp`

```sh
path    image                   endpoint
/hello  fndemouser/hello:0.0.2  localhost:8080/r/myapp/hello
```

The output confirms that myapp contains a `hello` function that is implemented
by the Docker container `fndemouser/hello:0.0.2` which may be invoked via the
specified URL.  Now that we've confirmed deployment was successsful, let's
call our function.

## Calling Your Deployed Function

There are two ways to call your deployed function.  The first is using
the `fn` CLI which makes invoking your function relatively easy.  Type
the following:

![user input](images/userinput.png)
>`fn call myapp /hello`

which results in our familiar output message.

```sh
Hello from Fn!
```

Of course this is unchanged from when you ran the function locally.
However when you called "myapp /hello" the fn server looked up the
"myapp" application and then looked for the Docker container image
bound to the "/hello" route.

The other way to call your function is via HTTP.  The Fn server
exposes our deployed function at "http://localhost:8080/r/myapp/hello", a URL
that incorporates our application and function route as path elements.

Use curl to invoke the function:

![user input](images/userinput.png)
>`curl http://localhost:8080/r/myapp/hello`

The result is once again the same.

```sh
Hello from Fn!
```

## Wrapping Up

Congratulations!  In this tutorial you've accomplished a lot.  You've
installed Fn, started up an Fn server, created your first function,
run it locally, and then deployed it where it can be invoked over HTTP.

**Go:** [Back to Contents](../README.md)
