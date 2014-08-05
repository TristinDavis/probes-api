/*
 * Copyright © 2013, 2014 JINSPIRED BV (http://www.autoletics.com)
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

package org.jinspired.probes.interceptor;

import org.jinspired.probes.Probes;

/**
 * The {@link ProbesInterceptorFactory ProbesInterceptorFactory} interface provides an extension factory point for defining interceptors which can be used to intercept the <code>Probe.begin()</code> and <code>Probe.end()</code> calls to a probe.
 *
 * @author William Louth
 */
public interface ProbesInterceptorFactory {

  /**
   * Called once per individual configuration and prior to the first creation of a interceptor by the factory.
   * <p/>
   * @param environment an environment instance holding possible configuration settings
   */
  public void init(Probes.Environment environment);

  /**
   * Creates an interceptor which will be associated with the probe identified by the name parameter being created within the current thread context.
   * <p/>
   *
   * @param context the thread metering context
   * @return The interceptor which will be associated with the thread context parameter.
   */
  public ProbesInterceptor create(Probes.Context context);

}
