/*
 * Copyright © 2013 JINSPIRED BV (http://www.autoletics.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.jinspired.probes;

import org.jinspired.probes.spi.ProbesProvider;
import org.jinspired.probes.spi.ProbesProviderFactory;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;

/**
 * The {@link Probes} class is the entry point into the activity metering API.<p/>
 * <p>The Open API allows developers to instrument application code for the purpose of measuring consumption of various meters during the course of an activity's execution.</p>
 * <p>Metering of an activity is performed by a {@link Probes.Probe Probe} created by a {@link Probes.Context Context} which is tied to the lifetime of a thread.</p>
 * <p> The following sample code shows how to instrument a block of code. </p>
 * <pre>
 *    Probes.Probe p = Probes.begin(Probes.name("func"));
 *    try {
 *    // perform some expensive operation
 *    } finally { p.end(); }
 * </pre>
 * <p> A {@link Probes.Probe Probe} can be fired multiple times during the execution of units of work. </p>
 * <pre>
 *    Probes.Probe p = Probes.create(Probes.name("func"));
 *    for(int i=0; i < count; i++) {
 *      p.begin();
 *      try {
 *      // perform some expensive operation
 *      } finally { p.end(); }
 *    }
 * </pre>
 * @author William Louth
 */
public final class Probes {

  private static final ProbesProvider PROVIDER;

  static {
    final ProbesProviderFactory factory;

    try {

      factory = (ProbesProviderFactory) Class.forName((String) AccessController.doPrivileged(new PrivilegedAction() {
        public Object run() {
          return System.getProperty("org.jinspired.probes.spi.factory",
              "com.jinspired.jxinsight.server.probes.ProviderFactory");
        }
      })).newInstance();

    } catch (Throwable t) {
      throw new RuntimeException(t);
    }

    PROVIDER = factory.create();
    PROVIDER.init();

  }

  private Probes() {}

  /**
   * Returns the probe {@link Probes.Context Context} for the calling thread.
   *
   * @return The probe {@link Probes.Context Context} for the calling thread.
   */
  public static Probes.Context context() {
    return PROVIDER.context();
  }

  /**
   * Returns a {@link Probes.Meter Meter} associated with the provided name.
   *
   * @param name the {@link Probes.Meter} name
   * @return The matching {@link Probes.Meter Meter} instance
   * @throws NullPointerException if the name parameter is <tt>null</tt>
   */
  public static Probes.Meter meter(Probes.Name name) {
    return PROVIDER.meter(name);
  }

  /**
   * Returns a thread specific {@link Probes.Probe Probe} instance.
   *
   * @param name the name for the {@link Probes.Probe Probe}
   * @return A thread specific {@link Probes.Probe Probe} instance
   * @throws NullPointerException if the name parameter is <tt>null</tt>
   * @see Probes.Context#create(Probes.Name)
   */
  public static Probes.Probe create(Probes.Name name) {
    return PROVIDER.context().create(name);
  }

  /**
   * Returns a thread specific {@link Probes.Probe Probe} instance with {@link Probes.Probe#begin()} already issued.
   *
   * @param name the name for the {@link Probes.Probe Probe}
   * @return A thread specific {@link Probes.Probe Probe} instance
   * @throws NullPointerException if the name parameter is <tt>null</tt>
   * @see Probes.Context#begin(Probes.Name)
   * @see Probes.Probe#begin()
   */
  public static Probes.Probe begin(Probes.Name name) {
    return PROVIDER.context().begin(name);
  }

  /**
   * Meters the execution of a {@code java.lang.Runnable}.
   *
   * @param runnable the {@code java.lang.Runnable} to run
   * @return A thread specific {@link Probes.Probe Probe} instance used in metering the running of the {@code java.lang.Runnable}
   * @throws NullPointerException if the runnable parameter is <tt>null</tt>
   * @see Probes.Context#run(Runnable)
   * @see Probes.Probe#run(Runnable)
   */
  public static Probes.Probe run(Runnable runnable) {
    return PROVIDER.context().run(runnable);
  }

