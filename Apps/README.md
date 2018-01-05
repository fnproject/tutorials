# Fn Applications

Fn supports grouping functions into a set that defines an application (or API), making it easy to organize and deploy.

## Create an app

This part is easy, just create an `app.yaml` file and put a name in it:

![user input](../images/userinput.png)
> `mkdir myapp2`

> `cd myapp2`

> `echo 'name: myapp2' > app.yaml`

This directory will be the root of your application.

## Create a root function

The root function will be available at `/` on your application.

![user input](../images/userinput.png)
> `fn init --runtime ruby`

Now we have a Ruby function alongside our `app.yaml`.

## Create a sub route

Now let's create a sub route at `/hello`:

![user input](../images/userinput.png)
>`fn init --runtime go hello`

Now we have two functions in our app. Run:

![user input](../images/userinput.png)
> `ls`

To see our root function, our `app.yaml` and a directory named `hello`.

## Deploy the entire app

Now we can deploy the entire application with one command:

![user input](../images/userinput.png)
> `fn deploy --all --local`

Once the command is done we can examine the structure of the `myapp2`
application.  First we can get a list of deployed applications.  The
`fn apps` command accepts either `l` or `list` to display the list of
applications:

![user input](../images/userinput.png)
> `fn apps list`

You should see `myapp2` in the list of deployed applications.  We can
then list application's routes using the `fn routes` command:

![user input](../images/userinput.png)
> `fn routes l myapp2`

```
/       myapp2-root:0.0.2 localhost:8080/r/myapp2
/hello  hello:0.0.2       localhost:8080/r/myapp2/hello
```
Once again `l` is a valid abbreviation for `list` followed by the name
of the application who's routes should be displayed.  We can see there
are two routes `/` and `/hello` with two different Docker images
assocated with them.

Let's surf to our application.  Open in a browser or use curl to call each
of the functions.

* Root function at: http://localhost:8080/r/myapp2/
* And the hello function at: http://localhost:8080/r/myapp2/hello

## Wrapping Up

Congratulations! In this tutorial you learned how to group functions into an application and deploy them
with a single command.

**Go:** [Back to Contents](../README.md)
