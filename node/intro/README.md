# Introduction to Fn with Node.js
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll walk through
developing a function using the JavaScript programming language and Node.js
(without installing any Node.js tools!) and deploying that function to a local
Fn server.  We'll also learn about the core Fn concepts like applications and
invoke endpoints.

### Before you Begin
* Set aside about 15 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](https://github.com/fnproject/tutorials/blob/master/install/README.md).
    * Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". [See here](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context).)

> As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Your First Function
Now that Fn server is up and running, let's start with a very simple "hello
world" function written in [Node.js JavaScript](https://nodejs.org/). Don't
worry, you don't need to know Node!  In fact you don't even need to have Node
installed on your development machine as Fn provides the necessary Node tools as
a Docker container.  Let's walk through your first function to become familiar
with the process and how Fn supports development.


### Create your Function
In the terminal type the following.

![user input](images/userinput.png)
>```
> fn init --runtime node nodefn
>```

The output will be

```yaml
Creating function at: /nodefn
Function boilerplate generated.
func.yaml created.
```

The `fn init` command creates a simple function with a bit of boilerplate to get
you started. The `--runtime` option is used to indicate that the function we're
going to develop will be written in Node. A number of other runtimes are also
supported.  Fn creates the simple function along with several supporting files
in the `/nodefn` directory.

### Review your Function File

With your function created change into the `/nodefn` directory.

![user input](images/userinput.png)
>```
> cd nodefn
>```

Now get a list of the directory contents.

![user input](images/userinput.png)
>```
> ls
>```

```sh
func.js func.yaml package.json
```

The `func.js` file which contains your actual Node function is generated along
with several supporting files. To view your Node function type:

![user input](images/userinput.png)
>```sh
> cat func.js
>```

```js
const fdk=require('@fnproject/fdk');

fdk.handle(function(input){
  let name = 'World';
  if (input.name) {
    name = input.name;
  }
  return {'message': 'Hello ' + name}
})
```

This function looks for JSON input in the form of `{"name": "Bob"}`. If this
JSON example is passed to the function, the function returns `{"message":"Hello
Bob"}`. If no JSON data is found, the function returns `{"message":"Hello
World"}`.

### Understand func.yaml
The `fn init` command generated a `func.yaml` function
configuration file. Let's look at the contents:

![user input](images/userinput.png)
>```sh
> cat func.yaml
>```

```yaml
schema_version: 20180708
name: nodefn
version: 0.0.1
runtime: node
entrypoint: node func.js
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* schema_version--identifies the version of the schema for this function file. Essentially, it determines which fields are present in `func.yaml`.
* name--the name of the function. Matches the directory name.
* version--automatically starting at 0.0.1.
* runtime--the name of the runtime/language which was set based on the value set
in `--runtime`.
* entrypoint--the name of the Docker execution command to invoke when your function is called,
in this case `node func.js`.

There are other user specifiable properties but these will suffice for
this example.  Note that the name of your function is taken from the containing
folder name.  We'll see this come into play later on.

### Other Function Files
The `fn init` command generated one other file.

* `package.json` --  specifies all the Node.js dependencies for your Node function.

### Fn and Node.js Dependencies
Fn handles Node.js dependencies in the following way:

* If a `package.json` is present without a `node_modules` directory, an Fn build runs an `npm install` within the build process and installs your dependencies.
* If the `node_modules` is present, Fn assumes you have provided the dependencies yourself and no installation is performed.

## Deploy Your First Function

With the `nodefn` directory containing `func.js` and `func.yaml` you've got
everything you need to deploy the function to Fn server. This server could be
running in the cloud, in your datacenter, or on your local machine like we're
doing here.

### Check your Context
Make sure your context is set to default and you are using a demo user. Use the `fn list contexts` command to check.

![user input](images/userinput.png)
>```sh
> fn list contexts
>```

```cs
CURRENT	NAME	PROVIDER	API URL			        REGISTRY
*       default	default		http://localhost:8080	fndemouser
```

If your context is not configured, please see [the context installation instructions](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context) before proceeding. Your context determines the server your function is deployed to.

### Create an App
Next, functions are grouped together into an application. The application acts as the main organizing structure for multiple functions. To create an application type the following:

![user input](images/userinput.png)
>```sh
> fn create app nodeapp
>``` 

A confirmation is returned:

```yaml
Successfully created app:  nodeapp
```

Now `nodeapp` is ready for functions to be deployed to it.

### Deploy your Function to your App
Deploying your function is how you publish your function and make it accessible
to other users and systems. To see the details of what is happening during a
function deploy,  use the `--verbose` switch.  The first time you build a
function of a particular language it takes longer as Fn downloads the necessary
Docker images. The `--verbose` option allows you to see this process.

In your terminal type the following:

![user input](images/userinput.png)
>```sh
> fn --verbose deploy --app nodeapp --local
>```

You should see output similar to:

```yaml
Deploying nodefn to app: nodeapp
Bumped to version 0.0.2
Building image fndemouser/nodefn:0.0.2 
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon   5.12kB
Step 1/9 : FROM fnproject/node:dev as build-stage
 ---> b557a05fec78
Step 2/9 : WORKDIR /function
 ---> Using cache
 ---> ea3ae80dbec2
Step 3/9 : ADD package.json /function/
 ---> Using cache
 ---> e7d319e022d2
Step 4/9 : RUN npm install
 ---> Using cache
 ---> bf5b6313c055
Step 5/9 : FROM fnproject/node
 ---> c8da69259495
Step 6/9 : WORKDIR /function
 ---> Using cache
 ---> 1fe19c6ca66c
Step 7/9 : ADD . /function/
 ---> 229045a70217
Step 8/9 : COPY --from=build-stage /function/node_modules/ /function/node_modules/
 ---> 59b17163fe37
Step 9/9 : ENTRYPOINT ["node", "func.js"]
 ---> Running in b0d793dd39bb
Removing intermediate container b0d793dd39bb
 ---> 7160364dec30
Successfully built 7160364dec30
Successfully tagged fndemouser/nodefn:0.0.2

Updating function nodefn using image fndemouser/nodefn:0.0.2...
Successfully created function: nodefn with fndemouser/nodefn:0.0.2
```

All the steps to load the current language Docker image are displayed.

Specifying `--app nodeapp` explicitly puts the function in the application "nodeapp".

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message
`Updating function nodefn using image fndemouser/nodefn:0.0.2...`
let's us know that the function is packaged in the image
"fndemouser/nodefn:0.0.2".

Note that the containing folder name 'nodefn' was used as the name of the
generated Docker container and used as the name of the function that
container was bound to.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.

### Understand fn deploy

If you have used Docker before the output of `fn --verbose deploy` should look
familiar--it looks like the output you see when running `docker build`
with a Dockerfile.  Of course this is exactly what's happening!  When
you deploy a function like this Fn is dynamically generating a Dockerfile
for your function, building a container, and then loading it for execution.

> __NOTE__: Fn is actually using two images.  The first contains the language
> compiler and all the necessary build tools. The second image packages all
> dependencies and any necessary language runtime components. Using this
> strategy, the final function image size can be kept as small as possible.
> Smaller Docker images are naturally faster to push and pull from a repository
> which improves overall performance.  For more details on this technique see
> [Multi-Stage Docker Builds for Creating Tiny Go
> Images](https://medium.com/travis-on-docker/multi-stage-docker-builds-for-creating-tiny-go-images-e0e1867efe5a).

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
fndemouser/nodefn    0.0.2               b9330bddec26        2 minutes ago      66.4MB
```

### Explore your Application

The fn CLI provides a couple of commands to let us see what we've deployed.
`fn list apps` returns a list of all of the defined applications.

![user input](images/userinput.png)
>```sh
> fn list apps
>```

Which, in our case, returns the name of the application we created when we
deployed our `nodefn` function:

```
NAME     ID
nodeapp  01DHHSP6X3NG8G00GZJ0000001
```

The `fn list functions <app-name>` command lists all the functions associated with and app.

![user input](images/userinput.png)
>```sh
> fn list functions nodeapp
>```

The returns all the functions associated with the `nodeapp`.

```
NAME	IMAGE                    ID
nodefn	fndemouser/nodefn:0.0.2	 01DJ900WFKNG8G00GZJ0000002	
```

## Invoke your Deployed Function

There are two ways to call your deployed function.

### Invoke with the CLI

The first is using the `fn` CLI which makes invoking your function relatively
easy.  Type the following:

![user input](images/userinput.png)
>```sh
> fn invoke nodeapp nodefn
>```

which results in:

```js
{"message":"Hello World"}
```

When you invoked "nodeapp nodefn" the fn server looked up the
"nodeapp" application and then looked for the Docker container image
bound to the "nodefn" function and executed the code.

You can also pass data to the run command. Note that you set the content type for the data passed. For example:

![user input](images/userinput.png)
>```sh
> echo -n '{"name":"Bob"}' | fn invoke nodeapp nodefn --content-type application/json
>```

```js
{"message":"Hello Bob"}
```

The JSON data was parsed and since `name` was set to "Bob", that value is passed
in the output.


### Getting a Function's Invoke Endpoint

In addition to using the Fn `invoke` command, we can call a function by using a
URL. To do this, we must get the function's invoke endpoint. Use the command
`fn inspect function <appname> <function-name>`.  To list the `nodefn` function's
invoke endpoint we can type:

![user input](images/userinput.png)
>```sh
> fn inspect function nodeapp nodefn
>```

```js
{
	"annotations": {
		"fnproject.io/fn/invokeEndpoint": "http://localhost:8080/invoke/01DJ900WFKNG8G00GZJ0000002"
	},
	"app_id": "01DJ90077CNG8G00GZJ0000001",
	"created_at": "2019-08-14T21:19:58.451Z",
	"id": "01DJ900WFKNG8G00GZJ0000002",
	"idle_timeout": 30,
	"image": "fndemouser/nodefn:0.0.3",
	"memory": 128,
	"name": "nodefn",
	"timeout": 30,
	"updated_at": "2019-08-14T21:21:09.868Z"
}
```

The output confirms that `nodefn` functions invoke endpoint is:
`http://localhost:8080/invoke/01DJ900WFKNG8G00GZJ0000002`. We can use this URL
to call the function.

### Invoke with Curl

Once we have the invoke endpoint, the second method for invoking our function
can be used, HTTP.  The Fn server exposes our deployed function at
`http://localhost:8080/invoke/01DJ900WFKNG8G00GZJ0000002`.

Use curl to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -X "POST" -H "Content-Type: application/json" http://localhost:8080/invoke/01DJ900WFKNG8G00GZJ0000002
>```

The result is once again the same.

```js
{"message":"Hello World"}
```

We can again pass JSON data to our function get the value of name passed to the
function back.

![user input](images/userinput.png)
>```sh
> curl -X "POST" -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/invoke/01DJ900WFKNG8G00GZJ0000002
>```

The result is once again the same.

```js
{"message":"Hello Bob"}
```

## Wrap Up

Congratulations!  In this tutorial you've accomplished a lot.  You've created
your first function, deployed it to your local Fn server and invoked it over
HTTP.

**Go:** [Back to Contents](../../README.md)