  /**
   * Returns a {@link Probes.Name Name} instance with a null name prefix and the value equal to the specified value parameter.
   *
   * @param value the string value of the name's part
   * @return A non-null {@link Probes.Name Name} object
   * @throws NullPointerException if the name parameter is <tt>null</tt>
   */
  public static Probes.Name name(String value) {
    return PROVIDER.name(value);
  }

  /**
   * Returns a {@link Probes.Name Name} instance with the name prefix representing the parsed package namespace and the unqualified class name as the value.
   *
   * @param cls the class for which a name will be created
   * @return A non-null {@link Probes.Name Name} object
   * @throws NullPointerException if the cls parameter is null
   */
  public static Probes.Name name(Class cls) {
    return PROVIDER.name(cls);
  }

  /**
   * Returns a {@link Probes.Name Name} instance with the name prefix representing the parsed fully qualified class name and the method name as the value.
   *
   * @param method the method for which a name will be created
   * @return A non-null {@link Probes.Name Name} object
   * @throws NullPointerException if the method parameter is null
   */
  public static Probes.Name name(Method method) {
    return PROVIDER.name(method);
  }

  /**
   * Returns a {@link Probes.Name Name} instance following the parsing of the string value with a delimiter of '.'
   *
   * @param value the value to be parsed
   * @return A non-null {@link Probes.Name Name} object
   * @throws NullPointerException if the value parameter is null
   */
  public static Probes.Name parse(String value) {
    return PROVIDER.parse(value);
  }

  /**
   * Returns a built-in {@link Probes.Label Label} mapped to a specified string value.
   *
   * @param value the value to be matched
   * @return The built-in {@link Probes.Label Label} mapped to the specified string value or null if not existing label matches.
   * @see Probes.Name#labels()
   * @see Probes.Name#contains(Probes.Label)
   */
  public static Probes.Label label(String value) {
    return PROVIDER.label(value);
  }

  /**
   * The {@link Probes.Name Name} interface represents an ordered sequence of name parts with an index starting from 0.
   */
  public interface Name {

    /**
     * The number of name parts.
     *
     * @return The number of name parts including the instance (i.e. number of prefixes + 1)
     */
    public int getLength();

    /**
     * The value of a name part.
     *
     * @param index the 0-based index of the name part to retrieve.
     * @return The value of a name part at the specified index
     * @throws IllegalArgumentException if the index value < 0 and >= {@link #getLength() getLength()}
     */
    public String valueAt(int index);

    /**
     * The {@link Probes.Name Name} instance prefixing this name.
     *
     * @return The prefixing {@link Probes.Name Name} instance or null
     */
    public Probes.Name getPrefix();

    /**
     * The value of this name.
     *
     * @return A non-null string value
     */
    public String getValue();

    /**
     * Returns a {@link Probes.Name Name} object that has this name instance as its prefixing name.
     *
     * @param value the value of the extended name returned
     * @return A non-null {@link Probes.Name Name} object that extends this name with the value provided
     * @throws NullPointerException if the value parameter is null
     */
    public Probes.Name name(String value);

    /**
     * Returns an immutable {@code java.util.Iterator<Probes.Label>} that allows navigation over the list of {@link Probes.Label Label} instances associated with the name.
     *
     * @return A {@code java.util.Iterator<Probes.Label>} over the name's {@link Probes.Label Label} instances.
     * @see Probes.Name#contains(Probes.Label)
     * @see #label(String)
     */
    public Iterator<Probes.Label> labels();

    /**
     * Returns <tt>true</tt> is the set of {@link Probes.Label Labels} associated with this {@link Probes.Name Name} contains the specified {@link Probes.Label Label}.
     *
     * @param label the label to be matched
     * @return <tt>true</tt> if the set of {@link Probes.Label Labels} associated with this {@link Probes.Name Name} contains the specified {@link Probes.Label Label}
     * @throws NullPointerException if the label parameter is null
     * @see Probes.Name#labels()
     * @see Probes#label(String)
     */
    public boolean contains(Probes.Label label);

