# Introduction to fdk-python

This tutorial introduces the  [Fn Python FDK (Function Development
Kit)](https://github.com/fnproject/fdk-python).  If you haven't completed the
[Introduction to Fn](../Introduction/README.md) tutorial you should head over
there before you proceed.

This tutorial takes you through the Fn developer experience for building
Python functions. It shows how easy it is to build, deploy and test
functions written in Python.

## What is an FDK?

The Fn Project allows developers to choose the intercommunication protocol
format between an application and a serverless function.  There are currently
three different formats that Fn defines:

* default: whatever an application sends inside the HTTP request body will be transferred to the function
* JSON: the function receives a JSON object similar to the [CNCF Cloud Event Format](https://github.com/cloudevents/spec/blob/master/spec.md)
* cloudevent: the function receives a JSON object using the [CNCF Cloud Event Format](https://github.com/cloudevents/spec/blob/master/spec.md).

What is the difference between these
[formats](https://github.com/fnproject/fn/blob/master/docs/developers/function-format.md)?
Why are there three and not just one?

The short answer is the default format is applied only to cold functions.
A cold function container lives no longer than the time needed to process
a single request.  This means higher latencies due to container start/stop
times, etc.  In a high performance, low latency situation this may not be
acceptable.

In contrast, JSON and CloudEvents formats are applied to hot functions. A hot
function stays alive as long as there are more requests to process, defined by
an `idle_timeout` value. For a developer, the JSON format is a bit different
because Fn automatically assembles a JSON object from the HTTP request. The
CloudEvents format differs in that is explicitly stores JSON data using the
[CNCF CloudEvents
Format](https://github.com/cloudevents/spec/blob/master/spec.md). Effectively,
when JSON data is stored, additional metadata is added.

So what is an FDK and why do we need it? No matter what kind of format is
defined for a particular function, Fn serves the requests through STDIN, waits
on STDOUT for the responses, and sends errors to STDERR for function logging. In
order to make a developer’s life simpler, the Fn team developed a set of
libraries for different programming languages like Java, Python, Node, and Go.
The main goal of an FDK is to let developers focus on their functions and hide
all routine complexity.

**Note:** FDK-Python supports only JSON format.

# Create function's boilerplate

>```sh
> mkdir pythonfn
>
> cd pythonfn
>
> fn init --runtime python3.6
>```

The output will be:

```yaml
Runtime: python3.6
Function boilerplate generated.
func.yaml created.
```
The Fn CLI created the following files in the directory:

   * `func.py`

```python
import fdk
import json


def handler(ctx, data=None, loop=None):
    name = "World"
    if data and len(data) > 0:
        body = json.loads(data)
        name = body.get("name")
    return {"message": "Hello {0}".format(name)}


if __name__ == "__main__":
    fdk.handle(handler)

```

   * `requirements.txt`

```text
fdk
```

   * `func.yaml`

```yaml
name: new-python
version: 0.0.1
runtime: python3.6
entrypoint: python3 func.py
format: json
```

Done! A very simple echo function ready to be deployed and executed!

## How to develop with Python's FDK?

Let’s take a look at the echo function:

```python
import fdk
import json


def handler(ctx, data=None, loop=None):
    name = "World"
    if data and len(data) > 0:
        body = json.loads(data)
        name = body.get("name")
    return {"message": "Hello {0}".format(name)}


if __name__ == "__main__":
    fdk.handle(handler)

```

Getting `fdk-python` is easy because the Fn team is responsible for FDK
distribution. With Python 3.6 or greater (no more support for Python 2,
hooray!!!), the Fn team published a consumable wheel distribution
which you can find in the Warehouse. The minimum requirement here is to
write a function with exactly the same signature:

```python
def handler(context, data=None, loop=None)
```

That’s it! Simple isn’t it? It takes only a couple of minutes from function idea to execution. Here are some tips:

* Handle function can be a coroutine (see Python 3 async/await syntax), this is very useful if you prefer async programming over sync.

```python
import fdk
import json


def handler(ctx, data=None, loop=None):
    name = "World"
    if data and len(data) > 0:
        body = json.loads(data)
        name = body.get("name")
    return {"message": "Hello {0}".format(name)}



if __name__ == "__main__":
    fdk.handle(handler)

```

* Through the context developers can access request headers, Fn application and route config as well as format-specific attributes request method, etc.

You can find more information and code samples here and read more about [Fn
formats
here](https://github.com/fnproject/fn/blob/master/docs/function-format.md).

## First run

To check if you functions runs well Fn CLI offers nifty feature: `fn run`.
It helps you identify whether a function's code can be packaged and executed using a wrapper around Docker CLI:

>```sh
> fn run
>```

```sh
Building image fndemouser/pythonfn:0.0.1 ..........................................
{"message":"Hello World"}
```

This is a small but still very meaningful step towards setting up a real serverless function with Fn server.

## Deploying your Python Function

Since we're running the server locally, use the `--local` option to save time by
not pushing the generated image out to a remote Docker repository.

>```sh
> fn deploy --local --app myapp
>```

```sh
Deploying pythonfn to app: myapp at path: /pythonfn
Bumped to version 0.0.2
Building image fndemouser/pythonfn:0.0.2
...
Successfully built 406b44a45821
Successfully tagged fndemouser/pythonfn:0.0.2
Updating route /pythonfn using image fndemouser/pythonfn:0.0.2...
```

Review the last line of the output. When deployed, a function's Docker image is
associated with a route which is the function's name. The function's name is
taken from the containing directory, in this case the route is `/pythonfn`.

You use the route to invoke the function via curl, passing the
JSON input as the body of the call.

>```sh
> curl -v -H "Content-Type: application/json" -d '{"name": "John"}' http://localhost:8080/r/myapp/pythonfn 
>```

```sh
{"message":"Hello John"}
```

Success!

## Wrapping Up

Congratulations! You've just completed an introduction to the Fn Python FDK.
There's so much more in the FDK than we can cover in a brief
introduction but we'll go deeper in subsequent tutorials.
