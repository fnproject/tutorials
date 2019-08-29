# Introduction to Fn with Ruby
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll walk through
developing a function using the Ruby programming language (without installing
any Ruby tools!) and deploying that function to a local Fn server.  We'll also
learn about the core Fn concepts like applications and invoke endpoints.

### Before you Begin
* Set aside about 15 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../../install/README.md).
    - Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". [See here](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context).)

> As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Your First Function
Now that Fn server is up and running, let's start with a very simple "Hello
World!" function written in [Ruby](https://www.ruby-lang.org/). Don't worry, you
don't need to know Ruby! as Fn provides the necessary Ruby compiler and tools as
a Docker container. Let's walk through your first function to become familiar
with the process and how Fn supports development.


### Create your Function
In the terminal type the following.

![user input](images/userinput.png)
>```sh
> fn init --runtime ruby rubyfn
>```

The output will be

```yaml
Creating function at: /rubyfn
Function boilerplate generated.
func.yaml created.
```

The `fn init` command creates an simple function with a bit of boilerplate to
get you started. The `--runtime` option is used to indicate that the function
we're going to develop is written in Ruby. A number of other runtimes are
also supported.  Fn creates the simple function along with several supporting
files in the `/rubyfn` directory.

### Review your Function File
With your function created change into the `/rubyfn` directory.

![user input](images/userinput.png)
>```sh
> cd rubyfn
>```

Now get a list of the directory contents.

![user input](images/userinput.png)
>```sh
> ls
>```

```sh
 Gemfile func.rb func.yaml
```

The `func.rb` file which contains your actual Ruby function is generated along
with several supporting files. To view your Ruby function type:

![user input](images/userinput.png)
>```sh
> cat func.rb
>```

```ruby
require 'fdk'

def myfunction(context:, input:)
  input_value = input.respond_to?(:fetch) ? input.fetch('name') : input
  name = input_value.to_s.strip.empty? ? 'World' : input_value
  { message: "Hello #{name}!" }
end

FDK.handle(target: :myfunction)
```

This function looks for JSON input in the form of `{"name": "Bob"}`
If this JSON example is passed to the function, the function returns `{"message":"Hello Bob!"}`.
If no JSON data is found, or the `name` field is missing or empty,
the function returns `{"message":"Hello World!"}`.  

### Understand func.yaml
The `fn init` command generated a `func.yaml` function
configuration file. Let's look at the contents:

![user input](images/userinput.png)
>```sh
> cat func.yaml
>```

```yaml
schema_version: 20180708
name: rubyfn
version: 0.0.1
runtime: ruby
entrypoint: ruby func.rb
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* schema_version--identifies the version of the schema for this function file. Essentially, it determines which fields are present in `func.yaml`.
* name--the name of the function. Matches the directory name.
* version--automatically starting at 0.0.1.
* runtime--the name of the runtime/language which was set based on the value set
in `--runtime`.
* entrypoint--the name of the executable to invoke when your function is called,
in this case `ruby func.rb`.

There are other user specifiable properties but these will suffice for
this example.  Note that the name of your function is taken from the containing
folder name.  We'll see this come into play later on.

### Other Function Files
The `fn init` command generated one other file.

* `Gemfile` --  specifies all the dependencies for your Ruby function.


## Deploy Your First Function

With the `rubyfn` directory containing `func.rb` and `func.yaml` you've got
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
CURRENT	NAME	PROVIDER	API URL			        REGISTRY
*       default	default		http://localhost:8080	fndemouser
```

If your context is not configured, please see [the context installation instructions](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context) before proceeding. Your context determines where your function is deployed.

### Create an App
Next, functions are grouped together into an application. The application acts as the main organizing structure for multiple functions. To create an application type the following:

![user input](images/userinput.png)
>```sh
> fn create app rubyapp
>```

A confirmation is returned:

```yaml
Successfully created app:  rubyapp
```

Now `rubyapp` is ready for functions to be deployed to it.

