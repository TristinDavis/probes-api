/*
 * Copyright 2013 JINSPIRED BV (http://www.autoletics.com)
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

package org.jinspired.probes.spi;

import org.jinspired.probes.Probes;

import java.lang.reflect.Method;

/**
 * The service provider interface for the probes metering runtime.
 *
 * @author William Louth
 */
public interface ProbesProvider {

  /**
   *  This method is called once on initialization of the probes runtime and before any other method in this interface is called.
   */
  public void init();

  /**
   * Returns the probe {@link Probes.Context Context} for the calling thread.
   *
   * @return The probe {@link Probes.Context Context} for the calling thread.
   *
   * @see Probes#context()
   */
  public Probes.Context context();

  /**
   * Returns a {@link Probes.Name Name} instance with a null name prefix and the value equal to the specified value parameter.
   *
   * @param value the string value of the name's part
   * @return A non-null {@link Probes.Name Name} object
   * @throws NullPointerException if the name parameter is <tt>null</tt>
   *
   * @see Probes#name(String)
   */
  public Probes.Name name(String value);

  /**
   * Returns a {@link Probes.Name Name} instance with the name prefix representing the parsed package namespace and the unqualified class name as the value.
   *
   * @param cls the class for which a name will be created
   * @return A non-null {@link Probes.Name Name} object
   * @throws NullPointerException if the cls parameter is null
   *
   * @see Probes#name(Class)
   */
  public Probes.Name name(Class cls);

  /**
   * Returns a {@link Probes.Name Name} instance with the name prefix representing the parsed fully qualified class name and the method name as the value.
   *
   * @param method the method for which a name will be created
   * @return A non-null {@link Probes.Name Name} object
   * @throws NullPointerException if the method parameter is null
   *
   * @see Probes#name(Method)
   */
  public Probes.Name name(Method method);

  /**
   * Returns a {@link Probes.Meter Meter} associated with the provided name.
   *
   * @param name the {@link Probes.Meter} name
   * @return The matching {@link Probes.Meter Meter} instance
   * @throws NullPointerException if the name parameter is <tt>null</tt>
   *
   * @see Probes#meter(Probes.Name)
   */
  public Probes.Meter meter(Probes.Name name);

  /**
   * Returns a {@link Probes.Name Name} instance following the parsing of the string value with a delimiter of '.'
   *
   * @param value the value to be parsed
   * @return A non-null {@link Probes.Name Name} object
   * @throws NullPointerException if the value parameter is null
   *
   * @see Probes#parse(String)
   */
  public Probes.Name parse(String value);

  /**
   * Returns a built-in {@link Probes.Label Label} mapped to a specified string value.
   *
   * @param value the value to be matched
   * @return The built-in {@link Probes.Label Label} mapped to the specified string value or null if not existing label matches.
   *
   * @see Probes#label(String)
   */
  public Probes.Label label(String value);

}
