# Create Apps with Fn

Fn supports grouping functions into a set that defines an application (or API), making it easy to
organize and deploy.

### Before you Begin
* Set aside about 15 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](https://github.com/fnproject/tutorials/blob/master/install/README.md).
    * Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". [See here](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context).)

> As you make your way through this tutorial, look out for this icon:
![user input](../images/userinput.png). Whenever you see it, it's time for you to
perform an action.

## Create an App
This part is easy, just create an `app.yaml` file and put a name in it:

![user input](../images/userinput.png)

>```sh
> mkdir myapp2
> cd myapp2
> echo 'name: myapp2' > app.yaml
>```

This directory will be the root of your application.

## Create a Root Function and Trigger
The root function will be available at `/` on your application.

![user input](../images/userinput.png)
>```sh
> fn init --runtime ruby --trigger http
> ```

Now we have a Ruby function alongside our `app.yaml`.

## Create a Sub Function and a Trigger

Now let's create a trigger`` at `/hello`:

![user input](../images/userinput.png)
>```sh
> fn init --runtime go --trigger http hello
>```

Now we have two functions in our app--one directly in the root folder
and one in the `hello` folder.  If you have the `tree` utility installed
run:

![user input](../images/userinput.png)
>```sh
> tree
>```

This will show the structure we've created which looks like this:

```sh
.
├── Gemfile
├── app.yaml
├── func.rb
├── func.yaml
├── hello
│   ├── func.go
│   ├── func.yaml
│   └── test.json
└── test.json
```

## Deploy the entire app
Now we can deploy the entire application with one command:

![user input](../images/userinput.png)
>```sh
> fn deploy --create-app --all --local
>```

Once the command is done we can examine the structure of the `myapp2` application.  First, get a
list of deployed applications. The `l` or `list` command, followed by `a`, `app` or `apps` displays
the list of applications:

![user input](../images/userinput.png)
>```sh
> fn list apps
>```

```txt
NAME		ID				
myapp2		01CT77FVBTNG8G00GZJ0000001	
```

You should see `myapp2` in the list of deployed applications.  We can
then list application's triggers by using the `list` command, followed by `triggers`:

![user input](../images/userinput.png)
>```sh
> fn list triggers myapp2
>```

```cs
FUNCTION    NAME            ID                          TYPE    SOURCE          ENDPOINT
hello       hello-trigger   01CT77HZ2NNG8G00GZJ0000005  http    /hello-trigger  http://localhost:8080/t/myapp2/hello-trigger
myapp2      myapp2-trigger  01CT77FWDFNG8G00GZJ000000   http    /myapp2-trigger http://localhost:8080/t/myapp2/myapp2-trigger
```

If you have previously set the `FN_REGISTRY` registry environment variable
your Docker image names will be prefixed by it. Otherwise your output will look
like:

```
fndemouser/hello        0.0.2       64049f5cfc52        10 minutes ago      15.6MB
fndemouser/myapp2       0.0.2       f9aebef4821a        11 minutes ago      59.5MB
```

Once again `l` is a valid abbreviation for `list` followed by the object you wish to display, either
apps or triggers. We can see there
are two triggers `fndemouser/hello` and `fndemouser/myapp2 ` with two different Docker images
associated with them.

Let's surf to our application.  Open in a browser or use curl to call each
of the functions.

* Root function at: <http://localhost:8080/t/myapp2/myapp2-trigger>
* And the hello function at: <http://localhost:8080/t/myapp2/hello-trigger>

## Wrapping Up

Congratulations! In this tutorial you learned how to group functions into an application and deploy them with a single command.

**Go:** [Back to Contents](../README.md)
