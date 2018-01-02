# Introduction

This tutorial introduces the 
[Fn Python FDK (Function Development Kit)](https://github.com/fnproject/fdk-python). 
If you haven't completed the [Introduction to Fn](../Introduction/README.md)
tutorial you should head over there before you proceed.

This tutorial takes you through the Fn developer experience for building
Python functions. It shows how easy it is to build, deploy and test
functions written in Python.

## What is an FDK?

The Fn Project allows developers to choose the intercommunication protocol format between an application and serverless function. 
There are currently 3 different formats that Fn defines:

    default: whatever an application sends inside the HTTP request body will be transferred to the function
    HTTP: the function will receive the full HTTP request
    JSON: the function will receive a JSON object very similar to CNCF OpenEvents

What is the difference between these formats? Why are there three and not just one? 
The short answer is that the default format is applied to cold functions only, 
meaning the functions container lives no longer than the time needed to process a single request. 
This means higher latencies due to container start/stop times, etc. 
In a high-performance low-latency situation, this may not be acceptable. 
In contrast, HTTP and JSON formats are applied to hot functions — meaning that the function stays alive 
as long as there are more requests to process, but not longer than the time defined by idle_timeout while waiting for more work). 
The HTTP format makes developers feel like they are talking to a web server because it looks very simple. 
JSON format is a bit different because Fn automatically performs the steps necessary to assemble a JSON object from the HTTP request.

So, what is an FDK and why do we need it? No matter what kind of format is defined for a particular function, 
Fn will serve the requests through STDIN and wait on STDOUT for the response and STDERR for function logs. 
In order to make a developer’s life simpler, the Fn team developed a set of libraries for different programming languages like Java, Python, Node, and Go. 
The main goal of the FDK is to let developers focus on their functions and hide all routine complexities underneath.

## How to develop with Python's FDK?

Let’s take a look at the echo function that uses the HTTP format:
```python
import fdk

from fdk import response

@fdk.coerce_input_to_content_type
def handler(context, data=None, loop=None):
    return response.RawResponse(
        context,
        status_code=200, 
        headers={}, 
        response_data=data
    )


if __name__ == "__main__":
    fdk.handle(handler)
```

Getting the FDK is easy because the Fn team is responsible for the FDKs distribution. 
With Python 3 (no more support for Python 2, hooray!!!) specifically, the Fn team published a consumable wheel distribution which you can find in the Warehouse. 
So the bare minimum requirement here is to write a function with exactly the same signature:
```
def handler(context, data=None, loop=None)
```

That’s it! Simple, isn’t it? It takes only a couple of minutes from the idea of the function to its execution. Here are some tips:

   * handle function can be a coroutine (see Python 3 async/await syntax), this is very useful if you prefer async programming over sync

```python
import asyncio
import fdk


@fdk.coerce_input_to_content_type
async def handler(context, data=None, loop=None):
    return data

if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    fdk.handle(handler, loop=loop)

```

   * through the context developers can access request headers, Fn application and route config as well as format-specific attributes like HTTP version, HTTP request method, etc.

You can find more information and code samples here and read more about Fn formats [here](https://github.com/fnproject/fn/blob/master/docs/function-format.md).

# Getting Started

>`mkdir pythonfn`

> `cd pythonfn`

>`fn init --runtime python3.6`

The output will be:
```sh
Runtime: python3.6
func.yaml created.
```
After that you need to create two files with the following content:

   * `func.py`
   
```python
import fdk

from fdk import response

@fdk.coerce_input_to_content_type
def handler(context, data=None, loop=None):
    return response.RawResponse(
        context,
        status_code=200, 
        headers={}, 
        response_data=data
    )


if __name__ == "__main__":
    fdk.handle(handler)
```

   * `requirements.txt`
 
```text
fdk==0.0.5
```

Before running your function it's recommended to adjust `func.yaml` by adding the following definition:
```yaml
format: json
build_image: python:3.6
```

Done! Your very simple echo function is ready to be deployed and executed!

# Running your Function

Let's build and run the generated function.  We're working locally and
won't be pushing our function images to a Docker registry like Docker
Hub. So before we build let's set `FN_REGISTRY` to a local-only registry
username like `fndemouser`.

>`export FN_REGISTRY=fndemouser`

Now we're ready to run.  Depending on whether this is your first time
developing a Python function you may or may not see Docker images being
pulled from Docker Hub.  Once the necessary base images are downloaded
subsequent operations will be faster.

>`echo 'test' | fn run --format json`

Here's what the abbreviated output will look like:
```text
Building image test:0.0.3 
Sending build context to Docker daemon   5.12kB
Step 1/6 : FROM python:3.6
 ---> c1e459c00dc3
Step 2/6 : WORKDIR /function
 ---> Using cache
 ---> eec798b79e70
Step 3/6 : ADD requirements.txt /function/
 ---> Using cache
 ---> 931e0f6d87d6
Step 4/6 : RUN pip3 install -r requirements.txt
 ---> Using cache
 ---> 49a4a4a207c1
Step 5/6 : ADD . /function/
 ---> 900011e4a904
Step 6/6 : ENTRYPOINT python3 func.py
 ---> Running in 6cd678649b3f
 ---> 6bc3fbe5cb7d
Removing intermediate container 6cd678649b3f
Successfully built 6bc3fbe5cb7d
Successfully tagged test:0.0.3

"test"
```

# Deploying your Python Function

As we're running the server on the local machine we can save time by not pushing the
generated image out to a remote Docker repository by using the `--local`
option.

>`fn deploy --local --app myapp`

```sh
Deploying pythonfn to app: myapp at path: /pythonfn
Bumped to version 0.0.2
Building image fndemouser/pythonfn:0.0.2
...
Successfully built 406b44a45821
Successfully tagged fndemouser/pythonfn:0.0.2
Updating route /pythonfn using image fndemouser/pythonfn:0.0.2...
```

Review the last line of the deploy output.  When deployed, a function's
Docker image is associated with a route which is the function's name and
the function's name is taken from the containing directory.  In this
case the route is `/pythonfn`.

We can use the route to invoke the function via curl and passing the
JSON input as the body of the call.

> `curl -v http://localhost:8080/r/myapp/pythonfn -d 'test'`

```sh
"test"
```

Success!

# Wrapping Up

Congratulations! You've just completed an introduction to the Fn Python
FDK.  There's so much more in the FDK than we can cover in a brief
introduction but we'll go deeper in subsequent tutorials.
