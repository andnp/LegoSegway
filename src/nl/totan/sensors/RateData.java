package nl.totan.sensors;


/**
 * Interface for classes that access rate sensors, also known as gyro sensors, and other classes that provide rate data. The interface assumes 
 * rate to be measured over three axes in X, Y, Z order.
 * <P>
 * The interface provides:
 *  <li>An enum type that provides constants to describe rate units and a method, <code>convertTo</code>, to convert 
 *     one rate unit to another </li>
 *  <li>Standard methods to fetch rate data and to set or query a default rate unit</li>
 *  <p>
 * @author Aswin Bouwmeester
 * @version 1.0
 * 
 */public interface RateData {
	/**
	 * List of possible units for Rate
	 */
	public enum RateUnits {

	  /**
	   * Rate in degrees/s
	   */
	  DPS,
	  /**
	   * Rate in radians/s
	   */
	  RPS,
	  /**
	   * Rate in cosine/s
	   */
	  CPS;

	  /**
	   * Converts a value to specified unit
	   * @param value
	   * Value to convert.
	   * @param unit
	   * Unit to convert to.
	   * @return
	   * Converted value.
	   */
	  public float convertTo(float value, RateUnits unit) {
	  	if (this==unit) return value;
	  	if (this==RateUnits.DPS && unit==RateUnits.RPS) return (float)Math.toRadians(value);
	  	if (this==RateUnits.RPS && unit==RateUnits.DPS) return (float)Math.toDegrees(value);
	  	if (this==RateUnits.RPS && unit==RateUnits.CPS) return (float)Math.cos(value);
	  	if (this==RateUnits.DPS && unit==RateUnits.CPS) return (float)Math.cos(Math.toRadians(value));
	  	if (this==RateUnits.CPS && unit==RateUnits.RPS) return (float)Math.acos(value);
	  	if (this==RateUnits.CPS && unit==RateUnits.DPS) return (float)Math.acos(Math.toRadians(value));
	  	return Float.NaN;
	  }

  
	  /**
	   * Converts an array of values in place to specified unit
	   * @param value
	   * Array of values to convert.
	   * @param unit
	   * Unit to convert to.
	   */
	  public void convertTo(float[] values, RateUnits unit) {
	  	for (int i=0;i<values.length;i++) 
	  		values[i]=convertTo(values[i],unit);
	  }
	  
  }

  
  /**
   * Returns the default unit for measurements
   * @return
   * unit
   */
   public RateUnits getRateUnit();

   /**
    * Sets the default unit for measurements.
    * @param TiltUnit
    * Unit to set as default.
    */
  public void setRateUnit(RateUnits unit);

  /**
   * Fetches the current Rate values expressed in the default tilt unit 
   * @param ret
   * An array of three elements to store the measurement in.
   * Array order corresponds to X, Y and Z-axis respectivily.
   */
  public void fetchAllRate(float[] ret);

  /**
   * Fetches the current rate values expressed in the specified tilt unit 
   * @param ret
   * An array of three elements to store the measurement in.
   * Array order corresponds to X, Y and Z-axis respectivily.
   * @param unit
   * Unit to express tilt data in.
   */
  public void fetchAllRate(float[] ret, RateUnits unit);


}
