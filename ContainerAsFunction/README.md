# Creating a Function from a Docker Image

This tutorial walks through how to use a custom Docker image to define an
Fn function.  Although Fn functions are packaged as Docker images, when
developing functions using the Fn CLI developers are not directly exposed
to the underlying Docker platform.  Docker isn't hidden (you can see
Docker build output and image names and tags), but you aren't
required to be very Docker-savvy to develop functions with Fn.
However, sometimes you need to handle advanced use cases and must take
complete control of the creation of the function image. Fortunately
the design and implementation of Fn enables you to do exactly that.  Let's
build a simple custom function image to walk through the process.

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Prequisites

This tutorial requires you to have both Docker and Fn installed. If you need
help with Fn installation you can find instructions in the
[Install and Start Fn Tutorial](../install/README.md).

# Getting Started

Before we can get starting there are a couple of configuration steps to take
care of.

## Login to Docker Hub

To make it possible to push images you need to authenticate yourself with your
Docker repository (default is Docker Hub).

![](images/userinput.png)
>```
> docker login
>```

NOTE: Depending on how you've installed Docker you may need to prefix `docker`
commands with `sudo`.

## Set your Docker Registry

Set your docker registry in your current fn context.  If you're using Docker
Hub then the value is just your Docker user id.  

   ![](images/userinput.png)
   >```
   > fn update context registry your-docker-id
   >```

## Start Fn Server

Next, if it isn't already running, you'll need to start the Fn server.  We'll
run it in the foreground to let us see the server log messages so let's open a
new terminal for this.

Start the Fn server using the `fn` cli:

   ![](images/userinput.png)
   >```
   > fn start
   >```

## A Custom Function Container Image

In this tutorial we only have two artifacts: a Dockerfile and a very simple
Node.js "Hello World" application that returns a customized greeting given 
a name.

### func.js

The func.js file is nothing special and simply reads from standard input
and writes to standard output.  This is the default Fn supported mechanism
for functions to receive input and return output.

![](images/userinput.png) Copy/paste the following into a file named `func.js`:

```javascript
name = "World";
fs = require('fs');
try {
	input = fs.readFileSync('/dev/stdin').toString();
	if (input) {
		name = input;
	}
} catch(e) {}
console.log("Hello", name, "from Node!");
```

### Dockerfile

The `Dockerfile` for our function is also very simple.  It starts with
a light alpine Node.js base image, copies the `func.js` into the image,
and sets the entrypoint so that when the container is started the
`func.js` is run.

NOTE: `func.js` has no required Node modules but if there were
you would have to run `npm install` to download them to the 
`/function/node_modules` folder in the generated image.

![](images/userinput.png) In the same folder as the `func.js` file, copy/paste
 the following into a file named `Dockerfile`:

```dockerfile
FROM node:8-alpine

WORKDIR /function

ADD func.js /function/func.js

ENTRYPOINT ["node", "./func.js"]
```

### Building the Function Image

In your working directory, build and run the image as you would any Docker image:

1. Build your function container image with `docker build`:

   ![](images/userinput.png)
   >```
   > docker build . -t your-docker-id/hello:0.0.1
   >```

2. Test the image by running it with no input:

   ![](images/userinput.png)
   >```
   > docker run --rm your-docker-id/hello:0.0.1
   >```

   The output should be:
   ```
   Hello World from Node!
   ```

3. Test the image by running it with a name parameter (note the addition of
`-i`):

   ![](images/userinput.png)
   >```
   > echo -n "Jane" | docker run -i --rm your-docker-id/hello:0.0.1
   >```

   The output should be the same as be except "Jane" in place of "World":

   ```
   Hello Jane from Node!
   ```

Great!  We have a working Docker image.  Now let's deploy it as a function.

## Publishing the Function Image

When developing locally you don't need to deploy to Docker Hub--the
local Fn server can find your function image on the local machine. But
eventually you are going to want to run your function on a remote
Fn server which requires you to publish your function image in
a repository like Docker Hub. You can do this with a standard `docker push` 
but again this step is optional when we're working locally.

![](images/userinput.png)
>```
> docker push your-docker-id/hello:0.0.1
>```

## Creating the Fn App and Defining a Function

Once we have a container image we can create a function with that image.

