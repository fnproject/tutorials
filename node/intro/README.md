# Introduction to Fn with Node.js
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll walk through
developing a function using the JavaScript programming language and Node.js
(without installing any Node.js tools!) and deploying that function to a local
Fn server.  We'll also learn about the core Fn concepts like applications and
triggers.

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
> fn init --runtime node --trigger http nodefn
>```

The output will be

```yaml
Creating function at: /nodefn
Runtime: node
Function boilerplate generated.
func.yaml created.
```

The `fn init` command creates a simple function with a bit of boilerplate to get
you started. The `--runtime` option is used to indicate that the function we're
going to develop will be written in Node. A number of other runtimes are also
supported.  The `--trigger` option creates an HTTP trigger for the function
allowing you to invoke the function from a URL. Fn creates the simple function
along with several supporting files in the `/nodefn` directory.

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
format: http-stream
triggers:
- name: nodefn-trigger
  type: http
  source: /nodefn-trigger
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* schema_version--identifies the version of the schema for this function file. Essentially, it determines which fields are present in `func.yaml`.
* name--the name of the function. Matches the directory name.
* version--automatically starting at 0.0.1.
* runtime--the name of the runtime/language which was set based on the value set
in `--runtime`.
* entrypoint--the name of the executable to invoke when your function is called,
in this case `node func.js`.
* format--the function uses `http-stream` as its input/output transport ([see: Open Function Format](https://github.com/fnproject/docs/blob/master/fn/develop/function-format.md)).
* triggers--identifies the automatically generated trigger name and source. For example, this function would be requested from the URL `http://localhost:8080/t/appname/nodefn-trigger`. Where `appname` is the name of the app chosen for your function when it is deployed.

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
dev: Pulling from fnproject/node
88286f41530e: Pull complete 
f2f101846395: Pull complete 
ff57497de382: Pull complete 
Digest: sha256:613685c22f65d01f2264bdd49b8a336488e14faf29f3ff9b6bf76a4da23c4700
Status: Downloaded newer image for fnproject/node:dev
 ---> 016382f39a51
Step 2/9 : WORKDIR /function
 ---> Running in 17d09f41ee90
Removing intermediate container 17d09f41ee90
 ---> db4b1e998348
Step 3/9 : ADD package.json /function/
 ---> 5acbd4f3710d
Step 4/9 : RUN npm install
 ---> Running in 9c0da981c192
npm info it worked if it ends with ok
npm info using npm@5.3.0
npm info using node@v8.4.0
npm info lifecycle hellofn@1.0.0~preinstall: hellofn@1.0.0
npm http fetch GET 200 https://registry.npmjs.org/@fnproject%2ffdk 456ms
http fetch GET 200 https://registry.npmjs.org/@fnproject/fdk/-/fdk-0.0.11.tgz 206ms
npm info lifecycle @fnproject/fdk@0.0.11~preinstall: @fnproject/fdk@0.0.11
npm info linkStuff @fnproject/fdk@0.0.11
npm info lifecycle @fnproject/fdk@0.0.11~install: @fnproject/fdk@0.0.11
npm info lifecycle @fnproject/fdk@0.0.11~postinstall: @fnproject/fdk@0.0.11
npm info linkStuff hellofn@1.0.0
npm info lifecycle hellofn@1.0.0~install: hellofn@1.0.0
npm info lifecycle hellofn@1.0.0~postinstall: hellofn@1.0.0
npm info lifecycle hellofn@1.0.0~prepublish: hellofn@1.0.0
npm info lifecycle hellofn@1.0.0~prepare: hellofn@1.0.0
npm info lifecycle undefined~preshrinkwrap: undefined
npm info lifecycle undefined~shrinkwrap: undefined
npm notice created a lockfile as package-lock.json. You should commit this file.
npm info lifecycle undefined~postshrinkwrap: undefined
npm WARN hellofn@1.0.0 No repository field.

added 1 package in 1.486s
npm info ok 
Removing intermediate container 9c0da981c192
 ---> 5ea578bf9df9
