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
> fn init --runtime python pythonfn
>```

The output will be

```yaml
Creating function at: /pythonfn
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
import io
import json


from fdk import response


def handler(ctx, data: io.BytesIO=None):
    name = "World"
    try:
        body = json.loads(data.getvalue())
        name = body.get("name")
    except (Exception, ValueError) as ex:
        print(str(ex))

    return response.Response(
        ctx, response_data=json.dumps(
            {"message": "Hello {0}".format(name)}),
        headers={"Content-Type": "application/json"}
    )

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
entrypoint: /python/bin/fdk /function/func.py handler
memory: 256
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* schema_version--identifies the version of the schema for this function file. Essentially, it determines which fields are present in `func.yaml`.
* name--the name of the function. Matches the directory name.
* version--automatically starting at 0.0.1.
* runtime--the name of the runtime/language which was set based on the value set
in `--runtime`.
* memory--The max memory size for a function in megabytes.
* entrypoint--the name of the executable to invoke when your function is called,
in this case `/python/bin/fdk /function/func.py handler`.

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

### Check your Context
Make sure your context is set to default and you are using a demo user. Use the `fn list context` command to check.

![user input](images/userinput.png)
>```sh
> fn list contexts
>```

```cs
CURRENT NAME     PROVIDER  API URL                REGISTRY
*       default  default   http://localhost:8080  fndemouser
```

If your context is not configured, please see [the context installation instructions](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context) before proceeding. Your context determines where your function is deployed.

### Create an App
Next, functions are grouped together into an application. The application acts as the main organizing structure for multiple functions. To create an application type the following:

![user input](images/userinput.png)
>```sh
> fn create app pythonapp
>```

A confirmation is returned:

```yaml
Successfully created app:  pythonapp
```

Now `pythonapp` is ready for functions to be deployed to it.

### Deploy your Function to your App
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

You should see something similar to:

```yaml
Deploying pythonfn to app: pythonapp
Bumped to version 0.0.2
Building image fndemouser/pythonfn:0.0.2
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon  5.632kB
Step 1/12 : FROM fnproject/python:3.7.1-dev as build-stage
3.7.1-dev: Pulling from fnproject/python
a5a6f2f73cd8: Pull complete
3a6fba040982: Pull complete
738ebe0cf907: Pull complete
a4b11c375c52: Pull complete
02c57c00f1bc: Pull complete
5dac448549aa: Pull complete
Digest: sha256:80dd569dfc2a616b513bba52d0e03ae9db933c7aa6039687c13bf59c1ce47410
Status: Downloaded newer image for fnproject/python:3.7.1-dev
 ---> f1676f17ed78
Step 2/12 : WORKDIR /function
 ---> Running in eceb6dfa8933
Removing intermediate container eceb6dfa8933
 ---> 1fe73929277e
Step 3/12 : ADD requirements.txt /function/
 ---> 58a2943bfd15
Step 4/12 : RUN pip3 install --target /python/  --no-cache --no-cache-dir -r requirements.txt &&			 rm -fr ~/.cache/pip /tmp* requirements.txt func.yaml Dockerfile .venv
 ---> Running in 451ecb930e43
Collecting fdk (from -r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/d2/33/90a998c3dde2d94b4b45db3ca08250596278a22a22efabd36a18abcc7b00/fdk-0.1.1-py3-none-any.whl (48kB)
Collecting pbr!=2.1.0,>=2.0.0 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/8c/7f/fed53b379500fd889707d1f6e61c2a35e12f2de87396894aff89b017d1d6/pbr-5.1.2-py2.py3-none-any.whl (107kB)
Collecting httptools>=0.0.10 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/a3/75/40cdb732e8ef547d9f34ceb83c43ea7188c0ffb719ddc6a1ad160464292d/httptools-0.0.11.tar.gz (99kB)
Collecting pytest==4.0.1 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/81/27/d4302e4e00497448081120f65029696070806bc8e649b83f644de006d710/pytest-4.0.1-py2.py3-none-any.whl (217kB)
Collecting pytest-asyncio==0.9.0 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/33/7f/2ed9f460872ebcc62d30afad167673ca10df36ff56a6f6df2f1d3671adc8/pytest_asyncio-0.9.0-py3-none-any.whl
Collecting iso8601==0.1.12 (from fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/ef/57/7162609dab394d38bbc7077b7ba0a6f10fb09d8b7701ea56fa1edc0c4345/iso8601-0.1.12-py2.py3-none-any.whl
Collecting six>=1.10.0 (from pytest==4.0.1->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/73/fb/00a976f728d0d1fecfe898238ce23f502a721c0ac0ecfedb80e0d88c64e9/six-1.12.0-py2.py3-none-any.whl
Collecting attrs>=17.4.0 (from pytest==4.0.1->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/3a/e1/5f9023cc983f1a628a8c2fd051ad19e76ff7b142a0faf329336f9a62a514/attrs-18.2.0-py2.py3-none-any.whl
Collecting py>=1.5.0 (from pytest==4.0.1->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/76/bc/394ad449851729244a97857ee14d7cba61ddb268dce3db538ba2f2ba1f0f/py-1.8.0-py2.py3-none-any.whl (83kB)
Collecting atomicwrites>=1.0 (from pytest==4.0.1->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/52/90/6155aa926f43f2b2a22b01be7241be3bfd1ceaf7d0b3267213e8127d41f4/atomicwrites-1.3.0-py2.py3-none-any.whl
Collecting setuptools (from pytest==4.0.1->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/d1/6a/4b2fcefd2ea0868810e92d519dacac1ddc64a2e53ba9e3422c3b62b378a6/setuptools-40.8.0-py2.py3-none-any.whl (575kB)
Collecting more-itertools>=4.0.0 (from pytest==4.0.1->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/ae/d4/d6bad4844831943dd667510947712750004525c5807711982f4ec390da2b/more_itertools-6.0.0-py3-none-any.whl (52kB)
Collecting pluggy>=0.7 (from pytest==4.0.1->fdk->-r requirements.txt (line 1))
  Downloading https://files.pythonhosted.org/packages/2d/60/f58d9e8197f911f9405bf7e02227b43a2acc2c2f1a8cbb1be5ecf6bfd0b8/pluggy-0.8.1-py2.py3-none-any.whl
Installing collected packages: pbr, httptools, six, attrs, py, atomicwrites, setuptools, more-itertools, pluggy, pytest, pytest-asyncio, iso8601, fdk
  Running setup.py install for httptools: started
    Running setup.py install for httptools: finished with status 'done'
Successfully installed atomicwrites-1.3.0 attrs-18.2.0 fdk-0.1.1 httptools-0.0.11 iso8601-0.1.12 more-itertools-6.0.0 pbr-5.1.2 pluggy-0.8.1 py-1.8.0 pytest-4.0.1 pytest-asyncio-0.9.0 setuptools-40.8.0 six-1.12.0
You are using pip version 18.1, however version 19.0.3 is available.
You should consider upgrading via the 'pip install --upgrade pip' command.
Removing intermediate container 451ecb930e43
 ---> 47ff7e511d91
Step 5/12 : ADD . /function/
 ---> e24a3c3ec737
Step 6/12 : RUN rm -fr /function/.pip_cache
 ---> Running in 389e75d27ea5
Removing intermediate container 389e75d27ea5
 ---> fe7635e860ca
Step 7/12 : FROM fnproject/python:3.7.1
3.7.1: Pulling from fnproject/python
a5a6f2f73cd8: Already exists
3a6fba040982: Already exists
738ebe0cf907: Already exists
a4b11c375c52: Already exists
02c57c00f1bc: Already exists
Digest: sha256:af0c785e711e34f8d0ba5a346e9a7900f6557d9cd96a0e7d0ea6e51adba6e797
Status: Downloaded newer image for fnproject/python:3.7.1
 ---> eda33421b45b
Step 8/12 : WORKDIR /function
 ---> Running in 03f4e9fe1b14
Removing intermediate container 03f4e9fe1b14
 ---> 02dd94b28d52
Step 9/12 : COPY --from=build-stage /function /function
 ---> 228e3e6138b0
Step 10/12 : COPY --from=build-stage /python /python
 ---> b00ecba090de
Step 11/12 : ENV PYTHONPATH=/python
 ---> Running in dc9df976381c
Removing intermediate container dc9df976381c
 ---> 2eef5e0cd896
Step 12/12 : ENTRYPOINT ["/python/bin/fdk", "/function/func.py", "handler"]
 ---> Running in 3c06bd61cdd7
Removing intermediate container 3c06bd61cdd7
 ---> e373c614b46f
Successfully built e373c614b46f
Successfully tagged fndemouser/pythonfn:0.0.2

Updating function pythonfn using image fndemouser/pythonfn:0.0.2...
Successfully created function: pythonfn with fndemouser/pythonfn:0.0.2
```

