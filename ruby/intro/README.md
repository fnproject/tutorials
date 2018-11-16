# Introduction to Fn with Ruby
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll walk through
developing a function using the Ruby programming language (without installing
any Ruby tools!) and deploying that function to a local Fn server.  We'll also
learn about the core Fn concepts like applications and triggers.

### Before you Begin
* Set aside about 15 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../../install/README.md).
    - Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". [See here](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context).)

> As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Your First Function
Now that Fn server is up and running, let's start with a very simple "hello
world" function written in [Ruby](https://www.ruby-lang.org/). Don't worry, you
don't need to know Ruby! as Fn provides the necessary Ruby compiler and tools as
a Docker container. Let's walk through your first function to become familiar
with the process and how Fn supports development.


### Create your Function
In the terminal type the following.

![user input](images/userinput.png)
>```sh
> fn init --runtime ruby --trigger http rubyfn
>```

The output will be

```yaml
Creating function at: /rubyfn
Runtime: ruby
Function boilerplate generated.
func.yaml created.
```

The `fn init` command creates an simple function with a bit of boilerplate to
get you started. The `--runtime` option is used to indicate that the function
we're going to develop is written in Ruby. A number of other runtimes are
also supported.  The `--trigger` option creates a URL for HTTP access to the function.  Fn creates the simple function along with several supporting
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

def myhandler(context, input)
	STDERR.puts "call_id: " + context.call_id
	name = "World"
	if input != nil
		if context.content_type == "application/json"
			nin = input['name']
			if nin && nin != ""
				name = nin
			end
		elsif context.content_type == "text/plain"
			name = input
		else
			raise "Invalid input, expecting JSON!"
		end
	end
	return {message: "Hello " + name.to_s + "!"}
end

FDK.handle(:myhandler)
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
name: rubyfn
version: 0.0.2
runtime: ruby
entrypoint: ruby func.rb
triggers:
- name: rubyfn-trigger
  type: http
  source: /rubyfn-trigger
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
* triggers--identifies the automatically generated trigger name and source. For
example, this function would be executed from the URL
<http://localhost:8080/t/appname/rubyfn-trigger>. Where appname is the name of
the app chosen for your function when it is deployed.

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
> fn --verbose deploy --app rubyapp --local
>```

You should see output similar to:

```yaml
Creating function at: /rubyfn
Function boilerplate generated.
func.yaml created.
~/lcl/1016 $ cd rubyfn
~/lcl/1016/rubyfn $ ls
Gemfile   func.rb   func.yaml
~/lcl/1016/rubyfn $ 
~/lcl/1016/rubyfn $ 
~/lcl/1016/rubyfn $ 
~/lcl/1016/rubyfn $ fn --verbose deploy --app rubyapp --local
Deploying rubyfn to app: rubyapp
Bumped to version 0.0.2
Building image fndemouser/rubyfn:0.0.2 
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon   5.12kB
Step 1/9 : FROM fnproject/ruby:dev as build-stage
dev: Pulling from fnproject/ruby
88286f41530e: Pull complete 
ee67ab14fe7c: Pull complete 
528d6e469a02: Pull complete 
bb1a5f0c7734: Pull complete 
b6f7bd536b78: Pull complete 
Digest: sha256:056069fae8e349187114df899f1744aee548b464a91fb38497de6ce30d8c1488
Status: Downloaded newer image for fnproject/ruby:dev
 ---> 907fbac5f177
Step 2/9 : WORKDIR /function
 ---> Running in 95c3e5e57b50
Removing intermediate container 95c3e5e57b50
 ---> e8b73f0c8416
Step 3/9 : ADD Gemfile* /function/
 ---> 713cc4c0d750
Step 4/9 : RUN bundle install
 ---> Running in 8ee8c95ebff4
Don't run Bundler as root. Bundler can ask for sudo if it is needed, and
installing your bundle as root will break this application for all non-root
users on this machine.
Fetching gem metadata from https://rubygems.org/..
Fetching version metadata from https://rubygems.org/.
Resolving dependencies...
Using bundler 1.15.4
Fetching json 2.1.0
Installing json 2.1.0 with native extensions
Fetching fdk 0.0.14
Installing fdk 0.0.14
Bundle complete! 1 Gemfile dependency, 3 gems now installed.
Use `bundle info [gemname]` to see where a bundled gem is installed.
Removing intermediate container 8ee8c95ebff4
 ---> 172f64d0553a
