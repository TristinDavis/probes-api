
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

/**
 * The service provider factory interface for the probes runtime.
 *
 * @author William Louth
 */
public interface ProbesProviderFactory {

  /**
   * This method is called by the {@link org.jinspired.probes.Probes Probes} to create the provider that will be used as the delegate for all entry point calls.
   *
   * @return an instance of {@link ProbesProvider ProbesProvider}
   */
  public ProbesProvider create();

}
