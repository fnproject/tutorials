# Introduction to Fn with C# on .NET Core

*Note: This is a community-contributed and maintained tutorial*

Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll walk through
developing a function using the C# programming language using .NET Core (without
installing any .NET tools!), and deploying that function to a local Fn server.
We'll also learn about the core Fn concepts like applications and triggers.

### Before you Begin
* Set aside about 15 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](https://github.com/fnproject/tutorials/blob/master/install/README.md).
    * Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". [See here](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context).)

> As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Your First Function
Now that Fn server is up and running, let's start with a very simple "hello
world" function written in [C#](https://docs.microsoft.com/en-us/dotnet/csharp/).
Don't worry, you don't need to know C#! In fact you don't even need to have .NET
Core installed on your development machine as Fn provides the necessary tools as
a Docker container. Let's walk through your first function to become familiar
with the process and how Fn supports development.


### Create your Function
In the terminal type the following.

![user input](images/userinput.png)

>```
> fn init --init-image daniel15/fn-dotnet-init --trigger http csharpfn
>```

The output will be

```yaml
Creating function at: /csharpfn
Building from init-image: daniel15/fn-dotnet-init
func.yaml created.
```

The `fn init` command creates a simple function with a bit of boilerplate to get
you started. The `--init-image` option is used to indicate the [init
image](https://github.com/fnproject/docs/blob/master/cli/how-to/create-init-image.md)
to use to create your function. Fn creates the simple function along with
several supporting files in the `/csharpfn` directory.

### Review your Function File

With your function created change into the `/csharpfn` directory.

![user input](images/userinput.png)

>```
> cd csharpfn
>```

Now get a list of the directory contents.

![user input](images/userinput.png)

>```
> ls
>```

```sh
csharpfn.csproj  Dockerfile  func.yaml  Program.cs
```

The `Program.cs` file which contains your actual C# function is generated along
with several supporting files. To view your C# function type:

![user input](images/userinput.png)

> ```sh
> cat Program.cs
> ```

```csharp
using System.Threading;
using System.Threading.Tasks;
using FnProject.Fdk;

namespace csharpfn
{
    public class Function
    {
        public Task<string> InvokeAsync(string input, CancellationToken timedOut)
        {
            if (string.IsNullOrWhiteSpace(input))
            {
                input = "world";
            }
            return Task.FromResult("Hello " + input);
        }

        public static void Main()
        {
            FdkHandler.Handle<Function>();
        }
    }
}
```

This function takes a string as input. If a string "Bob" is passed to the function, it returns `Hello Bob`. If no string is found, the function returns `Hello world`.

### Understand func.yaml
The `fn init` command generated a `func.yaml` function
configuration file. Let's look at the contents:

![user input](images/userinput.png)

>```sh
> cat func.yaml
>```

```yaml
schema_version: 20180708
name: csharpfn
version: 0.0.1
runtime: docker
triggers:
- name: csharpfn-trigger
  type: http
  source: /csharpfn-trigger
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* schema_version--identifies the version of the schema for this function file. Essentially, it determines which fields are present in `func.yaml`.
* name--the name of the function. Matches the directory name.
* version--automatically starting at 0.0.1.
* runtime--the name of the runtime/language used for the function. For community-supported languages that use init images, this will be `docker`

There are other user specifiable properties but these will suffice for
this example.  Note that the name of your function is taken from the containing
folder name.  We'll see this come into play later on.

### Other Function Files

The `fn init` command generated two other files:

* `Dockerfile` -- contains the comments to build the Docker image for the function
* `csharpfn.csproj` -- MSBuild project file for the function

## Deploy Your First Function

With the `csharpfn` directory containing these files you've got everything you
need to deploy the function to Fn server. This server could be running in the
cloud, in your datacenter, or on your local machine like we're doing here.

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
> fn create app csharpapp
>``` 

A confirmation is returned:

```yaml
Successfully created app: csharpapp
```

Now `csharpapp` is ready for functions to be deployed to it.

### Deploy your Function to your App
Deploying your function is how you publish your function and make it accessible
to other users and systems. To see the details of what is happening during a
function deploy,  use the `--verbose` switch.  The first time you build a
function of a particular language it takes longer as Fn downloads the necessary
Docker images. The `--verbose` option allows you to see this process.

In your terminal type the following:

![user input](images/userinput.png)

>```sh
> fn --verbose deploy --app csharpapp --local
>```

You should see output similar to:

```yaml
Deploying csharpfn to app: csharpapp
Bumped to version 0.0.2
Building image csharpfn:0.0.2
FN_REGISTRY:  FN_REGISTRY is not set.
Current Context:  No context currently in use.
Sending build context to Docker daemon   5.12kB
Step 1/14 : FROM microsoft/dotnet:2.2-aspnetcore-runtime AS base
 ---> 5f58a78e0e06
Step 2/14 : WORKDIR /app
 ---> Using cache
 ---> 71475dd699de
Step 3/14 : FROM microsoft/dotnet:2.2-sdk AS build
 ---> c628833b61f9
Step 4/14 : WORKDIR /src
 ---> Using cache
 ---> 264c89dba295
Step 5/14 : COPY ["csharpfn.csproj", "."]
 ---> da86e416df79
Step 6/14 : RUN dotnet restore "csharpfn.csproj"
 ---> Running in 716c92907e94
  Restoring packages for /src/csharpfn.csproj...
  Installing Mono.Posix.NETStandard 1.0.0.
  Installing Newtonsoft.Json 12.0.1.
  Installing FnProject.Fdk 1.0.0.
  Generating MSBuild file /src/obj/csharpfn.csproj.nuget.g.props.
  Generating MSBuild file /src/obj/csharpfn.csproj.nuget.g.targets.
  Restore completed in 4.27 sec for /src/csharpfn.csproj.
Removing intermediate container 716c92907e94
 ---> ecd3dd6c2973
Step 7/14 : COPY . .
 ---> 50ef842f2cd8
Step 8/14 : RUN dotnet build "csharpfn.csproj" -c Release -o /app
 ---> Running in 4af0566396fa
Microsoft (R) Build Engine version 15.9.20+g88f5fadfbe for .NET Core
Copyright (C) Microsoft Corporation. All rights reserved.

  Restore completed in 80.01 ms for /src/csharpfn.csproj.
  csharpfn -> /app/csharpfn.dll

Build succeeded.
    0 Warning(s)
    0 Error(s)

Time Elapsed 00:00:05.31
Removing intermediate container 4af0566396fa
 ---> 3a3db58f69c0
Step 9/14 : FROM build AS publish
 ---> 3a3db58f69c0
Step 10/14 : RUN dotnet publish "csharpfn.csproj" -c Release -o /app
 ---> Running in d5b151492cad
Microsoft (R) Build Engine version 15.9.20+g88f5fadfbe for .NET Core
Copyright (C) Microsoft Corporation. All rights reserved.

  Restore completed in 66.36 ms for /src/csharpfn.csproj.
  csharpfn -> /src/bin/Release/netcoreapp2.2/csharpfn.dll
  csharpfn -> /app/
Removing intermediate container d5b151492cad
 ---> a33fcca9dbb0
Step 11/14 : FROM base AS final
 ---> 71475dd699de
Step 12/14 : WORKDIR /app
 ---> Using cache
 ---> 9e451a71efa2
Step 13/14 : COPY --from=publish /app .
 ---> 5cec583a667d
Step 14/14 : ENTRYPOINT ["dotnet", "csharpfn.dll"]
 ---> Running in 70aee412b6e2
Removing intermediate container 70aee412b6e2
 ---> 5e3a26082ac1
Successfully built 5e3a26082ac1
Successfully tagged fndemouser/csharpfn:0.0.2

Updating function csharpfn using image csharpfn:0.0.2...
Successfully created function: csharpfn with fndemouser/csharpfn:0.0.2
Trigger Endpoint: http://localhost:8080/t/csharpapp/csharpfn-trigger
```

All the steps to load the current language Docker image are displayed.

Specifying `--app csharpapp` explicitly puts the function in the application "csharpapp".

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message
`Updating function csharpfn using image fndemouser/csharpfn:0.0.2...`
lets us know that the function is packaged in the image
"fndemouser/csharpfn:0.0.2".

Note that the containing folder 'csharpfn' was used as the name of the
generated Docker container and used as the name of the function that the
container was bound to. By convention it is also used to create the trigger name
`csharpfn-trigger`.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.


## Invoke your Deployed Function

There are two ways to call your deployed function.

### Invoke with the CLI

The first is using the `fn` CLI which makes invoking your function relatively
easy.  Type the following:

![user input](images/userinput.png)
>```sh
> fn invoke csharpapp csharpfn
>```

which results in:

```js
Hello world
```

When you invoked "csharpapp csharpfn" the fn server looked up the
"csharpapp" application and then looked for the Docker container image
bound to the "csharpfn" function and executed the code.

You can also pass data to the run command. Note that you set the content type for the data passed. For example:

![user input](images/userinput.png)

> ```sh
> echo Bob | fn invoke csharpapp csharpfn
> ```

```js
Hello Bob
```

Since the input string was set to "Bob", that value is passed in the output.

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
fndemouser/csharpfn    0.0.2               cf7501ceb018        2 minutes ago      276MB
```

### Explore your Application

The fn CLI provides a couple of commands to let us see what we've deployed.
`fn list apps` returns a list of all of the defined applications.

![user input](images/userinput.png)
>```sh
> fn list apps
>```

Which, in our case, returns the name of the application we created when we
deployed our `csharpfn` function:

```cs
NAME            ID
csharpapp       01D7KC6QGENG8G00GZJ0000001
```

We can also see the functions that are defined by an application.  Since
functions are exposed via triggers, the `fn list triggers <appname>` command
is used.  To list the functions included in "csharpapp" we can type:

![user input](images/userinput.png)
>```sh
> fn list triggers csharpapp
>```

```sh
FUNCTION        NAME                    ID                              TYPE    SOURCE                  ENDPOINT
csharpfn        csharpfn-trigger        01D7KCT0MHNG8G00GZJ000000A      http    /csharpfn-trigger       http://localhost:8080/t/csharpapp/csharpfn-trigger
```

The output confirms that csharpapp contains a `csharpfn` function that can be called via this URL.

### Invoke with cURL

The other way to invoke your function is via HTTP.  The Fn server exposes our
deployed function at `http://localhost:8080/t/csharpapp/csharpfn-trigger`, a URL
that incorporates our application and function trigger as path elements.

Use curl to invoke the function:

![user input](images/userinput.png)

> ```sh
> curl http://localhost:8080/t/csharpapp/csharpfn-trigger
> ```

The result is once again the same.

```
Hello world
```

We can again pass a string to our function get the value of name passed to the
function back.

![user input](images/userinput.png)

> ```sh
> curl -d 'Bob' http://localhost:8080/t/csharpapp/csharpfn-trigger
> ```

The result is once again the same.

```
Hello Bob
```

## JSON

Frequently, you will want to pass JSON to your function, and return JSON in your response. We can do this with a few changes to our `Program.cs` file:

![user input](images/userinput.png)

```csharp
using System.Threading;
using System.Threading.Tasks;
using FnProject.Fdk;

namespace csharpfn
{
    public class MyInput
    {
        public string Name { get; set; }
    }

    public class Function
    {
        public async Task<object> InvokeAsync(MyInput input, CancellationToken timedOut)
        {
            return new {
                Message = "Hello " + input.Name
            };
        }

        public static void Main()
        {
            FdkHandler.Handle<Function>();
        }
    }
}
```

Notice that in order to accept JSON as input, we simply changed the type of `input` from a string to a custom class. Similarly, to return JSON, we simply return a POCO (Plain Old C# Object). This can be either an anonymous type (like we did here), or a normal class. The framework handles automatically serializing the object.

Now if we redeploy and invoke the function, it'll take JSON as input, and return JSON as output:

![user input](images/userinput.png)

> ```shell
> fn --verbose deploy --app csharpapp --local
> echo '{"Name":"Daniel"}' | fn invoke csharpapp csharpfn
> ```

```json
{"Message": "Hello Daniel"}
```

## Wrap Up

Congratulations!  In this tutorial you've accomplished a lot.  You've created
your first function, deployed it to your local Fn server and invoked it over
HTTP.

To learn more about developing Fn functions using C#, refer to the [fdk-dotnet documentation](https://github.com/Daniel15/fdk-dotnet/).

**Go:** [Back to Contents](/tutorials/)
