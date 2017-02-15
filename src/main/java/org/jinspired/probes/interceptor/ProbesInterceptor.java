/*
 * Copyright © 2014 JINSPIRED BV (http://www.autoletics.com)
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
 * The {@link ProbesInterceptor} interface provides an extension point for defining interceptors which can be used to intercept the {@code Probe.begin()} and {@code Probe.end()} calls to a probe.
 *
 * @see ProbesInterceptorFactory#create(Probes.Context)
 *
 * @author William Louth
 */
public interface ProbesInterceptor {

  /**
   * An interception callback invoked after the dispatching of {@code Probe.begin()} to the next probes provider in the probes provider runtime stack beneath the interceptor probes provider.
   *
   * @param probe the probe intercepted
   * @see Probes.Probe#begin()
   */
  public void begin(Probes.Probe probe);

  /**
   * An interception callback invoked after the dispatching of {@code Probe.end()} to the next probes provider in the probes provider runtime stack beneath the interceptor probes provider.
   *
   * @param probe the probe intercepted
   * @see Probes.Probe#end()
   */
  public void end(Probes.Probe probe);

}
