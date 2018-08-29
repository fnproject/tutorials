# Introduction to Fn with Go
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll walk through
developing a function using the Go programming language (without installing any
Go tools!) and deploying that function to a local Fn server.  We'll also learn
about the core Fn concepts like applications and triggers.

### Before you Begin
* Set aside about 15 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../install/README.md).

As you make your way through this tutorial, look out for this icon. ![User Input
Icon](images/userinput.png) Whenever you see it, it's time for you to perform an
action.

## Your First Function
Now that Fn is up and running, let's start with a very simple "hello world"
function written in [Go](https://golang.org/). Don't worry, you don't need to
know Go!  In fact you don't even need to have Go installed on your development
machine as Fn provides the necessary Go compiler and tools as a Docker
container.  Let's walk through your first function to become familiar with the
process and how Fn supports development.

Before we start developing we need to set the `FN_REGISTRY`
environment variable.  Normally, it's set to your Docker Hub username.
However in this tutorial we'll work in local development mode so we can set
the `FN_REGISTRY` variable to an arbitrary value. Let's use `fndemouser`.

![User Input Icon](images/userinput.png)
>```sh
> export FN_REGISTRY=fndemouser
>```

With that out of the way, let's create a new function. In the terminal type the
following.

![User Input Icon](images/userinput.png)
>```sh
> fn init --runtime go --trigger http gofn
>```

The output will be

```yaml
Creating function at: /gofn
Runtime: go
Function boilerplate generated.
func.yaml created.
```

The `fn init` command creates an simple function with a bit of boilerplate to
get you started. The `--runtime` option is used to indicate that the function
we're going to develop will be written in Go. A number of other runtimes are
also supported.  Fn creates the simple function along with several supporting files in the `/gofn` directory.

### Reviewing your Function File

With your function created change into the `/gofn` directory.

![User Input Icon](images/userinput.png)
>```sh
> cd gofn
>```

Now get a list of the directory contents.

![User Input Icon](images/userinput.png)
>```sh
> ls
>```

```txt
Gopkg.toml func.go func.yaml  test.json
```

The `func.go` file which contains your actual Go function is generated along
with several supporting files. To view your Go function type:

![User Input Icon](images/userinput.png)
>```sh
> cat func.go
>```

```go
package main

import (
	"context"
	"encoding/json"
	"fmt"
	"io"

	fdk "github.com/fnproject/fdk-go"
)

func main() {
	fdk.Handle(fdk.HandlerFunc(myHandler))
}

type Person struct {
	Name string `json:"name"`
}

func myHandler(ctx context.Context, in io.Reader, out io.Writer) {
	p := &Person{Name: "World"}
	json.NewDecoder(in).Decode(p)
	msg := struct {
		Msg string `json:"message"`
	}{
		Msg: fmt.Sprintf("Hello %s", p.Name),
	}
	json.NewEncoder(out).Encode(&msg)
}
```

This function looks for JSON input in the form of `{"name": "Bob"}`. If this
JSON example is passed to the function, the function returns `{"message":"Hello
Bob"}`. If no JSON data is found, the function returns `{"message":"Hello
World"}`.  

### Understanding func.yaml
The `fn init` command generated a `func.yaml` function
configuration file. Let's look at the contents:

![User Input Icon](images/userinput.png)
>```sh
> cat func.yaml
>```

```yaml
schema_version: 20180708
name: gofn
version: 0.0.1
runtime: go
entrypoint: ./func
format: json
triggers:
- name: gofn-trigger
  type: http
  source: /gofn-trigger
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* schema_version--identifies the version of the schema for this function file. Essentially, it determines which fields are present in `func.yaml`.
* name--the name of the function. Matches the directory name.
* version--automatically starting at 0.0.1
* runtime--the name of the runtime/language which was set based on the value set
in `--runtime`.
* entrypoint--the name of the executable to invoke when your function is called,
in this case `./func`
* format--the function uses JSON as its input/output method ([see: Open
Function Format](https://github.com/fnproject/fn/blob/master/docs/developers/function-format.md)).
* triggers--identifies the automatically generated trigger name and source. For
example, this function would be executed from the URL
<http://localhost:8080/t/appname/gofn-trigger>. Where appname is the name of
the app chosen for your function when it is deployed.

There are other user specifiable properties but these will suffice for
this example.  Note that the name of your function is taken from the containing
folder name.  We'll see this come into play later on.

### Other Function Files
The `fn init` command generated two other files.

* `Gopkg.toml` --  the Go dep tool dependency management tool file which
specifies all the dependencies for your function.
* `test.json` -- a test file that is used to test your function, it defines an
input and the output of the function, helps to identify if the function works
correctly or not. Function testing is not covered in this tutorial.


## Running Your First Function

With the `gofn` directory containing `func.go` and `func.yaml` you've
got everything you need to run the function.  So let's run it and
observe the output.  To see the details of what is happening during a function run, use the `--verbose` switch. The first time you build a function of a particular language it takes longer as Fn downloads the necessary Docker images. The `--verbose` option allows you to see this process.

![User Input Icon](images/userinput.png)
>```
> fn --verbose run
>```

```yaml
Building image fndemouser/gofn:0.0.1
FN_REGISTRY:  fndemouser
Current Context:  No context currently in use.
Sending build context to Docker daemon  6.144kB
Step 1/10 : FROM fnproject/go:dev as build-stage
dev: Pulling from fnproject/go
ff3a5c916c92: Already exists
f32d2ea73378: Pull complete
3bdfb30a4c89: Pull complete
6487ee6212c5: Pull complete
074903419fc0: Pull complete
3db945ee2177: Pull complete
Digest: sha256:6ebffaea00a2f53373c68dd52e0df209d7e464d691db0d52b31060d06df8e839
Status: Downloaded newer image for fnproject/go:dev
 ---> fac877f7d14d
Step 2/10 : WORKDIR /function
 ---> Running in b1b65327d278
Removing intermediate container b1b65327d278
 ---> 95b07c89837f
Step 3/10 : RUN go get -u github.com/golang/dep/cmd/dep
 ---> Running in f34144b5de66
Removing intermediate container f34144b5de66
 ---> ff1478fb4f19
Step 4/10 : ADD . /go/src/func/
 ---> 38b2fab29c16
Step 5/10 : RUN cd /go/src/func/ && dep ensure
 ---> Running in 402a0040f77c
Removing intermediate container 402a0040f77c
 ---> 6de1582a430c
Step 6/10 : RUN cd /go/src/func/ && go build -o func
 ---> Running in 21cc04bf3d6d
Removing intermediate container 21cc04bf3d6d
 ---> 031d30155996
Step 7/10 : FROM fnproject/go
latest: Pulling from fnproject/go
1eae7a7426b0: Pull complete
7a855df78530: Pull complete
Digest: sha256:8e03716b576e955c7606e4d8b8748c0f959a916ce16ba305ab262f042562340f
Status: Downloaded newer image for fnproject/go:latest
 ---> 76aed4489768
Step 8/10 : WORKDIR /function
 ---> Running in 619a412d5ab1
Removing intermediate container 619a412d5ab1
 ---> 1487668015c4
Step 9/10 : COPY --from=build-stage /go/src/func/func /function/
 ---> cba454c3ad2c
Step 10/10 : ENTRYPOINT ["./func"]
 ---> Running in 19e4eb479fc9
Removing intermediate container 19e4eb479fc9
 ---> fc917eb83553
Successfully built fc917eb83553
Successfully tagged fndemouser/gofn:0.0.1

{"message":"Hello World"}
```

The last line of output `{"message":"Hello World"}` was produced
by the Go statement `Msg: fmt.Sprintf("Hello %s", p.Name),`.

Normally fn is run without the --verbose option. Let's rerun without the verbose output enabled.

![User Input Icon](images/userinput.png)
>```sh
> fn --verbose run
>```

```sh
Building image fndemouser/gofn:0.0.1 ...
{"message":"Hello World"}
```

Notice the duration of the run is much shorter.

You can also pass data to the run command. For example:

![User Input Icon](images/userinput.png)
>```sh
> echo -n '{"name":"Bob"}' | fn run
>```

```sh
Building image fndemouser/gofn:0.0.1 .....
{"message":"Hello Bob"}
```

The JSON data was parsed and since `name` was set to "Bob", that value is passed
in the output.

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

![User Input Icon](images/userinput.png)
>```sh
> docker images | grep fndemouser
>```

You should see something like:

```sh
fndemouser/gofn      0.0.1               7b586506a195        5 minutes ago        15MB
```

## Deploying Your First Function

When we used `fn run` your function was run in your local environment.
Now let's deploy your function to the Fn server we started previously.
This server could be running in the cloud, in your datacenter, or on
your local machine like we're doing here.

Deploying your function is how you publish your function and make it
accessible to other users and systems.

In your terminal type the following:

![User Input Icon](images/userinput.png)
>```sh
> fn deploy --app goapp --local
>```

You should see output similar to:

```yaml
Deploying gofn to app: goapp
Bumped to version 0.0.2
Building image fndemouser/gofn:0.0.2 .....
Updating function gofn using image fndemouser/gofn:0.0.2...
Successfully created app:  goapp
Successfully created function: gofn with fndemouser/gofn:0.0.2
Successfully created trigger: gofn-trigger
```

Functions are grouped into applications so by specifying `--app goapp`
we're implicitly creating the application "goapp" and associating our
function with it.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message `Updating function gofn using image fndemouser/gofn:0.0.2...`
let's us know that the function is packaged in the image
`fndemouser/gofn:0.0.2`.

Note that the containing folder name `gofn` was used as the name of the
generated Docker container and used as the name of the function that container
was bound to. By convention it is also used to create the trigger name
`gofn-trigger`.

The fn CLI provides a couple of commands to let us see what we've deployed.
`fn list apps` returns a list of all of the defined applications.

![User Input Icon](images/userinput.png)
>```sh
> fn list apps
>```

Which, in our case, returns the name of the application we created when we
deployed our gofn function:

```sh
NAME
goapp
```

We can also see the functions that are defined by an application. Since functions are exposed via triggers, the `fn list triggers <appname>` command is used. To list the functions included in "goapp" we can type:

![User Input Icon](images/userinput.png)
>```sh
> fn list triggers goapp
>```

```sh
FUNCTION    NAME            TYPE    SOURCE        ENDPOINT
gofn        gofn-trigger    http    /gofn-trigger http://localhost:8080/t/goapp/gofn-trigger
```

The output confirms that goapp contains a `gofn` function that is implemented
by the Docker container `fndemouser/gofn:0.0.2` which may be invoked via the
specified URL.  Now that we've confirmed deployment was successsful, let's
call our function.

## Calling Your Deployed Function

There are two ways to invoke your deployed function.  The first is using
the `fn` CLI which makes invoking your function relatively easy.  Type
the following:

![User Input Icon](images/userinput.png)
>```sh
> fn invoke goapp gofn
>```

which results in our familiar output message.

```sh
{"message":"Hello World"}
```

Of course this is unchanged from when you ran the function locally.
However when you called "goapp gofn" the fn server looked up the
"goapp" application and then looked for the Docker container image
bound to the "gofn" function.

The other way to call your function is via HTTP.  The Fn server exposes our
deployed function at "http://localhost:8080/t/goapp/gofn-trigger", a URL that
incorporates our application and function trigger as path elements.

Use curl to invoke the function:

![User Input Icon](images/userinput.png)
>```sh
> curl -H "Content-Type: application/json" http://localhost:8080/t/goapp/gofn-trigger
>```

The result is once again the same.

```sh
{"message":"Hello World"}
```

We can again pass JSON data to out function get the value of name passed to the
function back.

![User Input Icon](images/userinput.png)
>```sh
> curl -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/t/goapp/gofn-trigger
>```

The result is once again the same.

```sh
{"message":"Hello Bob"}
```

## Wrapping Up

Congratulations! In this tutorial you've accomplished a lot. You've created your
first function, run it locally, deployed it your local Fn server and invoked it
over HTTP.

**Go:** [Back to Contents](../README.md)