### Deploy your Function to your App
Deploying your function is how you publish your function and make it accessible
to other users and systems. To see the details of what is happening during a
function deploy,  use the `--verbose` switch.  The first time you build a
function of a particular language it takes longer as Fn downloads the necessary
Docker images. The `--verbose` option allows you to see this process.

In your terminal type the following:

![user input](images/userinput.png)
>```sh
> fn --verbose deploy --app rubyapp --local
>```

You should see output similar to:

```yaml
Deploying rubyfn to app: rubyapp
Bumped to version 0.0.2
Building image fndemouser/rubyfn:0.0.2
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon   5.12kB
Step 1/9 : FROM fnproject/ruby:dev as build-stage
dev: Pulling from fnproject/ruby
8e402f1a9c57: Pull complete
130243e17afe: Pull complete
6c9c3bad0e4b: Pull complete
975485ddf10f: Pull complete
ff91390ffeaa: Pull complete
e131fd4f74d4: Pull complete
Digest: sha256:5ac41003591b935641de791945fdbf15bcb926cecf6ecdb1daf2158b5de7e6d5
Status: Downloaded newer image for fnproject/ruby:dev
 ---> 37ecabff3a24
Step 2/9 : WORKDIR /function
 ---> Running in d950c6c534e2
Removing intermediate container d950c6c534e2
 ---> 823a90c41257
Step 3/9 : ADD Gemfile* /function/
 ---> 316c7eab8ab6
Step 4/9 : RUN bundle install
 ---> Running in 1cd8c41d3863
Don't run Bundler as root. Bundler can ask for sudo if it is needed, and
installing your bundle as root will break this application for all non-root
users on this machine.
Fetching gem metadata from https://rubygems.org/..
Resolving dependencies...
Using bundler 2.0.1
Fetching json 2.2.0
Installing json 2.2.0 with native extensions
Fetching webrick 1.4.2
Installing webrick 1.4.2
Fetching fdk 0.0.20
Installing fdk 0.0.20
Bundle complete! 1 Gemfile dependency, 4 gems now installed.
Use `bundle info [gemname]` to see where a bundled gem is installed.
Removing intermediate container 1cd8c41d3863
 ---> 9bf1d44c3c3f
Step 5/9 : FROM fnproject/ruby
latest: Pulling from fnproject/ruby
8e402f1a9c57: Already exists
130243e17afe: Already exists
6c9c3bad0e4b: Already exists
975485ddf10f: Already exists
Digest: sha256:46e981c2e093a87b98c05f21d4a7701d5c53f5e1aed1be64eea8ab0a935c9ddb
Status: Downloaded newer image for fnproject/ruby:latest
 ---> f5452b336752
Step 6/9 : WORKDIR /function
 ---> Running in f1a2c9206a03
Removing intermediate container f1a2c9206a03
 ---> 88a1389a388e
Step 7/9 : COPY --from=build-stage /usr/lib/ruby/gems/ /usr/lib/ruby/gems/
 ---> 0cdd932b123a
Step 8/9 : ADD . /function/
 ---> 51f09786d6b2
Step 9/9 : ENTRYPOINT ["ruby", "func.rb"]
 ---> Running in 19e5cf8f4523
Removing intermediate container 19e5cf8f4523
 ---> 43d04792b080
Successfully built 43d04792b080
Successfully tagged fndemouser/rubyfn:0.0.2

Updating function rubyfn using image fndemouser/rubyfn:0.0.2...
Successfully created function: rubyfn with fndemouser/rubyfn:0.0.2
```

All the steps to load the current language Docker image are displayed.

Specifying `--app rubyapp` explicitly puts the function in the application `rubyapp`.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message
`Updating function rubyfn using image fndemouser/rubyfn:0.0.2...`
let's us know that the function packaged in the image
`fndemouser/rubyfn:0.0.2`.

Note that the containing folder name `rubyfn` was used as the name of the generated Docker container and used as the name of the function that container was bound to.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.

### Understand Fn deploy
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

