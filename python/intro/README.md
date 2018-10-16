# Introduction to Fn with Python
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll walk through
developing a function using the Python programming language (without installing
any Python tools!) and deploying that function to a local Fn server.  We'll also
learn about the core Fn concepts like applications and routes.

### Before you Begin
* Set aside about 15 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../../install/README.md).
    * Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". [See here](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context).)

> As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Your First Function

Let's start with a very simple "hello world" function written in
[Python](https://www.python.org/). Don't worry, you don't need to know Python!
In fact you don't even need to have Python installed on your development machine
as Fn provides the necessary Python interpreter and build tools as a Docker
container. Let's walk through your first function to become familiar with the
process and how Fn supports development.

### Create your Function
In the terminal type the following:

![user input](images/userinput.png)
>```sh
> fn init --runtime python --trigger http pythonfn
>```

The output will be

```yaml
Creating function at: /pythonfn
Runtime: python
Function boilerplate generated.
func.yaml created.
```

The `fn init` command creates an simple function with a bit of boilerplate to
get you started. The `--runtime` option is used to indicate that the function
we're going to develop will be written in Python. A number of other runtimes are
also supported.  Fn creates the simple function along with several supporting
files in the `/pythonfn` directory.

### Review your Function File

With your function created change into the `/pythonfn` directory.

![user input](images/userinput.png)
>```sh
> cd pythonfn
>```

Now get a list of the directory contents.

![user input](images/userinput.png)
>```sh
> ls
>
> func.py func.yaml requirements.txt
>```

The `func.py` file which contains your actual Python function is generated along
with several supporting files. To view your Python function type:

![user input](images/userinput.png)
>```sh
> cat func.py
>```

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

This function looks for JSON input in the form of `{"name": "Bob"}`. If this
JSON example is passed to the function, the function returns `{"message":"Hello
Bob!"}`. If no JSON data is found, the function returns `{"message":"Hello
World!"}`.  

### Understand func.yaml
The `fn init` command generated a `func.yaml` function
configuration file. Let's look at the contents:

![user input](images/userinput.png)
>```sh
> cat func.yaml
>```

```yaml
schema_version: 20180708
name: pythonfn
version: 0.0.1
runtime: python
entrypoint: python3 func.py
format: http-stream
triggers:
- name: pythonfn-trigger
  type: http
  source: /pythonfn-trigger
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* schema_version--identifies the version of the schema for this function file. Essentially, it determines which fields are present in `func.yaml`.
* name--the name of the function. Matches the directory name.
* version--automatically starting at 0.0.1.
* runtime--the name of the runtime/language which was set based on the value set
in `--runtime`.
* entrypoint--the name of the executable to invoke when your function is called,
in this case `python3 func.py`.
* format--the function uses JSON as its input/output method ([see: Open Function Format](https://github.com/fnproject/docs/blob/master/fn/develop/function-format.md)).
* triggers--identifies the automatically generated trigger name and source. For
example, this function would be executed from the URL
<http://localhost:8080/t/appname/pythonfn-trigger>. Where appname is the name of
the app chosen for your function when it is deployed.

There are other user specifiable properties but these will suffice for
this example.  Note that the name of your function is taken from the containing
folder name.  We'll see this come into play later on.

### Other Function Files
The `fn init` command generated one other file.

* `requirements.txt` --  specifies all the dependencies for your Python
function.

## Deploying Your First Function

With the `pythonfn` directory containing `func.py` and `func.yaml` you've got
everything you need to deploy the function to Fn server. This server could be
running in the cloud, in your datacenter, or on your local machine like we're
doing here.

Make sure your context is set to default and you are using a demo user. Use the `fn list context` command to check.

![user input](images/userinput.png)
>```sh
> fn list contexts
>```

```cs
CURRENT	NAME	PROVIDER	API URL			        REGISTRY
*       default	default		http://localhost:8080	fndemouser
```

If your context is not configured, please see [the context installation instructions](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context) before proceeding. Your context determines where your function is deployed.

Deploying your function is how you publish your function and make it accessible
to other users and systems. To see the details of what is happening during a
function deploy,  use the `--verbose` switch.  The first time you build a
function of a particular language it takes longer as Fn downloads the necessary
Docker images. The `--verbose` option allows you to see this process.

In your terminal type the following:


![user input](images/userinput.png)
>```sh
> fn --verbose deploy --app pythonapp --local
>```

You should see output similar to:

```yaml
Deploying pythonfn to app: pythonapp
Bumped to version 0.0.2
Building image fndemouser/pythonfn:0.0.2 
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon   5.12kB
Step 1/10 : FROM fnproject/python:3.6-dev as build-stage
3.6-dev: Pulling from fnproject/python
f2aa67a397c4: Pull complete 
862a29fe9d1e: Pull complete 
3227a4ed3d61: Pull complete 
3174c48f7eb1: Pull complete 
18529cf5d1ec: Pull complete 
6259a1295b71: Pull complete 
Digest: sha256:9434f87decd1d45c7b2f194b71a9698730af57925b4a29d4654daafaf946d204
Status: Downloaded newer image for fnproject/python:3.6-dev
 ---> aa4e9945b65f
Step 2/10 : WORKDIR /function
 ---> Running in 7633389270c3
Removing intermediate container 7633389270c3
 ---> 12d73905ee01
Step 3/10 : ADD . /function/
 ---> d29c19fc5efc
Step 4/10 : RUN pip3 install --target /python/  --no-cache --no-cache-dir -r requirements.txt &&    rm -fr ~/.cache/pip /tmp* requirements.txt func.yaml Dockerfile .venv
 ---> Running in 4641110eb6af
Collecting fdk (from -r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/ff/ce/045fa3e00ae8335072803cfb6adc57b202e5b1d00bf105beb9f22a5729ca/fdk-0.0.35-py2.py3-none-any.whl
Collecting uvloop (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/6f/62/1d3561a6c5ce0d0665b815efd1d9cff77a0879905371ec4da6b6ba4ccba9/uvloop-0.11.2-cp36-cp36m-manylinux1_x86_64.whl (3.7MB)
Collecting pytest-aiohttp==0.3.0 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/c9/2f/34f8581a799d1e58f0b64d9eb4aa0864b53f520d160281c2eb692340fefc/pytest_aiohttp-0.3.0-py3-none-any.whl
Collecting pbr!=2.1.0,>=2.0.0 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/01/0a/1e81639e7ed6aa51554ab05827984d07885d6873e612a97268ab3d80c73f/pbr-4.3.0-py2.py3-none-any.whl (106kB)
Collecting ujson==1.35 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/16/c4/79f3409bc710559015464e5f49b9879430d8f87498ecdc335899732e5377/ujson-1.35.tar.gz (192kB)
Collecting aiohttp==3.4.4 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/52/f9/c22977fc95346911d8fe507f90c3c4e4f445fdf339b750be6f03f090498d/aiohttp-3.4.4-cp36-cp36m-manylinux1_x86_64.whl (1.1MB)
Collecting iso8601==0.1.12 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/ef/57/7162609dab394d38bbc7077b7ba0a6f10fb09d8b7701ea56fa1edc0c4345/iso8601-0.1.12-py2.py3-none-any.whl
Collecting requests==2.18.4 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/49/df/50aa1999ab9bde74656c2919d9c0c085fd2b3775fd3eca826012bef76d8c/requests-2.18.4-py2.py3-none-any.whl (88kB)
Collecting dill==0.2.7.1 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/91/a0/19d4d31dee064fc553ae01263b5c55e7fb93daff03a69debbedee647c5a0/dill-0.2.7.1.tar.gz (64kB)
Collecting pytest (from pytest-aiohttp==0.3.0->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/08/e0/a4945a06380802264b3416d788ad607588c334662b6cd0af54144c45912d/pytest-3.8.2-py2.py3-none-any.whl (209kB)
Collecting chardet<4.0,>=2.0 (from aiohttp==3.4.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/bc/a9/01ffebfb562e4274b6487b4bb1ddec7ca55ec7510b22e4c51f14098443b8/chardet-3.0.4-py2.py3-none-any.whl (133kB)
Collecting async-timeout<4.0,>=3.0 (from aiohttp==3.4.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/e1/1e/5a4441be21b0726c4464f3f23c8b19628372f606755a9d2e46c187e65ec4/async_timeout-3.0.1-py3-none-any.whl
Collecting attrs>=17.3.0 (from aiohttp==3.4.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/3a/e1/5f9023cc983f1a628a8c2fd051ad19e76ff7b142a0faf329336f9a62a514/attrs-18.2.0-py2.py3-none-any.whl
Collecting multidict<5.0,>=4.0 (from aiohttp==3.4.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/60/10/05f29904a073fdba2d367cdd8c7862260cc23cdee52ba0443f4599acbc93/multidict-4.4.2-cp36-cp36m-manylinux1_x86_64.whl (385kB)
Collecting idna-ssl>=1.0; python_version < "3.7" (from aiohttp==3.4.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/46/03/07c4894aae38b0de52b52586b24bf189bb83e4ddabfe2e2c8f2419eec6f4/idna-ssl-1.1.0.tar.gz
Collecting yarl<2.0,>=1.0 (from aiohttp==3.4.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/61/67/df71b367680e06bb4127e3df6189826d4b9daebf83c3bd5b9341c99ef528/yarl-1.2.6-cp36-cp36m-manylinux1_x86_64.whl (253kB)
Collecting certifi>=2017.4.17 (from requests==2.18.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/df/f7/04fee6ac349e915b82171f8e23cee63644d83663b34c539f7a09aed18f9e/certifi-2018.8.24-py2.py3-none-any.whl (147kB)
Collecting idna<2.7,>=2.5 (from requests==2.18.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/27/cc/6dd9a3869f15c2edfab863b992838277279ce92663d334df9ecf5106f5c6/idna-2.6-py2.py3-none-any.whl (56kB)
Collecting urllib3<1.23,>=1.21.1 (from requests==2.18.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/63/cb/6965947c13a94236f6d4b8223e21beb4d576dc72e8130bd7880f600839b8/urllib3-1.22-py2.py3-none-any.whl (132kB)
Collecting more-itertools>=4.0.0 (from pytest->pytest-aiohttp==0.3.0->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/79/b1/eace304ef66bd7d3d8b2f78cc374b73ca03bc53664d78151e9df3b3996cc/more_itertools-4.3.0-py3-none-any.whl (48kB)
Collecting pluggy>=0.7 (from pytest->pytest-aiohttp==0.3.0->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/f5/f1/5a93c118663896d83f7bcbfb7f657ce1d0c0d617e6b4a443a53abcc658ca/pluggy-0.7.1-py2.py3-none-any.whl
Collecting atomicwrites>=1.0 (from pytest->pytest-aiohttp==0.3.0->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/3a/9a/9d878f8d885706e2530402de6417141129a943802c084238914fa6798d97/atomicwrites-1.2.1-py2.py3-none-any.whl
Collecting setuptools (from pytest->pytest-aiohttp==0.3.0->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/96/06/c8ee69628191285ddddffb277bd5abdf769166e7a14b867c2a172f0175b1/setuptools-40.4.3-py2.py3-none-any.whl (569kB)
Collecting py>=1.5.0 (from pytest->pytest-aiohttp==0.3.0->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/c8/47/d179b80ab1dc1bfd46a0c87e391be47e6c7ef5831a9c138c5c49d1756288/py-1.6.0-py2.py3-none-any.whl (83kB)
Collecting six>=1.10.0 (from pytest->pytest-aiohttp==0.3.0->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/67/4b/141a581104b1f6397bfa78ac9d43d8ad29a7ca43ea90a2d863fe3056e86a/six-1.11.0-py2.py3-none-any.whl
Installing collected packages: uvloop, chardet, async-timeout, attrs, multidict, idna, idna-ssl, yarl, aiohttp, six, more-itertools, pluggy, atomicwrites, setuptools, py, pytest, pytest-aiohttp, pbr, ujson, iso8601, certifi, urllib3, requests, dill, fdk
  Running setup.py install for idna-ssl: started
    Running setup.py install for idna-ssl: finished with status 'done'
  Running setup.py install for ujson: started
    Running setup.py install for ujson: finished with status 'done'
  Running setup.py install for dill: started
    Running setup.py install for dill: finished with status 'done'
Successfully installed aiohttp-3.4.4 async-timeout-3.0.1 atomicwrites-1.2.1 attrs-18.2.0 certifi-2018.8.24 chardet-3.0.4 dill-0.2.7.1 fdk-0.0.35 idna-2.6 idna-ssl-1.1.0 iso8601-0.1.12 more-itertools-4.3.0 multidict-4.4.2 pbr-4.3.0 pluggy-0.7.1 py-1.6.0 pytest-3.8.2 pytest-aiohttp-0.3.0 requests-2.18.4 setuptools-40.4.3 six-1.11.0 ujson-1.35 urllib3-1.22 uvloop-0.11.2 yarl-1.2.6
You are using pip version 10.0.1, however version 18.1 is available.
You should consider upgrading via the 'pip install --upgrade pip' command.
Removing intermediate container 4641110eb6af
 ---> 887436080f58
Step 5/10 : FROM fnproject/python:3.6
3.6: Pulling from fnproject/python
f2aa67a397c4: Already exists 
862a29fe9d1e: Already exists 
3227a4ed3d61: Already exists 
3174c48f7eb1: Already exists 
18529cf5d1ec: Already exists 
Digest: sha256:6ffa941edd18cb97cb3beaed8670e404732a657d62f6a032868d2b7b410839e5
Status: Downloaded newer image for fnproject/python:3.6
 ---> 0acac5f4cdb6
Step 6/10 : WORKDIR /function
 ---> Running in 93ee9a57e490
Removing intermediate container 93ee9a57e490
 ---> ac8887c31f87
Step 7/10 : COPY --from=build-stage /function /function
 ---> 86a9c176c52b
Step 8/10 : COPY --from=build-stage /python /python
 ---> 3f778ca30eb8
Step 9/10 : ENV PYTHONPATH=/python
 ---> Running in 06a922391727
Removing intermediate container 06a922391727
 ---> 17c76bc6eaad
Step 10/10 : ENTRYPOINT ["python3", "func.py"]
 ---> Running in e86a2d888dcc
Removing intermediate container e86a2d888dcc
 ---> fa4cafcb2b74
Successfully built fa4cafcb2b74
Successfully tagged fndemouser/pythonfn:0.0.2

Updating function pythonfn using image fndemouser/pythonfn:0.0.2...
Successfully created app:  pythonapp
Successfully created function: pythonfn with fndemouser/pythonfn:0.0.2
Successfully created trigger: pythonfn-trigger
```

All the steps to load the current language Docker image are displayed.

Functions are grouped into applications so by specifying `--app pythonapp`
we're implicitly creating the application `pythonapp` and associating our
function with it.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message `Updating function pythonfn using image
fndemouser/pythonfn:0.0.2...` let's us know that the function is packaged in the
image `fndemouser/pythonfn:0.0.2`.

Note that the containing folder name `pythonfn` was used as the name of the
generated Docker container and used as the name of the function that container
was bound to. By convention it is also used to create the trigger name
`pythonfn-trigger`.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.


## Invoke your Deployed Function

There are two ways to call your deployed function.  

### Invoke with the CLI

The first is using the Fn CLI which makes invoking your function relatively
easy.  Type the following:

![user input](images/userinput.png)
>```sh
> fn invoke pythonapp pythonfn
>```

which results in:

```js
{"message":"Hello World"}
```

When you invoked "pythonapp pythonfn" the fn server looked up the "pythonapp"
application and then looked for the Docker container image bound to the
"pythonfn" function and executed the code. Fn `invoke` invokes your function
directly and independently of any associated triggers.  You can always invoke a
function even without it having any triggers bound to it.

You can also pass data to the invoke command. Note that you set the content type
for the data passed. For example:

![user input](images/userinput.png)
>```sh
> echo -n '{"name":"Bob"}' | fn invoke pythonapp pythonfn --content-type application/json
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
the language interpreter and all the necessary build tools.  The second
image packages all dependencies and any necessary language
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
fndemouser/pythonfn      0.0.2               cde014cefdad        7 minutes ago       15.1MB
```

### Explore your Application

The fn CLI provides a couple of commands to let us see what we've deployed.
`fn list apps` returns a list of all of the defined applications.

![User Input Icon](images/userinput.png)
>```sh
> fn list apps
>```

Which, in our case, returns the name of the application we created when we
deployed our pythonfn function:

```sh
NAME
pythonapp
```

We can also see the functions that are defined by an application. Since functions are exposed via triggers, the `fn list triggers <appname>` command is used. To list the functions included in "pythonapp" we can type:

![User Input Icon](images/userinput.png)
>```sh
> fn list triggers pythonapp
>```

```sh
FUNCTION    NAME                TYPE    SOURCE            ENDPOINT
pythonfn    pythonfn-trigger    http    /pythonfn-trigger http://localhost:8080/t/pythonapp/pythonfn-trigger
```

The output confirms that pythonapp contains a `pythonfn` function which may be
requested via the specified URL.

### Invoke with Curl

The other way to invoke your function is via HTTP.  The Fn server exposes our
deployed function at `http://localhost:8080/t/pythonapp/pythonfn-trigger`, a URL
that incorporates our application and function trigger as path elements.

Use `curl` to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -H "Content-Type: application/json" http://localhost:8080/t/pythonapp/pythonfn-trigger
>```

The result is once again the same.

```js
{"message":"Hello World"}
```

We can again pass JSON data to our function and get the value of name passed to
the function back.

![user input](images/userinput.png)
>```
> curl -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/t/pythonapp/pythonfn-trigger
>```

The result is once again the same.

```js
{"message":"Hello Bob"}
```

## Wrap Up

Congratulations! In this tutorial you've accomplished a lot. You've created your
first function, deployed it to your local Fn server and invoked it
over HTTP.

**Go:** [Back to Contents](../../README.md)
