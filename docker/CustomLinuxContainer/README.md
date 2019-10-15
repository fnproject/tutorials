# Make your own Linux command Function with HotWrap and a Customer Docker Image

This tutorial walks through how to use a custom Docker image to define an
Fn function.  Although Fn functions are packaged as Docker images, when
developing functions using the Fn CLI developers are not directly exposed
to the underlying Docker platform.  Docker isn't hidden (you can see
Docker build output and image names and tags), but you aren't
required to be very Docker-savvy to develop functions with Fn.

What if you want to make a function using Linux command line tools, or a script
that does not involve one of the supported Fn languages? Can you use your Docker
image as a function? Fortunately the design and implementation of Fn enables you
to do exactly that.  Let's build a simple custom function container image to see
how it's done.

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.


## Prequisites
This tutorial requires you to have both Docker and Fn installed. If you need
help with Fn installation you can find instructions in the
[Install and Start Fn Tutorial](../install/README.md).


## Getting Started
If it isn't already running, you'll need to start the Fn server.  We'll
run it in the foreground to let us see the server log messages so let's open a
new terminal for this.

Start the Fn server using the `fn` cli:

![](images/userinput.png)
>```sh
> fn start
>```


## Fn HotWrap Tool
The [Fn HotWrap tool](https://github.com/fnproject/hotwrap) allows you to create functions using conventional Unix command line tools and a Docker container. The tool provides the FDK contract for any command that can be run on the command line. Once wrapped, event data is passed to your function via STDIN and the output is returned through STDOUT.


## Initial Linux Command
To begin with, let's use the Linux `rev` command. The command reverses text, so this command:

![](images/userinput.png)
>```sh
> echo "Hello World" | rev
>```

Produces:

>```sh
> dlroW olleH
>```

So we only need to call the command `/bin/rev` for our function.


## Create Function Directory and Metadata
Next we need a directory for our project.

![](images/userinput.png)
> Create a folder named **revfunc**. Change into the folder.

![](images/userinput.png)
> In the folder , create a `func.yaml` file and copy/paste the following as its
> content:

```yaml
schema_version: 20180708
name: revfunc
version: 0.0.1
runtime: docker
triggers:
- name: revfunc
  type: http
  source: /revfunc
```

This is a typical `func.yaml` except that instead of declaring the **runtime**
as a programming language we've specified "**docker**".  If you were to type `fn
build` right now you'd get the error:

> Fn: Dockerfile does not exist for 'docker' runtime

This is because when you set the runtime type to "docker". The `fn build`
command defers to your Dockerfile to build the function container image--and you
haven't defined one yet!

## Create a Function Dockerfile
Now you need to create a `Dockerfile`.

![](images/userinput.png)
> Create a file named `Dockerfile` and copy/paste the following as its
> content:

```Dockerfile
FROM alpine:latest

# Install hotwrap binary in your container
COPY --from=fnproject/hotwrap:latest  /hotwrap /hotwrap

CMD "/bin/rev"

ENTRYPOINT ["/hotwrap"]
```

Here is an explanation of each of the Docker commands.

* `FROM alpine:latest` - Use the latest version of Alpine Linux as the base image.
* `COPY --from=fnproject/hotwrap:latest  /hotwrap /hotwrap` - Install the HotWrap Fn tool.
* `CMD "/bin/rev"` - The Linux command to run.
* `ENTRYPOINT ["/hotwrap"]` - Tells the container to execute the previous command using HotWrap: `/hotwrap /bin/rev`


## Building and Deploying

Once you have your custom Dockerfile you can simply use `fn build` to build
your function.  Give it a try:

![](images/userinput.png)
>```
> fn -v build
>```

You should see output similar to:

```sh
Building image fndemouser/revfunc:0.0.1
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon  3.072kB
Step 1/4 : FROM alpine:latest
latest: Pulling from library/alpine
9d48c3bd43c5: Pull complete
Digest: sha256:72c42ed48c3a2db31b7dafe17d275b634664a708d901ec9fd57b1529280f01fb
Status: Downloaded newer image for alpine:latest
 ---> 961769676411
Step 2/4 : COPY --from=fnproject/hotwrap:latest  /hotwrap /hotwrap
latest: Pulling from fnproject/hotwrap
e6b890787691: Pull complete
Digest: sha256:bf6303d7d216581c0e760f33dd74c3cdea83edad69f3d9614b7f573ba62c22b4
Status: Downloaded newer image for fnproject/hotwrap:latest
 ---> b999e7d793ff
Step 3/4 : CMD "/bin/rev"
 ---> Running in b60971040cd0
Removing intermediate container b60971040cd0
 ---> 5fa7a768081c
Step 4/4 : ENTRYPOINT ["/hotwrap"]
 ---> Running in 693c2d86cb7e
Removing intermediate container 693c2d86cb7e
 ---> 34d833b6acc2
Successfully built 34d833b6acc2
Successfully tagged fndemouser/revfunc:0.0.1
```

Just like with a default build, the output is a container image.  From this
point forward everything is just as it would be for any Fn function.  Since
you've previously started an Fn server, you can deploy it.  First, create
an application named 'revapp'.

![](images/userinput.png)
> Create an application:
>```sh
> fn create app revapp
>```

Next, deploy your function to that app.

![](images/userinput.png)
> Deploy the function to the application:

>```sh
> fn deploy --app revapp --local --no-bump
>```

We can confirm the function is correctly defined by getting a list of the
functions in the "revapp" application:

![](images/userinput.png)
>```sh
> fn list functions revapp
>```

You should get output similar to:

```sh
NAME    IMAGE                       ID
revfunc fndemouser/revfunc:0.0.1    01DQ8R97QTNG8G00GZJ0000002
```

**Pro tip**: The Fn cli let's you abbreviate most of the keywords so you can
also say `fn ls f revapp`! You should see the same output.


## Invoking the Function

With the function deployed let's invoke it to make sure it's working as
expected.

![](images/userinput.png)
>```sh
> echo "Hello World" | fn invoke revapp rev-func
>```

For this command you should see the following output:

>```txt
>dlroW olleH
>```

## Calling the Function with curl

We included an HTTP trigger declaration in the `func.yaml` so we can also call
the function with `curl`:

![](images/userinput.png)
>```sh
> curl --data "Hello World" -H "Content-Type: text/plain" -X POST http://localhost:8080/t/revapp/revfunc
>```

You should get exactly the same output as when using `fn invoke`.

>```txt
>dlroW olleH
>```


# Conclusion

One of the most powerful features of Fn is the ability to use custom defined
Docker container images as functions. This feature makes it possible to
customize your function's runtime environment including letting you use Linux
command line tools as your function. And thanks to the Fn CLI's support for
Dockerfiles it's the same user experience as when developing any function.

Having completed this tutorial you've successfully built a function using
a custom Dockerfile. Congratulations!

**Go:** [Back to Contents](../README.md)
