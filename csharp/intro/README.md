# Introduction to Fn with Dotnet

This tutorial introduces the
[Fn Function Development Kit for Dotnet (FDK for Dotnet)](https://github.com/fnproject/fdk-dotnet).
If you haven't completed the [Introduction to Fn](../Introduction/README.md)
tutorial you should head over there before you proceed.

This tutorial takes you through the Fn developer experience for building
C# functions and running them on Dotnet runtime. It shows how easy it is
to build, deploy and test functions written in C#.

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

### Before you Begin
* Set aside about 30 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../install/README.md).
    * Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". [See here](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context).)


## Your First Function

Let's start by creating a new function.  In a terminal type the following:

![](images/userinput.png)
>`fn init --runtime dotnet dotnetfn`

The output will be:

```sh
Creating function at: ./dotnetfn
Function boilerplate generated.
func.yaml created.
```

![](images/userinput.png)
>```sh
>cd dotnetfn
>```

The `fn init` command creates an simple function with a bit of boilerplate to get you
started. The `--runtime` option is used to indicate that the function
we're going to develop will be written in C# and will work on dotnet core 3.1, 
the default version as of this writing.

__If__ you have the `tree` utility installed
you can see the directory structure that the `init` command has created.

![](images/userinput.png)
>`tree`

```sh
.
├── Function.sln
├── func.yaml
├── src
│   └── Function
│       ├── Function.csproj
│       └── Program.cs
└── tests
    └── Function.Tests
        ├── Function.Tests.csproj
        └── ProgramTest.cs

4 directories, 6 files
```


The init command has created a `func.yaml` file, a solution file, source project, and test project.

Take a look at the contents of the generated func.yaml file.

![](images/userinput.png)
>```sh
>cat func.yaml
>```

```yaml
name: dotnetfn
version: 0.0.1
runtime: dotnet
build_image: fnproject/dotnet:3.1-1.0.1-dev
run_image: fnproject/dotnet:3.1-1.0.1
cmd: Function:Greeter:greet
entrypoint: dotnet Function.dll
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* name--Name of your function and directory.
* version--the version of the function.
* runtime--the language/runtime used for this function.
* build_image--the image used to build your function's image.
* run_image--the image your function runs in.
* cmd--the `cmd` property is the identifier in form `Namespace:Class:Method`.
* entrypoint--the entrypoint into the docker image. `Function.dll` in this case
comes from the source project's `Function.csproj` file. This value is used by CLI to properly locate the binary and should __NOT__ be modified.


## Deploy your Dotnet Function

Make sure your context is set to default and you are using a demo user. Use the `fn list contexts` command to check.

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
> fn create app dotnet-app
>```

A confirmation is returned:

```yaml
Successfully created app:  dotnet-app
```

Now `dotnet-app` is ready for functions to be deployed to it.

### Deploy your Function to your App
Deploying your function is how you publish your function and make it accessible
to other users and systems. To see the details of what is happening during a
function deploy,  use the `--verbose` switch.  The first time you build a
function of a particular language it takes longer as Fn downloads the necessary
Docker images. The `--verbose` option allows you to see this process.

![](images/userinput.png)
>```sh
> fn --verbose deploy --app dotnet-app --local
>```