    /**
     * Returns the first {@link Probes.Name Name} ancestor that has a name value equal to a specified value.
     *
     * @param value the name value part to be matched
     * @return The first {@link Probes.Name Name} ancestor that has a name value equal to a specified value or <t>null</t>.
     */
    public Probes.Name find(String value);

    /**
     * Returns true if the name is directly or indirectly prefixed by a specified name.
     *
     * @param name the name.
     * @return Returns true if the specified {@link Probes.Name Name} parameter is a direct or indirect prefix of this target name.
     */
    public boolean startsWith(Probes.Name name);

    /**
     * Returns true if the name has been labeled "disabled".
     *
     * @return <tt>true</tt> if the name has been labeled "disabled"
     */
    public boolean isDisabled();

  }

  /**
   * The {@link Probes.Probe Probe} interface represents a measurement device in the resource metering runtime.
   */
  public interface Probe {

    /**
     * The name of the {@link Probes.Probe Probe}.
     *
     * @return A non-null {@link Probes.Name Name} identifying the {@link Probes.Probe Probe}.
     */
    public Probes.Name getName();

    /**
     * Creates and updates all {@link Probes.Reading Reading} instances with the current {@link Probes.Meter Meter} values.
     */
    public void begin();

    /**
     * Updates all {@link Probes.Reading Reading} instances with the current {@link Probes.Meter Meter} values.
     */
    public void end();

    /**
     * Returns an immutable {@code java.util.Iterator<Probes.Reading>} that allows navigation over the list of meter {@link Probes.Reading Reading} instances created during the first invocation of {@link #begin()}.
     *
     * @return An immutable {@code java.util.Iterator<Probes.Reading>} over the {@link Probes.Probe Probe}'s meter {@link Probes.Reading Reading} instances
     */
    public Iterator<Probes.Reading> readings();

    /**
     * Returns the {@link Probes.Reading Reading} instance for a particular {@link Probes.Meter Meter}.
     *
     * @param name the name of the {@link Probes.Meter Meter}
     * @return The {@link Probes.Reading Reading} instance for a particular {@link Probes.Meter Meter} or <tt>null</tt>.
     * @throws NullPointerException if the name parameter is <tt>null</tt>
     */
    public Probes.Reading reading(Probes.Name name);

    /**
     * Meters the execution of a {@code java.lang.Runnable}.
     *
     * @param runnable the {@code java.lang.Runnable} to run
     * @throws NullPointerException if the runnable parameter is <tt>null</tt>
     * @see Probes.Context#run(Runnable)
     * @see Probes#run(Runnable)
     */
    public void run(Runnable runnable);

    /**
     * The current state of the probe in terms of firing (begin-end window) and metering (meters reads).
     * </p>
     * Prior to the <code>begin()</code> method being called and post the calling of the <code>end()</code> method the value returned is always zero.
     * </p>
     * Note: Irrespective of the probe state following a <code>begin()</code> call the probe should always have the <code>end()</code> method called.
     *
     * @return A non-zero value if the <code>begin()</code> has been called but not the <code>end()</code> with a negative value indicating the firing was not metered.
     */
    public int getState();

  }

  /**
   * The {@link Probes.Context Context} interface provides a thread specific interaction with the probes resource metering runtime.
   *
   * @see Probes#context()
   */
  public interface Context {

    /**
     * The name of the {@link Probes.Context Context}'s thread.
     *
     * @return The name of the {@link Probes.Context Context}'s thread.
     */
    public String getName();

    /**
     * Returns a thread specific {@link Probes.Probe Probe} instance.
     *
     * @param name the name for the {@link Probes.Probe Probe}
     * @return A thread specific {@link Probes.Probe Probe} instance
     * @throws NullPointerException if the name parameter is <tt>null</tt>
     */
    public Probes.Probe create(Probes.Name name);

