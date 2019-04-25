# Creating a Function from a Docker Image

This tutorial walks through how to use a custom Docker image to define an
Fn function.  Although Fn functions are packaged as Docker images, when
developing functions using the Fn CLI developers are not directly exposed
to the underlying Docker platform.  Docker isn't hidden (you can see
Docker build output and image names and tags), but you aren't
required to be very Docker-savvy to develop functions with Fn.
However, sometimes you need to handle advanced use cases and must take
complete control of the creation of the function container image. Fortunately
the design and implementation of Fn enables you to do exactly that.  Let's
build a simple custom function container image to become familiar with the key
elements of the process.

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Prequisites

This tutorial requires you to have both Docker and Fn installed. If you need
help with Fn installation you can find instructions in the
[Install and Start Fn Tutorial](../install/README.md).

# Getting Started


If it isn't already running, you'll need to start the Fn server.  We'll
run it in the foreground to let us see the server log messages so let's open a
new terminal for this.

Start the Fn server using the `fn` cli:

   ![](images/userinput.png)
   >```
   > fn start
   >```


# Magick Functions

One of the most common reasons for writing a custom Dockerfile for a function
is to install a Linux package that your function needs.  In our example we're
going to use the the ever popular [ImageMagick](https://www.imagemagick.org) to
do some image processing in our function and while there is a Node.js module for
ImageMagick, it's just a wrapper on the underlying native libary.  So we'll
have to install the library in addition to adding the Node module to our
`package.json` dependencies. Let's start by creating the Node function.

## Function Definition

![](images/userinput.png)
> In an **empty folder** create a file named `func.js` and copy/paste the
following as its content:
 
```javascript
const fdk = require('@fnproject/fdk');
const fs  = require('fs');
const tmp = require('tmp');
const im  = require('imagemagick');

fdk.handle((buffer, ctx) => {
  return new Promise((resolve, reject) => {
    tmp.tmpName((err, tmpFile) => {
      if (err) throw err;
      fs.writeFile(tmpFile, buffer, (err) => {
        if (err) throw err;
        im.identify(['-format', '{"width": %w, "height": %h}', tmpFile],
          (err, output) => {
            if (err) {
              reject(err);
            } else {
              resolve(JSON.parse(output));
            }
          }
        );
      });
    });
  });
}, { inputMode: 'buffer' });
```

The function takes a binary image as it's argument, writes it to a tmp
file, and then uses ImageMagick to obtain the width and height of the image.
Since the function argument type is binary we need to set the "inputMode"
property to "buffer" when we call the the FDK's handle function.

## Declaring Node.js Dependencies

There are lots of interesting elements to this function but the key one for us
is the use of the "imagemagick" Node module for image processing.  To use it
we need to include it in our dependencies in the `package.json` along with the
other dependencies.

![](images/userinput.png)
> In same folder as the `func.js` file, create a `package.json` file and
copy/paste the following as its content:

```json
{
	"name": "imagedims",
	"version": "1.0.0",
	"description": "Function using ImageMagick that returns dimensions",
	"main": "func.js",
	"author": "fnproject.io",
	"license": "Apache-2.0",
	"dependencies": {
		"@fnproject/fdk": ">=0.0.11",
		"tmp": "^0.0.33",
		"imagemagick": "^0.1.3"
	}
}
```

Like all Node.js functions using the Fn Node FDK we include it as a dependency
along with the "tmp" module for temporary file utilities and "imagemagick" for
image processing.  

## Function Metadata

Now that we have a Node.js function and it's dependencies captured in the 
`package.json` we need a `func.yaml` to capture the function metadata.

![](images/userinput.png)
> In the folder containing the previously created files, create a `func.yaml`
file and copy/paste the following as its content:

```yaml
schema_version: 20180708
name: imagedims
version: 0.0.1
runtime: docker
triggers:
- name: imagedims-trigger
  type: http
  source: /imagedims
```

This is a typical `func.yaml` for a Node.js function except that instead of
declaring the **runtime** as "node" we've specified "**docker**".  If you were
to type `fn build` right now you'd get the error:

> Fn: Dockerfile does not exist for 'docker' runtime

This is because when you set the runtime type to "docker" `fn build` defers to
your Dockerfile to build the function container image--and you haven't defined
one yet!

## Default Node.js Function Dockerfile

The Dockerfile that `fn build` would normally generate to build a Node.js
function container image looks like this:

```Dockerfile
FROM fnproject/node:dev as build-stage
WORKDIR /function
ADD package.json /function/
RUN npm install

FROM fnproject/node
WORKDIR /function
ADD . /function/
COPY --from=build-stage /function/node_modules/ /function/node_modules/
ENTRYPOINT ["node", "func.js"]
```

It's a two stage build with the `fnproject/node:dev` image containing `npm` and
other build tools, and the `fnproject/node` image containing just the Node
runtime.  This approach is designed to ensure that deployable function container
images are as small as possible--which is beneficial for a number of reasons.

## Custom Node.js Function Dockerfile

The `fnproject/node` container image is built on Alpine so we'll need to install
the
[ImageMagick Alpine package](https://pkgs.alpinelinux.org/packages?name=imagemagick&branch=edge)
using the `apk` package management utility.  You can do this with a Dockerfile
`RUN` command:

```Dockerfile
RUN apk add --no-cache imagemagick
```

We want to install ImageMagick into the runtime image, not the build image,
so we need to add the `RUN` command after the `FROM fnproject/node` command.

![](images/userinput.png)
> In the folder containing the previously created files, create a file named
`Dockerfile` and copy/paste the following as its content:

```Dockerfile
FROM fnproject/node:dev as build-stage
WORKDIR /function
ADD package.json /function/
RUN npm install

FROM fnproject/node
RUN apk add --no-cache imagemagick
WORKDIR /function
ADD . /function/
COPY --from=build-stage /function/node_modules/ /function/node_modules/
ENTRYPOINT ["node", "func.js"]
```

With this Dockerfile, the Node.js function, it's dependencies 
(including the "imagemagick" wrapper), and the "imagemagick" Alpine package
will be included in an image derived from the base `fnproject/node` image. We
should be good to go!

## Building and Deploying

Once you have your custom Dockerfile you can simply use `fn build` to build
your function.  Give it a try:

![](images/userinput.png)
>```
> fn -v build
>```

You should see output similar to:

```shell
Building image imagedims:0.0.1
Current Context:  default
Sending build context to Docker daemon  27.65kB
Step 1/10 : FROM fnproject/node:dev as build-stage
 ---> 016382f39a51
...
Step 6/10 : RUN apk add --no-cache imagemagick
 ---> Using cache
 ---> 5c4a1e19767c
...
Successfully built 8f199b0bef00
Successfully tagged imagedims:0.0.1

Function imagedims:0.0.1 built successfully.
```

Just like with a default build, the output is a container image.  From this
point forward everything is just as it would be for any Fn function.  Since
you've previously started an Fn server, you can deploy it.  Let's deploy to
an application named 'tutorial':


![](images/userinput.png)
>```
> fn deploy --create-app --app tutorial --local --no-bump
>```

We can confirm the function is correctly defined by getting a list of the
functions in the "tutorial" application:

![](images/userinput.png)
>```
> fn list functions tutorial
>```

**Pro tip**: The fn cli let's you abbreviate most of the keywords so you can
also say `fn ls f tutorial`!

You should see output similar to:

```shell
NAME        IMAGE             ID
imagedims   imagedims:0.0.1   01CWFAS9DBNG8G00RZJ0000002
```

## Invoking the Function

With the function deployed let's invoke it to make sure it's working as
expected. You'll need a jpeg or png file so either find one on your machine
or download one.  If you've cloned this tutorial's Git repo you can use the
`3x3.jpg` image that has a height and width of 3 pixels.

![](images/userinput.png)
>```
> cat 3x3.jpg | fn invoke tutorial imagedims
>```

For this file you should see the following output:

>```json
>{"width":3,"height":3}
>```

## Calling the Function with curl

We included an HTTP trigger declaration in the `func.yaml` so we can also call
the function with curl.  It's a little more complicated as you need to declare
the content type because the request body is binary.  You also need to use the
`--data-binary` switch:

![](images/userinput.png)
>```
> curl --data-binary @3x3.jpg -H "Content-Type: application/octet-stream" -X POST http://localhost:8080/t/tutorial/imagedims
>```

You should get exactly the same output as when using `fn invoke`.

# Conclusion

One of the most powerful features of Fn is the ability to use custom defined
Docker container images as functions. This feature makes it possible to
customize your function's runtime environment including letting you install
any Linux libraries or utilities that your function might need. And thanks to
the Fn CLI's support for Dockerfiles it's the same user experience as when
developing any function.

Having completed this tutorial you've successfully built a function using
a custom Dockerfile. Congratulations!

**Go:** [Back to Contents](../README.md)
