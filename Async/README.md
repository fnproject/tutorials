# Asynchronous Functions

Asynchronous functions are queued up and run at some point in the future. Great for expensive
or bulk operations.

## Writing Async Functions

When writing functions, the only differences between an async function and a sync function are:

1. There is no immediate response.
2. The response when calling a function returns a JSON object containing the `call_id`.

Response:

```json
{"call_id":"01BVJ2NZ1N07WGA00000000000"}
```

You can then use this `call_id` to retrieve information later.

## Create a Function

Let's create a function and make it async:

![user input](../images/userinput.png)
>```sh
>fn init --runtime go asyncfn
>cd asyncfn
>```

Now open `func.yaml` and add `type: async`, for example:

![user input](../images/userinput.png)
>```yaml
>name: asyncfn
>type: async
>version: 0.0.1
>runtime: go
>entrypoint: ./func
>```

Now let's deploy it:

![user input](../images/userinput.png)
>```sh
>fn deploy --local --app asyncapp
>```

Now we've deployed it as an async function so when we call it, it will be queued up to run later.

And call the asyncfn function:

![user input](../images/userinput.png)
>```sh
>fn call asyncapp /asyncfn
>```

Now you'll get a response like:

```json
{"call_id":"01BVJ5T7CA07WGE00000000000"}
```

## Check Status

We can retrieve the function call status by checking the status endpoint in the API.

Using the CLI, try running the following, replacing `CALL_ID` with the `call_id` returned above.

![user input](../images/userinput.png)
>```sh
>fn get call asyncapp CALL_ID
>```

You'll get something like the following:

```
ID: 01BVJ5T7CA07WGE00000000000
App: asyncapp
Route: /asyncfn
Created At: 2017-10-03T22:31:01.258Z
Started At: 2017-10-03T22:31:01.908Z
Completed At: 2017-10-03T22:31:02.615Z
Status: success
```

We can see that the Status is `success` which means the call finished properly. And, you can see the time it started and completed.

## Wrapping Up

Congratulations! In this tutorial you learned how to create and setup an async function. It's not much different
than a synchronous function as you learned, but it's a powerful difference for expensive, long running or batch operations.

**Go:** [Back to Contents](../README.md)
