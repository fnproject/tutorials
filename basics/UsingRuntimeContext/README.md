# Using the Fn RuntimeContext with Functions
In addition to the normal variables you use in function creation, Fn allows you to pass variable data, created by you, into your function.  This data, along with other automatically generated information, is converted into environment variables and made available to your function's runtime context. This tutorial covers how to set your own variables and use them in a function.

## Function Scenario
In this tutorial, you create a `cfg-fn` function that displays configuration data passed to the function. For the example, let's use some database information that we might need in a function.

* `DB_HOST_URL` displays the hostname and path to the database.
* `DB_USER` is the user name used to connect to the database.
* `DB_PASSWORD` is the password for our database user.

> As you make your way through this tutorial, look out for this icon.
![User input icon](images/userinput.png) Whenever you see it, it's time for you to
perform an action.


## Using Config Variables and Environment Variables
Fn config variables can be set for applications or functions. In addition, Fn automatically generates a number of environment variables for your use.

* **Application Config Variables:** Variables stored in an application are available to all functions that are deployed to that application.
* **Function Config Variables:** Variables stored for a function are only available to that function.
* **Pre-defined environment variables:** By default, a number of environment variables are automatically generated in an Fn Docker image. The section that follows details the automatically generated variables.

#### Default Environment Variables
Here is the list of automatically generated environment variables that are available to your functions.

|Fn Generated Var|Sample Value|Description|
|----------------|------------|-----------|
|FN_APP_ID|01NNNNNNNNNG8G00GZJ0000001|The application ID for the app the current function is contained in.|
|FN_FN_ID|01DYNNNNNNNG8G00GZJ0000002|The ID of the current function|
|FN_FORMAT|http-stream|(Deprecated). Communications protocol.|
|FN_LISTENER|unix:/tmp/iofs/lsnr.sock|The Unix socket address (prefixed with "unix:") on the file system that the FDK should create to listen for requests. The platform will guarantee that this directory is writable to the function. FDKs must not write any other data than the unix socket to this directory.|
|FN_MEMORY|128|The maximum memory of the function in MB.|
|FN_TYPE|sync|The type of function. Always `sync` currently.|



### Where to Set our Config Variables
For this tutorial, let's set the `DB_HOST_URL` at the application level and the `DB_USER`, and `DB_PASSWORD` variables at the function level. That gives us a little practices setting both types of variables.


## Create the Sample Configuration Function.
Ensure you have the Fn server running to host your function.

(1) Start the server.

![User input icon](images/userinput.png)
```sh
% fn start --log-level DEBUG
```

(2) Create the `<lang>-cfg-app` for your function. Replace `<lang>` with the language you are using for your function.

![User input icon](images/userinput.png)

```sh
% fn c a java-cfg-app
```

(3) Create a boilerplate `cfg-fn` function.

![User input icon](images/userinput.png)
```sh
% fn init --runtime java cfg-fn
```

(4) Change into the `cfg-fn` directory.

![User input icon](images/userinput.png)
```sh
% cd cfg-fn
```

(5) Remove the `src/test` directory so we don't have to update our tests.

![User input icon](images/userinput.png)
```sh
% rm -r src/test
```

(6) Add the the `DB_HOST_URL` variable to `<lang>-cfg-app`.

![User input icon](images/userinput.png)
```sh
% fn cf a java-cfg-app DB_HOST_URL //myhost/mydb
```

(7) Verify the value has been added.

![User input icon](images/userinput.png)
```sh
% fn ls cf a java-cfg-app
KEY         VALUE
DB_HOST_URL //myhost/mydb
```


(8) Modify the "Hello World!" boiler plate example (located in src -> main -> java -> com -> example -> fn -> HelloFunction.java) as described in the next section.

(9) **Deploy** and **invoke** your function locally. (**Note:** You must complete the code changes described in the next section to get the output shown.)

![User input icon](images/userinput.png)
```sh
% fn -v dp --app java-cfg-app --local
```

![User input icon](images/userinput.png)
```sh
% fn iv cfg-app cfg-fn
```

Your output should be similar to:
```yaml
Hello world!
DB Host URL: //myhost/mydb
DB User: your-db-account
DB Passwd: your-db-password
```

## Update your Function
### Adding the Fn RuntimeContext to your Java Function
Update the `HelloFunction` class as shown below. An explanation of the changes follows below.