When using `fn deploy --local`, Fn server builds and packages your function
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
fndemouser/rubyfn    0.0.2               d0d71f5a2a23        11 minutes ago      63.9MB
```

### Explore your Application
The Fn CLI provides a couple of commands to let us see what we've deployed.
`fn list apps` returns a list of all of the defined applications.

![User Input Icon](images/userinput.png)
>```sh
> fn list apps
>```

Which, in our case, returns the name of the application we created when we
deployed our `rubyfn` function:

```txt
NAME		ID
rubyapp		01DKA1TFRQNG8G00GZJ000000K
```

The `fn list functions <app-name>` command lists all the functions associated with and app.

![user input](images/userinput.png)
>```sh
> fn list functions rubyapp
>```

The returns all the functions associated with the `nodeapp`.

```txt
NAME    IMAGE                    ID
rubyfn  fndemouser/rubyfn:0.0.2  01DKA1TFRQNG8G00GZJ000000K
```


## Invoke your Deployed Function
There are two ways to call your deployed function.

### Invoke with the CLI
The first is using the Fn CLI which makes invoking your function relatively
easy.  Type the following:

![user input](images/userinput.png)
>```sh
> fn invoke rubyapp rubyfn
>```

which results in:

```js
{"message":"Hello World!"}
```

When you invoked `rubyapp rubyfn` the Fn server looked up the `rubyapp`
application and then looked for the Docker container image bound to the
`rubyfn` function and executed the code.

You can also pass data to the invoke command. For example:

![user input](images/userinput.png)
>```sh
> echo -n '{"name":"Bob"}' | fn invoke rubyapp rubyfn --content-type application/json
>```

```js
{"message":"Hello Bob!"}
```

The JSON data was parsed and since `name` was set to `Bob`, that value is passed
in the output.

### Getting a Function's Invoke Endpoint

In addition to using the Fn `invoke` command, we can call a function by using a
URL. To do this, we must get the function's invoke endpoint. Use the command
`fn inspect function <appname> <function-name>`.  To list the `nodefn` function's
invoke endpoint we can type:

![user input](images/userinput.png)
>```sh
> fn inspect function rubyapp rubyfn
>```

```js
{
	"annotations": {
		"fnproject.io/fn/invokeEndpoint": "http://localhost:8080/invoke/01DKA1TFRQNG8G00GZJ000000K"
	},
	"app_id": "01DKA1S0N3NG8G00GZJ000000J",
	"created_at": "2019-08-27T17:26:22.231Z",
	"id": "01DKA1TFRQNG8G00GZJ000000K",
	"idle_timeout": 30,
	"image": "fndemouser/rubyfn:0.0.2",
	"memory": 128,
	"name": "rubyfn",
	"timeout": 30,
	"updated_at": "2019-08-27T17:26:22.231Z"
}
```

The output confirms that `rubyfn` functions invoke endpoint is:
`http://localhost:8080/invoke/01DKA1TFRQNG8G00GZJ000000K`. We can use this URL
to call the function.

### Invoke with Curl

Once we have the invoke endpoint, the second method for invoking our function
can be used, HTTP.  The Fn server exposes our deployed function at
`http://localhost:8080/invoke/01DKA1TFRQNG8G00GZJ000000K`.

Use `curl` to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -X "POST" -H "Content-Type: application/json" http://localhost:8080/invoke/01DKA1TFRQNG8G00GZJ000000K
>```

The result is once again the same.

```js
{"message":"Hello World!"}
```

We can again pass JSON data to our function and get the value of name passed to
the function back.

![user input](images/userinput.png)
>```sh
> curl -X "POST" -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/invoke/01DKA1TFRQNG8G00GZJ000000K
>```

The result is once again the same.

```js
{"message":"Hello Bob!"}
```

## Wrap Up

Congratulations! In this tutorial you've accomplished a lot. You've created your
first function, deployed it to your local Fn server and invoked it
over HTTP.

**Go:** [Back to Contents](../../README.md)