Step 5/9 : FROM fnproject/ruby
latest: Pulling from fnproject/ruby
88286f41530e: Already exists 
ee67ab14fe7c: Already exists 
528d6e469a02: Already exists 
Digest: sha256:9390a5c8df48f10499930a2ed4968bd2e46b6e6041aea8f5a5dc589b7b2ecaa2
Status: Downloaded newer image for fnproject/ruby:latest
 ---> 9ab2c72e7fd0
Step 6/9 : WORKDIR /function
 ---> Running in 98c77e59831c
Removing intermediate container 98c77e59831c
 ---> 04f0c7220634
Step 7/9 : COPY --from=build-stage /usr/lib/ruby/gems/ /usr/lib/ruby/gems/
 ---> aa358bb28a7b
Step 8/9 : ADD . /function/
 ---> 6d31c787c51c
Step 9/9 : ENTRYPOINT ["ruby", "func.rb"]
 ---> Running in 1258d901657e
Removing intermediate container 1258d901657e
 ---> f9bd02b56f9b
Successfully built f9bd02b56f9b
Successfully tagged fndemouser/rubyfn:0.0.2

Updating function rubyfn using image fndemouser/rubyfn:0.0.2...
Successfully created app:  rubyapp
Successfully created function: rubyfn with fndemouser/rubyfn:0.0.2
Successfully created trigger: rubyfn-trigger
Trigger Endpoint: http://localhost:8080/t/rubyapp/rubyfn-trigger
```

All the steps to load the current language Docker image are displayed.

Functions are grouped into applications so by specifying `--app rubyapp`
we're implicitly creating the application `rubyapp` and associating our
function with it.

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message
`Updating function rubyfn using image fndemouser/rubyfn:0.0.2...`
let's us know that the function packaged in the image
`fndemouser/rubyfn:0.0.2`.

Note that the containing folder name `rubyfn` was used as the name of the generated Docker container and used as the name of the function that container was bound to. By convention it is also used to create the trigger name `rubyfn-trigger`.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.

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
`rubyfn` function and executed the code. Fn `invoke` invokes your function
directly and independently of any associated triggers.  You can always invoke a
function even without it having any triggers bound to it. Note the content type was specified as well.

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

```sh
NAME		ID
rubyapp		01CT433P8VNG8G00GZJ000001A
```

We can also see the functions that are defined by an application. Since functions are exposed via triggers, the `fn list triggers <appname>` command is used. To list the functions included in "rubyapp" we can type:

![User Input Icon](images/userinput.png)
>```sh
> fn list triggers rubyapp
>```

```sh
FUNCTION    NAME            TYPE  SOURCE          ENDPOINT
rubyfn      rubyfn-trigger  http  /rubyfn-trigger http://localhost:8080/t/rubyapp/rubyfn-trigger

FUNCTION    NAME               ID                           TYPE    SOURCE          ENDPOINT
rubyfn      rubyfn-trigger     01CT433P9TNG8G00GZJ000001C   http    /rubyfn-trigger http://localhost:8080/t/rubyapp/
```

The output confirms that `rubyapp` contains a `rubyfn` function that is
implemented by the Docker container `fndemouser/rubyfn:0.0.2` which may be
invoked via the specified URL.

### Invoke with Curl
The other way to invoke your function is via HTTP.  The Fn server exposes our
deployed function at `http://localhost:8080/t/rubyapp/rubyfn-trigger`, a URL
that incorporates our application and function trigger as path elements.

Use `curl` to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -H "Content-Type: application/json" http://localhost:8080/t/rubyapp/rubyfn-trigger
>```

The result is once again the same.

```js
{"message":"Hello World!"}
```

We can again pass JSON data to our function and get the value of name passed to
the function back.

![user input](images/userinput.png)
>```
> curl -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/t/rubyapp/rubyfn-trigger
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
