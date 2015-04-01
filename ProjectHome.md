Jetlang provides a high performance java threading library. The library is based upon [Retlang](http://code.google.com/p/retlang).

The library is a complement to the [java.util.concurrent package](http://java.sun.com/javase/6/docs/api/java/util/concurrent/package-frame.html) introduced in 1.5 and should be used for [message based concurrency](http://en.wikipedia.org/wiki/Message_passing) similar to [event based actors in Scala](http://lamp.epfl.ch/~phaller/doc/haller07actorsunify.pdf).

The library does not provide remote messaging capabilities. It is designed specifically for high performance in-memory messaging.

### Features ###
  * All messages to a particular Fiber are delivered sequentially. Components can easily keep state without synchronizing data access or worrying about thread races.
  * Single Fiber interface that can be backed by a dedicated thread or a thread pool.
  * Supports single or multiple subscribers for messages.
  * Subscriptions for single events or event batching
  * Single or recurring event scheduling
  * High performance design optimized for low latency and high scalability
  * Publishing is thread safe, allowing easy integration with other threading models.
  * Low Lock Contention - Minimizing lock contention is critical for performance. Other concurrency solutions are limited by a single lock typically on a central thread pool or message queue. Jetlang is optimized for low lock contention. Without a central bottleneck, performance easily scales to the needs of the application.
  * <a href='http://code.google.com/p/jetlang/source/browse/trunk/src/test/java/org/jetlang/channels/MemoryRequestChannelTest.java'>Powerful Async Request/Reply Support</a>
  * Single jar with no dependencies except the jdk (1.6+)
  * Integrates with any JVM language - [jruby](http://github.com/reevesg/jretlang), [scala](http://www.jroller.com/mrettig/entry/scala_actors_implemented_with_jetlang), clojure, groovy, etc
  * Distributed Messaging - Jetlang [Remoting](Remoting.md)

### Example ###
```
Fiber fiber = new ThreadFiber();
fiber.start();
final CountDownLatch latch = new CountDownLatch(2);
Runnable toRun = new Runnable(){
   public void run(){
      latch.countDown();
   }
};
//enqueue runnable for execution
fiber.execute(toRun);
//repeat to trigger latch a 2nd time
fiber.execute(toRun);

latch.await(10, TimeUnit.SECONDS);

//shutdown thread
fiber.dispose();
```

### Channel Example ###
```
        // start thread backed receiver. 
        // Lighweight fibers can also be created using a thread pool
        Fiber receiver = new ThreadFiber();
        receiver.start();

        // create java.util.concurrent.CountDownLatch to notify when message arrives
        final CountDownLatch latch = new CountDownLatch(1);

        // create channel to message between threads
        Channel<String> channel = new MemoryChannel<String>();

        Callback<String> onMsg = new Callback<String>() {
            public void onMessage(String message) {
                //open latch
                latch.countDown();
            }
        };
        //add subscription for message on receiver thread
        channel.subscribe(receiver, onMsg);

        //publish message to receive thread. the publish method is thread safe.
        channel.publish("Hello");

        //wait for receiving thread to receive message
        latch.await(10, TimeUnit.SECONDS);

        //shutdown thread
        receiver.dispose();
```

### [Browse the Api](http://jetlang.googlecode.com/svn/site/apidocs/index.html) ###

### [More Examples](http://code.google.com/p/jetlang/source/browse/trunk/src/test/java/org/jetlang/examples/BasicExamples.java) ###