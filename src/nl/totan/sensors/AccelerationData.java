package nl.totan.sensors;

/**
 * Interface for classes that access accelerometers and other classes that provide acceleration data. The interface assumes 
 * acceleration to be measured over three axes in X, Y, Z order.
 * <P>
 * The interface provides:
 *  <li>An enum type that provides constants to describe acceleration units and a method, <code>convertTo</code>, to convert 
 *     one accelereration unit to another </li>
 *  <li>Standard methods to fetch acceleration data and to set or query a default acceleration unit</li>
 *  <p>
 * @author Aswin Bouwmeester
 * @version 1.0
 * 
 */public interface AccelerationData {
	
	/**
	 * List of possible units for acceleration
	 */
	public enum AccelUnits {

	  /**
	   * Acceleration in milli-G
	   */
	  MILLIG,
	  /**
	   * Acceleration in G
	   */
	  G,
	  /**
	   * Acceleration in m/s^2
	   */
	  MS2,
	  /**
	   * Acceleration in mm/s^2
	   */
	  MILLIMS2;
	  
	  /**
	   * Converts a value to specified unit
	   * @param value
	   * Value to convert.
	   * @param unit
	   * Unit to convert to.
	   * @return
	   * Converted value.
	   */
	  public float convertTo(float value, AccelUnits unit) {
	  	if (this==unit) return value;
	  	if (this==AccelUnits.MILLIG && unit==AccelUnits.G) return value * 0.001f;
	  	if (this==AccelUnits.MILLIG && unit==AccelUnits.MS2) return value * 0.00981f;
	  	if (this==AccelUnits.MILLIG && unit==AccelUnits.MILLIMS2) return value * 9.81f;

	  	if (this==AccelUnits.G && unit==AccelUnits.MILLIG) return value * 1000f;
	  	if (this==AccelUnits.G && unit==AccelUnits.MS2) return value * 9.81f;
	  	if (this==AccelUnits.G && unit==AccelUnits.MILLIMS2) return value * 9810f;
	  	
	  	if (this==AccelUnits.MS2 && unit==AccelUnits.MILLIG) return value /  0.00981f;
	  	if (this==AccelUnits.MS2 && unit==AccelUnits.G) return value / 9.81f;
	  	if (this==AccelUnits.MS2 && unit==AccelUnits.MILLIMS2) return value * 10001f;
	  	
	  	if (this==AccelUnits.MILLIMS2 && unit==AccelUnits.MILLIG) return value /  9.81f;
	  	if (this==AccelUnits.MILLIMS2 && unit==AccelUnits.G) return value / 9810f;
	  	if (this==AccelUnits.MILLIMS2 && unit==AccelUnits.MS2) return value * 0.001f;
	  	
	  	return Float.NaN;
	  }

	  /**
	   * Converts an array of values in place to specified unit
	   * @param value
	   * Array of values to convert.
	   * @param unit
	   * Unit to convert to.
	   */
	  public void convertTo(float[] values, AccelUnits unit) {
	  	for (int i=0;i<values.length;i++) 
	  		values[i]=convertTo(values[i],unit);
	  }	  
 
 
	}

   

	
  /**
   * Returns the default unit for measurements
   * @return
   * unit
   */
  public AccelUnits getAccelUnit();

  /**
   * Sets the default unit for measurements.
   * @param TiltUnit
   * Unit to set as default.
   */
  public void setAccelUnit(AccelUnits unit);

  /**
   * Fetches the current acceleration values expressed in the default acceleration unit 
   * @param ret
   * An array of three elements to store the measurement in.
   * Array order corresponds to X, Y and Z-axis respectivily.
   */
  public void fetchAllAccel(float[] ret);
  /**
   * Fetches the current acceleration values expressed in the specified acceleration unit 
   * @param ret
   * An array of three elements to store the measurement in.
   * Array order corresponds to X, Y and Z-axis respectivily.
   * @param unit
   * Unit to express tilt data in.
   */
  public void fetchAllAccel(float[] ret,AccelUnits unit);
   
  /**
   * Fetches the current acceleration values expressed in the raw values that the sensor provides 
   * @param ret
   * An array of three elements to store the measurement in.
   * Array order corresponds to X, Y and Z-axis respectivily.
  */
  public void fetchRawAccel(int[] ret);
  
}
