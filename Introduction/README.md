# Introduction to Fn with Go
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll walk through
developing a function using the Go programming language (without installing any
Go tools!) and deploying that function to a local Fn server.  We'll also learn
about the core Fn concepts like applications and triggers.

### Before you Begin
* Set aside about 15 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../install/README.md).
    * Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". [See here](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context).)

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


### Create your Function
In the terminal type the following:

![User Input Icon](images/userinput.png)
>```sh
> fn init --runtime go gofn
>```

The output will be

```yaml
Creating function at: ./gofn
Function boilerplate generated.
func.yaml created.
```

The `fn init` command creates an simple function with a bit of boilerplate to
get you started. The `--runtime` option is used to indicate that the function
we're going to develop will be written in Go. A number of other runtimes are
also supported.  Fn creates the simple function along with several supporting files in the `/gofn` directory.

### Review your Function File
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
func.go func.yaml go.mod
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

There are other user specifiable properties but these will suffice for
this example.  Note that the name of your function is taken from the containing
folder name.  We'll see this come into play later on.

### Other Function Files
The `fn init` command generated one other file.

* `go.mod` --  the Go modules file which
specifies all the dependencies for your function.

## Deploy Your First Function

With the `gofn` directory containing `func.go` and `func.yaml` you've got
everything you need to deploy the function to Fn server. This server could be
running in the cloud, in your datacenter, or on your local machine like we're
doing here.

### Check your Context
Make sure your context is set to default and you are using a demo user. Use the `fn list contexts` command to check.

![user input](images/userinput.png)
>```sh
> fn list contexts
>```

```cs
CURRENT	NAME    PROVIDER    API URL                 REGISTRY
*       default	default     http://localhost:8080   fndemouser
```

If your context is not configured, please see [the context installation instructions](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context) before proceeding. Your context determines where your function is deployed.

### Create an App
Next, functions are grouped together into an application. The application acts as the main organizing structure for multiple functions. To create an application type the following:

>```sh
>fn create app goapp
>```

A confirmation is returned:

```yaml
Successfully created app:  goapp
```

Now `goapp` is ready for functions to be deployed to it.

### Deploy your Function to your App
Deploying your function is how you publish your function and make it accessible
to other users and systems. To see the details of what is happening during a
function deploy,  use the `--verbose` switch.  The first time you build a
function of a particular language it takes longer as Fn downloads the necessary
Docker images. The `--verbose` option allows you to see this process.

In your terminal type the following:

![User Input Icon](images/userinput.png)
>```sh
> fn --verbose deploy --app goapp --local
>```

You should see output similar to:

```yaml
Deploying gofn to app: goapp
Bumped to version 0.0.2
Building image fndemouser/gofn:0.0.2
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon   5.12kB
Step 1/10 : FROM fnproject/go:dev as build-stage
 ---> 96c8fb94a8e1
Step 2/10 : WORKDIR /function
 ---> Using cache
 ---> bee171e861d4
Step 3/10 : WORKDIR /go/src/func/
 ---> Using cache
 ---> d0102d3148a1
Step 4/10 : ENV GO111MODULE=on
 ---> Using cache
 ---> 22ecbf50c559
Step 5/10 : COPY . .
 ---> 0a2992d2d99a
Step 6/10 : RUN cd /go/src/func/ && go build -o func
 ---> Running in e480baa937d4
go: finding github.com/fnproject/fdk-go latest
go: downloading github.com/fnproject/fdk-go v0.0.0-20190716163646-1458ca84e01d
Removing intermediate container e480baa937d4
 ---> d8cc615e1e64
Step 7/10 : FROM fnproject/go
 ---> bc635796c9df
Step 8/10 : WORKDIR /function
 ---> Using cache
 ---> b853b5d6b840
Step 9/10 : COPY --from=build-stage /go/src/func/func /function/
 ---> Using cache
 ---> ee3af55a0670
Step 10/10 : ENTRYPOINT ["./func"]
 ---> Using cache
 ---> 3e41594de5c8
Successfully built 3e41594de5c8
Successfully tagged fndemouser/gofn:0.0.2

Updating function gofn using image fndemouser/gofn:0.0.2...
Successfully created function: gofn with fndemouser/gofn:0.0.2
```

All the steps to load the current language Docker image are displayed.

Specifying `--app goapp` explicitly puts the function in the application `goapp`.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message `Updating function gofn using image fndemouser/gofn:0.0.2...`
let's us know that the function is packaged in the image
`fndemouser/gofn:0.0.2`.

Note that the containing folder name `gofn` was used as the name of the
generated Docker container and used as the name of the function that container
was bound to.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.


## Invoke your Deployed Function

There are two ways to call your deployed function.  

### Invoke with the CLI

The first is using the Fn CLI which makes invoking your function relatively
easy.  Type the following:

![user input](images/userinput.png)
>```sh
> fn invoke goapp gofn
>```

which results in:

```js
{"message":"Hello World"}
```

When you invoked "goapp gofn" the fn server looked up the "goapp" application
and then looked for the Docker container image bound to the "gofn" function and
executed the code. Fn `invoke` invokes your function directly and independently
of any associated triggers.  You can always invoke a function even without it
having any triggers bound to it.

You can also pass data to the invoke command. Note that you set the content type
for the data passed. For example:

![user input](images/userinput.png)
>```sh
> echo -n '{"name":"Bob"}' | fn invoke goapp gofn --content-type application/json
>```

```js
{"message":"Hello Bob"}
```

The JSON data was parsed and since `name` was set to "Bob", that value is passed
in the output.

### Understand fn deploy
If you have used Docker before the output of `fn --verbose deploy` should look
familiar--it looks like the output you see when running `docker build`
with a Dockerfile.  Of course this is exactly what's happening!  When
you deploy a function like this Fn is dynamically generating a Dockerfile
for your function, building a container, and then loading it for execution.

> __NOTE__: Fn is actually using two images.  The first contains
the language compiler and is used to generate a binary.  The second
image packages only the generated binary and any necessary language
runtime components. Using this strategy, the final function image size
can be kept as small as possible.  Smaller Docker images are naturally
faster to push and pull from a repository which improves overall
performance.  For more details on this technique see [Multi-Stage Docker
Builds for Creating Tiny Go Images](https://medium.com/travis-on-docker/multi-stage-docker-builds-for-creating-tiny-go-images-e0e1867efe5a).

When using `fn deploy --local`, fn server builds and packages your function
into a container image which resides on your local machine.

As Fn is built on Docker you can use the `docker` command to see the local
container image you just generated. You may have a number of Docker images so
use the following command to see only those created by fndemouser:

![user input](images/userinput.png)
>```sh
> docker images | grep fndemouser
>```

You should see something like:

```sh
fndemouser/gofn      0.0.2               cde014cefdad        7 minutes ago       15.1MB
```

### Explore your Application

The fn CLI provides a couple of commands to let us see what we've deployed.
`fn list apps` returns a list of all of the defined applications.

![User Input Icon](images/userinput.png)
>```sh
> fn list apps
>```

Which, in our case, returns the name of the application we created when we
deployed our gofn function:

```sh
NAME    ID
goapp    01D37WY2N2NG8G00GZJ0000001
```

The `fn list functions <app-name>` command lists all the functions associated with an app.

![user input](images/userinput.png)
>```sh
> fn list functions goapp
>```

The returns all the functions associated with the `goapp`.

```
NAME	IMAGE                    ID
gofn	fndemouser/gofn:0.0.2	 01DJZQXW47NG8G00GZJ0000014
```

The output confirms that `goapp` contains a `gofn` function which may be invoked via the
specified URL.  Now that we've confirmed deployment was successful, let's
call our function.

# Invoke your Deployed Function

There are two ways to call your deployed function.

### Invoke with the CLI

The first is using the `fn` CLI which makes invoking your function relatively
easy.  Type the following:

![user input](images/userinput.png)
>```sh
> fn invoke goapp gofn
>```

which results in:

```js
{"message":"Hello World"}
```

When you invoked "goapp gofn" the Fn server looked up the
"goapp" application and then looked for the Docker container image
bound to the "gofn" function and executed the code.

You can also pass data to the run command. Note that you set the content type for the data passed. For example:

![user input](images/userinput.png)
>```sh
> echo -n '{"name":"Bob"}' | fn invoke goapp gofn --content-type application/json
>```

```js
{"message":"Hello Bob"}
```

The JSON data was parsed and since `name` was set to "Bob", that value is passed
in the output.


### Getting a Function's Invoke Endpoint

In addition to using the Fn `invoke` command, we can call a function by using a
URL. To do this, we must get the function's invoke endpoint. Use the command
`fn inspect function <appname> <function-name>`.  To list the `gofn` function's
invoke endpoint we can type:

![user input](images/userinput.png)
>```sh
> fn inspect function goapp gofn
>```

```js
{
	"annotations": {
		"fnproject.io/fn/invokeEndpoint": "http://localhost:8080/invoke/01DJZQXW47NG8G00GZJ0000014"
	},
	"app_id": "01DJZQWHVWNG8G00GZJ0000013",
	"created_at": "2019-08-23T17:21:03.111Z",
	"id": "01DJZQXW47NG8G00GZJ0000014",
	"idle_timeout": 30,
	"image": "fndemouser/gofn:0.0.2",
	"memory": 128,
	"name": "gofn",
	"timeout": 30,
	"updated_at": "2019-08-23T17:21:03.111Z"
}
```

The output confirms that the `gofn` function's invoke endpoint is:
`http://localhost:8080/invoke/01DJZQXW47NG8G00GZJ0000014`. We can use this URL
to call the function.

### Invoke with Curl

Once we have the invoke endpoint, the second method for invoking our function
can be used, HTTP.  The Fn server exposes our deployed function at
`http://localhost:8080/invoke/01DJZQXW47NG8G00GZJ0000014`.

Use curl to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -X "POST" -H "Content-Type: application/json" http://localhost:8080/invoke/01DJZQXW47NG8G00GZJ0000014
>```

The result is once again the same.

```js
{"message":"Hello World"}
```

We can again pass JSON data to our function get the value of name passed to the
function back.

![user input](images/userinput.png)
>```sh
> curl -X "POST" -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/invoke/01DJZQXW47NG8G00GZJ0000014
>```

The result is once again the same.

```js
{"message":"Hello Bob"}
```

## Wrap Up

Congratulations!  In this tutorial you've accomplished a lot.  You've created
your first function, deployed it to your local Fn server and invoked it over
HTTP.

**Go:** [Back to Contents](../README.md)
