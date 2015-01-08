package nl.totan.sensors;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.nxt.Sound;


/**
 * Class to access the rate sensor (gyro) from Dexter Industries IMU.
 * It fetches rate data and temperature data from the sensor and allows users to set the sample rate and dynamic range of the sensor
 *
 * @author Aswin.Bouwmeester
 * @version 1.0
 * 
 */

public class L3G4200D extends I2CSensor implements RateData, TemperatureData {

	/**
	 * Internal sample rates supported by the sensor
	 */
	public enum SampleRate {

		_100Hz((byte) 0x00), 
		_200Hz((byte) 0x40), 
		_400Hz((byte) 0x80), 
		_800Hz((byte) 0xC0);
		
		private final byte	code;

		SampleRate(byte code) {
			this.code = code;
		}

		public byte getCode() {
			return code;
		}
	}

	/**
	 * Dynamic ranges supported by the sensor
	 */
	public enum Range {
		_250DPS((byte) 0x00, 8.75f), 
		_500DPS((byte) 0x10, 17.5f), 
		_2000DPS((byte) 0x20, 70f);
		private final byte	code;
		private final float	multiplier;

		Range(byte code, float multiplier) {
			this.code = code;
			this.multiplier = multiplier;
		}

		public byte getCode() {
			return code;
		}

		public float getMultiplier() {
			return multiplier;
		}

	}

	private static int				CTRL_REG1				= 0x020;
	private static int				CTRL_REG2				= 0x021;
	private static int				CTRL_REG3				= 0x022;
	private static int				CTRL_REG4				= 0x023;
	private static int				CTRL_REG5				= 0x024;
	private static int				REG_RATE				= 0x28 | 0x80;
	private static int				SAMPLESIZE			= 400;
	private static int				REG_TEMP				= 0x26;
	private static int				REG_STATUS			= 0x27;
	private static int 				address 				= 0xD2;
	private static int 				DATA_REG 				= 0x27 | 0x80;

	protected float						I								= 0.0001f;
	protected boolean[]				dynamicOffset		= { false, false, false };
	protected float[]					offset					= { 0, 0, 0 };

	protected Range						range						= Range._500DPS;
	protected SampleRate			sampleRate			= SampleRate._800Hz;

	protected TemperatureUnits temperatureUnit= TemperatureUnits.CELCIUS;
	protected RateUnits				rateUnit				= RateUnits.DPS;

	byte[]										buf							= new byte[16];
	int[]											raw							= new int[16];
	float[]										t								= new float[3];
	float											multiplier;

	
	public L3G4200D(I2CPort port) {
		super(port, address, I2CPort.LEGO_MODE, I2CPort.TYPE_LOWSPEED);
		init();
	}

	/**
	 * configures the sensor. Method is called whenever one of the sensor settings changes
	 */

	private void init() {
		int reg;
		// put in sleep mode;
		sendData(CTRL_REG1, (byte) 0x08);
		// oHigh-pass cut off 1 Hz;
		sendData(CTRL_REG2, (byte) 0x00);
		// no interrupts, no fifo
		sendData(CTRL_REG3, (byte) 0x08);
		// set range
		reg = range.getCode() | 0x80;
		multiplier = (float) range.getMultiplier() / 1000f;
		sendData(CTRL_REG4, (byte) reg);
		// disable fifo and high pass
		sendData(CTRL_REG5, (byte) 0x00);
		// stabilize output signal;
		// enable all axis, set output data rate ;
		reg = sampleRate.getCode() | 0x3F;
		// set sample rate, wake up
		sendData(CTRL_REG1, (byte) reg);
		for (int s = 1; s <= 15; s++) {
			while(!isNewDataAvailable()) Thread.yield();
			getRawRate(raw);
		}
		this.calculateOffset();
	}


	private byte getStatus() {
		getData(REG_STATUS, buf, 1);
		return buf[0];
	}

	protected boolean isDataOverrun() {
		if ((getStatus() & 0x80) == 0x80)
			return true;
		else
			return false;
	}

	/**
	 * Returns true if new data is available from the sensor
	 */
	public boolean isNewDataAvailable() {
		if ((getStatus() & 0x08) == 0x08)
			return true;
		else
			return false;
	}

