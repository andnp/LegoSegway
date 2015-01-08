package nl.totan.sensors;

/**
 * Interface for classes that access accelerometers and other classes that provide tilt data. The interface assumes 
 * tilt to be measured over three axes in X, Y, Z order.
 * <P>
 * The interface provides:
 *  <li>An enum type that provides constants to describe tilt units and a method, <code>convertTo</code>, to convert 
 *     one tilt unit to another </li>
 *  <li>Standard methods to fetch tilt data and to set or query a default tilt unit</li>
 *  <p>
 * @author Aswin Bouwmeester
 * @version 1.0
 * 
 */
public interface TiltData {

	/**
	 * List of possible units for Tilt
	 */
	public enum TiltUnits {

		/**
		 * Tilt in degrees
		 */
		DEGREES,
		/**
		 * Tilt in radians
		 */
		RADIANS,
		/**
		 * Tilt in cosine
		 */
		COSINE;

		/**
		 * Converts a value to specified unit
		 * 
		 * @param value
		 *          Value to convert.
		 * @param unit
		 *          Unit to convert to.
		 * @return Converted value.
		 */
		public float convertTo(float value, TiltUnits unit) {
			if (this == unit)
				return value;
			if (this == TiltUnits.DEGREES && unit == TiltUnits.RADIANS)
				return (float) Math.toRadians(value);
			if (this == TiltUnits.RADIANS && unit == TiltUnits.DEGREES)
				return (float) Math.toDegrees(value);
			if (this == TiltUnits.RADIANS && unit == TiltUnits.COSINE)
				return (float) Math.cos(value);
			if (this == TiltUnits.DEGREES && unit == TiltUnits.COSINE)
				return (float) Math.cos(Math.toRadians(value));
			if (this == TiltUnits.COSINE && unit == TiltUnits.RADIANS)
				return (float) Math.acos(value);
			if (this == TiltUnits.COSINE && unit == TiltUnits.DEGREES)
				return (float) Math.toDegrees(Math.acos(value));
			return Float.NaN;
		}


		/**
		 * Converts an array of values in place to specified unit
		 * 
		 * @param value
		 *          Array of values to convert.
		 * @param unit
		 *          Unit to convert to.
		 */
		public void convertTo(float[] values, TiltUnits unit) {
			for (int i = 0; i < values.length; i++)
				values[i] = convertTo(values[i], unit);
		}

	}
	
	
		/**
	 * Returns the default unit for measurements
	 * 
	 * @return unit
	 */
	public TiltUnits getTiltUnit();

	/**
	 * Sets the default unit for measurements.
	 * 
	 * @param TiltUnit
	 *          Unit to set as default.
	 */
	public void setTiltUnit(TiltUnits TiltUnit);

	/**
	 * Fetches the current tilt values expressed in the default tilt unit
	 * 
	 * @param ret
	 *          An array of three elements to store the measurement in. Array
	 *          order corresponds to X, Y and Z-axis respectivily.
	 */
	public void fetchAllTilt(float[] ret);

	/**
	 * Fetches the current tilt values expressed in the specified tilt unit
	 * 
	 * @param ret
	 *          An array of three elements to store the measurement in. Array
	 *          order corresponds to X, Y and Z-axis respectivily.
	 * @param unit
	 *          Unit to express tilt data in.
	 */
	public void fetchAllTilt(float[] ret, TiltUnits unit);

}
