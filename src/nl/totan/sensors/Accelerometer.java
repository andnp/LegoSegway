package nl.totan.sensors;


import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;

/**
 * This class can be used as a basis for classes that access accelerometers. 
 * It implemenents {@link nl.totan.sensors.TiltData TiltData} and {@link nl.totan.sensors.AccelererationData AccelererationData} methods 
 * and provides functionallity to {@link nl.totan.sensors.AccelCalibration calibrate the sensor}.
 * <P> 
 * <B>Usage guide</B>
 * <p>
 * When using this calss as a super class for implementig an accelerometer one should follow these guidelines:
 * <li>Implement a <code>fetchRawAccel(int[] ret)</code> method that returns raw values for X, Y and Z accelereration from the accelerometer.
 * This method should take care of any endian conversions but not alter values.</li>
 * <li>Implement a <code> fetchAllAccel(float[] ret, AccelUnits unit) </code> method that converts raw values to the specified unit using 
 * {@link #multiplier}, {@link nl.sensors.totan.AccelCalibration#offset offset} and {@link nl.sensors.totan.AccelCalibration#scale scale}. Like in this example:
 * <br>
 * <code> 
 * 
 * 	// convert to G
 * <br>
 * 	ret[i] = (raw[i] - Settings.offset[i]) * multiplier / Settings.scale[i]; 
 * <br>
 *  // convert to specified unit
 * <br>
 *  AccelUnits.G.convertTo(ret, unit);
 * </code> 
 * 
 * 
 *  
 * 
 * @author Aswin
 *
 */
public abstract class Accelerometer extends I2CSensor implements TiltData, AccelerationData{
	//<editor-fold defaultstate="collapsed" desc="Properties">
	
  /**
   * The default unit to use when retuning acceleration data from the accelerometer
   */
  protected AccelUnits accelUnit = AccelUnits.MILLIG;
  /**
   * The default unit to use when retuning tilt data from the accelerometer
   */
  protected TiltUnits tiltUnit = TiltUnits.DEGREES;
  
  /**
   * A factor to use when converting raw data from the accelerometer. Normally multiplier corresponds to 1 / Number of least significant bits per unit. This number and the unit are given in the documentation of the sensor.
   */
  protected float multiplier=1;


	/**
	 * A utility class for handling {@link nl.totan.sensors.AccelCalibration calibration parameters}
	 */
	protected AccelCalibration Settings=new AccelCalibration(this);

	  /**
	   * 
	   * @param port
	   */
	  public Accelerometer(I2CPort port) {
	    this(port, DEFAULT_I2C_ADDRESS);
	  }

	  /**
	   * 
	   * @param port
	   * @param address
	   */
	  public Accelerometer(I2CPort port, int address) {
	    super(port, address, I2CPort.HIGH_SPEED, TYPE_LOWSPEED);
	  }

	  
	  /**
	   * 
	   * @return
	   * A factor to use when converting raw data from the accelerometer.
	   */
	  protected float getMultiplier() {
	  	return multiplier;
	  }
	  
	  /**
	   * Sets the unit for acceleration
	   * @param accelUnit
	   */
	  public void setAccelUnit(AccelUnits accelUnit) {
	    this.accelUnit = accelUnit;
	  }

	  /**
	   * Returns the current acceleration unit
	   * @return accelUnit
	   */
	  public AccelUnits getAccelUnit() {
	    return accelUnit;
	  }
	  
	  /**
	   * Acceleration along 3 axis. 
	   * @return Acceleration in selected units in X,Y, Z order
	   */
	  public void fetchAllAccel(float[] ret) {
	    fetchAllAccel(ret,accelUnit);
	  }


	  /**
	   * Sets the unit for tilt
	   * @param tiltUnit
	   */
	  public void setTiltUnit(TiltUnits tiltUnit) {
	    this.tiltUnit = tiltUnit;
	  }

	  /**
	   * Returns the current unit for tilt
	   * @return
	   */
	  public TiltUnits getTiltUnit() {
	    return tiltUnit;
	  }

	  /**
	   * Tilt along 3 axis
	   * @return Tilt in selected unit
	   */
	  public void fetchAllTilt(float[] ret) {
	    fetchAllTilt(ret, tiltUnit);
	  }

	  /**
	   * Tilt along 3 axis. 
	   * Calculation of tilt is based acceleration and on the fact that under static circumstances (no acceleration) 
	   * the accleration (in G) equals the cosine of the tilt angle.
	   * @param unit of tilt
	   * @return Tilt in provided unit
	   */
	  public void fetchAllTilt(float[] ret, TiltUnits unit) {
	  	// acceleration in G equals the Cosine of the tilt value (under static conditions).
	    fetchAllAccel(ret, AccelUnits.G);
      TiltUnits.COSINE.convertTo(ret, unit);
      }
	  }
	  
	  


	


