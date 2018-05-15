# Introduction to Fn with Node.js
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll walk through
developing a function using the JavaScript programming language and Node.js
(without installing any Node.js tools!) and deploying that function to a local
Fn server.  We'll also learn about the core Fn concepts like applications and
routes.

### Before you Begin
* Set aside about 15 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../../install/README.md).

> As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Your First Function
Now that Fn server is up and running, let's start with a very simple "hello world" function written in [Node.js
JavaScript](https://nodejs.org/). Don't worry, you don't need to know Node!  In
fact you don't even need to have Node installed on your development machine as
Fn provides the necessary Node tools as a Docker container.  Let's
walk through your first function to become familiar with the process and how Fn
supports development.

Before we start developing we need to set the `FN_REGISTRY`
environment variable.  Normally, it's set to your Docker Hub username.
However in this tutorial we'll work in local development mode so we can set
the `FN_REGISTRY` variable to an arbitrary value. Let's use `fndemouser`.

![user input](images/userinput.png)
>```
> export FN_REGISTRY=fndemouser
>```




With that out of the way, let's create a new function. In the terminal type the
following.

![user input](images/userinput.png)
>```
> fn init --runtime node nodefn
>```

The output will be

```yaml
Creating function at: /nodefn
Runtime: node
Function boilerplate generated.
func.yaml created.
```

The `fn init` command creates an simple function with a bit of boilerplate to
get you started. The `--runtime` option is used to indicate that the function
we're going to develop will be written in Node. A number of other runtimes are
also supported.  Fn creates the simple function along with several supporting
files in the `/nodefn` directory.

### Reviewing your Function File

With your function created change into the `/nodefn` directory.

![user input](images/userinput.png)
>```
> cd nodefn
>```

Now get a list of the directory contents.

![user input](images/userinput.png)
>```
> ls
>```

>```sh
> func.js func.yaml package.json test.json
>```

The `func.js` file which contains your actual Node function is generated along
with several supporting files. To view your Node function type:

![user input](images/userinput.png)
>```sh
> cat func.js
>```

```js
var fdk=require('@fnproject/fdk');

fdk.handle(function(input){
  var name = 'World';
  if (input.name) {
    name = input.name;
  }
  response = {'message': 'Hello ' + name}
  return response
})
```

This function looks for JSON input in the form of `{"name": "Bob"}`. If this
JSON example is passed to the function, the function returns `{"message":"Hello Bob"}`. If no
JSON data is found, the function returns `{"message":"Hello World"}`.  

### Understanding func.yaml
The `fn init` command generated a `func.yaml` function
configuration file. Let's look at the contents:

![user input](images/userinput.png)
>```sh
>cat func.yaml
>```

```yaml
name: nodefn
version: 0.0.1
runtime: node
entrypoint: node func.js
format: json
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* name--the name of the function. Matches the directory name.
* version--automatically starting at 0.0.1.
* runtime--the name of the runtime/language which was set based on the value set
in `--runtime`.
* entrypoint--the name of the executable to invoke when your function is called,
in this case `node func.js`.
* format--the function uses JSON as its input/output method ([see: Open Function Format](https://github.com/fnproject/fn/blob/master/docs/developers/function-format.md)).

There are other user specifiable properties but these will suffice for
this example.  Note that the name of your function is taken from the containing
folder name.  We'll see this come into play later on.

### Other Function Files
The `fn init` command generated two other files.

* `package.json` --  specifies all the dependencies for your Node function.
* `test.json` -- a test file that is used to test your function. It defines an
input and the output of the function and helps to identify if the function works
correctly or not. Function testing is not covered in this tutorial.


## Running Your First Function
With the `nodefn` directory containing `func.js` and `func.yaml` you've
got everything you need to run the function.  So let's run it and
observe the output.  Note that the first time you build a
function of a particular language it takes longer as Fn downloads
the necessary Docker images.

![user input](images/userinput.png)
>```sh
> fn run
>```

```sh
Building image fndemouser/nodefn:0.0.1 ...
{"message":"Hello World"}
```

The last line of output is `{"message":"Hello World"}` since no input was passed
to the function.

If you ever want more details on what a given fn command is doing behind the
scenes you can add the `--verbose` switch.  Let's rerun with verbose output
enabled.

![user input](images/userinput.png)
>```sh
> fn --verbose run
>```

```sh
Building image fndemouser/nodefn:0.0.1
Sending build context to Docker daemon  6.144kB
Step 1/9 : FROM fnproject/node:dev as build-stage
 ---> 016382f39a51
Step 2/9 : WORKDIR /function
 ---> Using cache
 ---> 218f67289b52
Step 3/9 : ADD package.json /function/
 ---> Using cache
 ---> bcafd420d385
Step 4/9 : RUN npm install
 ---> Using cache
 ---> cbb426ad9aad
Step 5/9 : FROM fnproject/node
 ---> 016382f39a51
Step 6/9 : WORKDIR /function
 ---> Using cache
 ---> 218f67289b52
Step 7/9 : ADD . /function/
 ---> edfdcd211c37
Step 8/9 : COPY --from=build-stage /function/node_modules/ /function/node_modules/
 ---> bb096b9f4689
Step 9/9 : ENTRYPOINT ["node", "func.js"]
 ---> Running in ba675c5b1a7e
Removing intermediate container ba675c5b1a7e
 ---> 642427f07a52
Successfully built 642427f07a52
Successfully tagged fndemouser/nodefn:0.0.1

{"message":"Hello World"}
```

You can also pass data to the run command. For example:

![user input](images/userinput.png)
>```sh
>echo -n '{"name":"Bob"}' | fn run
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

![user input](images/userinput.png)
>```sh
>`docker images | grep fndemouser`
>```

You should see something like:

```sh
fndemouser/nodefn    0.0.1               ab8be0a2ddc8        58 seconds ago      66.4MB
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
>```sh
> `fn deploy --app nodeapp --local`
>```

You should see output similar to:

```sh
Deploying nodefn to app: nodeapp at path: /nodefn
Bumped to version 0.0.2
Building image fndemouser/nodefn:0.0.2 .
Updating route /nodefn using image fndemouser/nodefn:0.0.2...
```

Functions are grouped into applications so by specifying `--app nodeapp`
we're implicitly creating the application "nodeapp" and associating our
function with it.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message
`Updating route /nodefn using image fndemouser/nodefn:0.0.2...`
lets us know that the function packaged in the image
"fndemouser/nodefn:0.0.2" has been bound by the Fn server to the route
"/nodefn".  We'll see how to use the route below.

Note that the containing folder name 'nodefn' was used as the name of the
generated Docker container and used as the name of the route that
container was bound to.

The fn CLI provides a couple of commands to let us see what we've deployed.
`fn apps list` returns a list of all of the defined applications.

![user input](images/userinput.png)
>```sh
> fn apps list
>```

Which, in our case, returns the name of the application we created when we
deployed our gofn function:

```sh
nodeapp
```

We can also see the functions that are defined by an application.  Since
functions are exposed via routes, the `fn routes list <appname>` command
is used.  To list the functions included in "nodeapp" we can type:

![user input](images/userinput.png)
>```sh
> fn routes list nodeapp
>```

```sh
path    image			        endpoint
/nodefn fndemouser/nodefn:0.0.2 localhost:8080/r/nodeapp/nodefn
```

The output confirms that nodeapp contains a `nodefn` function that is implemented
by the Docker container `fndemouser/nodefn:0.0.2` which may be invoked via the
specified URL.  Now that we've confirmed deployment was successful, let's
call our function.

## Calling Your Deployed Function

There are two ways to call your deployed function.  The first is using
the `fn` CLI which makes invoking your function relatively easy.  Type
the following:

![user input](images/userinput.png)
>```sh
> fn call nodeapp /nodefn
>```

which results in our familiar output message.

```sh
{"message":"Hello World"}
```

Of course this is unchanged from when you ran the function locally.
However when you called "nodeapp /nodefn" the fn server looked up the
"nodeapp" application and then looked for the Docker container image
bound to the "/nodefn" route.

The other way to call your function is via HTTP.  The Fn server
exposes our deployed function at "http://localhost:8080/r/nodeapp/nodefn", a URL
that incorporates our application and function route as path elements.

Use curl to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -H "Content-Type: application/json" http://localhost:8080/r/nodeapp/nodefn
>```

The result is once again the same.

```sh
{"message":"Hello World"}
```

We can again pass JSON data to our function get the value of name passed to the
function back.

![user input](images/userinput.png)
>```
> curl -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/r/nodeapp/nodefn 
>```

The result is once again the same.

```sh
{"message":"Hello Bob"}
```

## Wrapping Up

Congratulations!  In this tutorial you've accomplished a lot.  You've 
created your first function, run it locally, deployed it to your local 
Fn server and invoked it over HTTP.

**Go:** [Back to Contents](../README.md)