**cfg-fn: HelloFunction.java**
```java
package com.example.fn;

import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;


public class HelloFunction {
	/* Vars for Env Variables */
	private String dbHost;		// DB_HOST
	private String dbUser;		// DB_USER
	private String dbPassword;	// DB_PASSWORD
	
	@FnConfiguration
	public void config(RuntimeContext ctx) {
		
		dbHost = ctx.getConfigurationByKey("DB_HOST_URL").orElse("//localhost/DBName");
		
		dbUser = ctx.getConfigurationByKey("DB_USER").orElse("your-db-account");
				
		dbPassword = ctx.getConfigurationByKey("DB_PASSWORD").orElse("your-db-password");
		
	}
	
    public String handleRequest(String input) {
    	String resultStr = "";
        String name = (input == null || input.isEmpty()) ? "world"  : input;

        resultStr = resultStr + "Hello " + name + "!\n";
        resultStr = resultStr + "DB Host URL: " + dbHost + "\n";
        resultStr = resultStr + "DB User: " + dbUser + "\n";
        resultStr = resultStr + "DB Passwd: " + dbPassword + "\n";
        
        return resultStr;
    }

}
```

First, we import a couple of FDK for Java classes.

```java
import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;
```

The `com.fnproject.fn.api.FnConfiguration` class allows you to annotate methods to be used for configuration. This allows you to setup and configure variables before the `handleRequest` method is run. **Note:** This annotation can be applied to more than one method if you wish.

The `com.fnproject.fn.api.RuntimeContext` class gives you access to the environment variables set automatically or created by the Fn CLI. We can pass a `RuntimeContext` variable to a method and gain access to all the available environment variables.


The following method is an example of how the `RuntimeContext` can be passed to a method and used to initialize data.

```java
@FnConfiguration
public void config(RuntimeContext ctx) {
    
    dbHost = ctx.getConfigurationByKey("DB_HOST_URL").orElse("//localhost/DBName");
    
    dbUser = ctx.getConfigurationByKey("DB_USER").orElse("your-db-account");
            
    dbPassword = ctx.getConfigurationByKey("DB_PASSWORD").orElse("your-db-password");
    
}
```

The `config` method uses the `RuntimeContext` to initialize the `dbHost`, `dbUser`, and `dbPassword` variables. String optionals are used to set default variables just in case the variables we are looking for are not set. Additional methods may be annotated this way if more configuration is required.

Completing the code changes above produces the output shown above where `DB_HOST_URL` has been set to `//myhost/mydb`.  


## Add the Function Variables and Retest
With our function setup and deployed, let's add some function variables to see if our output changes. Add the `DB_USER` and `DB_PASSWORD` variables to your function config and re-test your function.

(1) Add the `DB_USER` variable to the `cfg-fn` function.

![User input icon](images/userinput.png)
```sh
% fn cf f java-cfg-app cfg-fn DB_USER mydbuser
```

(2) Verify the value has been added.

![User input icon](images/userinput.png)
```sh
% fn ls cf f java-cfg-app cfg-fn
KEY     VALUE
DB_USER mydbuser
```

(3) Add the `DB_PASSWORD` variable to the `java-cfg-fn` function.

![User input icon](images/userinput.png)
```sh
% fn cf f java-cfg-app cfg-fn DB_PASSWORD mydbpassword
```

(4) Verify the value has been added.

![User input icon](images/userinput.png)
```sh
% fn ls cf f java-cfg-app cfg-fn
KEY             VALUE
DB_PASSWORD     mydbpassword
DB_USER         mydbuser
```

(9) Invoke your function again.

![User input icon](images/userinput.png)
```sh
% fn iv java-cfg-app cfg-fn
```

Your output should be similar to:
```sh
Hello world!
DB Host URL: //myhost/mydb
DB User: mydbuser
DB Passwd: mydbpassword
```

Notice your function immediately picks up and uses the variables. You don't need to redeploy the function or make any other modifications. The variables are picked up and injected into the Docker instance when the function is invoked.


## Setting Variables with Fn YAML Files
In addition to using the CLI to set Fn variables for your `RuntimeContext`, you can set them in Fn YAML configuration files.

### Create a new Function
Next, create a new function that will display environment variables. Follow the steps (1) thru (5) above, but this time, name the function `env-fn`. Be sure the `env-fn` has the same parent directory as the `cfg-fn` function. (The source code for this function is included in the `code` directory for this tutorial listed by language.) 

### Create an Java env-fn Function
Update the hello function with the following Java code:

**env-fn: HelloFunction.java**
```java
package com.example.fn;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import com.fnproject.fn.api.RuntimeContext;

public class HelloFunction {

    public String handleRequest(String input, RuntimeContext ctx) {
        Map<String, String> environmentMap = ctx.getConfiguration();
        SortedMap<String, String> sortedEnvMap = new TreeMap<>(environmentMap);
        Set<String> keySet = sortedEnvMap.keySet();
        
        String outStr  = "---\n";
        
        for (String key : keySet) {
        	String value = environmentMap.get(key);
        	outStr = outStr + ( key + ": " + value + "\n");
        }
        
        return outStr;
    }

}
```

