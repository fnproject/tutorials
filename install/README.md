# Install Fn

Fn is a lightweight Docker-based serverless functions platform you can
run on your laptop, server, or cloud.  In this installation tutorial
we'll walk through installing Fn.

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

Setting up a working Fn install is a two-step process.  First you need
to ensure you have the necessary prerequisites and then you can install
Fn itself.

## Prerequisites

Before we can install Fn you'll need:

1. A computer running Linux or MacOS.  If you have a Windows machine the
easiest thing to do is install [VirtualBox](https://www.virtualbox.org/)
and run a free Linux virtual machine.
2. [Docker](https://www.docker.com/) 17.05 (or higher) needs to be
installed and running.

> __NOTE__ In this tutorial we'll work in a purely local development
mode.  However, when deploying functions to a remote Fn server, a Docker
Hub (or other Docker registry) account is required.

That's it.  You can use your favorite IDE for function development.
However, for this tutorial, an IDE isn't necessary.


## Downloading and Installing Fn

From a terminal type the following:

![](images/userinput.png)
>`curl -LSs https://raw.githubusercontent.com/fnproject/cli/master/install | sh`

Once installed you'll see the Fn version printed out.  You should see
something similar to the following displayed (although likely with a later
version number):

```sh
fn version 0.4.62
```

## Starting Fn Server

The final install step is to start the Fn server.  Since Fn runs on
Docker it'll need to be up and running too.

To start Fn you can use the `fn` command line interface (CLI).  Type the
following but note that the process will run in the foreground so that
it's easy to stop with Ctrl-C:

![user input](images/userinput.png)
>`fn start`

You should see output similar to:

```sh
time="2017-09-18T14:37:13Z" level=info msg="datastore dialed" datastore=sqlite3 max_idle_connections=256
time="2017-09-18T14:37:13Z" level=info msg="available memory" ram=1655975936
time="2017-09-18T14:37:13Z" level=info msg="Serving Functions API on address `:8080`"

      ______
     / ____/___
    / /_  / __ \
   / __/ / / / /
  /_/   /_/ /_/
```

**Note:** The Fn server creates a temporary `data` directory it uses to store metadata. If you want to retain this data after a restart, make sure you start Fn server in the same directory.

#### Changing the Fn Server Port
Fn Server starts on port 8080 by default. To change the value use the `--port` or the `-p` option. For example

```sh
fn server --port 8081
fn server -p 8081
```

In addition after changing the port, the Fn client must be configured for the new port value using the `FN_API_URL` environment variable or setting the `api_url` using Fn [contexts](https://github.com/fnproject/cli/blob/master/CONTEXT.md). For example

```sh
export FN_API_URL=http://127.0.0.1:8081
```


## Testing the Install
Let's verify everthing is up and running correctly.

**Open a new terminal** and run the following:

![user input](images/userinput.png)
>`fn version`

You should see the version of the fn CLI (client) and server displayed (your
version will likely differ):

```sh
Client version:  0.4.62
Server version:  0.3.335
```

If the server version is "?" then the fn CLI cannot reach the server.  If
this happens it's likely you have something else running on port 8080. In this
case stop the other process, and stop (**ctrl-c**) and restart the fn server as
described previously.

## Learn More

Congratulations! You've installed Fn and started up an Fn server. Now you are
ready to create your first function for one of the supported languages. You can
start with:

* [Go](../Introduction/README.md)
* [Java](../JavaFDKIntroduction//README.md)
* [Node.js](../node/intro/README.md)
* [Ruby](../ruby/intro/README.md)

**Go:** [Back to Contents](../README.md)
