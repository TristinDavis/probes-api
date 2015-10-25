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
