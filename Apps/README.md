# Fn Applications

Fn supports grouping functions into a set that defines an application (or API), making it easy to organize and deploy.

## Create an app

This part is easy, just create an `app.yaml` file and put a name in it:

![user input](../images/userinput.png)

```sh
mkdir myapp2
cd myapp2
echo 'name: myapp2' > app.yaml
```

This directory will be the root of your application.

## Create a root function

The root function will be available at `/` on your application.

![user input](../images/userinput.png)

```sh
fn init --runtime ruby
```

Now we have a Ruby function alongside our `app.yaml`.

## Create a sub route

Now let's create a sub route at `/hello`:

```sh
fn init --runtime go hello
```

Now we have two functions in our app. Run:

![user input](../images/userinput.png)

```sh
ls
```

To see our root function, our `app.yaml` and a directory named `hello`.

## Deploy the entire app

Now we can deploy the entire application with one command:

![user input](../images/userinput.png)

```sh
fn deploy --all --local
```

Once the command is done, let's surf to our application:

* Root function at: https://localhost:8080/r/myapp2/
* And the hello function at: https://localhost:8080/r/myapp2/hello

## Wrapping Up

Congratulations! In this tutorial you learned how to group functions into an application and deploy them
with a single command.

**Go:** [Back to Contents](../README.md)
