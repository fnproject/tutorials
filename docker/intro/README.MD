# Introduction to Fn with Docker
Fn is a lightweight Docker-based serverless functions platform you can run on
your laptop, server, or cloud.  In this introductory tutorial we'll see how to
use an existing Docker image as an Fn function and deploy this function to a local
Fn server. We'll also learn about the core Fn concepts like applications and
routes.

### Before you Begin
* Set aside about 5 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../../install/README.md).

> As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

## Your First Function
Now that Fn server is up and running, let's start with a simple "hello world" function 
using the Docker [hello-world image](https://hub.docker.com/_/hello-world/).

Let's set the `FN_REGISTRY` environment variable. Normally, it's set to your Docker Hub username.
However in this tutorial we'll work in local development mode so we can set
the `FN_REGISTRY` variable to an arbitrary value. Let's use `fndemouser`.

![user input](images/userinput.png)
>```
> export FN_REGISTRY=fndemouser
>```

Let's work inside the tutorials/docker/intro directory

![user input](images/userinput.png)
>```
> cd tutorials/docker/intro
>```

Now let's create a Dockerfile. 

![](images/userinput.png) Copy/paste the following into a file named `Dockerfile`:

```dockerfile
FROM hello-world
```

Now let's generate the function boilerplate. In the terminal type the following.

![user input](images/userinput.png)
>```
> fn init --runtime docker
>```

The output will be

```yaml
Dockerfile found. Using runtime 'docker'.
func.yaml created.
```

