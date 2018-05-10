# Install Fn

Fn is a lightweight Docker-based serverless functions platform you can
run on your laptop, server, or cloud.  In this installation tutorial
we'll walk through installing Fn.

Setting up a working Fn installation involves these three simple steps:
* Ensure you have the necessary prerequisites
* Download the `fn` command line interface (CLI) utility
* Run `fn start` command which will download the Fn server docker image and start the Fn server

### Before you Begin

Before we can install Fn you'll need:

1. A computer running Linux or MacOS.  If you have a Windows machine the
easiest thing to do is install [VirtualBox](https://www.virtualbox.org/)
and run a free Linux virtual machine.
2. [Docker](https://www.docker.com/) 17.05 (or higher) needs to be
installed and running.

> As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Download and Install the fn CLI

From a terminal type the following:

![](images/userinput.png)
>```sh
>curl -LSs https://raw.githubusercontent.com/fnproject/cli/master/install | sh
>```

Once installed you'll see the `fn` CLI version printed out.  You should see
something similar to the following displayed (although likely with a later
version number):

```sh
fn version 0.4.87

        ______
       / ____/___
      / /_  / __ \
     / __/ / / / /
    /_/   /_/ /_/`
    
```

## Start the Fn Server

The final install step is to start the Fn server.  Since Fn runs on
Docker it'll need to be up and running too.

To start the Fn server you use the `fn` CLI. Run the `fn start` command. This will 
download the Fn server docker image and start the Fn Server on port 8080 by default. 
Note that this process runs in the foreground so that it's easy to stop with Ctrl-C:

![user input](images/userinput.png)
>```sh
>fn start
>```

If the Fn Server starts up successfully, you should see output similar to:

```sh
...
time="2018-05-10T11:32:49Z" level=info msg="available cpu" availCPU=2000 totalCPU=2000
time="2018-05-10T11:32:49Z" level=info msg="sync and async cpu reservations" cpuAsync=1600 cpuAsyncHWMark=1280 cpuSync=400

        ______
       / ____/___
      / /_  / __ \
     / __/ / / / /
    /_/   /_/ /_/
        v0.3.439

time="2018-05-10T11:32:49Z" level=info msg="Fn serving on `:8080`" type=full
```

**Note:** The Fn server creates a temporary `data` directory it uses to store metadata. If you want to retain this data after a restart, make sure you start Fn server in the same directory.

If you have some other process running on port 8080, `fn start` will 
fail with the following error:

```sh
docker: Error response from daemon: driver failed programming external connectivity on endpoint fnserver (d9478f85df4ef97d23d618c2318c243f1e8b65d69ca2547d889d80b148c5be09): Error starting userland proxy: Bind for 0.0.0.0:8080 failed: port is already allocated.
2018/05/10 16:49:25 error: processed finished with error exit status 125
```

In this case you can stop the other process and run `fn start` again. Alternatively, 
you can start Fn server on a different port.

#### Start the Fn Server on a Different Port
Fn Server starts on port 8080 by default. To use a different port use the `--port` or the `-p` option. For example

![user input](images/userinput.png)
>```sh
>fn server -p 8081
>```

When using a non-default port, you must point the `fn` CLI to the new port using 
the `FN_API_URL` environment variable:

```sh
export FN_API_URL=http://127.0.0.1:8081
```

Alternatively, you can also set the `api_url` using Fn [contexts](https://github.com/fnproject/cli/blob/master/CONTEXT.md).

## Test the Install
Let's verify everthing is up and running correctly.

**Open a new terminal** and run the following:

![user input](images/userinput.png)
>```sh
>fn version
>```

You should see the version of the fn CLI (client) and server displayed (your
version will likely differ):

```sh
Client version:  0.4.87
Server version:  0.3.439
```

**Note:** If the server version is "?" then the `fn` CLI cannot reach the Fn server.  
If this happens it's likely you have something else running on port 8080 or you 
started the server on a different port but forgot to set the `FN_API_URL`. 

## Learn More

Congratulations! You've installed Fn and started up an Fn server. Now you are
ready to create your first function for one of the supported languages. You can
start with:

* [Go](../Introduction/README.md)
* [Java](../JavaFDKIntroduction//README.md)
* [Node.js](../node/intro/README.md)
* [Ruby](../ruby/intro/README.md)

**Go:** [Back to Contents](../README.md)