This code just displays all of the environment variables set inside the Docker container the function runs in.

### Add key/value pairs to a Function's func.yaml File
Edit the function's `func.yaml` file for `env-fn` as follows:

**func.yaml**
```yaml
schema_version: 20180708
name: env-fn
version: 0.0.1
runtime: java
build_image: fnproject/fn-java-fdk-build:jdk11-1.0.104
run_image: fnproject/fn-java-fdk:jre11-1.0.104
cmd: com.example.fn.HelloFunction::handleRequest
config:
    funcKey1: funcValue1
    funcKey2: funcValue2
```

Save the file. Adding a `config:` section to the `func.yaml` file allows you to specify key/value pairs within the file.

**Deploy** and **invoke** your function locally from the `env-fn` directory.

![User input icon](images/userinput.png)
```sh
% fn -v dp --app java-cfg-app --local
```

![User input icon](images/userinput.png)
```sh
% fn iv java-cfg-app cfg-fn
```

Your output should be similar to:
```yaml
FN_APP_ID: 01DZ564ZT6NG8G00GZJ0000001
FN_FN_ID: 01DZ565WYGNG8G00GZJ0000002
FN_FORMAT: http-stream
FN_LISTENER: unix:/tmp/iofs/lsnr.sock
FN_MEMORY: 128
FN_TYPE: sync
HOME: /home/fn
HOSTNAME: cd27aa54fd89
JAVA_BASE_URL: https://github.com/AdoptOpenJDK/openjdk11-upstream-binaries/releases/download/jdk-11.0.5%2B10/OpenJDK11U-jre_
JAVA_HOME: /usr/local/openjdk-11
JAVA_URL_VERSION: 11.0.5_10
JAVA_VERSION: 11.0.5
LANG: C.UTF-8
PATH: /usr/local/openjdk-11/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
funcKey1: funcValue1
funcKey2: funcValue2
```

Notice the two `funcKey` values we set are at the end of the output.

### Add key/value Pairs to the app.yaml File
If you put all of your functions under the same parent directory, you can setup an `app.yaml` file to hold configuration data. For example, see one of the lanaguage examples in the `code` directory stored with this tutorial. The `app.yaml` file for Java looks like this:

**app.yaml**
```yaml
name: java-cfg-app
config:
  appKey1: appValue1
  appKey2: appValue2
```

Create an `app.yaml` file in the parent directory of your two functions. You can copy the values from the above `app.yaml` file.

**Deploy** and **invoke** your function locally from the parent directory of your functions. Notice the deploy syntax is a little different when using an `app.yaml` file.

![User input icon](images/userinput.png)
```sh
% fn -v dp -all --local
```

The command deploys all functions under the parent directory to the application specified in `app.yaml`. Now invoke the new function.

![User input icon](images/userinput.png)
```sh
% fn iv java-cfg-app env-fn
```

Your output should be similar to:
```yaml
---
FN_APP_ID: 01DZ564ZT6NG8G00GZJ0000001
FN_FN_ID: 01DZ565WYGNG8G00GZJ0000002
FN_FORMAT: http-stream
FN_LISTENER: unix:/tmp/iofs/lsnr.sock
FN_MEMORY: 128
FN_TYPE: sync
HOME: /home/fn
HOSTNAME: cd27aa54fd89
JAVA_BASE_URL: https://github.com/AdoptOpenJDK/openjdk11-upstream-binaries/releases/download/jdk-11.0.5%2B10/OpenJDK11U-jre_
JAVA_HOME: /usr/local/openjdk-11
JAVA_URL_VERSION: 11.0.5_10
JAVA_VERSION: 11.0.5
LANG: C.UTF-8
PATH: /usr/local/openjdk-11/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
appKey1: appValue1
appKey2: appValue2
funcKey1: funcValue1
funcKey2: funcValue2
```

Voila! Both functions are deployed in one step and the new application variables will be available to any function in this app.


## Summary
You have set variables using the Fn CLI and Fn YAML configuration files. You then accessed application and function variables in a Java function using the Java `RuntimeContext`. Fn makes it easy to store configuration data locally and use it in your functions.

For more information see the [configuration vars documentation page](https://github.com/fnproject/docs/blob/master/fn/develop/configs.md). See also [Function Configuration and Initialization](https://github.com/fnproject/docs/blob/master/fdks/fdk-java/FunctionConfiguration.md) for other examples of how to access the variables and the `RuntimeContext` in a function.
