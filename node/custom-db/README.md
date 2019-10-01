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

This tutorial requires you to have both Docker and Fn installed. If you need
help with Fn installation you can find instructions in the
[Install and Start Fn Tutorial](../install/README.md).

# Getting Started