```yaml
Deploying dotnetfn to app: dotnet-app
Bumped to version 0.0.2
Using Container engine docker
Building image fndemouser/dotnetfn:0.0.2 
Dockerfile content
-----------------------------------
FROM fnproject/dotnet:3.1-1.0.1-dev as build-stage
WORKDIR /function
COPY . .
RUN dotnet sln add src/Function/Function.csproj tests/Function.Tests/Function.Tests.csproj
RUN dotnet build -c Release
RUN dotnet test -c Release
RUN dotnet publish src/Function/Function.csproj -c Release -o out
FROM fnproject/dotnet:3.1-1.0.1
WORKDIR /function
COPY --from=build-stage /function/out/ /function/
ENTRYPOINT ["dotnet", "Function.dll"]
CMD ["Function:Greeter:greet"]
-----------------------------------
FN_REGISTRY:  fndemouser
Current Context:  default
[+] Building 42.0s (17/17) FINISHED                                                                                                                                                                         
 => [internal] load build definition from Dockerfile2136271709                                                                                                                                         0.0s
 => => transferring dockerfile: 513B                                                                                                                                                                   0.0s
 => [internal] load .dockerignore                                                                                                                                                                      0.0s
 => => transferring context: 2B                                                                                                                                                                        0.0s
 => [internal] load metadata for docker.io/fnproject/dotnet:3.1-1.0.1                                                                                                                                  3.5s
 => [internal] load metadata for docker.io/fnproject/dotnet:3.1-1.0.1-dev                                                                                                                              3.5s
 => [auth] fnproject/dotnet:pull token for registry-1.docker.io                                                                                                                                        0.0s
 => [build-stage 1/7] FROM docker.io/fnproject/dotnet:3.1-1.0.1-dev@sha256:10a817c5dc72c6f593c55d114755fc5c3b66a860f7674ee52e2afb1a6da048bf                                                           18.2s
 => => resolve docker.io/fnproject/dotnet:3.1-1.0.1-dev@sha256:10a817c5dc72c6f593c55d114755fc5c3b66a860f7674ee52e2afb1a6da048bf                                                                        0.0s
 => => sha256:10a817c5dc72c6f593c55d114755fc5c3b66a860f7674ee52e2afb1a6da048bf 742B / 742B                                                                                                             0.0s
 => => sha256:455c8008f5642117a25f434009af0181609b2eef703b262c81cd121b51551f45 1.88kB / 1.88kB                                                                                                         0.0s
 => => sha256:e4430e06691f65e516df7d62db0ee5393acea9ade644cc6bc620efef0956dd17 42.11MB / 42.11MB                                                                                                       1.9s
 => => sha256:b899d482eac32e4cef25fba11edd4ab6213ea48fc48dd1a5d855f1dcb9b6834c 130.23MB / 130.23MB                                                                                                    12.3s
 => => extracting sha256:e4430e06691f65e516df7d62db0ee5393acea9ade644cc6bc620efef0956dd17                                                                                                              2.4s
 => => extracting sha256:b899d482eac32e4cef25fba11edd4ab6213ea48fc48dd1a5d855f1dcb9b6834c                                                                                                              5.7s
 => [internal] load build context                                                                                                                                                                      0.0s
 => => transferring context: 3.22kB                                                                                                                                                                    0.0s
 => [stage-1 1/3] FROM docker.io/fnproject/dotnet:3.1-1.0.1@sha256:e621d26f008176d1a26663cdce46d024323e38aad397ab9db05c6dec01d15bd7                                                                   11.8s
 => => resolve docker.io/fnproject/dotnet:3.1-1.0.1@sha256:e621d26f008176d1a26663cdce46d024323e38aad397ab9db05c6dec01d15bd7                                                                            0.0s
 => => sha256:bba59524aa1202b0b8e932ab2b1db46083658ceab75f8c3014a7435194be0e6e 2.00kB / 2.00kB                                                                                                         0.0s
 => => sha256:e4430e06691f65e516df7d62db0ee5393acea9ade644cc6bc620efef0956dd17 42.11MB / 42.11MB                                                                                                       1.9s
 => => sha256:ae9a010054f7e0dc053356770bd073288cfd60746801d5c8afde0f0651a034df 61.16MB / 61.16MB                                                                                                       8.4s
 => => sha256:e621d26f008176d1a26663cdce46d024323e38aad397ab9db05c6dec01d15bd7 741B / 741B                                                                                                             0.0s
 => => extracting sha256:e4430e06691f65e516df7d62db0ee5393acea9ade644cc6bc620efef0956dd17                                                                                                              2.4s
 => => extracting sha256:ae9a010054f7e0dc053356770bd073288cfd60746801d5c8afde0f0651a034df                                                                                                              3.0s
 => [stage-1 2/3] WORKDIR /function                                                                                                                                                                    0.1s
 => [build-stage 2/7] WORKDIR /function                                                                                                                                                                0.1s
 => [build-stage 3/7] COPY . .                                                                                                                                                                         0.0s
 => [build-stage 4/7] RUN dotnet sln add src/Function/Function.csproj tests/Function.Tests/Function.Tests.csproj                                                                                       0.9s
 => [build-stage 5/7] RUN dotnet build -c Release                                                                                                                                                     13.4s
 => [build-stage 6/7] RUN dotnet test -c Release                                                                                                                                                       3.7s 
 => [build-stage 7/7] RUN dotnet publish src/Function/Function.csproj -c Release -o out                                                                                                                1.7s 
 => [stage-1 3/3] COPY --from=build-stage /function/out/ /function/                                                                                                                                    0.1s 
 => exporting to image                                                                                                                                                                                 0.1s 
 => => exporting layers                                                                                                                                                                                0.1s 
 => => writing image sha256:dfc4f9133b232b5e337e7b6ec23024619872d648b040ba1a64429f9427f3e7fc                                                                                                           0.0s 
 => => naming to docker.io/fndemouser/dotnetfn:0.0.2                                                                                                                                                   0.0s

Use 'docker scan' to run Snyk tests against images to find vulnerabilities and learn how to fix them

Updating function dotnetfn using image fndemouser/dotnetfn:0.0.2...
Successfully created function: dotnetfn with fndemouser/dotnetfn:0.0.2
```