    /**
     * Returns a thread specific {@link Probes.Probe Probe} instance with {@link Probes.Probe#begin() Probe.begin()} already issued
     *
     * @param name the name for the {@link Probes.Probe Probe}
     * @return A thread specific {@link Probes.Probe Probe} instance
     * @throws NullPointerException if the name parameter is <tt>null</tt>
     */
    public Probes.Probe begin(Probes.Name name);

    /**
     * Meters the execution of a {@code java.lang.Runnable}.
     *
     * @param runnable the {@code java.lang.Runnable} to run
     * @return A thread specific {@link Probes.Probe Probe} instance used in metering the running of the {@code java.lang.Runnable}
     * @throws NullPointerException if the runnable parameter is <tt>null</tt>
     * @see Probes.Probe#run(Runnable)
     * @see Probes#run(Runnable)
     */
    public Probes.Probe run(Runnable runnable);

    /**
     * Creates a new {@link Probes.SavePoint SavePoint} instance representing the current metering state of the thread.
     *
     * @return A new {@link Probes.SavePoint SavePoint} instance representing the current metering state of the thread.
     * @see Probes.Context#savepoint(Probes.SavePoint)
     */
    public Probes.SavePoint savepoint();

    /**
     * Updates the provided {@link Probes.SavePoint SavePoint} instance with the current metering state of the thread or creates a new {@link Probes.SavePoint SavePoint} instance.
     *
     * @param savepoint a savepoint previously created by this thread {@link Probes.Context Context}
     * @return A new or updated {@link Probes.SavePoint SavePoint} instance representing the current metering state of the thread.
     * @see Probes.Context#savepoint()
     */
    public Probes.SavePoint savepoint(Probes.SavePoint savepoint);

    /**
     * Returns a {@link Probes.ChangeSet ChangeSet} instance representing the result of a delta analysis between the current thread metering and the {@link Probes.SavePoint SavePoint} parameter.
     *
     * @param savepoint the savepoint to be compared with the current thread metering
     * @return The {@link Probes.ChangeSet ChangeSet} instance representing the result of a delta analysis between the current thread metering and the {@link Probes.SavePoint SavePoint} parameter.
     * @throws NullPointerException if the <tt>savepoint</tt> parameter is <tt>null</tt>
     * @see Probes.SavePoint#compare(Probes.SavePoint)
     */
    public Probes.ChangeSet compare(Probes.SavePoint savepoint);

    /**
     * Returns the {@link Probes.Environment Environment} instance associated with this metered thread {@link Probes.Context Context}.
     *
     * @return An {@link Probes.Environment Environment} instance associated with this metered thread {@link Probes.Context Context}.
     */
    public Probes.Environment getEnvironment();

    /**
     * Returns an immutable {@code java.util.Iterator<Probes.Meter>} that allows navigation over the list of {@link Probes.Meter Meter} instances enabled for this {@link Probes.Context Context}.
     *
     * @return A {@code java.util.Iterator<Probes.Meter>} over {@link Probes.Meter Meter} instances enabled for this {@link Probes.Context Context}.
     */
    public Iterator<Probes.Meter> meters();

    /**
     * Returns the {@link org.jinspired.probes.Probes.Counter Counter} instance associated with the name.
     * <p/>
     * <p>Note: A {@link org.jinspired.probes.Probes.Counter Counter} will be created if not already present.
     * <p/>
     *
     * @param name the name of the counter
     * @return The {@link org.jinspired.probes.Probes.Counter Counter} instance associated with the name.
     * @throws NullPointerException if the name parameter is <tt>null</tt>
     */
    public Counter counter(Name name);

  }

  /**
   * The {@link Probes.Meter Meter} interface represents a metered resource counter within the metering runtime.
   *
   * @see Probes.Context#meters()
   */
  public interface Meter {

