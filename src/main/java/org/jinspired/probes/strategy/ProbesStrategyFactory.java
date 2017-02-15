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

package org.jinspired.probes.strategy;

import org.jinspired.probes.Probes;

/**
 * The {@link ProbesStrategyFactory} interface provides an extension factory point for defining new intelligent and dynamic metering strategies that can be used to control the metering of paired {@code Probe.begin()} and {@code Probe.end()} calls.
 *
 * @see ProbesStrategy#vote(Probes.Probe)
 *
 * @author William Louth
 */
public interface ProbesStrategyFactory {

  /**
   * Called once per individual configuration and prior to the first creation of a strategy by the factory.
   *
   * @param environment an environment instance holding possible configuration settings
   */
  public void init(Probes.Environment environment);

  /**
   * Creates a strategy to be associated with the thread context parameter.
   *
   * @param context the thread metering context
   * @return The interceptor to be associated with the thread context.
   */
  public ProbesStrategy create(Probes.Context context);

}