1. First we need an 
['application'](https://github.com/fnproject/fn/blob/master/docs/developers/model.md#applications)
to contain our functions.  Applications define a namespace to organize functions
and can contain configuration values that are shared across all functions in
that application:

   ![](images/userinput.png)
   >```
   > fn create app demoapp
   >```

   ```
   Successfully created app:  demoapp
   ```

2. We then create a function that uses our manually built container image:

   ![](images/userinput.png)
   >```
   > fn create function demoapp hello your-docker-id/hello:0.0.1
   >```

   ```
   Successfully created function: hello with your-docker-id/hello:0.0.1
   ```

3. We can confirm the function is correctly defined by getting a list of the 
functions in an application:

   ![](images/userinput.png)
   >```
   > fn list functions demoapp
   >```

   **Pro tip**: The fn cli let's you abbreviate most of the keywords so you can
also say `fn ls f demoapp`!

   You should see something like:

   ```xml
   NAME    IMAGE
   hello   your-docker-id/hello:0.0.1
   ```

At this point all the Fn server has is configuration metadata.
It has the name of an application and a function that is part
of that application that points to a named and tagged Docker image.  It's
not until that function is invoked that this metadata is used to instantiate
a function container from the specified image.

## Invoking the Function

Invoking a function that was manually defined is no different from calling a
function defined using `fn deploy`, which is exactly as intended!

1. Call the function using `fn invoke`:

   ![](images/userinput.png)
   >```
   > echo -n "Jane" | fn invoke demoapp hello
   >```

   This will produce the expected output:

   ```sh
   Hello Jane from Node!
   ```
  If you want to call the function over HTTP you will need to define a
  [trigger](https://github.com/fnproject/docs/blob/master/fn/develop/triggers.md),
  which we'll skip for now.


When the function is invoked the Fn server
looks up the function image name and tag associated with the function and creates
a container with that image. If the required image is not already available
locally then Docker will attempt to pull the image from the Docker registry.

In our local development scenario, the image is already on the local machine
so you won't see a 'pull' message in the Fn server log.

# Using Fn's Dockerfile Support

Now that you've successfully manually built and deployed a Docker image as a
function, let's use Fn's built in Docker runtime support to simplify things.
By providing a `func.yaml` file for your function you can use the Fn CLI to
build, run, and deploy Docker image-based functions exactly as you would when
working wth Java, Node, or any other Fn supported programming language based
function.

![](images/userinput.png) Copy/paste the following into a file named
`func.yaml`:

```yaml
schema_version: 20180708
name: hello2
version: 0.0.1
runtime: docker
format: default
```

This file defines a function with the name `hello2`, to distinguish it from the
manually deployed function, the same initial version as the
image we created earlier, and identifies the function runtime as `docker`
rather than one of the Fn supported programming languages.

With the `func.yaml` defined you can now easily build the function:

   ![](images/userinput.png)
   >```
   > fn build
   >```

   ```xml
   Building image your-docker-id/hello:0.0.1
   Function your-docker-id/hello:0.0.1 built successfully.
   ```

Deploying a function using `fn deploy` is much simpler than a manual deployment
as it'll both push your container image and define the function with one
command:

  ![](images/userinput.png)
   >```
   > fn deploy --app demoapp --no-bump
   >```

   ```
   Deploying hello to app: demoapp
   Building image your-docker-id/hello:0.0.1
   Parts:  [your-docker-id hello:0.0.1]
   Pushing your-docker-id/hello:0.0.1 to docker registry...The push refers to repository [docker.io/your-docker-id/hello]
   ...
   Updating function hello using image your-docker-id/hello:0.0.1...
   ```

`fn deploy` orchestrates both the push and the function definition.  Now you
should have two functions, `hello` and `hello2` in your demoapp application.
You can see all functions defined in an application by typing (using fn cli
supported abbreviations):

  ![](images/userinput.png)
   >```
   > fn ls f demoapp
   >```

   ```
   NAME    IMAGE
   hello   shaunsmith/hello:0.0.1
   hello2  shaunsmith/hello2:0.0.1   
   ```

Once deployed you can use `fn invoke` to call the `hello2` function. Which makes
sense since all we've done is use the CLI to deploy the exact same image and
defined the exact same function as before.  We've simply automated the process.

Try out the same command you ran earlier with our newly defined `hello2`
function.

![](images/userinput.png)
>```
> echo -n "Jane" | fn invoke demoapp hello2
>```

# Conclusion

One of the most powerful features of Fn is the ability to use custom defined
Docker container images as functions. This feature makes it possible to deploy
pretty much anything that you can put in a contaner as a function. This includes
existing code or utilities as well as functions written in your favourite
programming language, regardless of whether Fn has built-in support or not. It
also let's you install any Linux libraries or utilities that your function might
need. And thanks to the Fn CLI's support for Dockerfiles it's both easy and
the same user experience as when developing any function.

Having completed this tutorial you've successfully built a Docker image,
defined a function as implemented by that image, and invoked the function
resulting in the creation of a container using that image.  Congratulations!

**Go:** [Back to Contents](../README.md)