    /**
     * The name uniquely identifying the {@link Probes.Meter Meter}.
     *
     * @return A non-null {@link Probes.Name Name} uniquely identifying the {@link Probes.Meter Meter}.
     */
    public Probes.Name getName();

  }

  /**
   * The {@link Probes.Reading Reading} interface represents a measurement of one or two {@link Probes.Meter Meter} readings.
   *
   * @see Probes.Probe#readings()
   */
  public interface Reading {

    /**
     * The name of the {@link Probes.Meter Meter} associated with this reading.
     *
     * @return A non-null meter {@link Probes.Name Name}.
     * @see Probes.Meter#getName()
     */
    public Probes.Name getName();

    /**
     * The previous {@link Probes.Meter Meter} reading.
     *
     * @return The previous {@link Probes.Meter Meter} reading or zero if only one reading has occurred.
     */
    public long getLow();

    /**
     * The last and most current {@link Probes.Meter Meter} value read.
     * <p/>
     * The high value is always equals or greater than the {@link #getLow()} value.
     *
     * @return The last {@link Probes.Meter Meter} reading.
     */
    public long getHigh();


    /**
     * The delta of the high and low readings.
     *
     * @return The delta of the high and low readings.
     */
    public long getDelta();

  }

  /**
   * The {@link Probes.Label Label} interface represents meta-data associated with a {@link Probes.Name Name}.
   *
   * @see Probes.Name#labels()
   */
  public interface Label {

    /**
     * The value of the label.
     *
     * @return A non-null value identifying the label.
     */
    public String getValue();

  }

  /**
   * The {@link Probes.SavePoint SavePoint} interface represents an opaque snapshot of a threads metering at a point in time.
   *
   * @see Probes.Context#savepoint()
   * @see Probes.Context#savepoint(Probes.SavePoint)
   */
  public interface SavePoint {

    /**
     * Returns a {@link Probes.ChangeSet ChangeSet} instance representing the result of a delta analysis between this instance and the parameter instance.
     *
     * @param savepoint an older savepoint to be compared with this savepoint instance
     * @return The {@link Probes.ChangeSet ChangeSet} instance representing the result of a delta analysis between this instance and the parameter instance.
     * @throws NullPointerException if the <tt>savepoint</tt> parameter is <tt>null</tt>
     * @see Probes.Context#compare(Probes.SavePoint)
     */
    public Probes.ChangeSet compare(Probes.SavePoint savepoint);

  }

  /**
   * The {@link Probes.ChangeSet ChangeSet} interface represents a set of {@link Probes.ChangePoint ChangePoints} and {@link Probes.Change Changes} generated based on a delta analysis of metering between two points in time.
   *
   * @see Probes.Context#compare(Probes.SavePoint)
   * @see Probes.SavePoint#compare(Probes.SavePoint)
   */
  public interface ChangeSet {

    /**
     * Returns an immutable {@code java.util.Iterator<Probes.Change>} that allows navigation over the list of {@link Probes.Change Change} instances associated with the top level group of thread.
     *
     * @return A {@code java.util.Iterator<Probes.Change>} over the list of {@link Probes.Change Change} instances associated with the top level group of thread.
     * @see Probes.ChangePoint#change(Probes.Name)
     */
    public Iterator<Probes.Change> changes();

    /**
     * Returns the {@link Probes.Change Change} instance associated with the top level group of thread.
     *
     * @param name the name of the {@link Probes.Meter Meter}
     * @return The {@link Probes.Change Change} instance for a particular {@link Probes.Meter Meter} or <tt>null</tt>.
     * @throws NullPointerException if the name parameter is <tt>null</tt>
     * @see Probes.ChangePoint#changes()
     */
    public Probes.Change change(Probes.Name name);

