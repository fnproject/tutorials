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
2. [Docker](https://www.docker.com/) 17.10 (or higher) needs to be
installed and running.

> As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Download and Install the fn CLI

From a terminal type the following:

![](images/userinput.png)
>```sh
> curl -LSs https://raw.githubusercontent.com/fnproject/cli/master/install | sh
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

**Note:** The above `fn` CLI install script requires write access to restricted folders like /usr/local/bin. If the user doesn't have write access to /usr/local/bin, or if you prefer to install `fn` CLI in a different location, please see the [Fn Manual Install](#fn-manual-install) section.

## Start the Fn Server

The final install step is to start the Fn server.  Since Fn runs on
Docker it'll need to be up and running too.

To start the Fn server you use the `fn` CLI. Run the `fn start` command. This will
download the Fn server docker image and start the Fn Server on port 8080 by default.
Note that this process runs in the foreground so that it's easy to stop with Ctrl-C:

![user input](images/userinput.png)
>```sh
> fn start
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

**Note:** The Fn server stores its metadata in the `~/.fn/data` directory. If you run in to errors after updating the Fn server, you may want to delete the contents of this `data` directory and restart Fn server.

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
> fn start -p 8081
>```

When using a non-default port, you must point the `fn` CLI to the new port using
the `FN_API_URL` environment variable:

![user input](images/userinput.png)
>```sh
> export FN_API_URL=http://127.0.0.1:8081
>```

Alternatively, you can also set the `api_url` using Fn [contexts](https://github.com/fnproject/cli/blob/master/CONTEXT.md).

## Test the Install
Let's verify everthing is up and running correctly.

**Open a new terminal** and run the following:

![user input](images/userinput.png)
>```sh
> fn version
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


## Fn Manual Install
The steps to install Fn manually are described in this section. The system requirements are the same as those outlined for the script installation.

You will need to follow these steps if the user doesn't have write access to /usr/local/bin, or if you prefer to install `fn` CLI in a different location.

### Download Fn CLI
Download the CLI for your operating system. For this example, files are saved to the `~/Downloads` directory.

![user input](images/userinput.png)
1. Open the Fn project release directory in your browser: <https://github.com/fnproject/cli/releases/>. You should see a list of executables for supported operating systems:

| Operating System | Executable |
| ------------- |:-------------:|
| MacOS | fn_mac |
| Linux | fn_linux |
| Alpine Linux | fn_alpine |
| Windows | fn.exe |

2. Click on the Fn executable for your operating system and save the file locally

#### For Mac / Linux Systems
* Open a Terminal Window
* Change into your home directory

![user input](images/userinput.png)
>```sh
> cd ~
>```

* Create a directory for your executable

![user input](images/userinput.png)
>```sh
> mkdir lbin
>```

* Copy the downloaded executable file into this `lbin` directory

![user input](images/userinput.png) On Mac:
>```sh
> mv ~/Downloads/fn_mac.dms lbin/fn
>```

![user input](images/userinput.png) On Linux:
>```sh
> mv ~/Downloads/fn_linux lbin/fn
>```

* Make the file executable.

![user input](images/userinput.png)
>```sh
> chmod +x lbin/fn
>```

* Add the `~/lbin` directory to your `PATH` environment variable as shown below

![user input](images/userinput.png)
>```sh
> export PATH=~/lbin:$PATH
>```

You can confirm using the `which fn` command, the output should look like `/Users/<your-user>/lbin/fn`. Beyond this point you can use `fn` directly.

(Alternatively, you can use `~/lbin/fn` if you don't want to add to your `PATH` environment variable.)


#### For Windows Systems
For Windows the current recommendation is to run Fn in Linux virtual machine on Windows. You can do with with [VirtualBox](https://www.virtualbox.org/) or other commercial VM software.


## Learn More

Congratulations! You've installed Fn and started up an Fn server. Now you are
ready to create your first function for one of the supported languages. You can
start with:

* [Go](../Introduction/README.md)
* [Java](../JavaFDKIntroduction//README.md)
* [Node.js](../node/intro/README.md)
* [Ruby](../ruby/intro/README.md)

**Go:** [Back to Contents](../README.md)
