##Probes Open API

###API Instrumentation
There are a number reasons why you would consider calling the Probes Open API directly from your application code.
* You wish to have complete control over which packages, classes and methods that are instrumented.
* You don’t want to incur any overhead at startup using a bytecode instrumentation (BCI) agent.
* You already have existing hooks built into your application code base to insert this instrumentation more efficiently.
* You would like to give probes names that reflect more of the context of the execution.
* You need to instrument particular code blocks in a large method. _Most BCI agents don’t offer this granularity._

Finally, you can use the Open API to create software that is self-aware, able to self-reflect on its own execution behavior at various levels and phases in its processing, even if the actual instrumentation is performed by a BCI agent.

###API Style
The Open API has a single class, `Probes`, that contains a number of enclosed interfaces such as `Context`, `Probe`, `Meter`, `Name`, and `Reading`. This is done to allow us to bootstrap the metering runtime off the static initialization of the `Probes` class. It also helps avoid name collisions in manually instrumenting existing code. Many code bases would have a `Name` class but how many would have `Probes.Name`? Without this, the complete package, `org.jinspired.probes`, would have to be listed alongside the class name in any field or variable declaration.

The API style also makes it incredibly simple to switch in an alternative implementation via a service provider interface (SPI). It also gives a third party vendor the ultimate flexibility in its implementation, which can be tailored to a specific runtime, environment or platform.

###API Introduction
Before exploring the Open API lets look at how instrumentation is typically coded today in measuring the performance of method named a within a class named `A`.

    private static final Logger LOGGER = Logger.getLogger(A.class);
      ...
      long start = System.nanoTime();
      try {
         ...
      } finally {
        logger.log(Level.INFO, "A.a took [{0}] ns to execute", System.nanoTime() - start);
      }

Unfortunately the above does not write a log record until after the method has completed. This can be addressed with trace methods.

    private static final Logger LOGGER = Logger.getLogger(A.class);
      ...
      LOGGER.entering("A", "a");
      long start = System.nanoTime();
      try {
        ...
      } finally {
        LOGGER.exiting("A", "a", System.nanoTime() - start);
      }

Now lets look at how this is performed using the Probes Open API.

    private static final Probes.Name NAME = Probes.name(A.class).name("a");
      ...
      Probes.Probe p = Probes.begin(NAME);
      try {
        ...
      } finally {
        p.end();
      }

With a wildcard `static import` on the `Probes` class the code looks like.

    private static final Name NAME = name(A.class).name("a");
      ...
      Probe p = begin(NAME);
      try {
        ...
      } finally {
        p.end();
      }

The biggest and the most important difference in the approach taken by the Probes Open API is that the developer coding the instrumentation does not specify how the probe is measured (metered). The probe could be measured with clock time, cpu time or some other resource related measure. It can employ one measure, many measures, or none at all. The point is that such measurement decisions should not be hardwired into code. The developer should only be responsible for the naming and demarcation of the code block that needs to measured, even controlled, by those responsible for monitoring and managing its execution in production.

The Probes Open API is designed to be always on whereas the above `Logger` calls only ever involves the creation of records when the logging level has been set to `FINER` or lower. Once the appropriate Level is set in the Logging API (or its configuration file) the performance impact of such calls is significant which seems the worst possible result considering these would be turned on to find a problem not introduce one.

The benefit of this approach includes not just the reduction in boilerplate code, in not calling `System.nanoTime()` from multiple places, but cost (overhead) management as the underlying measurement engine can dynamically decide to not perform whatever measurement is configured based on the name of the probe, its past measurement, the current execution context or some other environment-related state attribute.

There are three main activities performed by a performance monitoring solution – **instrumentation**, **measurement**, and **collection**. With the Probes Open API approach, we allow for the last two of these to be optional and dynamic. The underlying implementation of the Probes Open API could perform the logger calls itself based on some measurement threshold. This is a concern for another person, role or time.

