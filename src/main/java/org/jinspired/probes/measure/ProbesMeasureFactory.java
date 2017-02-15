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

import org.jinspired.probes.Probes;

/**
 * The {@link ProbesMeasureFactory ProbesMeasureFactory} interface provides an extension factory point for registering custom meters with the metering runtime.
 *
 * @author William Louth
 */
public interface ProbesMeasureFactory {

  /**
   * Called once per individual configuration and prior to the first creation of a meter measure by the factory.
   *
   * @param environment an environment instance holding possible configuration settings
   */
  public void init(Probes.Environment environment);

  /**
   * Creates a measure to be associated with a named meter specific to thread context parameter.
   *
   * @param context the thread metering context
   * @return The measure to be associated with a named meter specific to thread context parameter.
   */
  public ProbesMeasure create(Probes.Context context);

}
