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
   * This method is called when either <code>Probe.begin()</code> or <code>Probe.end()</code> is called on a firing probe that is metered.
   *
   * @return The cumulative value of the underlying meter specific to the calling thread.
   */
  public long getValue();

}
