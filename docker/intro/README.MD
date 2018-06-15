# Introduction to Fn with Docker
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll see how to
use an existing Docker image as an Fn function and deploy this function to a local
Fn server. 

### Before you Begin
* Set aside about 10 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../../install/README.md).

> As you make your way through this tutorial, look out for this icon.
![](../../images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Your First Function
Now that Fn server is up and running, let's start with a simple "hello world" function 
using the Docker [hello-world image](https://hub.docker.com/_/hello-world/).

Let's set the `FN_REGISTRY` environment variable. Normally, it's set to your Docker Hub username.
However in this tutorial we'll work in local development mode so we can set
the `FN_REGISTRY` variable to an arbitrary value. Let's use `fndemouser`.

![user input](../../images/userinput.png)
>```
> export FN_REGISTRY=fndemouser
>```

Let's work inside the tutorials/docker/intro directory

![user input](../../images/userinput.png)
>```
> cd tutorials/docker/intro
>```

Now let's create a Dockerfile. 

![](../../images/userinput.png) Copy/paste the following into a file named `Dockerfile`:

```dockerfile
FROM hello-world
```

Now let's generate the function boilerplate. In the terminal type the following.

![user input](../../images/userinput.png)
>```
> fn init --runtime docker
>```

The output will be

```yaml
Dockerfile found. Using runtime 'docker'.
func.yaml created.
```

The `fn init` command creates an simple function boilerplate. The `--runtime` 
option is used to indicate that the function will use a Docker container. 
A number of other runtimes are also supported.

### Understanding func.yaml

The `fn init` command generated a `func.yaml` function configuration file. 
Let's look at the contents:

![user input](../../images/userinput.png)
>```sh
> cat func.yaml
>```

```yaml
name: intro
version: 0.0.1
runtime: docker
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* name--the name of the function. Matches the directory name
* version--automatically starting at 0.0.1
* runtime--the name of the runtime/language which was set based on the value set
in `--runtime`

There are other user specifiable properties but these will suffice for
this example.  Note that the name of your function is taken from the containing
folder name.  We'll see this come into play later on.

## Running Your First Function

With the `docker/intro` directory containing `Dockerfile` and `func.yaml` you've
everything you need to run the function.  So let's run it and observe the output. 
Note that the first time you run a function of a particular language it takes longer
as Fn downloads the necessary Docker images.

![user input](../../images/userinput.png)
>```sh
> fn run
>```

```sh
Building image fndemouser/intro:0.0.1 

Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://hub.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/engine/userguide/

```

If you ever want more details on what a given fn command is doing behind the
scenes you can add the `--verbose` switch. You can rerun with verbose output
enabled.

![user input](../../images/userinput.png)
>```sh
> fn --verbose run
>```

```sh
Building image fndemouser/intro:0.0.1 
Sending build context to Docker daemon  3.072kB
Step 1/1 : FROM hello-world
 ---> e38bc07ac18e
Successfully built e38bc07ac18e
Successfully tagged fndemouser/intro:0.0.1


Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://hub.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/engine/userguide/
```

### Understanding fn run
If you have used Docker before the output of `fn --verbose run` should look
familiar. It looks like the output you see when running `docker build`
with a Dockerfile. Of course this is exactly what's happening! 

`fn run` built a docker image using the Dockerfile, ran a container using this image, 
returned the output and then removed the container. And you saw the default
"Hello from Docker! ... " message that's inside the Docker hello-world image. 

`fn run` is a local operation. The container image resides on your local machine. 
`fn run` doesn't deploy anything to the Fn server. In fact you don't need the Fn 
server to be running when you execute `fn run`.

As Fn is built on Docker you can use the `docker` command to see the local
container image you just generated.

You may have a number of Docker images so use the following command
to see only those created by fndemouser:

![user input](../../images/userinput.png)
>```sh
> docker images | grep fndemouser
>```

You should see something like:

```sh
fndemouser/intro    0.0.1               e38bc07ac18e        2 months ago        1.85kB
```

## Deploying Your First Function

Now let's deploy your function to the Fn server we started previously.
This server could be running in the cloud, in your datacenter, or on
your local machine like we're doing here.

Deploying your function is how you publish your function and make it
accessible to other users and systems.

In your terminal type the following:

![user input](../../images/userinput.png)
>```sh
> fn deploy --app docker-hello --local
>```

You should see output similar to:

```sh
Deploying intro to app: docker-hello at path: /intro
Bumped to version 0.0.2
Building image fndemouser/intro:0.0.2 
Updating route /intro using image fndemouser/intro:0.0.2...
```

Functions are grouped into applications so by specifying `--app docker-hello`
we're implicitly creating the application "docker-hello" and associating our
function with it.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message
`Updating route /intro using image fndemouser/intro:0.0.2...`
lets us know that the function packaged in the image
"fndemouser/intro:0.0.2" has been bound by the Fn server to the route
"/intro".  We'll see how to use the route below.

Note that the containing folder name 'intro' was used as the name of the
generated Docker container and used as the name of the route that
container was bound to.

The fn CLI provides a couple of commands to let us see what we've deployed.
`fn list apps` returns a list of all of the defined applications.

![user input](../../images/userinput.png)
>```sh
> fn list apps
>```

Which, in our case, returns the name of the application we created when we
deployed our gofn function:

```sh
docker-hello
```

We can also see the functions that are defined by an application.  Since
functions are exposed via routes, the `fn list routes <appname>` command
is used.  To list the functions included in "docker-hello" we can type:

![user input](../../images/userinput.png)
>```sh
> fn list routes docker-hello
>```

```sh
path    image			        endpoint
/intro fndemouser/intro:0.0.2 localhost:8080/r/docker-hello/intro
```

The output confirms that `docker-hello` app contains an `intro` function that is 
implemented by the Docker container `fndemouser/intro:0.0.2` which may be invoked 
via the specified URL. Now that we've confirmed deployment was successful, let's
call our function.

## Calling Your Deployed Function

There are two ways to call your deployed function.  The first is using
the `fn` CLI which makes invoking your function relatively easy.  Type
the following:

![user input](../../images/userinput.png)
>```sh
> fn call docker-hello /intro
>```

which results in our familiar output message.

```sh
Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://hub.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/engine/userguide/
```

Of course the output looks the same as when you ran the function with `fn run`.
However when you called "docker-hello /intro" the fn server looked up the
"docker-hello" application and then looked for the Docker container image
bound to the "/intro" route.

The other way to call your function is via HTTP. The Fn server exposes our deployed 
function at /r/<app-name>/<fn-name> which in our case maps to 
"http://localhost:8080/r/docker-hello/intro".

Use curl to invoke the function:

![user input](../../images/userinput.png)
>```sh
> curl http://localhost:8080/r/docker-hello/intro
>```

The result is once again the same.

```sh
Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://hub.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/engine/userguide/
```

## Wrapping Up

Congratulations!  In this tutorial you have taken an existing Docker image,
used it as your first function, run it locally, deployed it to your local 
Fn server and invoked it over HTTP.

**Go:** [Back to Contents](../README.md)