All the steps to load the current language Docker image are displayed.

Specifying `--app pythonapp` explicity puts the function in the application `pythonapp`.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message `Updating function pythonfn using image
fndemouser/pythonfn:0.0.2...` let's us know that the function is packaged in the
image `fndemouser/pythonfn:0.0.2`.

Note that the containing folder name `pythonfn` was used as the name of the
generated Docker container and used as the name of the function that container
was bound to.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.


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

```
NAME        ID
pythonapp   01D4BBS7BPNG8G00GZJ0000001
```

The fn list functions <app-name> command lists all the functions associated with and app.

![User Input Icon](images/userinput.png)
>```sh
> fn list functions pythonapp
>```

```sh
NAME      IMAGE                      ID
pythonfn  fndemouser/pythonfn:0.0.2  01DJRP8FT8NG8G00GZJ0000002
```

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
directly and independently.

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


### Getting a Function's Invoke Endpoint
In addition to using the Fn invoke command, we can call a function by using a URL. To do this, we must get the function's invoke endpoint. Use the command `fn inspect function <appname> <function-name>`. To list the nodefn function's invoke endpoint we can type:

![user input](images/userinput.png)
>```sh
> fn inspect function pythonapp pythonfn
>```

```js
{
	"annotations": {
		"fnproject.io/fn/invokeEndpoint": "http://localhost:8080/invoke/01DJRP8FT8NG8G00GZJ0000002"
	},
	"app_id": "01DJRP674QNG8G00GZJ0000001",
	"created_at": "2019-08-20T23:37:12.776Z",
	"id": "01DJRP8FT8NG8G00GZJ0000002",
	"idle_timeout": 30,
	"image": "fndemouser/pythonfn:0.0.2",
	"memory": 256,
	"name": "pythonfn",
	"timeout": 30,
	"updated_at": "2019-08-20T23:42:47.297Z"
}
```

The output confirms that `nodefn` functions invoke endpoint is:
`http://localhost:8080/invoke/01DJRP8FT8NG8G00GZJ0000002`. We can use this URL
to call the function.


### Invoke with Curl
Use `curl` to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -X "POST" -H "Content-Type: application/json" http://localhost:8080/invoke/01DJRP8FT8NG8G00GZJ0000002
>```

The result is once again the same.

```js
{"message":"Hello World"}
```

We can again pass JSON data to our function and get the value of name passed to
the function back.

![user input](images/userinput.png)
>```
> curl -X "POST" -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/invoke/01DJRP8FT8NG8G00GZJ0000002
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
