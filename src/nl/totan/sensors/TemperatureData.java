package nl.totan.sensors;

/**
 * Interface for classes that access thermometers and other classes that provide temperature data. 
 * <P>
 * The interface provides:
 *  <li>An enum type that provides constants to describe temperature units and a method, <code>convertTo</code>, to convert 
 *     one temperature unit to another </li>
 *  <li>Standard methods to fetch temperature data and to set or query a default temperature unit</li>
 *  <p>
 * @author Aswin Bouwmeester
 * @version 1.0
 * 
 */public interface TemperatureData {
	public enum TemperatureUnits {

	  /**
	   * Rate in degrees
	   */
	  CELCIUS,
	  /**
	   * Rate in radians
	   */
	  FAHRENHEIT;
	  
	  /**
	   * Converts a value to specified unit
	   * @param value
	   * Value to convert.
	   * @param unit
	   * Unit to convert to.
	   * @return
	   * Converted value.
	   */
	  public float convertTo(float value, TemperatureUnits unit) {
	  	if (this==unit) return value;
	  	if (this==TemperatureUnits.CELCIUS && unit==TemperatureUnits.FAHRENHEIT) return  1.8f * value + 32f;
	  	if (this==TemperatureUnits.FAHRENHEIT && unit==TemperatureUnits.CELCIUS) return (value-32f)/1.8f;
	  	return Float.NaN;
	  }
	  
	}

  	
  /**
   * Fetches the current temperature. 
   * @return
   * temperature.
   */

	public float fetchTemperature();
	
  /**
   * Returns the default unit for measurements
   * @return
   * unit
   */
public TemperatureUnits getTemperatureUnit();
	
/**
 * Sets the default unit for measurements.
 * @param Unit
 * Unit to set as default.
 */
	public void setTemperatureUnit(TemperatureUnits unit);

}
