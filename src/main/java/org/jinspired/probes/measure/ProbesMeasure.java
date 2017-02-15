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

package org.jinspired.probes.measure;

/**
 * The {@link ProbesMeasure ProbesMeasure} interface provides an extension point for registering custom meters with the metering runtime.
 *
 * @author William Louth
 *
 */
public interface ProbesMeasure {

  /**
   * Returns the cumulative value, zero or greater, of the underlying meter specific to the calling thread.
   *
   * This method is called when either {@code Probe.begin()} or {@code Probe.end()} is called on a firing probe that is metered.
   *
   * @return The cumulative value of the underlying meter specific to the calling thread.
   */
  public long getValue();

}
