
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
   * @see Probes#context()
   */
  public Probes.Context context();

  /**
   * @see org.jinspired.probes.Probes#name(String)
   */
  public Probes.Name name(String name);

  /**
   * @see org.jinspired.probes.Probes#name(Class)
   */
  public Probes.Name name(Class cls);

  /**
   * @see org.jinspired.probes.Probes#name(java.lang.reflect.Method)
   */
  public Probes.Name name(Method method);

  /**
   * @see org.jinspired.probes.Probes#meter(org.jinspired.probes.Probes.Name)
   */
  public Probes.Meter meter(Probes.Name name);

  /**
   * @see org.jinspired.probes.Probes#parse(String)
   */
  public Probes.Name parse(String value);

  /**
   * @see org.jinspired.probes.Probes#label(String)
   */
  public Probes.Label label(String value);

}