All the steps to load the current language Docker image are displayed.

Specifying `--app dotnet-app` explicitly puts the function in the application "dotnet-app".

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message
`Updating function dotnetfn using image fndemouser/dotnetfn:0.0.2...`
let's us know that the function is packaged in the image
"fndemouser/dotnetfn:0.0.2".

Note that the containing folder name `dotnetfn` was used as the name of the
generated Docker container and used as the name of the function that
container was bound to.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.


## Invoke your Deployed Function

Use the the `fn invoke` command to call your function from the command line.

### Invoke with the CLI

The first is using the Fn CLI which makes invoking your function relatively
easy.  Type the following:

![user input](images/userinput.png)
>```sh
> fn invoke dotnet-app dotnetfn
>```

which results in:

```txt
Hello World!
```

In the background, Dotnet compiles the code and runs any tests, the function is
packaged into a container, and then the function is run to produce the output
"Hello World!".

You can also pass data to the invoke command. For example:

![user input](images/userinput.png)
>```sh
> echo -n 'Bob' | fn invoke dotnet-app dotnetfn
>```

```txt
Hello Bob!
```

"Bob" was passed to the function where it is processed and returned in the output.


## Exploring the Code

We've generated, compiled, deployed, and invoked the Dotnet function so let's take
a look at the code.

Below is the generated `src/Function/Program.cs`.  As you can
see the function is just a method in a that takes a string value
and returns another string value, but the FDK for Dotnet also supports binding
input parameters to primitive types, byte arrays and classes
unmarshalled from JSON. Functions can also be static or instance
methods and async.

```csharp
using Fnproject.Fn.Fdk;

using System.Runtime.CompilerServices;
[assembly:InternalsVisibleTo("Function.Tests")]
namespace Function {
	class Greeter {
		public string greet(string input) {
			return string.Format("Hello {0}!",
				input.Length == 0 ? "World" : input.Trim());
		}

		static void Main(string[] args) { Fdk.Handle(args[0]); }
	}
}
```

This function returns the string "Hello World!" unless an input string
is provided in which case it returns "Hello &lt;input string&gt;!".  We saw
this previously when we piped "Bob" into the function.   Notice that
the FDK for Dotnet reads from standard input and automatically puts the
content into the string passed to the function.  This greatly simplifies
the function code.

## Testing with NUnit

The `fn init` command also generated a NUnit test for the function.  With this framework you can setup test fixtures with various function input values and verify the results.

