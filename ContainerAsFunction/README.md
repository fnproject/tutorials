# Creating a Function from a Docker Image

This tutorial walks through how to use a custom Docker image to define an
Fn function.  Although Fn functions are packaged as Docker images, when
developing functions using the Fn CLI developers are not directly exposed
to the underlying Docker platform.  Docker isn't hidden (you can see
Docker build output and image names and tags in routes), but you aren't
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

## Start Fn Server

Next, if it isn't already running, you'll need to start the Fn server.  We'll
run it in the foreground to let us see the server log messages so let's open a
new terminal for this.

1. Define the FN_REGISTRY environment variable to point the Fn server to where
it should pull function images from. If using the default Docker Hub registry 
you just need to specify your docker user id:

   ![](images/userinput.png)
   >```
   > export FN_REGISTRY=<your-docker-id>
   >```

2. If it isn't already running, start the Fn server using the `fn` cli:

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
and writes to standard output.  This is the standard Fn supported mechanism
for functions to receive input and return output.  
['Hot Functions'](https://github.com/fnproject/fn/blob/master/docs/developers/hot-functions.md)
(not discussed in this tutorial) are slightly different.  

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
   > docker build . -t <your-docker-id>/node-hello:0.0.1
   >```

2. Test the image by running it with no input:

   ![](images/userinput.png)
   >```
   > docker run --rm <your-docker-id>/node-hello:0.0.1
   >```

   The output should be:
   ```
   Hello World from Node!
   ```

3. Test the image by running it with a name parameter:

   ![](images/userinput.png)
   >```
   > echo -n "Jane" | docker run -i --rm <your-docker-id>/node-hello:0.0.1
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
> docker push <your-docker-id>/node-hello:0.0.1
>```

## Creating the Fn App and Defining the Route

Once we have a function container image we can associate that image with a
['route'](https://github.com/fnproject/fn/blob/master/docs/developers/model.md#routes).  

1. First we need an 
['application'](https://github.com/fnproject/fn/blob/master/docs/developers/model.md#applications)
to contain our functions.  Applications define a namespace to organize functions
and can contain configuration values that are shared across all functions in
that application:

   ![](images/userinput.png)
   >```
   > fn create apps demoapp
   >```

   ```
   Successfully created app:  demoapp
   ```

2. We then create a route that uses our manually built container image:

   ![](images/userinput.png)
   >```
   > fn create routes demoapp /hello <your-docker-id>/node-hello:0.0.1
   >```

   ```xml
   /hello created with <yourdockerid>/node-hello:0.0.1
   ```

3. We can confirm the route is correctly defined by getting a list of the routes
defined for an application:

   ![](images/userinput.png)
   >```
   > fn list routes demoapp
   >```

   You should see something like:

   ```xml
   path    image                            endpoint
   /hello  <your-docker-id>/node-hello:0.0.1  localhost:8080/r/demoapp/hello
   ```

At this point all the Fn server has is configuration metadata.
It has the name of an application and a function route that is part
of that application that points to a named and tagged Docker image.  It's
not until that route is invoked that this metadata is used.

## Calling the Function

Calling a function that was created through a manually defined route is no
different from calling a function defined using `fn deploy`, which is exactly
as intended!

1. Call the function using `fn call`:

   ![](images/userinput.png)
   >```
   > echo -n "Jane" | fn call demoapp /hello
   >```

   This will produce the expected output:

   ```sh
   Hello Jane from Node!
   ```

2. Call the function with curl using its http endpoint. You can find out the
endpoints for each of your routes using the `fn list routes` command we used
above.

   ![](images/userinput.png)
   >```
   > curl -d "Jane" http://localhost:8080/r/demoapp/hello
   >```

   This will produce exactly the same output as when using `fn call`, as 
   expected.

   ```sh
   Hello Jane from Node!
   ```

When the function is invoked, regardless of the mechanism, the Fn server
looks up the function image name and tag associated with the route and creates
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
name: node-hello
version: 0.0.1
runtime: docker
path: /hello2
```

This file defines a function with the same name and initial version as the
image we created earlier, identifies the function runtime as `docker`
rather than one of the Fn supported programming languages, and specifies the
path (i.e., route) to the function when it is eventually deployed. The path is
set to `hello2` to distinguish it from the manually deployed function.

Note: If `path:` is not specified the path will default to the containing folder
name.

With the `func.yaml` defined you can now easily build the function:

   ![](images/userinput.png)
   >```
   > fn build
   >```

   ```xml
   Building image <your-docker-id>/node-hello:0.0.1
   Function <your-docker-id>/node-hello:0.0.1 built successfully.
   ```

You can run the function with `fn run`:

   ![](images/userinput.png)
   >```
   > fn run
   >```

   ```xml
   Building image <your-docker-id>/node-hello:0.0.1
   Hello World from Node!
   ```

And you can pipe input into the function just like you did when using
`docker run`:

   ![](images/userinput.png)
   >```
   > echo -n "Jane" | fn run
   >```

   ```xml
   Building image <your-docker-id>/node-hello:0.0.1
   Hello Jane from Node!
   ```

Deploying a function using `fn deploy` is much simpler than a manual deployment
as it'll both push your container image and define the function route with one
command:

  ![](images/userinput.png)
   >```
   > fn deploy --app demoapp
   >```

   ```
  Deploying node-hello to app: demoapp at path: /hello2
  Bumped to version 0.0.2
  Building image <your-docker-id>/node-hello:0.0.2
  Updating route /hello2 using image <your-docker-id>/node-hello:0.0.2...
   ```

`fn deploy` orchestrates the push and route definition and it also "bumps",
or increments, the version number each time a function is deployed.

Once deployed you can use `fn call` to invoke the function or use `curl`
just like you did when you manually deployed your container.  Which makes
sense since all we've done is use the CLI to deploy the exact same image and
defined the exact same route as before.  We've simply automated the process.

Try out the same commands you ran earlier with our newly defined `hello2`
function.

![](images/userinput.png)
>```
> echo -n "Jane" | fn call demoapp /hello2
>```

![](images/userinput.png)
>```
> curl -d "Jane" http://localhost:8080/r/demoapp/hello2
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