It is still possible to inspect what has been measured at the `begin()` and `end()` call points following completion of the probe.

    Iterator<Reading> rs = p.readings();
      while(rs.hasNext()) {
        Reading r = rs.next();
        r.getName(); // the name of the measure
        r.getLow(); // the begin measurement
        r.getHigh(); // the end measurement
        r.getDelta(); // high - low
      }

###API Concepts – Basic
In short, a `Probe` is created, with a specified `Name`, by a `Context` that is tied to a particular thread and its lifetime. The `Probe` reads one or more `Meter` measures at both the `begin()` and `end()` call points. The measures are maintained in a `Reading` object, one of for each `Meter`.

What transpires within the metering runtime in the course of firing a probe depends on the underlying implementation and its configuration. We simplify the instrumentation interface and allow for all the power and dynamism of the monitoring and management to be pushed down into the metering runtime and deferred until runtime.

#####Context
For every thread in the runtime that calls into the Open API, a `Context` is created and remains associated with the thread for its entire lifetime in the process. The `Context` is used to create a `Probe`. The `Probes.begin(Name)` method is an utility method that simply delegates to the `Context` by calling `context().begin(Name)`.

#####Probe
The `Probe` interface represents the interval based measurement instrument in an application’s code base. It reads the measure for a meter when the `begin()` method is called and reads it again when the `end()` method is called. What it does with the delta between these two call points depends entirely on the metering extensions enabled within the underlying metering runtime. Some extensions will use the data to create value distributions for the delta, others will record it (or log it) and some will use the interception call backs at these points to temporarily take control of the thread execution.

#####Meter
The `Meter` interface represents a resource measure that is read when `Probe.begin()` and `Probe.end()` are called. The actual underlying measure, which is generally specific to a thread, is not exposed in the interface.

The `Context.meters()` method can be used to iterate over each `Meter` included in the readings performed for a specific `Context`.

All implementations should support a wall clock time meter named `clock.time`. The resolution of the `clock.time` meter should be microseconds. Optionally implementors can offer a nanosecond time resolution meter named `clock.tick`.

#####Reading
The `Reading` interface represents a measurement of a `Meter` for a `Probe` at both `Probe.begin()` and `Probe.end()` call points. The `getHigh()` method returns the most recent measurement. The `getLow()` method returns the previous measurement.

The `Probe.readings()` method returns an immutable `Iterator<Reading>` for inspection purposes.

The `getName()` method returns the same `Name` instance that the corresponding `Meter` returns in its `getName()` method.

#####Name
A `Name` is an ordered composite of string values. Calling `Probes.parse("A.a")` will return a `Name` with a value of `"a"` and a `Name` prefix, obtained calling `getPrefix()`, with a value of `"A"`.

For performance reasons `Name` instances are interned, which means reference equality is applicable in the case of `Probes.parse("A.a") == Probes.name("A").name("a")`.

Whilst the `Probes` class offers utility methods to create `Name` instances from `Class` or `Method` parameters the names do not hold any reference to such code metadata. A benefit of this is that we can playback a metering recording by calling the Open API with the same name values used by the real application but without the class being actually present.

A `Name` can represent a Java `package`, Java `class`, Java method, Ruby module, Ruby class, Ruby call-site, a SQL statement, an HTTP URL and so on. The metering engine can be used to replay events from other languages and runtimes recorded in a log file. It could also be used to simulate behavior with a `Probe` representing an activity and a `Meter` a resource and a `Context` an actor, process or workflow lane.

#####Label
A `Label` is a classification that can be associated with one or more `Name` instances. It is used by the underlying metering runtime and its extensions to expose metadata associated with a `Name` during the course of the process execution. For example when a `Name` is created from a `Class` a `Label` with the string value `"class"` is associated with the particular `Name` instance. When the hotspot metering extension deems a probe as a performance hotspot it will associate a `Label` having a string value of `"hotspot"` with the `Probe`.

You can use the `labels()` method in the `Name` interface, which returns an `Iterator<Label>`, to inspect the associations made by the metering runtime.

The Open API does not offer the means to list all possible `Label` instances but you can look up a known `Label` by its string value and then check whether a particular `Name` instance has such a classification using the `Name.contains(Label)` method.

The Open API does not support the creation of a `Label` or the association of a `Label` with a `Name`.

