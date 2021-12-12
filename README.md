## Overview

Jacomax (Java Connector for Maxima) is a basic Java interface for the Maxima computer algebra system.

Jacomax is open source, with the 3-clause BSD license.

The software was initially developed as part of the Jisc MathAssess and FETLAR projects and
is now a standalone open source project, hosted on GitHub.

## Getting Jacomax

### Download

Jacomax can be found on GitHub at https://github.com/davemckain/jacomax.

### Downloading a binary distribution

* You can download the latest binary distribution from https://github.com/davemckain/jacomax/releases.
  Currently, this is a single ZIP bundle containing the main Jacomax JAR, some additional JARs for
  running the examples and diagnostics, plus all required (and some optional) dependencies.
* If you use Apache Maven, Jacomax can be downloaded as part of your build process by declaring it as a dependency as follows:
    ```
    <dependency>
      <groupId>uk.ac.ed.ph.jacomax</groupId>
      <artifactId>jacomax</artifactId>
      <version>0.2.3</version>
    </dependency>
    ```
  
All official releases of Jacomax may be downloaded in this fashion.
(Note that you will need to add my Maven repository to your project; its URL is 
https://www2.ph.ed.ac.uk/maven2.)

### Building from source

Jacomax uses Git for version control. It is hosted on GitHub at
https://github.com/davemckain/jacomax.

Jacomax uses Apache Maven for project build and management, so you will first need to
download and install Maven if you haven't already done so.

Consult the Maven documentation and read the pom.xml file to find out about the main goals
you can use. Note that Jacomax uses a multi-module layout.

## Using Jacomax

### Requirements

You obviously first need to have Maxima installed! Jacomax was only been tested with 
fairly recent (at the time!) versions of Maxima (5.16.x and above).

The main Jacomax code itself is in **jacomax-nnn.jar**, where nnn is the current version number,
and you will need to ensure this is in the compile-time classpath of any project you want
to use Jacomax in.

Jacomax uses the [SLF4J](http://www.slf4j.org/) logging framework, so you will also need to 
have some SLF4J JARs in your runtime ClassPath:

* **slf4j-api-nnn.jar** (required in all cases)
* and exactly ONE of the following, depending on your own logging requirements:
  * **slf4j-simple-nnn.jar** (if you want to use simple console logging)
  * **slf4j-log4j12-nnn.jar** and **log4j-nnn.jar** (if you're using [Log4J 1.2.x](http://logging.apache.org/log4j/1.2/)
  * **slf4j-nop-nnn.jar** (if you don't want any logging)
  * ... or indeed any of the other options listed in the SLF4J manual
 
The Jacomax binary distribution contains **slf4j-api-nnn.jar** and **slf4j-simple-nnn.jar**.
You may need to add in the additional SLF4J JARs to match your logging requirements by downloading the
entire SLF4J binary distribution from the SLF4J download page, or directly via its Maven repository.
(Note this behaviour changed slightly in the 0.2.4 release, which switched from using Log4J to slf4j-simple
as the default logging option.)

## How Jacomax works

Jacomax works by running Maxima processes and communicating with them through their standard
input and output handles, so it is arguably little more than a thin layer on top of some
routine I/O logic!

## The Jacomax API

(All Jacomax classes described below are in the **uk.ac.ed.ph.jacomax** package, 
unless otherwise stated.)

### MaximaConfiguration

The first thing you need to use Jacomax is a
[MaximaConfiguration](https://github.com/davemckain/jacomax/blob/master/jacomax/src/main/java/uk/ac/ed/ph/jacomax/MaximaConfiguration.java)
Object. This is a plain old Java Object that tells Jacomax how to invoke and run Maxima.

The most important property here is **maximaExecutablePath**, which tells Jacomax which
Maxima binary to run. Consult the API docs or source code for other properties.

Filling in a MaximaConfiguration will of course depend on the platform the code
is being run on, so you would normally want to avoid hard-coding a specific
MaximaConfiguration into your code. To this end, Jacomax provides a few utility
classes to help you here:

#### JacomaxPropertiesConfigurator

This class constructs a **MaximaConfiguration** by reading information in from a
standard Java properties file. This can be done in a number of ways, but the simplest
is to use the default constructor of JacomaxPropertiesConfigurator, which searches
for a file called **jacomax.properties** firstly in the current directory
(as defined by the **user.dir** System property), then in your User's home directory
(as defined by the **user.home** System property) and finally in the ClassPath. 
(The search order can be changed if required.)

If you want to use this method of configuration, you will find a file called 
**jacomax.properties.sample** at the top of the source tree (and in binary distributions)
that you can use as a blank template.

#### JacomaxAutoConfigurator

JacomaxAutoConfigurator looks for Maxima in "standard" place depending on the
Operating System being run on. It requires no explicit configuration and will
probably work very well with vanilla Maxima installs on Windows, Mac OS X,
Linux and other Unix-like operating systems. It won't work so well for custom
installs or if you want to use a specific Lisp runtime or do something more advanced,
in which case you probably would be better using **JacomaxPropertiesConfigurator**.

This class is used via a simple static method:

```java
MaximaConfiguration config = JacomaxAutoConfigurator.guessMaximaConfiguration();
```

An unchecked **MaximaConfigurationException** is thrown if this process didn't succeed.

#### JacomaxSimpleConfigurator

**JacomaxSimpleConfigurator** gives you the best of both of the above worlds.
By default, it first uses **JacomaxPropertiesConfigurator** to look for an explicit
**jacomax.properties** file, falling back on **JacomaxAutoConfigurator** if that doesn't work.
(The ordering can be changed if required.)

Usage:

```java
MaximaConfiguration config = JacomaxSimpleConfigurator.configure();
```

An unchecked **MaximaConfigurationException** is thrown if this process didn't succeed.

### Calling Maxima

The main gateway to calling Maxima is the MaximaProcessLauncher class.
You create it using a MaximaConfiguration. You can then do two types of things:

1. Run a **MaximaInteractiveProcess**. This creates a new Maxima process and lets you
   submit a series of "calls" to it and get a result for each of them.
1. Perform a "batch" run whereby Maxima's input is fed from an InputStream and its
   output is sent to an OutputStream.

### MaximaInteractiveProcess

Here is a simple example:

```java
MaximaConfiguration configuration = JacomaxSimpleConfigurator.configure();
MaximaProcessLauncher launcher = new MaximaProcessLauncher(configuration);
MaximaInteractiveProcess process = launcher.launchInteractiveProcess();
System.out.println(process.executeCall("1+2;"));
process.terminate();
```

This will run a Maxima process, get it to evaluate "1+2;", printing the raw output
to the console. In this case, it would consist of an output label and the number 3
formatted as it would be inside Maxima. (There are some utility methods for parsing
the outputs as well; see below.)

You can execute as many calls as you like. Jacomax will do some rudimentary checking
to make sure the call ends with a ';' or '$' character (or with ')' if it saw a ':lisp'
in your input).

Jacomax runs each call with a "timeout" value associated to it, which specifies a
time limit in seconds for the call to complete running. If a timeout is exceeded,
the underlying Maxima process will be terminated and a **MaximaTimeoutException**
is thrown. Any further calls made to a MaximaInteractiveProcess after this time will
result in a **MaximaProcessTerminatedException** (which is a subclass of **IllegalStateException**). 
A timeout can be passed explicitly for each call, or set for the
**MaximaInteractiveProcess** itself, and an initial timeout is inherited from the **MaximaConfiguration**.
A timeout of zero (which results if you don't configure anything) is interpreted as "use a sensible default".
A negative timeout indicates that the call should be allowed to run indefinitely... use with care!!

The **softReset()** method of **MaximaInteractiveProcess** executes the Maxima call **[kill(all),reset()]**,
which resets most of Maxima's state. Consult the Maxima documentation for more information on this.

### Batch mode

Use the various **MaximaProcessLauncher.runBatchProcess()** methods for this. 
All take an **InputStream** and an **OutputStream**, both of which you are responsible for closing.
Timeouts are supported in the same way as **MaximaInteractiveProcess**,
though configured via a different property as you would probably want to allow batch processes
to run longer than a single call.

## Examples

The **jacomax-samples-nnn.jar** contains a selection of examples that you can try out. 
It probably makes sense to download (or view) the source code so that you can see what is going on.

You can run these samples on the command line with something like:

```sh
$ java -classpath jacomax-nnn.jar:jacomax-samples-nnn.jar:slf4j-api-nnn.jar:slf4j-simple-nnn.jar uk.ac.ed.ph.jacomax.samples.InteractiveProcessExample
```
where nnn should be substituted with the version numbers found in your ZIP distribution.
(You will get a **ClassNotFoundException** if your ClassPath is not set correctly here.)

If you downloaded the source and are using Maven, you can also run this by changing into the
jacomax-samples folder and typing:

```sh
$ mvn compile exec:exec -Dexample.class=uk.ac.ed.ph.jacomax.samples.InteractiveProcessExample
```

## Troubleshooting

Maxima runs on a number of different Lisp runtimes atop a number of different Operating Systems,
hence there are inevitably going to be issues trying to get Jacomax to work in some cases!

To this end, the jacomax-samples.jar contains a simple Class that attempts to run Maxima
and execute a trivial call, outputting very detailed logging messages in the process.

If you downloaded the binary distribution, you can run this with:

```sh
$  java -classpath jacomax-nnn.jar:jacomax-samples-nnn.jar:slf4j-api-nnn.jar:slf4j-simple-nnn.jar uk.ac.ed.ph.jacomax.diagnostics.JacomaxDiagnostic
```

where nnn should be substituted with the version numbers found in your ZIP distribution.
(You will get a **ClassNotFoundException** if your ClassPath is not set correctly here.)

If you downloaded the source and are using Maven, you can also run this by changing into the
jacomax-samples folder and typing:

```sh
$ mvn compile exec:exec -Dexample.class=uk.ac.ed.ph.jacomax.diagnostics.JacomaxDiagnostic
```

Regardless of how you run it, you should see a lot of debugging output appear,
which may or may not help you diagnose any problems.

If you need support because you can't get Jacomax to work, please run this diagnostic
and send me the complete output at the same time.

## The test suite
Jacomax has a small test suite that can be run (from source) with:

```sh
$ mvn test
```

All tests should pass. If a test fails for you, please contact me about following this up.

## Tested platforms

I've tested successfully Jacomax on the following setups using **JacomaxSimpleConfigurator**
so far:

* Linux (RHEL4)/CLISP 2.46/Maxima 5.16.3
* Linux (RHEL4)/CLISP 2.48/Maxima 5.20.1
* Linux (Scientific Linux 5.3)/SBCL 1.0.30/Maxima 5.20.1
* Linux (Scientific Linux 7.x)/Maxima 5.41.1
* Mac OS X (10.6)/SBCL 1.0.29/Maxima 5.19.3 (standard download, installed in /Applications)
* Mac OS X (10.6)/SBCL 1.0.29/Maxima 5.21.1 (standard download, installed in /Applications)
* Mac OS X (10.6)/SBCL 1.0.29/Maxima 5.18.1 (via MacPorts)
* Mac OS X (10.15)/SBCL 2.1.9/Maxima 5.45.1 (via MacPorts)
* Windows XP (SP 3)/GCL 2.6.8/Maxima 5.20.1 (standard download)
