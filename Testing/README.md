# Testing Functions

Of course you can test your code in your usual language specific ways, but we've also created a way to
both define and test your functions, regardless of how or what language it is implemented in. This works
by defining a series of inputs and expected outputs, essentially creating a contract for your function.

## Test Format

The test format is simply a json file with an array if inputs and expected outputs. For example:

```json
{
    "tests": [
        {
            "input": {
                "body": {
                    "name": "Johnny"
                }
            },
            "output": {
                "body": {
                    "message": "Hello Johnny"
                }
            }
        },
        {
            "input": {
                "body": ""
            },
            "output": {
                "body": {
                    "message": "Hello World"
                }
            }
        }
    ]
}
```

This is two tests, one with the input:

```json
{
    "name": "Johnny"
}
```

And the other with an empty body. The expected outputs are:

```json
{
    "message": "Hello Johnny"
}
```

and:

```json
{
    "message": "Hello World"
}
```

respectively.

When you run `fn test`, it will run your function with the inputs provided
and compare them against the expected outputs. If they don't match, it will fail.

## Let's Try It

![user input](../images/userinput.png)
>```sh
>fn init --runtime go tester
>cd tester
>cat test.json
>```

You'll see the same file as above. 

Now run:

![user input](../images/userinput.png)
> `fn test`

You should see the following results:

```
Test 1
PASSED -    ( 1.262317046s )

Test 2
PASSED -    ( 1.449515441s )

2 tests passed, 0 tests failed.
```

## Add another test

Let's add another test to show you how it's done. Add the following test to `test.json` and save it:

```json
{
    "input": {
        "body": {
            "name": "Jane"
        }
    },
    "output": {
        "body": {
            "message": "Hello Jane"
        }
    }
}
```

Now run `fn test` again. That's it!

## Wrapping Up

Congratulations! In this tutorial you learned how to write function tests to ensure you don't break your function contract.

**Go:** [Back to Contents](../README.md)