All implementations should support the following labels.
* `java` – a `Name` created from a `Class` reference
* `class` – a `Name` created from a `Class` reference
* `disabled` – a `Name` disabled for all future measurement
* `probe` – a fully qualified `Name` used by a `Probe`

#####Environment
The `Environment` interface provides a means to share contextual data, scoped to the current thread and possibly individual frames on its probe stack, with extensions enabled in the metering runtime as well as with other probes on the stack. An `Environment` object is obtained by calling `Probes.context().getEnvironment()`.

For primitive and intrinsic data types there are pairs of `getXXX(Name)` and `setXXX(Name,XXX)` methods. There is also a `getXXX(Name,XXX) that allows the specification of a default value to be returned.

The use of the `Name` interface as a property key, with its ordered sequence of string value parts, allows the `Environment` to be used as a registry in that we can check whether a named property has been set by checking whether one of its `Name` prefixes exist within the `Environment` using the `contains(Name) method.

The `Environment` does not support the adding of `Object` values other than `String` and `Name` but using the registry like interface you can create sub-trees that represent more complex data structures and then use the `remove(Name)` on the root `Name` of the object to clear all child property values.

>**Note**: The [Autoletics](www.autoletics.com) implementation of the Open API for the JVM uses a global `Environment` instance that is not directly accessible from the Open API but that is used by thread local `Environment` instances when a value is not found within its own scope. Values are added to this global instance by an administrator via a configuration file in our case that file is `jxinsight.override.config`.

#####Counter
The `Counter` interface represents a resource counter that may be mapped, via some external configuration, by a metering runtime implementation to a `Meter` and then have its `getValue()` method called when a firing `Probe` is metered. A `Counter` is specific to a thread `Context`. To retrieve a `Counter` from a `Context` use the `counter(Name)` method which will automatically create the `Counter` if it has not already being created previously. A `Counter` is an incrementing only value - a requirement for any measure mapped to a meter.

    // hold a static constant reference to the name of the counter to be updated
    static final Probes.Name COUNTER = Probes.name(...).name(...);

    // increment by 1
    Probes.context().counter(COUNTER).inc();

    // increment by x
    Probes.context().counter(COUNTER).inc(x);

###API Concepts – Advance
The Open API serves two primary use cases. The first is to expose the software execution behavior, contextual and/or code based, to other systems which can monitor and manage the application more effectively than the actual language/platform runtime itself.

The second use case is to allow the application itself to self reflect on its own execution behavior. Java and many other languages especially dynamic allow state and type reflection but none behavior. Self reflection is achieved through a call combination of `Context.savepoint()` and `Context.compare(SavePoint)` that generates a set of measured changes between two thread execution points

Here is a code snippet demonstrating the use of all interfaces supporting this behavioral form of self reflection.

    Probes.Context ctx = Probes.context();

    Probes.SavePoint sp = ctx.savepoint();
    ...
    // execute some activity we wish to self reflect on
    ...
    Probes.ChangeSet cset = ctx.compare(sp);

    // we can now inspect the behavior that occurred between the savepoint() and compare() methods
    Iterator<Probes.ChangePoint> cps = cset.changepoints();
    while(cps.hasNext()) {

     ChangePoint cp = cps.next();
     Probes.Name pn = cp.getName(); // name of probe
     ...

     Iterator<Change> cs = cp.changes();
     while(cs.hasNext()) {

       Probes.Change c = cs.next();
       Probes.Name mn = cs.getName(); // name of meter
       c.getCount(); // how many meter measures aggregated
       c.getTotal();
       c.getInherentTotal();
       ...

      }
    }

#####SavePoint
The `SavePoint` interface represents an opaque snapshot of the current threads measurement data. It is created by calling `Context.savepoint()`. It is also possible to reuse a `SavePoint` instance, created by the same thread, by passing it in as a parameter to the method `Context.savepoint(SavePoint)` which returns either the `SavePoint` updated or a new `SavePoint`. Implementations are free to trade performance with possible increased safety in such cases.