Step 5/9 : FROM fnproject/node
latest: Pulling from fnproject/node
Digest: sha256:613685c22f65d01f2264bdd49b8a336488e14faf29f3ff9b6bf76a4da23c4700
Status: Downloaded newer image for fnproject/node:latest
 ---> 016382f39a51
Step 6/9 : WORKDIR /function
 ---> Using cache
 ---> db4b1e998348
Step 7/9 : ADD . /function/
 ---> 6f9312321676
Step 8/9 : COPY --from=build-stage /function/node_modules/ /function/node_modules/
 ---> 31399d7f619d
Step 9/9 : ENTRYPOINT ["node", "func.js"]
 ---> Running in 9a5e505e8bb2
Removing intermediate container 9a5e505e8bb2
 ---> 2f7b61423621
Successfully built 2f7b61423621
Successfully tagged fndemouser/nodefn:0.0.2

Updating function nodefn using image fndemouser/nodefn:0.0.2...
Successfully created app:  nodeapp
Successfully created function: nodefn with fndemouser/nodefn:0.0.2
Successfully created trigger: nodefn-trigger
```

All the steps to load the current language Docker image are displayed.

Functions are grouped into applications so by specifying `--app nodeapp`
we're implicitly creating the application "nodeapp" and associating our
function with it.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message
`Updating function nodefn using image fndemouser/nodefn:0.0.2...`
let's us know that the function is packaged in the image
"fndemouser/nodefn:0.0.2".

Note that the containing folder name 'nodefn' was used as the name of the
generated Docker container and used as the name of the function that
container was bound to. By convention it is also used to create the trigger name
`nodefn-trigger`.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.


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

### Understand fn deploy
If you have used Docker before the output of `fn --verbose deploy` should look
familiar--it looks like the output you see when running `docker build`
with a Dockerfile.  Of course this is exactly what's happening!  When
you deploy a function like this Fn is dynamically generating a Dockerfile
for your function, building a container, and then loading it for execution.

> __NOTE__: Fn is actually using two images.  The first contains the language compiler
and all the necessary build tools. The second image packages all dependencies
and any necessary language runtime components. Using this strategy, the final
function image size can be kept as small as possible.  Smaller Docker images are
naturally faster to push and pull from a repository which improves overall
performance.  For more details on this technique see [Multi-Stage Docker Builds
for Creating Tiny Go
Images](https://medium.com/travis-on-docker/multi-stage-docker-builds-for-creating-tiny-go-images-e0e1867efe5a).

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

```cs
NAME
nodeapp
```

We can also see the functions that are defined by an application.  Since
functions are exposed via triggers, the `fn list triggers <appname>` command
is used.  To list the functions included in "nodeapp" we can type:

![user input](images/userinput.png)
>```sh
> fn list triggers nodeapp
>```

```sh
FUNCTION    NAME             TYPE    SOURCE          ENDPOINT
nodefn      nodefn-trigger   http    /nodefn-trigger http://localhost:8080/t/nodeapp/nodefn-trigger
```

The output confirms that nodeapp contains a `nodefn` function that can be called via this URL.

### Invoke with Curl

The other way to invoke your function is via HTTP.  The Fn server exposes our
deployed function at `http://localhost:8080/t/nodeapp/nodefn-trigger`, a URL
that incorporates our application and function trigger as path elements.

Use curl to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -H "Content-Type: application/json" http://localhost:8080/t/nodeapp/nodefn-trigger
>```

The result is once again the same.

```js
{"message":"Hello World"}
```

We can again pass JSON data to our function get the value of name passed to the
function back.

![user input](images/userinput.png)
>```sh
> curl -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/t/nodeapp/nodefn-trigger
>```

The result is once again the same.

```js
{"message":"Hello Bob"}
```

## Wrap Up

Congratulations!  In this tutorial you've accomplished a lot.  You've created
your first function, deployed it to your local Fn server and invoked it over
HTTP.

**Go:** [Back to Contents](../README.md)
