# Create a Function from an existing Node/Oracle DB Docker Image

This tutorial walks through how to use an existing Docker image to define an
Fn function.  In this case, we use a Docker image that defines a Node.js application with Oracle Database drivers. The Docker image is defined in the Medium post: [Dockerfiles for node-oracledb are Easy and Simple](https://blogs.oracle.com/opal/dockerfiles-for-node-oracledb-are-easy-and-simple). By making a few tweaks to the configuration files and adding some Fn files, the image can be converted into an Fn function.

Although Fn functions are packaged as Docker images, when
developing functions using the Fn CLI developers are not directly exposed
to the underlying Docker platform.  Docker isn't hidden (you can see
Docker build output and image names and tags), but you aren't
required to be very Docker-savvy to develop functions with Fn.
However, sometimes you need to handle advanced use cases and must take
complete control of the creation of the function container image. Fortunately
the design and implementation of Fn enables you to do exactly that.

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Prequisites
This tutorial requires the following:
* Docker installed
* Fn installed
    * If you need help with Fn installation you can find instructions in the
[Install and Start Fn Tutorial](../install/README.md).

**Note:** Docker commands shown are for Linux. If you are using MacOS, the `sudo` command is not required.

## Getting Started with Docker Image
As a first step, let's create and run our initial Docker image.

**Note:** You can find these files in the `docker-only` directory.

Here is the dockerfile for the application.

```txt
FROM oraclelinux:7-slim

RUN  yum -y install oracle-release-el7 oracle-nodejs-release-el7 && \
     yum-config-manager --disable ol7_developer_EPEL && \
     yum -y install oracle-instantclient19.3-basiclite nodejs && \
     rm -rf /var/cache/yum

WORKDIR /myapp
ADD package.json /myapp/
ADD index.js /myapp/
RUN npm install

CMD exec node index.js
```

An Oracle Linux image is setup along with Node.js drivers for the Oracle database. Then, the application components are copied into the `myapp` directory. The `npm install` command installs any dependencies and then the application is executed `exec node index.js`.

The application is made of two parts. First, the `package.json` file defines any Node dependencies needed by the application.

```js
{
  "name": "nodedb-test",
  "version": "1.0.0",
  "private": true,
  "description": "Node DB Test application",
  "scripts": {
    "start": "node index.js"
  },
  "keywords": [
    "myapp"
  ],
  "dependencies": {
    "oracledb" : "^3.1"
  },
  "author": "Fn Example",
  "license": "MIT"
}
```

Only one dependency here, the `oracledb` driver.

Next the `index.js` is our actual Node program.
```js
const oracledb = require('oracledb')
console.log(oracledb.versionString)
```

An `oracledb` constant is called to return the version of the driver. Getting the version string validates that the driver code is packaged correctly in the Docker container and the driver is functional. In a production situation, you would actually connect to a database.

We are now ready to build the Docker image.

(1) Change into the `docker-only` directory included with this project.

(2) Build the image from the `docker-only` directory.
![](images/userinput.png)
>```
> sudo docker build --pull -t node-img .
>```

(3) Run the image with Docker.
![](images/userinput.png)
>```
> sudo docker run node-img
>```

(4) This returns the version of the Oracle DB driver:
```txt
3.1.2
```

That's it. You have a working Docker image.


## Convert your Docker image into  an Fn function
Now let's convert that working Docker image into a function.

**Note:** You can find these files in the `hellodb` directory.

The first step is some changes to the docker file.

```txt
FROM oraclelinux:7-slim

RUN  yum -y install oracle-release-el7 oracle-nodejs-release-el7 && \
     yum-config-manager --disable ol7_developer_EPEL && \
     yum -y install oracle-instantclient19.3-basiclite nodejs && \
     rm -rf /var/cache/yum

WORKDIR /function
ADD . /function/
RUN npm install

CMD exec node func.js
```

Most of the changes are just cosmetic. The working directory name is "function" rather than "myapp". The launch script is changed from `index.js` to `func.js`  following a normal function template.

Next let's add a `func.yaml` file.
```yaml
schema_version: 20180708
name: hellodb
version: 0.0.1
runtime: docker
entrypoint: node func.js
memory: 512
```

The function name is `hellodb`. The runtime is set to `docker`. Again notice our node script is `func.js` in this case.

Next, we update the `package.json` file.
```js
{
	"name": "hellodb",
    "version": "1.0.0",
	"description": "Node DB Test function",
	"main": "func.js",
	"dependencies": {
		"@fnproject/fdk": ">=0.0.13",
		"oracledb" : "^3.1"
	},
    "author": "Fn Example",
	"license": "Apache-2.0"
}
```

The main change is in dependencies. Here we add the `@fnproject/fdk` dependency to our list. This provides a helper code to run our function on an Fn server. For details on the [Fn Project NPM package click here](https://www.npmjs.com/package/@fnproject/fdk).  Finally, notice we changed the execution script to `func.js`.

For our last preparation step, update the `func.js` file to work with Fn.
```js
const fdk=require('@fnproject/fdk');
const oracledb = require('oracledb');

fdk.handle((input, ctx) => {
  return {'version': oracledb.versionString};
})
```
A `handle` method is added which is called when a function is invoked. Then, the version is returned as in the previous Docker image. A constant for `fdk` is also added to the script.

With those changes in place perform the following steps to execute the function.

(1) Start the Fn server if it is not already running.

(2) Change into the `hellodb` directory.
![](images/userinput.png)
>```
> cd hellodb
>```

(3) Create an Fn application to deploy our function to.
![](images/userinput.png)
>```
> fn create app helloapp
>```

The `helloapp` application is ready to store our function.

(4) Deploy the function to Fn and the `helloapp` application.
![](images/userinput.png)
>```
> fn -v deploy --app helloapp --local
>```

This deploys the function locally.

(5) Invoke the function.
![](images/userinput.png)
>```
> fn invoke helloapp hellodb
>```

This returns the version of the Oracle DB driver in JSON format:

```js
{"version":"3.1.2"}
```

That's it. You have converted a Node Docker image into a function.


# Conclusion

One of the most powerful features of Fn is the ability to use custom defined
Docker container images as functions. This feature makes it possible to
customize your function's runtime environment including letting you install
any Node libraries or drivers that your function might need. And thanks to
the Fn CLI's support for Dockerfiles it's the same user experience as when
developing any function.

**Go:** [Back to Contents](../../README.md)
