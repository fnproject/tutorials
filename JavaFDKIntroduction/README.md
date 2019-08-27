# Introduction to Fn with Java

This tutorial introduces the
[Fn Java FDK (Function Development Kit)](https://github.com/fnproject/fdk-java).
If you haven't completed the [Introduction to Fn](../Introduction/README.md)
tutorial you should head over there before you proceed.

This tutorial takes you through the Fn developer experience for building
Java functions. It shows how easy it is to build, deploy and test
functions written in Java.

As you make your way through this tutorial, look out for this icon.
![](images/userinput.png) Whenever you see it, it's time for you to
perform an action.

### Before you Begin
* Set aside about 30 minutes to complete this tutorial.
* Make sure Fn server is up and running by completing the [Install and Start Fn Tutorial](../install/README.md).
    * Make sure you have set your Fn context registry value for local development. (for example, "fndemouser". [See here](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context).)


## Your First Function

Let's start by creating a new function.  In a terminal type the following:

![](images/userinput.png)
>`fn init --runtime java javafn`

The output will be:

```sh
Creating function at: ./javafn
Function boilerplate generated.
func.yaml created.
```

![](images/userinput.png)
>```sh
>cd javafn
>```

The `fn init` command creates an simple function with a bit of boilerplate to get you
started. The `--runtime` option is used to indicate that the function
we're going to develop will be written in Java 9, the default version
as of this writing. A number of other runtimes are also supported.  

__If__ you have the `tree` utility installed
you can see the directory structure that the `init` command has created.

![](images/userinput.png)
>`tree`

```sh
.
├── func.yaml
├── pom.xml
└── src
    ├── main
    │   └── java
    │       └── com
    │           └── example
    │               └── fn
    │                   └── HelloFunction.java
    └── test
        └── java
            └── com
                └── example
                    └── fn
                        └── HelloFunctionTest.java

11 directories, 4 files
```


As usual, the init command has created a `func.yaml` file for your
function but in the case of Java it also creates a Maven `pom.xml` file
as well as a function class and function test class.

Take a look at the contents of the generated func.yaml file.

![](images/userinput.png)
>```sh
>cat func.yaml
>```

```yaml
schema_version: 20180708
name: javafn
version: 0.0.1
runtime: java
build_image: fnproject/fn-java-fdk-build:jdk11-1.0.100
run_image: fnproject/fn-java-fdk:jre11-1.0.100
cmd: com.example.fn.HelloFunction::handleRequest
```

The generated `func.yaml` file contains metadata about your function and
declares a number of properties including:

* schema_version--identifies the version of the schema for this function file.
* name--Name of your function and directory.
* version--the version of the function.
* runtime--the language used for this function.
* build_image--the image used to build your function's image.
* run_image--the image your function runs in.
* cmd--the `cmd` property is set to the fully qualified name of the function
class and the method that should be invoked when your `javafn` function is
called.

The Java function init also generates a Maven `pom.xml` file to build and test your function.  The pom includes the Fn Java FDK runtime and test libraries your function needs.


## Deploy your Java Function

With the `javafn` directory containing `pom.xml` and `func.yaml` you've got
everything you need to deploy the function to Fn server. This server could be
running in the cloud, in your datacenter, or on your local machine like we're
doing here.

Make sure your context is set to default and you are using a demo user. Use the `fn list contexts` command to check.

![user input](images/userinput.png)
>```sh
> fn list contexts
>```

```cs
CURRENT	NAME	PROVIDER	API URL			        REGISTRY
*       default	default		http://localhost:8080	fndemouser
```

If your context is not configured, please see [the context installation instructions](https://github.com/fnproject/tutorials/blob/master/install/README.md#configure-your-context) before proceeding. Your context determines where your function is deployed.

### Create an App
Next, functions are grouped together into an application. The application acts as the main organizing structure for multiple functions. To create an application type the following:

![user input](images/userinput.png)
>```sh
> fn create app java-app
>```

A confirmation is returned:

```yaml
Successfully created app:  java-app
```

Now `java-app` is ready for functions to be deployed to it.

### Deploy your Function to your App
Deploying your function is how you publish your function and make it accessible
to other users and systems. To see the details of what is happening during a
function deploy,  use the `--verbose` switch.  The first time you build a
function of a particular language it takes longer as Fn downloads the necessary
Docker images. The `--verbose` option allows you to see this process.

![](images/userinput.png)
>```sh
> fn --verbose deploy --app java-app --local
>```

```yaml
Deploying javafn to app: java-app
Bumped to version 0.0.2
Building image fndemouser/javafn:0.0.2
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon  14.34kB
Step 1/11 : FROM fnproject/fn-java-fdk-build:jdk11-1.0.100 as build-stage
jdk11-1.0.100: Pulling from fnproject/fn-java-fdk-build
1ab2bdfe9778: Pull complete
7aaf9a088d61: Pull complete
b9283b89acb2: Pull complete
16677eca0612: Pull complete
5b4cb6528d6a: Pull complete
170b0f62f6c7: Pull complete
65c78033cc54: Pull complete
ac64120fa016: Pull complete
974d72e5031c: Pull complete
0b2992d79dc1: Pull complete
aa6278e1bf2c: Pull complete
5ef836f5ad65: Pull complete
Digest: sha256:46dcd238a984da488131c4726a32fe4ec67b686f798b6e4506cddd9939f5d10d
Status: Downloaded newer image for fnproject/fn-java-fdk-build:jdk11-1.0.100
 ---> 5686a17e235e
Step 2/11 : WORKDIR /function
 ---> Running in d2e67ce2f29e
Removing intermediate container d2e67ce2f29e
 ---> 1f39b9acf702
Step 3/11 : ENV MAVEN_OPTS -Dhttp.proxyHost= -Dhttp.proxyPort= -Dhttps.proxyHost= -Dhttps.proxyPort= -Dhttp.nonProxyHosts= -Dmaven.repo.local=/usr/share/maven/ref/repository
 ---> Running in 4db208b67bb6
Removing intermediate container 4db208b67bb6
 ---> c398d3e86671
Step 4/11 : ADD pom.xml /function/pom.xml
 ---> 7ee0d858e8eb
Step 5/11 : RUN ["mvn", "package", "dependency:copy-dependencies", "-DincludeScope=runtime", "-DskipTests=true", "-Dmdep.prependGroupId=true", "-DoutputDirectory=target", "--fail-never"]
 ---> Running in 2bad89832851
[INFO] Scanning for projects...
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.3/maven-compiler-plugin-3.3.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.3/maven-compiler-plugin-3.3.pom (11 kB at 22 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-plugins/27/maven-plugins-27.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-plugins/27/maven-plugins-27.pom (11 kB at 344 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/26/maven-parent-26.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/26/maven-parent-26.pom (40 kB at 1.0 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.3/maven-compiler-plugin-3.3.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.3/maven-compiler-plugin-3.3.jar (46 kB at 1.3 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-deploy-plugin/2.7/maven-deploy-plugin-2.7.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-deploy-plugin/2.7/maven-deploy-plugin-2.7.pom (5.6 kB at 144 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-deploy-plugin/2.7/maven-deploy-plugin-2.7.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-deploy-plugin/2.7/maven-deploy-plugin-2.7.jar (27 kB at 639 kB/s)
[INFO]
[INFO] ------------------------< com.example.fn:hello >------------------------
[INFO] Building hello 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
Downloading from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/api/1.0.100/api-1.0.100.pom
Downloaded from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/api/1.0.100/api-1.0.100.pom (0 B at 0 B/s)
Downloading from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/fdk/1.0.100/fdk-1.0.100.pom
Downloaded from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/fdk/1.0.100/fdk-1.0.100.pom (0 B at 0 B/s)
Downloading from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/testing-core/1.0.100/testing-core-1.0.100.pom
Downloaded from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/testing-core/1.0.100/testing-core-1.0.100.pom (0 B at 0 B/s)
Downloading from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/runtime/1.0.100/runtime-1.0.100.pom
Downloaded from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/runtime/1.0.100/runtime-1.0.100.pom (0 B at 0 B/s)
Downloading from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/testing-junit4/1.0.100/testing-junit4-1.0.100.pom
Downloaded from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/testing-junit4/1.0.100/testing-junit4-1.0.100.pom (0 B at 0 B/s)
Downloading from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/api/1.0.100/api-1.0.100.jar
Downloading from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/runtime/1.0.100/runtime-1.0.100.jar
Downloading from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/testing-junit4/1.0.100/testing-junit4-1.0.100.jar
Downloading from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/testing-core/1.0.100/testing-core-1.0.100.jar
Downloaded from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/testing-core/1.0.100/testing-core-1.0.100.jar (0 B at 0 B/s)
Downloaded from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/testing-junit4/1.0.100/testing-junit4-1.0.100.jar (0 B at 0 B/s)
Downloaded from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/runtime/1.0.100/runtime-1.0.100.jar (0 B at 0 B/s)
Downloaded from fn-release-repo: https://dl.bintray.com/fnproject/fnproject/com/fnproject/fn/api/1.0.100/api-1.0.100.jar (0 B at 0 B/s)
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ hello ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /function/src/main/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.3:compile (default-compile) @ hello ---
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting-api/2.2.1/maven-reporting-api-2.2.1.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting-api/2.2.1/maven-reporting-api-2.2.1.pom (1.9 kB at 74 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting/2.2.1/maven-reporting-2.2.1.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting/2.2.1/maven-reporting-2.2.1.pom (1.4 kB at 58 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-sink-api/1.1/doxia-sink-api-1.1.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-sink-api/1.1/doxia-sink-api-1.1.pom (2.0 kB at 85 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia/1.1/doxia-1.1.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia/1.1/doxia-1.1.pom (15 kB at 632 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-logging-api/1.1/doxia-logging-api-1.1.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-logging-api/1.1/doxia-logging-api-1.1.pom (1.6 kB at 66 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.2/commons-cli-1.2.pom
Downloaded from central: https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.2/commons-cli-1.2.pom (8.0 kB at 319 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-parent/11/commons-parent-11.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-parent/11/commons-parent-11.pom (25 kB at 795 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-utils/0.7/maven-shared-utils-0.7.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-utils/0.7/maven-shared-utils-0.7.pom (5.0 kB at 179 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/20/maven-shared-components-20.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/20/maven-shared-components-20.pom (5.1 kB at 196 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-api/2.5/plexus-compiler-api-2.5.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-api/2.5/plexus-compiler-api-2.5.pom (865 B at 32 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler/2.5/plexus-compiler-2.5.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler/2.5/plexus-compiler-2.5.pom (5.3 kB at 222 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-components/1.3.1/plexus-components-1.3.1.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-components/1.3.1/plexus-components-1.3.1.pom (3.1 kB at 123 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-manager/2.5/plexus-compiler-manager-2.5.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-manager/2.5/plexus-compiler-manager-2.5.pom (690 B at 20 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-javac/2.5/plexus-compiler-javac-2.5.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-javac/2.5/plexus-compiler-javac-2.5.pom (769 B at 27 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compilers/2.5/plexus-compilers-2.5.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compilers/2.5/plexus-compilers-2.5.pom (1.3 kB at 38 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-utils/0.7/maven-shared-utils-0.7.jar
Downloading from central: https://repo.maven.apache.org/maven2/com/google/code/findbugs/jsr305/2.0.1/jsr305-2.0.1.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-manager/2.5/plexus-compiler-manager-2.5.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-javac/2.5/plexus-compiler-javac-2.5.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-api/2.5/plexus-compiler-api-2.5.jar
Downloaded from central: https://repo.maven.apache.org/maven2/com/google/code/findbugs/jsr305/2.0.1/jsr305-2.0.1.jar (32 kB at 393 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-container-default/1.5.5/plexus-container-default-1.5.5.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-utils/0.7/maven-shared-utils-0.7.jar (170 kB at 2.0 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-classworlds/2.2.2/plexus-classworlds-2.2.2.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-manager/2.5/plexus-compiler-manager-2.5.jar (4.6 kB at 51 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/xbean/xbean-reflect/3.4/xbean-reflect-3.4.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-javac/2.5/plexus-compiler-javac-2.5.jar (19 kB at 217 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/log4j/log4j/1.2.12/log4j-1.2.12.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-api/2.5/plexus-compiler-api-2.5.jar (25 kB at 251 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/commons-logging/commons-logging-api/1.1/commons-logging-api-1.1.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-classworlds/2.2.2/plexus-classworlds-2.2.2.jar (46 kB at 341 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/com/google/collections/google-collections/1.0/google-collections-1.0.jar
Downloaded from central: https://repo.maven.apache.org/maven2/commons-logging/commons-logging-api/1.1/commons-logging-api-1.1.jar (45 kB at 290 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/junit/junit/3.8.2/junit-3.8.2.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/xbean/xbean-reflect/3.4/xbean-reflect-3.4.jar (134 kB at 731 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-container-default/1.5.5/plexus-container-default-1.5.5.jar (217 kB at 1.1 MB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/junit/junit/3.8.2/junit-3.8.2.jar (121 kB at 541 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/log4j/log4j/1.2.12/log4j-1.2.12.jar (358 kB at 1.3 MB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/com/google/collections/google-collections/1.0/google-collections-1.0.jar (640 kB at 2.1 MB/s)
[INFO] No sources to compile
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ hello ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /function/src/test/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.3:testCompile (default-testCompile) @ hello ---
[INFO] No sources to compile
[INFO]
[INFO] --- maven-surefire-plugin:2.22.1:test (default-test) @ hello ---
[INFO] Tests are skipped.
[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ hello ---
[WARNING] JAR will be empty - no content was marked for inclusion!
[INFO] Building jar: /function/target/hello-1.0.0.jar
[INFO]
[INFO] --- maven-dependency-plugin:2.8:copy-dependencies (default-cli) @ hello ---
[INFO] Copying api-1.0.100.jar to /function/target/com.fnproject.fn.api-1.0.100.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.436 s
[INFO] Finished at: 2019-08-27T16:12:02Z
[INFO] ------------------------------------------------------------------------
Removing intermediate container 2bad89832851
 ---> cb5aac9c0127
Step 6/11 : ADD src /function/src
 ---> 2657414a13e9
Step 7/11 : RUN ["mvn", "package"]
 ---> Running in 4a5348470166
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------< com.example.fn:hello >------------------------
[INFO] Building hello 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ hello ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /function/src/main/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.3:compile (default-compile) @ hello ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /function/target/classes
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ hello ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /function/src/test/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.3:testCompile (default-testCompile) @ hello ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /function/target/test-classes
[INFO]
[INFO] --- maven-surefire-plugin:2.22.1:test (default-test) @ hello ---
[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.fn.HelloFunctionTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.287 s - in com.example.fn.HelloFunctionTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ hello ---
[INFO] Building jar: /function/target/hello-1.0.0.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.523 s
[INFO] Finished at: 2019-08-27T16:12:08Z
[INFO] ------------------------------------------------------------------------
Removing intermediate container 4a5348470166
 ---> b4a0bec68b67
Step 8/11 : FROM fnproject/fn-java-fdk:jre11-1.0.100
jre11-1.0.100: Pulling from fnproject/fn-java-fdk
1ab2bdfe9778: Already exists
7aaf9a088d61: Already exists
b9283b89acb2: Already exists
1e2c32308970: Pull complete
3c43fe67926d: Pull complete
36e6981d5b2d: Pull complete
450dbf503d31: Pull complete
751d235f8f5e: Pull complete
Digest: sha256:349157b433e01686c2e5d5258b57f1cd3adf0b96931b5eed1dee992afd40e2c5
Status: Downloaded newer image for fnproject/fn-java-fdk:jre11-1.0.100
 ---> d41fcf9cdae1
Step 9/11 : WORKDIR /function
 ---> Running in 573e39859346
Removing intermediate container 573e39859346
 ---> eb1482ca1480
Step 10/11 : COPY --from=build-stage /function/target/*.jar /function/app/
 ---> de0462bef344
Step 11/11 : CMD ["com.example.fn.HelloFunction::handleRequest"]
 ---> Running in 6ff3b337334f
Removing intermediate container 6ff3b337334f
 ---> e76791e5f3d5
Successfully built e76791e5f3d5
Successfully tagged fndemouser/javafn:0.0.2

Updating function javafn using image fndemouser/javafn:0.0.2...
Successfully created function: javafn with fndemouser/javafn:0.0.2
```

All the steps to load the current language Docker image are displayed.

Specifying `--app java-app` explicitly puts the function in the application "java-app".

Specifying `--local` does the deployment to the local server but does
not push the function image to a Docker registry--which would be necessary if
we were deploying to a remote Fn server.

The output message
`Updating function javafn using image fndemouser/javafn:0.0.2...`
let's us know that the function is packaged in the image
"fndemouser/javafn:0.0.2".

Note that the containing folder name `javafn` was used as the name of the
generated Docker container and used as the name of the function that
container was bound to.

Normally you deploy an application without the `--verbose` option. If you rerun the command a new image and version is created and loaded.


## Invoke your Deployed Function

Use the the `fn invoke` command to call your function from the command line.

### Invoke with the CLI

The first is using the Fn CLI which makes invoking your function relatively
easy.  Type the following:

![user input](images/userinput.png)
>```sh
> fn invoke java-app javafn
>```

which results in:

```txt
Hello, world!
```

In the background, Maven compiles the code and runs any tests, the function is
packaged into a container, and then the function is run to produce the output
"Hello, world!".

You can also pass data to the invoke command. For example:

![user input](images/userinput.png)
>```sh
> echo -n 'Bob' | fn invoke java-app javafn
>```

```txt
Hello, Bob!
```

"Bob" was passed to the function where it is processed and returned in the output.


## Exploring the Code

We've generated, compiled, deployed, and invoked the Java function so let's take
a look at the code.  You may want to open the code in your favorite IDE or
editor.

Below is the generated `com.example.fn.HelloFunction` class.  As you can
see the function is just a method on a POJO that takes a string value
and returns another string value, but the Java FDK also supports binding
input parameters to streams, primitive types, byte arrays and Java POJOs
unmarshalled from JSON.  Functions can also be static or instance
methods.

```java
package com.example.fn;

public class HelloFunction {

    public String handleRequest(String input) {
        String name = (input == null || input.isEmpty()) ? "world"  : input;

        return "Hello, " + name + "!";
    }

}
```

This function returns the string "Hello, world!" unless an input string
is provided in which case it returns "Hello, &lt;input string&gt;!".  We saw
this previously when we piped "Bob" into the function.   Notice that
the Java FDK reads from standard input and automatically puts the
content into the string passed to the function.  This greatly simplifies
the function code.

## Testing with JUnit

The `fn init` command also generated a JUnit test for the function which uses
the Java FDK's function test framework.  With this framework you can setup test
fixtures with various function input values and verify the results.

The generated test confirms that when no input is provided the function returns "Hello, world!".

```java
package com.example.fn;

import com.fnproject.fn.testing.*;
import org.junit.*;

import static org.junit.Assert.*;

public class HelloFunctionTest {

    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();

    @Test
    public void shouldReturnGreeting() {
        testing.givenEvent().enqueue();
        testing.thenRun(HelloFunction.class, "handleRequest");

        FnResult result = testing.getOnlyResult();
        assertEquals("Hello, world!", result.getBodyAsString());
    }

}
```

Let's add a test that confirms that when an input string like "Bob" is
provided we get the expected result.

Add the following method to `HelloFunctionTest`:

```java
    @Test
    public void shouldReturnWithInput() {
        testing.givenEvent().withBody("Bob").enqueue();
        testing.thenRun(HelloFunction.class, "handleRequest");

        FnResult result = testing.getOnlyResult();
        assertEquals("Hello, Bob!", result.getBodyAsString());
    }
```

You can see the `withBody()` method used to specify the value of the
function input.

You can run the tests by building your function with `fn build`.  This
will cause Maven to compile and run the updated test class.  You can also invoke your tests directly from Maven using `mvn test` or from your IDE.

![](images/userinput.png)
>`fn build`

```sh
Building image fndemouser/javafn:0.0.2 .......
Function fndemouser/javafn:0.0.2 built successfully.
```

## Accepting JSON Input

Let's convert this function to use JSON for its input and output.
Replace the definition of `HelloFunction` with the following:

```java
package com.example.fn;

public class HelloFunction {

    public static class Input {
        public String name;
    }

    public static class Result {
        public String salutation;
    }

    public Result handleRequest(Input input) {
        Result result = new Result();
        result.salutation = "Hello " + input.name;
        return result;
    }

}
```

We've created a couple of simple Pojos to bind the JSON input and output
to and changed the function signature to use these Pojos.  The
Java FDK will automatically bind input data based on the Java arguments
to the function. JSON support is built-in but input and output binding
is extensible and you could plug in marshallers for other
data formats like `protobuf`, `avro` or `xml`.

Let's build the updated function:

![](images/userinput.png)
>`fn build`

returns:

```sh
Building image fndemouser/javafn:0.0.2 .....
Error during build. Run with `--verbose` flag to see what went wrong. eg: `fn --verbose CMD`

Fn: error running docker build: exit status 1

See 'fn <command> --help' for more information. Client version: 0.5.16
```

To find out what happened rerun build with the verbose switch:

![](images/userinput.png)
>`fn --verbose build`

```sh
...
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.example.fn.HelloFunctionTest
An exception was thrown during Input Coercion: Failed to coerce event to user function parameter type class com.example.fn.HelloFunction$Input
...
An exception was thrown during Input Coercion: Failed to coerce event to user function parameter type class com.example.fn.HelloFunction$Input
...
Tests run: 2, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 0.893 sec <<< FAILURE!
...
Results :

Tests in error:
  shouldReturnGreeting(com.example.fn.HelloFunctionTest): One and only one response expected, but 0 responses were generated.
  shouldReturnWithInput(com.example.fn.HelloFunctionTest): One and only one response expected, but 0 responses were generated.

Tests run: 2, Failures: 0, Errors: 2, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.477 s
[INFO] Finished at: 2017-09-21T14:59:21Z
[INFO] Final Memory: 16M/128M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.12.4:test (default-test) on project hello: There are test failures.
```

Oops! as we can see this function build has failed due to test failures--we
changed the code significantly but didn't update our tests!  We really
should be doing test driven development and updating the test first but
at least our bad behavior has been caught.  Let's update the tests
to reflect our new expected results.  Replace the definition of
`HelloFunctionTest` with:

```java
package com.example.fn;

import com.fnproject.fn.testing.*;
import org.junit.*;

import static org.junit.Assert.*;

public class HelloFunctionTest {

    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();

    @Test
    public void shouldReturnGreeting(){
        testing.givenEvent().withBody("{\"name\":\"Bob\"}").enqueue();
        testing.thenRun(HelloFunction.class,"handleRequest");

        FnResult result = testing.getOnlyResult();
        assertEquals("{\"salutation\":\"Hello Bob\"}", result.getBodyAsString());
    }
}
```

In the new `shouldReturnGreeting()` test method we're passing in the
JSON document

```js
{ "name": "Bob" }
```
and expecting a result of
```js
{ "salutation": "Hello Bob" }
```

If you re-run the test via `fn -verbose build` we can see that it now passes:

![](images/userinput.png)
>`fn --verbose build`

Redeploy your updated Java function

![user input](images/userinput.png)
>```sh
> fn deploy --app java-app --local
>```

## Invoke with Curl

The other way to invoke your function is via HTTP. With the changes to the code,
we can pass JSON and return JSON from the the function.  The Fn server exposes
our deployed function at system produced endpoint. Next, we need to look up the invoke endpoint for our function.

### Getting a Function's Invoke Endpoint

In addition to using the Fn `invoke` command, we can call a function by using a
URL. To do this, we must get the function's invoke endpoint. Use the command
`fn inspect function <appname> <function-name>`.  To list the `javafn` function's
invoke endpoint we can type:

![user input](images/userinput.png)
>```sh
> fn inspect function java-app javafn
>```

```js
{
	"annotations": {
		"fnproject.io/fn/invokeEndpoint": "http://localhost:8080/invoke/01DK9XJSCXNG8G00GZJ0000002"
	},
	"app_id": "01DK9XFXNXNG8G00GZJ0000001",
	"created_at": "2019-08-27T16:12:15.645Z",
	"id": "01DK9XJSCXNG8G00GZJ0000002",
	"idle_timeout": 30,
	"image": "fndemouser/javafn:0.0.3",
	"memory": 128,
	"name": "javafn",
	"timeout": 30,
	"updated_at": "2019-08-27T16:21:25.928Z"
}
```

The output confirms that `javafn` function's invoke endpoint is:
`http://localhost:8080/invoke/01DK9XJSCXNG8G00GZJ0000002`. We can use this URL
to call the function.

### Curl Commands
Use `curl` to invoke the function:

![user input](images/userinput.png)
>```sh
> curl -X "POST" -H "Content-Type: application/json" http://localhost:8080/invoke/01DK9XJSCXNG8G00GZJ0000002
>```

The result is now in a JSON format.

```js
{"salutation":"Hello world"}
```

**Note:** Currently an error occurs if you pass an empty value to the JSON enabled function. See [FDK-Java Issue 148](https://github.com/fnproject/fdk-java/issues/148) for details.

We can pass JSON data to our function and get the value of name passed to
the function back.

![user input](images/userinput.png)
>```
> curl -X "POST" -H "Content-Type: application/json" -d '{"name":"Bob"}' http://localhost:8080/invoke/01DK9XJSCXNG8G00GZJ0000002
>```

The result is now in JSON format with the passed value returned.

```js
{"salutation":"Hello Bob"}
```



## Wrap Up

Congratulations! You've just completed an introduction to the Fn Java
FDK.  There's so much more in the FDK than we can cover in a brief
introduction but we'll go deeper in subsequent tutorials.

**Go:** [Back to Contents](../README.md)
