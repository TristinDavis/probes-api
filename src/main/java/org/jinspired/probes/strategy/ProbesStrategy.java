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
 * The {@link ProbesStrategy} interface provides an extension point for defining new intelligent and dynamic metering strategies that can be used to control the metering of paired <code>Probe.begin()</code> and <code>Probe.end()</code> calls.
 *
 * @see ProbesStrategyFactory#create(org.jinspired.probes.Probes.Context)
 *
 * @author William Louth
 */
public interface ProbesStrategy {

  /**
   * An inquiry method invoked prior to the dispatching of <code>Probe.begin()</code> to the next probes provider in the provider runtime stack beneath the strategy probes provider.
   *
   * @param probe the probe being fired
   *
   * @return A value less than 0 is a <tt>NO</tt> vote, a value greater than 0 is a <tt>YES</tt> vote otherwise a zero is an <tt>ABSTAIN</tt>.
   * @see org.jinspired.probes.Probes.Probe#getState()
   */
  public int vote(Probes.Probe probe);

}