    /**
     * Returns an immutable {@code java.util.Iterator<Probes.ChangePoint>} that allows navigation over the list of metered {@link Probes.ChangePoint ChangePoint} instances associated with the {@link Probes.Context Context}'s thread.
     *
     * @return A {@code java.util.Iterator<Probes.ChangePoint>} over the list of metered {@link Probes.ChangePoint ChangePoint} instances associated with the {@link Probes.Context Context}'s thread.
     * @see Probes.ChangeSet#changepoint(Probes.Name)
     */
    public Iterator<Probes.ChangePoint> changepoints();

    /**
     * Returns a {@link Probes.ChangePoint ChangePoint} associated with the provided group name at the thread level.
     *
     * @param name the group name
     * @return The matching {@link Probes.ChangePoint ChangePoint} instance
     * @throws NullPointerException if the name parameter is <tt>null</tt>
     * @see Probes.ChangeSet#changepoints()
     */
    public Probes.ChangePoint changepoint(Probes.Name name);

  }

  /**
   * The {@link Probes.ChangePoint ChangePoint} interface represents a {@link Probes.Probe Probe} metering delta recorded between two points in time.
   *
   * @see Probes.ChangeSet#changepoints()
   */
  public interface ChangePoint {

    /**
     * The name of the group.
     *
     * @return The complete or partial prefix name of a {@link Probes.Probe Probe}.
     */
    public Probes.Name getName();

    /**
     * Returns an {@code java.util.Iterator<Probes.Change>} that allows navigation over the list of {@link Probes.Change Change} instances associated with the change point.
     *
     * @return A {@code java.util.Iterator<Probes.Change>} over the change point's {@link Probes.Change Change} instances.
     * @see Probes.ChangePoint#change(Probes.Name)
     */
    public Iterator<Probes.Change> changes();

    /**
     * Returns the {@link Probes.Change Change} instance for a particular {@link Probes.Meter Meter}
     *
     * @param name the name of the {@link Probes.Meter Meter}
     * @return The {@link Probes.Change Change} instance for a particular {@link Probes.Meter Meter} or <tt>null</tt>.
     * @throws NullPointerException if the name parameter is <tt>null</tt>
     * @see Probes.ChangePoint#changes()
     */
    public Probes.Change change(Probes.Name name);

  }

  /**
   * The {@link Probes.Change Change} interface represents a positive metering delta recorded between two points in time.
   *
   * @see Probes.ChangeSet#changes()
   * @see Probes.ChangePoint#changes()
   */
  public interface Change {

    /**
     * The name of the {@link Probes.Meter Meter}.
     *
     * @return The name of the {@link Probes.Meter Meter} associated with this change.
     */
    public Probes.Name getName();

    /**
     * Returns the number of {@link Probes.Meter Meter} readings recorded between two points in time.
     *
     * @return The number of {@link Probes.Meter Meter} readings recorded between two points in time.
     */
    public long getCount();

    /**
     * The cumulative total for all {@link Probes.Meter Meter} readings recorded between two points in time.
     *
     * @return The cumulative total for all {@link Probes.Meter Meter} readings recorded between two points in time.
     */
    public long getTotal();

    /**
     * The average {@link Probes.Meter Meter} reading recorded for all {@link Probes.Meter Meter} readings recorded between two points in time.
     *
     * @return a non-negative number representing the average {@link Probes.Meter Meter} reading recorded for all {@link Probes.Meter Meter} readings recorded between two points in time.
     */
    public double getAvg();

    /**
     * The cumulative inherent total for all {@link Probes.Meter Meter} readings recorded between two points in time.
     *
     * @return The cumulative inherent total for all {@link Probes.Meter Meter} readings recorded between two points in time.
     */
    public long getInherentTotal();

    /**
     * The inherent average {@link Probes.Meter Meter} reading recorded for all {@link Probes.Meter Meter} readings recorded between two points in time.
     *
     * @return a non-negative number representing the inherent average {@link Probes.Meter Meter} reading recorded for all {@link Probes.Meter Meter} readings recorded between two points in time.
     */
    public double getInherentAvg();

  }