#####ChangeSet
The `ChangeSet` interface represents a set of `ChangePoint` instances and `Change` instances generated from a delta analysis of a `SavePoint` with the current measurement data for a thread. A `ChangeSet` is returned from the `Context.compare(SavePoint)` method call.

#####ChangePoint
A `ChangePoint` represents a `Probe`, actually the `Name` of a `Probe`, that has been measured and its measurement data updated between two execution points. The name of the `Probe` is the value returned by the `ChangePoint.getName()` method.

#####Change
Where a `ChangePoint` represents a `Probe` a `Change` represents a `Reading` or the measurement that has occurred for a particular `Probe` and `Meter` pair. The name of the `Meter` is the value returned by the `Change.getName()` method.

###API Contract
Probably the most important contractual obligation, especially for portability across different implementations, is that there is no leakage or corruption of the probe stack by not calling `Probe.end()` for a `Probe` that has had `Probe.begin()` called.

Both probe call stack and measurement data corruption can occur when there is an overlapping of `Probe.begin()` and `Probe.end()` calls within a thread. Whilst nesting of `Probe.begin()` and `Probe.end()` calls is fully supported (and expected), both pairs of calls should be performed within the same possible outer (caller) probing scope.

>**Note**: [Autoletics](Autoletics.com) offers metering extensions that can help detect such violations in test and mitigate them in production if need be, though we recommend catching this before delivery to ensure the maximum possible performance especially in high frequency trading and online gaming environments.

The following call sequences violate the Open API client contract.

    Probes.Probe p = Probes.begin(...);
    p = Probes.begin(NAME);
    p.end();

    Probes.Probe p = Probes.begin(...);
    p.begin();
    p.end();

    Probes.Probe outer = Probes.begin(...);
    Probes.Probe inner = Probes.begin(...);
    outer.end();
    inner.end();

To ensure that for every `Probe.begin()` call there is a corresponding `Probe.end()` call you should employ a `try-finally` clause.

    final Probes.Probe p = Probes.begin(...);
    try {
      ...
    } finally {
      p.end();
    }

None of the interfaces should be implemented by the application. Instances of the interfaces passed back into the API should be the same implementation that was returned from the API. Implementations of the SPI and API should be able to assume that a `Name` reference passed in is of the same implementation type that is returned from one of the `Probes.name(...)` methods.

####API Optimizations
When invoking multiple times the utility methods in the `Probes` class within a specific code block it is more efficient to instead look up the `Context` once using the `Probes.context()` and then call the corresponding methods on this thread (local) specific instance.

Consider caching `Name` references in static fields instead of recreating them repeatedly during execution. Because `Name` instances are interned equality is referenced based.

###API Openness
Many open source libraries and frameworks claim to be open when in fact it is practically impossible to replace wholesale the implementation with another third-party implementation. Openness here is largely in terms of access to source code and it’s licensing and has nothing to do with the design of the framework or library itself. In the design of the Probes Open API, we make it incredibly easy to replace one implementation with another by having a service provider interface (SPI) used by the `Probes` class – the entry point into the library. This is also helped by the fact that we have only one single class, `Probes`, in the library used to bootstrap the appropriate implementation with everything else being an interface.

Whilst an open source license increases the likelihood of collaboration an Open API, as defined largely by an SPI, increases the chance of multiple competing implementations. An open source license improves a single implementation with baked-in engineering trade-offs. An Open API improves many implementations, allowing each to make a different set of engineering trade-offs. An Open API offers greater choice and vendor independence. Both forms of “open” are not necessarily incompatible but they are not the same.

###API SPI
The Open API provides the means to delegate calls to an alternative implementation via an Open SPI. Whilst our implementation is currently the default implementation chosen at runtime we plan to offer a barebones open source reference implementation under the `org.jinspired.probes.impl.*` namespace.

To use an alternative implementation the system property, `org.jinspired.probes.spi.factory`, must be set to the fully qualified name of a class implementing `org.jinspired.probes.spi.ProbesProviderFactory`, before the `Probes` class is initialized. Ideally, this should be done on the command line with `-Dorg.jinspired.probes.spi.factory=`.
