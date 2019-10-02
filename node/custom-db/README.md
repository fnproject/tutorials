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

**Note:** Commands show are for Linux. If you are using MacOS, the `sudo` command is not if you are using an admin account.

## Getting Started with Docker Image
As a first step, let's create and run our starting point Docker image.

**Note:** You can find these files in the `docker-only` directory.

Here is the dockerfile for the application.

```dockerfile
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

An Oracle Linux images is setup along with Node.js drivers for Oracle database. Then the application components are copied into the my add directory. The `npm install` command installs any dependencies and then the application is executed `exec node index.js`.

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

Next the `index.js` is our actual program.

```js
const oracledb = require('oracledb')
console.log(oracledb.versionString)
```

An `oracledb` constant is made and call is made to return the version of the driver.

We are now ready to build the Docker image.

(1) Change into the `docker-only` directory included with this project.

(2) Build the image from the `docker-only` directory.
![](images/userinput.png)
>```
> sudo docker build --pull -t node-img .
>```


![](images/userinput.png)
>```
> sudo docker run node-img
>```

This returns the version of the Oracle DB driver:

>```
> 3.1.2
>```

That's it. You have a working Docker image.


## Convert your Docker image into  an Fn function


![](images/userinput.png)
>```
> fn -v build
>```


![](images/userinput.png)
>```
> fn -v build
>```


![](images/userinput.png)
>```
> fn -v build
>```




![](images/userinput.png)
>```
> fn -v build
>```




![](images/userinput.png)
>```
> fn -v build
>```




![](images/userinput.png)
>```
> fn -v build
>```