  /**
   * The {@link Probes.Environment Environment} interface represents a thread context specific named value set that can be used to share contextual data between the application and custom extensions such as interceptors, plugins, and strategies.
   *
   * @see Probes.Context#getEnvironment()
   */
  public interface Environment {

    /**
     * Returns true if one or more values with the same name or name prefix exist in the environment.
     *
     * @param name the name of the environment value
     * @return true if the one or more values with the same name or name prefix exist in the environment else false.
     */
    public boolean contains(Probes.Name name);

    /**
     * Removes one or more values with the same name or name prefix.
     *
     * @param name the name identifying a specific value or the prefix for a group of named values.
     */
    public void remove(Probes.Name name);


    /**
     * Clears the value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name the name of the environment value
     */
    public void setNull(Probes.Name name);

    /**
     * Returns true if the specified name does not exist within this environment or no value is currently set.
     *
     * @param name the name of the environment value
     * @return true if the specified name does not exist within this environment or no value is currently set.
     */
    public boolean isNull(Probes.Name name);

    /**
     * Returns the <tt>long</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name the name of the environment value
     * @return the value associated with the name parameter or 0 if not currently set
     * @see #getLong(Probes.Name, long)
     * @see #setLong(Probes.Name, long)
     */
    public long getLong(Probes.Name name);

    /**
     * Returns the <tt>long</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param defValue the value returned if not currently set
     * @return the value associated with the name parameter or the value of the defValue parameter if not currently set
     * @see #getLong(Probes.Name)
     * @see #setLong(Probes.Name, long)
     */
    public long getLong(Probes.Name name, long defValue);

    /**
     * Sets the <tt>long</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param newValue the value to be associated with the specified name parameter within this environment
     * @see #getLong(Probes.Name, long)
     * @see #getLong(Probes.Name)
     */
    public void setLong(Probes.Name name, long newValue);

    /**
     * Returns the <tt>boolean</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name the name of the environment value
     * @return the value associated with the name parameter or false if not currently set
     * @see #getBoolean(Probes.Name, boolean)
     * @see #setBoolean(Probes.Name, boolean)
     */
    public boolean getBoolean(Probes.Name name);

    /**
     * Returns the <tt>boolean</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param defValue the value returned if not currently set
     * @return the value associated with the name parameter or the value of the defValue parameter if not currently set
     * @see #getBoolean(Probes.Name)
     * @see #setBoolean(Probes.Name, boolean)
     */
    public boolean getBoolean(Probes.Name name, boolean defValue);

    /**
     * Sets the <tt>boolean</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param newValue the value to be associated with the specified name parameter within this environment
     * @see #getBoolean(Probes.Name, boolean)
     * @see #getBoolean(Probes.Name)
     */
    public void setBoolean(Probes.Name name, boolean newValue);

    /**
     * Returns the <tt>int</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name the name of the environment value
     * @return the value associated with the name parameter or 0 if not currently set
     * @see #getInt(Probes.Name, int)
     * @see #setInt(Probes.Name, int)
     */
    public int getInt(Probes.Name name);

    /**
     * Returns the <tt>int</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param defValue the value returned if not currently set
     * @return the value associated with the name parameter or the value of the defValue parameter if not currently set
     * @see #getInt(Probes.Name)
     * @see #setInt(Probes.Name, int)
     */
    public int getInt(Probes.Name name, int defValue);

    /**
     * Sets the <tt>int</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param newValue the value to be associated with the specified name parameter within this environment
     * @see #getInt(Probes.Name, int)
     * @see #getInt(Probes.Name)
     */
    public void setInt(Probes.Name name, int newValue);

    /**
     * Returns the <tt>double</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name the name of the environment value
     * @return the value associated with the name parameter or 0 if not currently set
     * @see #getDouble(Probes.Name, double)
     * @see #setDouble(Probes.Name, double)
     */
    public double getDouble(Probes.Name name);

