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

![user input](images/userinput.png)
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

`fn run` builds a docker image using the Dockerfile, runs a container using this image, 
returns the output and then removes the container. In this case you will see the default
"Hello from Docker! ... " message that's inside the Docker hello-world image.

![user input](images/userinput.png)
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

![user input](images/userinput.png)
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


fn deploy --app docker-hello --local


