# Triggering Functions

Since all functions have a URL, you can easily trigger them via anything that can hit an HTTP URL (ie: a webhook).

In this tutorial, we'll setup another service and configure a webhook that will trigger a function.

## Triggering

Let's write a function that takes an image URL, downloads it, modifies it, then uploads it again.


```sh
mkdir triggers
cd triggers
npm init -f
fn init --runtime node
npm install jimp
echo '{"img":"http://yo"}' | fn run
```

docker run --rm --name minio -p 9000:9000 -e MINIO_ACCESS_KEY=ACCESSKEY -e MINIO_SECRET_KEY=SECRETKEY minio/minio server /data

docker run --rm -it -v $PWD:/mc -w /mc --entrypoint=/bin/sh minio/mc

mc config host add local http://docker.for.mac.localhost:9000 ACCESSKEY SECRETKEY
mc mb local/mybucket
mc policy public local/mybucket

https://github.com/treeder/vista/blob/master/scripts/setup_minio.sh



When writing functions, the only differences between an async function and a sync function are:

1. There is no immediate response, output goes to the logs instead.
1. The response when calling a function returns a JSON object containing the `call_id`.

Response:

```json
{"call_id":"01BVJ2NZ1N07WGA00000000000"}
```

You can then use this `call_id` to retrieve information later.

## Create a Function

Let's create a function and make it async:

![user input](../images/userinput.png)

```sh
fn init --runtime go async
cd async
```

Now open `func.yaml` and add `type: async`, for example:

```yaml
type: async
version: 0.0.1
runtime: go
entrypoint: ./func
```

Now let's deploy it:

![user input](../images/userinput.png)

```sh
fn deploy --local --app myapp
```

Now we've deployed it as an `async` function so when we call it, it will be queued up to run later.

And call the async function:

![user input](../images/userinput.png)

```sh
fn call myapp async
```

Now you'll get a response like:

```json
{"call_id":"01BVJ5T7CA07WGE00000000000"}
```

## Check Status and Get Logs

We can retrieve the function call status by checking the status endpoint in the API.

Using the CLI, try running the following, replacing `CALL_ID` with the `call_id` returned above.

![user input](../images/userinput.png)

```sh
fn get calls myapp CALL_ID
```

You'll get something like the following:

```
ID: 01BVJ5T7CA07WGE00000000000
App: myapp
Route: /async
Created At: 2017-10-03T22:31:01.258Z
Started At: 2017-10-03T22:31:01.908Z
Completed At: 2017-10-03T22:31:02.615Z
Status: success
```

We can see that the Status is `success` which means the call finished properly. And you can see the time it started and completed.

But how do we check the logs to debug and ensure things ran properly? There's a `log` endpoint to allow you to check this and you can 
access it via the CLI with:

![user input](../images/userinput.png)

```sh
fn get logs myapp CALL_ID
```

For this function, the logs will contain:

```json
{"message":"Hello World"}
```

## Wrapping Up

Congratulations! In this tutorial you learned how to create and setup an async function. It's not much different
than a synchronous function as you learned, but it's a powerful difference for expensive, long running or batch operations.

**Go:** [Back to Contents](../README.md)