    /**
     * Returns the <tt>double</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param defValue the value returned if not currently set
     * @return the value associated with the name parameter or the value of the defValue parameter if not currently set
     * @see #getDouble(Probes.Name)
     * @see #setDouble(Probes.Name, double)
     */
    public double getDouble(Probes.Name name, double defValue);

    /**
     * Sets the <tt>double</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param newValue the value to be associated with the specified name parameter within this environment
     * @see #getDouble(Probes.Name, double)
     * @see #getDouble(Probes.Name)
     */
    public void setDouble(Probes.Name name, double newValue);

    /**
     * Returns the <tt>String</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name the name of the environment value
     * @return the value associated with the name parameter or <tt>null</tt> if not currently set
     * @see #getString(Probes.Name, String)
     * @see #setString(Probes.Name, String)
     */
    public String getString(Probes.Name name);

    /**
     * Returns the <tt>String</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param defValue the value returned if not currently set
     * @return the value associated with the name parameter or the value of the defValue parameter if not currently set
     * @see #getString(Probes.Name)
     * @see #setString(Probes.Name, String)
     */
    public String getString(Probes.Name name, String defValue);

    /**
     * Sets the <tt>String</tt> value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param newValue the value to be associated with the specified name parameter within this environment
     * @see #getString(Probes.Name, String)
     * @see #getString(Probes.Name)
     */
    public void setString(Probes.Name name, String newValue);

    /**
     * Returns the {@link Probes.Name Name} value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name the name of the environment value
     * @return the value associated with the name parameter or <tt>null</tt> if not currently set
     * @see #getName(Probes.Name, Probes.Name)
     * @see #setName(Probes.Name, Probes.Name)
     */
    public Probes.Name getName(Probes.Name name);

    /**
     * Returns the {@link Probes.Name Name} value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param defValue the value returned if not currently set
     * @return the value associated with the name parameter or the value of the defValue parameter if not currently set
     * @see #getName(Probes.Name)
     * @see #setName(Probes.Name, Probes.Name)
     */
    public Probes.Name getName(Probes.Name name, Probes.Name defValue);

    /**
     * Sets the {@link Probes.Name Name} value associated with the specified {@link Probes.Name Name} within this environment.
     *
     * @param name     the name of the environment value
     * @param newValue the value to be associated with the specified name parameter within this environment
     * @see #getName(Probes.Name, Probes.Name)
     * @see #getName(Probes.Name)
     */
    public void setName(Probes.Name name, Probes.Name newValue);

  }

  /**
   * The {@link org.jinspired.probes.Probes.Counter Counter} interface represents a resource counter that may be mapped to a <code>Meter</code> and metered.
   * <p/>
   * <p>{@link org.jinspired.probes.Probes.Counter Counters}, unlike meters, can be dynamically added to the resource metering runtime and can be updated (incremented) for each executing thread by extensions or custom application code.
   * <p/>
   * <p>{@link org.jinspired.probes.Probes.Counter Counters} provide a mechanism for extending the list of possible meters supported by the metering runtime as custom meters can be configured to use underlying one or more counters as the resource metered.
   * They also offer a smaller runtime overhead for simple event/incident reporting than {@link org.jinspired.probes.Probes.Probe Probes} which are interval based.
   * <p/>
   * <p>Note: The {@link org.jinspired.probes.Probes.Counter Counter} is specific to a particular thread and thus should not be reused across multiple threads of execution.
   *
   * @see org.jinspired.probes.Probes.Context#counter(org.jinspired.probes.Probes.Name)
   */
  public interface Counter {

    /**
     * The name uniquely identifying the counter.
     *
     * @return A non-null {@link org.jinspired.probes.Probes.Name Name} uniquely identifying the counter.
     */
    public Name getName();

    /**
     * The current value of the counter.
     *
     * @return The current value of the counter.
     */
    public long getValue();

    /**
     * Increments the counter value by one.
     */
    public void inc();

    /**
     * Increments the counter by the value specified.
     *
     * @param value the positive value to be added to the counter
     */
    public void inc(long value);

  }

}