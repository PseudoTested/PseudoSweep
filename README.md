# PseudoSweep

PseudoSweep is a tool to identify Pseudo-tested statements and methods in Java code. 

The Tool Demonstration files and script can be found at [pseudosweep-demo](https://github.com/PseudoTested/pseudosweep-demo) and a video demonstration below.

[![PseudoSweep Video Demonstration](https://img.youtube.com/vi/5QCsu7MbiXI/0.jpg)](https://www.youtube.com/watch?v=5QCsu7MbiXI)

## Requirements

PseudoSweep can run with projects written in Java 12 or earlier. (PseudoSweep
requires JavaParser, which only supports up to Java 12.) Currently, PseudoSweep 
cannot handle `var` declarations as it requires explicit type declarations for 
instrumentation. 

PseudoSweep itself has been developed and tested with JDK 17 and Gradle 8.6.

## Building PseudoSweep

To build PseudoSweep, you will need to install the latest version of
[Java](https://adoptium.net). The 
[Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper_basics.html) 
can be used without installing [Gradle](https://gradle.org/install/). 
Build a JAR file (on Linux or OSX) using:

```
./gradlew build
```
See [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper_basics.html) 
for instructions for Windows machines. 
## Running PseudoSweep

To see this project in action, you can run it using the demo example in
[pseudosweep-demo](https://github.com/PseudoTested/pseudosweep-demo)
repository. The following example is included in the demo repo. 

### Updating the Project's POM File

In the project's `pom.xml` file, add the following with the
`<dependencies>...</dependencies>` tag (creating one if it does not exist). Then
paste in the following:

```
        <dependency>
            <groupId>org.pseudosweep</groupId>
            <artifactId>pseudosweep</artifactId>
            <version>0.0.1</version>
            <scope>system</scope>
            <systemPath>${env.PSWCP}</systemPath>
        </dependency>
```

You must set your `PSWCP` environment variable to point to PseudoSweep's JAR file.

In the following, you must also **ensure the JAR file is on your classpath**.

Currently, the tool identifies pseudo-tested statements and methods. This is achieved by 
running each command with a flag corresponding to your desired level (statement `-sdl` 
or method `-xmt`). Each command for a given level must be run using the corresponding flag. 
Each command must be run for a given level before switching flags; otherwise, the 
instrumentation will not correspond with the studied level.

### Instrumenting the Project Files

Ensure the JAR file generated by Gradle for PseudoSweep is on your classpath.

Then, from the root directory of the pseudosweep demo project, instrument the
`Triangle.java` class to find pseudo-tested statements with the following command:

```
java org.pseudosweep.Launch instrument -f src/main/java/examples/triangle/Triangle.java -sdl
```

It is also possible to skip `trivial` methods using the `-st` as pre-defined within the tool. This 
feature is currently under development.

You can also use the `-p` switch to instrument all files on a particular path.
and its subdirectories, e.g.:

```
java org.pseudosweep.Launch instrument -p src/main/ -sdl
```

You then need to recompile your instrumented code. Since the example class is
part of the PseudoSweep project, you can achieve this by rebuilding the examples
project with:

```
./gradlew clean build
```

### Sweeping Coverage

Then, run the tests with PseudoSweep to sweep their effective coverage. To do
this you need to run the following command from the root directory of the
examples project, ensuring the compiled project and its tests are on the
classpath:

```
java org.pseudosweep.Launch sweep -f target/test-classes/examples/triangle/TriangleJUnit4Test.class -sdl
```

Again you can use the `-p` switch, which will include all test classes on a path
and its subdirectories:

```
java org.pseudosweep.Launch sweep -p target/test-classes/ -sdl
```

Check the output in the `PS-data` folder, where reports are written.

### Analyzing Results
Finally, to compile the results of pseudo-tested classes, run the _analyze_ command.

```
java org.pseudosweep.Launch analyze -sdl
```

The output will be in the `PS-data` folder under `analysis`. The output `json` files will highlight the elements that are pseudo-tested. 


[//]: # ()
[//]: # (You can also specify which metrics you would like in the reports using the `-me` switch. For example;)

[//]: # ()
[//]: # (```)

[//]: # ( java org.pseudosweep.Launch analyze -me IF_THEN -me IF_ELSE)

[//]: # (```)

[//]: # (Otherwise, all metrics will be included.)

### Removing Instrumentation

You can remove the instrumentation from the example with:

```
java org.pseudosweep.Launch restore -f src/test/java/examples/triangle/Triangle.java -sdl
```

### For More Information on Running PseudoSweep

To find out more about ways of running PseudoSweep, use the `--help` option:

```
java org.pseudosweep.Launch --help
```