	public void setSampleRate(SampleRate rate) {
		sampleRate = rate;
		init();
	}

	public void setRange(Range range) {
		this.range = range;
		init();
	}

	public final Range getRange() {
		return range;
	}

	public final SampleRate getSampleRate() {
		return sampleRate;

	}

	@Override
	public RateUnits getRateUnit() {
		return rateUnit;
	}

	@Override
	public void setRateUnit(RateUnits unit) {
		this.rateUnit = unit;
	}

	@Override
	public void fetchAllRate(float[] ret) {
		fetchAllRate(ret, rateUnit);

	}

	@Override
	public void fetchAllRate(float[] ret, RateUnits unit) {
		getRawRate(raw);
		for (int i = 0; i < 3; i++) {
			ret[i] = ((float) raw[i] - offset[i]) * multiplier;
		}
		RateUnits.DPS.convertTo(ret,unit);
	}

	public void getRawRate(int[] ret) {
		buf[0]=0;
		int attempts=0;
		// loop while no new data available or data overrun occured, break out after 20 attempts
		while(((buf[0] &  0x80) == 0x80 || (buf[0] & 0x08) != 0x08) && attempts++<=20) {
			getData(DATA_REG, buf, 7);
			if ((buf[0] & 0x08) != 0x08) Thread.yield();
		}
		if (attempts>=20) Sound.beep();
		// The Dexter IMU has swapped the x and y axis of the gyro;
		ret[0] = ((buf[4]) << 8) | (buf[3] & 0xFF);
		ret[1] = -((buf[2]) << 8) | (buf[1] & 0xFF);
		ret[2] = ((buf[6]) << 8) | (buf[5] & 0xFF);
		for (int i = 0; i < 3; i++) {
			if (dynamicOffset[i])
				offset[i] = (1 - I) * offset[i] + I * ret[i];
		}
	}

	public  void calculateOffset() {
		for (int s = 1; s <= SAMPLESIZE / 2; s++) {
			getRawRate(raw);
		}
		for (int i = 0; i < 3; i++)
			offset[i] = 0;
		for (int s = 1; s <= SAMPLESIZE; s++) {
			getRawRate(raw);
			for (int i = 0; i < 3; i++)
				offset[i] += raw[i];
			try {
				Thread.sleep(12);
			}
			catch (InterruptedException e) {
			}
		}
		for (int i = 0; i < 3; i++)
			offset[i] /= SAMPLESIZE;
	}

	public float getOffsetCorrectionSpeed() {
		return I;
	}

	public void setOffsetCorrectionSpeed(float i) {
		I = i;
	}

	public boolean[] getDynamicOffset() {
		return dynamicOffset.clone();
	}

	
	/**
	 * Enables or disables the dynamic offset correction mechanism of the sensor
	 * Dynamic offset correction assumes that in the long run the sensor keeps its orientation.
	 * It updates the offset using a I-controller. The update speed (I-factor) is controlled by 
	 * setOffsetCorrectionSpeed().
	 * @param set
	 * an 3-element array of booleans. True enables Dynamic Offset Correction, false disables it.
	 * Order of elements is X, Y, Z.
	 */
	public void setDynamicOffset(boolean[] set) {
		dynamicOffset = set.clone();
	}

	@Override
	public String getProductID() {
		return "dIMU";
	}

	public String getSensorType() {
		return "L3G4200D";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}
	
	
	/* (non-Javadoc)
	 * @see nl.totan.sensors.TemperatureData#fetchTemperature()
	 * Temperature is not calibrated
	 */
	public float fetchTemperature() {
		float temp = 0;
		getData(REG_TEMP, buf, 1);
		temp = 50.0f - buf[0];
		if (temperatureUnit == TemperatureUnits.FAHRENHEIT)
			temp = 1.8f * temp + 32;
		return temp;
	}

	@Override
	public TemperatureUnits getTemperatureUnit() {
		return temperatureUnit;
	}

	@Override
	public void setTemperatureUnit(TemperatureUnits unit) {
		temperatureUnit = unit;

	}

}
