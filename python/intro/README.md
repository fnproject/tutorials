# Introduction to Fn with Python
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll walk through
developing a function using the Python programming language (without installing
any Python tools!) and deploying that function to a local Fn server.  We'll also
learn about the core Fn concepts like applications and routes.

### Before you Begin
* Set aside about 15 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../../install/README.md).
    * Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". See [here](../install/README.md).)

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
> func.py func.yaml requirements.txt test.json
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
format: json
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
in this case `python3 func.rb`.
* format--the function uses JSON as its input/output method ([see: Open Function Format](https://github.com/fnproject/fn/blob/master/docs/developers/function-format.md)).
* triggers--identifies the automatically generated trigger name and source. For
example, this function would be executed from the URL
<http://localhost:8080/t/appname/pythonfn-trigger>. Where appname is the name of
the app chosen for your function when it is deployed.

There are other user specifiable properties but these will suffice for
this example.  Note that the name of your function is taken from the containing
folder name.  We'll see this come into play later on.

### Other Function Files
The `fn init` command generated two other files.

* `requirements.txt` --  specifies all the dependencies for your Python
function.
* `test.json` -- a test file that is used to test your function. It defines an
input and the output of the function and helps to identify if the function works
correctly or not. Function testing is not covered in this tutorial.


## Deploying Your First Function

With the `pythonfn` directory containing `func.py` and `func.yaml` you've got
everything you need to deploy the function to Fn server. This server could be
running in the cloud, in your datacenter, or on your local machine like we're
doing here.

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
Sending build context to Docker daemon  6.144kB
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
 ---> Running in 3088753b5dd9
Removing intermediate container 3088753b5dd9
 ---> 227c1a697ca6
Step 3/10 : ADD . /function/
 ---> e00a81b000f4
Step 4/10 : RUN pip3 install --target /python/  --no-cache --no-cache-dir -r requirements.txt &&    rm -fr ~/.cache/pip /tmp* requirements.txt func.yaml Dockerfile .venv
 ---> Running in 2639f4d0283b
Collecting fdk (from -r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/55/c9/0632f8210c8d7b8aad729f5dbeef39c2090a7c818be962beca565a3eea5b/fdk-0.0.32-py2.py3-none-any.whl
Collecting ujson==1.35 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/16/c4/79f3409bc710559015464e5f49b9879430d8f87498ecdc335899732e5377/ujson-1.35.tar.gz (192kB)
Collecting requests==2.18.4 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/49/df/50aa1999ab9bde74656c2919d9c0c085fd2b3775fd3eca826012bef76d8c/requests-2.18.4-py2.py3-none-any.whl (88kB)
Collecting iso8601==0.1.12 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/ef/57/7162609dab394d38bbc7077b7ba0a6f10fb09d8b7701ea56fa1edc0c4345/iso8601-0.1.12-py2.py3-none-any.whl
Collecting uvloop (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/6f/62/1d3561a6c5ce0d0665b815efd1d9cff77a0879905371ec4da6b6ba4ccba9/uvloop-0.11.2-cp36-cp36m-manylinux1_x86_64.whl (3.7MB)
Collecting pbr!=2.1.0,>=2.0.0 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/69/1c/98cba002ed975a91a0294863d9c774cc0ebe38e05bbb65e83314550b1677/pbr-4.2.0-py2.py3-none-any.whl (100kB)
Collecting dill==0.2.7.1 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/91/a0/19d4d31dee064fc553ae01263b5c55e7fb93daff03a69debbedee647c5a0/dill-0.2.7.1.tar.gz (64kB)
Collecting certifi>=2017.4.17 (from requests==2.18.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/df/f7/04fee6ac349e915b82171f8e23cee63644d83663b34c539f7a09aed18f9e/certifi-2018.8.24-py2.py3-none-any.whl (147kB)
Collecting idna<2.7,>=2.5 (from requests==2.18.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/27/cc/6dd9a3869f15c2edfab863b992838277279ce92663d334df9ecf5106f5c6/idna-2.6-py2.py3-none-any.whl (56kB)
Collecting urllib3<1.23,>=1.21.1 (from requests==2.18.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/63/cb/6965947c13a94236f6d4b8223e21beb4d576dc72e8130bd7880f600839b8/urllib3-1.22-py2.py3-none-any.whl (132kB)
Collecting chardet<3.1.0,>=3.0.2 (from requests==2.18.4->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/bc/a9/01ffebfb562e4274b6487b4bb1ddec7ca55ec7510b22e4c51f14098443b8/chardet-3.0.4-py2.py3-none-any.whl (133kB)
Installing collected packages: ujson, certifi, idna, urllib3, chardet, requests, iso8601, uvloop, pbr, dill, fdk
  Running setup.py install for ujson: started
    Running setup.py install for ujson: finished with status 'done'
  Running setup.py install for dill: started
    Running setup.py install for dill: finished with status 'done'
Successfully installed certifi-2018.8.24 chardet-3.0.4 dill-0.2.7.1 fdk-0.0.32 idna-2.6 iso8601-0.1.12 pbr-4.2.0 requests-2.18.4 ujson-1.35 urllib3-1.22 uvloop-0.11.2
You are using pip version 10.0.1, however version 18.0 is available.
You should consider upgrading via the 'pip install --upgrade pip' command.
Removing intermediate container 2639f4d0283b
 ---> aabd5c7367db
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
 ---> Running in 6a309ec88d08
Removing intermediate container 6a309ec88d08
 ---> 3ecd72f2990e
Step 7/10 : COPY --from=build-stage /function /function
 ---> 971e8feb927b
Step 8/10 : COPY --from=build-stage /python /python
 ---> 3965f0363d1d
Step 9/10 : ENV PYTHONPATH=/python
 ---> Running in c7d2e0c88f4e
Removing intermediate container c7d2e0c88f4e
 ---> f3d3658c334f
Step 10/10 : ENTRYPOINT ["python3", "func.py"]
 ---> Running in 2a882b7f0538
Removing intermediate container 2a882b7f0538
 ---> 364fb65e45a9
Successfully built 364fb65e45a9
Successfully tagged fndemouser/pythonfn:0.0.2

Updating function pythonfn using image fndemouser/pythonfn:0.0.2...
Successfully created app:  pythonapp
Successfully created function: pythonfn with fndemouser/pythonfn:0.0.2
Successfully created trigger: pythonfn-trigger
```

All the steps to load the current language Docker image are displayed.

Functions are grouped into applications so by specifying `--app pythonapp`
we're implicitly creating the application "pythonapp" and associating our
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

The output confirms that pythonapp contains a `pythonfn` function that is
implemented by the Docker container `fndemouser/pythonfn:0.0.2` which may be
invoked via the specified URL.

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