```csharp
using Function;
using NUnit.Framework;

namespace Function.Tests {
	public class GreeterTest {
		[Test]
		public void TestGreetValid() {
			Greeter greeter = new Greeter();
			string response = greeter.greet("Dotnet");
			Assert.AreEqual("Hello Dotnet!", response);
		}

		[Test]
		public void TestGreetEmpty() {
			Greeter greeter = new Greeter();
			string response = greeter.greet("");
			Assert.AreEqual("Hello World!", response);
		}
	}
}
```

You can run the tests by building your function with `fn build`.  This
will cause Dotnet to add both Source and Test project to `Function.sln`. 
The code will be build and tests will be run on creating a new function.

![](images/userinput.png)
>`fn build`

```sh
Building image fndemouser/dotnetfn:0.0.2 .......
Function fndemouser/dotnetfn:0.0.2 built successfully.
```

## Accepting JSON Input

Let's convert this function to use JSON for its input and output.
Replace the definition of `HelloFunction` with the following:

```csharp
using Fnproject.Fn.Fdk;

using System.Runtime.CompilerServices;
[assembly:InternalsVisibleTo("Function.Tests")]
namespace Function {
  class Input {
    public string name;
  }

	class Greeter {
		public string greet(Input input) {
			return string.Format("Hello {0}!",
				input.name.Length == 0 ? "World" : input.name.Trim());
		}

		static void Main(string[] args) { Fdk.Handle(args[0]); }
	}
}
```

Since we modified the `Program.cs` and changes the function format, we need to modify the `ProgramTest.cs` class.

```csharp
using Function;
using NUnit.Framework;

namespace Function.Tests {
	public class GreeterTest {
		[Test]
		public void TestGreetEmpty() {
			Greeter greeter = new Greeter();
			Input input = new Input();
            input.name = "Dotnet";
			string response = greeter.greet(input);
			Assert.AreEqual("Hello Dotnet!", response);
		}
	}
}
```

## Invoke with Curl

The other way to invoke your function is via HTTP. With the changes to the code,
we can pass JSON and return JSON from the the function.  The Fn server exposes
our deployed function at system produced endpoint. Next, we need to look up the invoke endpoint for our function.

### Getting a Function's Invoke Endpoint

In addition to using the Fn `invoke` command, we can call a function by using a
URL. To do this, we must get the function's invoke endpoint. Use the command
`fn inspect function <appname> <function-name>`.  To list the `dotnetfn` function's
invoke endpoint we can type:

![user input](images/userinput.png)
>```sh
> fn inspect function dotnet-app dotnetfn
>```

```js
{
	"annotations": {
		"fnproject.io/fn/invokeEndpoint": "http://localhost:8080/invoke/01G1ZZMSA7NG8G00GZJ0000002"
	},
	"app_id": "01G1ZZK3FNNG8G00GZJ0000001",
	"created_at": "2022-05-01T14:15:58.023Z",
	"id": "01G1ZZMSA7NG8G00GZJ0000002",
	"idle_timeout": 30,
	"image": "fndemouser/dotnetfn:0.0.2",
	"memory": 128,
	"name": "dotnetfn",
	"timeout": 30,
	"updated_at": "2022-05-01T14:15:58.023Z"
}
```

The output confirms that `dotnetfn` function's invoke endpoint is:
`http://localhost:8080/invoke/01G1ZZMSA7NG8G00GZJ0000002`. We can use this URL
to call the function.

### Curl Commands
Use `curl` to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -X "POST" -H "Content-Type: application/json" http://localhost:8080/invoke/01G1ZZMSA7NG8G00GZJ0000002
>```

The result is now in a JSON format.

```
Hello World!
```

## Wrap Up

Congratulations! You've just deployed a dotnet function on Fn. 
There's so much more in the FDK than we can cover in a brief
introduction but we'll go deeper in subsequent tutorials.

**Go:** [Back to Contents](../../README.md)
