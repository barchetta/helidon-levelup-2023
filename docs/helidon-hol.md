
# Helidon Virtual Threads Hands On Lab

In this lab you will build, run and modify microservices using the Helidon Nima WebServer
available in Helidon 4. Nima is written from the ground up to leverage Java 19's virtual
threads and provides a simple blocking programming model. You will also see how it compares to
more complex reactive programming, plus experience the ease of running a MicroProfile
based service on top of virtual threads.

## Prerequisits:

* Java 19
* Maven 3.8
* curl

Verify your prerequisites:
```
java --version
mvn --version
curl --version
```

## Part 1: Setup OCI

Not covered in this outline

## Part 2: Helidon WebServer: Nima (blocking) vs SE (reactive)

Helidon is a Java framework for writing microservices. In Helidon 3 we support a reactive
set of APIs called Helidon SE. In Helidon 4 we support blocking APIs called Helidon Nima
that are based on Java 19 virtual threads.

In this lab you will see the benefits of using the simple blocking APIs enabled by
virtual threads vs more complex reactive APIs.

1. Download and unzip Helidon lab example
   1. https://github.com/barchetta/helidon-levelup-2023/archive/refs/heads/main.zip
   2. This example contains two applications (microservices). The first is based on 
      Helidon Nima WebServer with virtual threads. The second is based on Helidon's Reactive 
      WebServer that uses Netty and an event loop with traditional threads.
2. Build, run and exercise each of the applications
   1. Build both apps from top `mvn clean package -DskipTests`
   2. Run the blocking (Nima) version of the app
      1. `java --enable-preview -jar nima/target/example-nima-blocking.jar`
      2. Write down the port number on which the server runs (see log entry for `@default`)
      3. In a separate window, try `curl http://localhost:<port>/one`
      4. Try other paths such as `/sequence` and `/parallel` 
         1. Notice the order of the results and the time to complete each curl command
         2. As suggeted by their names, the first resource invokes a remote resource multiple 
            times in sequence, while the second does the same invocation in parallel.
   3. Repeat the same process for the reactive (SE) version
      1. `java --enable-preview -jar reactive/target/example-nima-reactive.jar`
      2. (repeat steps above)
3. Browse the source 
   1. See how endpoints are implemented in each version of the app. Edit these two files:
      1. `nima/src/main/java/io/examples/helidon/nima/BlockingService.java`
      2. `reactive/src/main/java/io/examples/helidon/nima/BlockingService.java`
   3. See that reactive code is more complicated than blocking (Virtual Thread)
      1. Check methods `sequence` and `parallel` in `BlockingService` and `ReactiveService` respectively. See if you understand how they work!
4. Modify the `one` endpoint
   1. Edit `BlockService` and `ReactiveService` method `one` replacing the empty line by
      `System.out.println("## one " + Thread.currentThread());`
   2. Rebuild, rerun and verify server output when accessing `/one` on each application
      1. For Nima the thread will be a `VirtualThread`, for reactive it will be a Netty platform thread: `Thread[#33,nioEventLoopGroup-3-1,10,main]` 
      2. You may block a VirtualThread, but you must not block a Netty event loop thread. This means the Nima request handlers can use simple blocking code, but the reactive handlers must not.
5. Stack traces in reactive and blocking apps are very different
   1. Run each of the applications as shown in previous steps
   2. Force an exception by running the following command (`count` must be an integer!)
      1. curl http://localhost:57955/parallel?count=foo
      2. Compare stack traces for each app

## Part 3: MicroProfile Running on Virtual Threads

Helidon also supports MicroProfile, a set of industry standard APIs for writing microservices.
MicroProfile is based on a small set of Jakarta EE APIs plus some additional MicroProfile
specific APIs.

In this lab you will start with a Helidon 3 application running on our original reactive
Web Server based on Netty. You will then migrate the application to Helidon 4 running on the
new Helidon Nima WebServer using virtual threads.

1. Go to https://helidon.io/starter
   1. Select "Helidon MP", click "Next"
   2. Select "Quickstart", click "Next"
   3. Select "Jackson", click "Next"
   4. Click "Download" . Here and in the rest of the tutorial "myproject" will be used as the name of your project.
2. Open Project
   1. Uzip "myproject.zip" to a desired location
   2. Open the project in your favorite IDE if desired.
3. Build and run the project locally
   1. In the root of the project run `mvn clean package` to build the project
   2. Run the application: `java -jar target/myproject.jar`
   3. In another window exercise the application
      ```
      curl -X GET http://localhost:8080/simple-greet
      {"message":"Hello World!"}
      curl -X GET http://localhost:8080/greet
      {"message":"Hello World!"}
      curl -X GET http://localhost:8080/greet/Joe
      {"message":"Hello Joe!"}
      ```
    4. Stop the application with `CTRL-C`
4. Edit the file `src/main/java/com/example/myproject/GreetResource.java` 
   1. Find the method  `createResponse(String who)`
   2. Add this line as the first line in the method:
      ```
      System.out.println("Running on thread " + Thread.currentThread());
      ```
   3. Rebuild, run and exercise the application as described in the previous step.
   4. Note that the thread is named `helidon-server-n`. This is a platform thread in a 
      threadpool created by Helidon to handle JAX-RS requests.
   5. Stop the application with `CTRL-C`
5. Now change the project to use 4.0.0-ALPHA4
   1. Edit `pom.xml` and change the parent pom from `3.1.N`. to `4.0.0-ALPHA4`:
      ```
              <version>4.0.0-ALPHA4</version>
      ````
   2. Edit `src/main/resources/logging.properties` and change `io.helidon.common.HelidonConsoleHandler` to `io.helidon.logging.jul.HelidonConsoleHandler`
6. Your application has now been migrated to Helidon 4! Build the application as described previously.
7. Run the application with `java --enable-preview  -jar target/myproject.jar`
8. Note that the thread is now a `VirtualThread`. 
9. Stop the application with `CTRL-C`

### Deploy to OCI

Not covered in this outline

