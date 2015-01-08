package nl.totan.filters;

import lejos.nxt.LCD;
import lejos.util.Matrix;
import nl.totan.util.*;
import nl.totan.sensors.*;
import nl.totan.sensors.RateData.RateUnits;

/**
 * A filter that provides attitude information based on fused gyrosensor and
 * accelerometer output.
 * 
 * The filter assumes the sensors are more or less level and facing north at start. It then
 * uses the gyro to calculate a new attitude at a rate of {@link #Frequency}
 * Hertz. The gyro readings are corrected using tilt information from the
 * accelerometer for drift, the values of {@link #kP} and {@link #kI} determine
 * how strong this correction is.
 * <p>
 * Use {@link #getAttitude()}To get the current attitude from the
 * filter.
 * 
 * 
 * @author Aswin Bouwmeester
 * @version 1.0
 * @see <a href="http://gentlenav.googlecode.com/files/DCMDraft2.pdf">An
 *      explanation of the filter</a>
 */


public class NLCFilter implements TiltData {

	/**
	 * Inner class that allows the filter to run in a seperate thread
	 * 
	 * @author Aswin
	 * 
	 */
	private class Filter extends Thread {
		public void run() {
			while (true) {
				if (running) {
					timer2.reset();
					update();
//					if (dataLogger != null) {
//						dataLogger.writeLog(getRoll());
//						dataLogger.writeLog(getPitch());
//						dataLogger.writeLog(getYaw());
//						dataLogger.finishLine();
//					}
				}
				if (targetFrequency > 0) {
					try {
						Thread.sleep(Math.max(0, (int) (1000.0f / targetFrequency - timer2.elapsed())));
					}
					catch (InterruptedException e) {
					}
				}
				Frequency = (1.0f - alpha) * Frequency + alpha * (1000.0f / (float)timer2.elapsed());
			}
		}
	}
	
	/**
	 * aplha is used for the low pass filter that keeps track of the filter frequency.
	 */
	private static float						alpha							= 0.01f;

	/**
	 * vector representing north. Used to calculate yaw.
	 */
	static public final Vector			NORTH							= new Vector(1, 0, 0);

	/**
	 * vector representing vertical. Used to calculate pitch and roll
	 */
	static public final Vector			VERTICAL					= new Vector(0, 0, 1);
	
	/**
	 * Integral of absoltue error. Can be used for tuning Kp and Ki.
	 */
	private Vector									absI							= new Vector();
	
	/**
	 * holds the accelrometer object
	 */
	protected TiltData			accel							= null;
	
	/**
	 * vector to hold accelromer readings.
	 */
	private Vector									acceleration			= new Vector();
	
	/**
	 * direction cosine matrix that holds the attitude of the sensor.
	 */
	private Matrix									attitude					= Matrix.identity(3, 3);
	
	/**
	 * object to the datalogger
	 */
	//private NXTDataLogger						dataLogger				= null;
	
	/**
	 * holds the time interval between filter iterations.
	 */
	private float									dt								= 0;
	
	/**
	 * holds the frequency the filter is actually running at.
	 */
	private float									Frequency					= 0;
	
	/**
	 * holds the gyro object
	 */
	protected RateData					gyro							= null;
	
	/**
	 * holds the integral. Used for correcting gyro drift.
	 */
	protected Vector								iCorrection				= new Vector();
	
	/**
	 * The I factor of the filter. Determines how fast the offset of the gyro is corrected.
	 */
	protected float								kI								= 0.1f;										// 0.01;
	
	/**
	 * The P factor of the filter. Determines how strong the correction by the accelerometer is.
	 */
		protected float								kP								= 0.09f;									// 0.09;
	
		/**
	 * The P-factor that the filter uses during stabalizing.
	 */
	protected float								kPSettle					= 0.5f;
	
	/**
	 * The Target frequency the filter runs at. 
	 */
	private float									targetFrequency			= 100;
	
	/**
	 * Skew symmetric matrix to hold the gyro reading.
	 */
	private Matrix									movement					= Matrix.identity(3, 3);
	
	/**
	 * The vector that holds the P error for the PI correction 
	 */
	
	protected Vector								pCorrection;
	
	/**
	 * The vector that holds the PI error for the PI correction 
	 */
	protected Vector								piCorrection			= new Vector();
	
	/**
	 * The vector that holds the reading from the accelerometer
	 */
	private Vector									rate							= new Vector();
	
	/**
	 * The vector that holds the error for the PI correction 
	 */
	protected Vector								rpError;
	
	/**
	 * Indicates that the filter is running.
	 */
	protected boolean								running						= false;
	
	/**
	 * Indicates that the filter is stabalizing.
	 */
	protected boolean								initializing					= true;
	
	/**
	 * The treshold to finish stabalizing. If the error is below this value the filter exist the settling state.
	 */
	protected float								settlingTreshHold	= 0.01f;									// 0.005;
	
	protected int										n=0;
	
	/**
	 * Timer object to determine dt.
	 */
	private Stopwatch								timer							= new Stopwatch();
	
	/**
	 * Timer object to control the filter frequency.
	 */
	private Stopwatch								timer2						= new Stopwatch();

	/**
	 * The unit to be used when returning filter state (RADIANS, DEGREES or COSINE).
	 */
	protected TiltUnits							tiltUnit							= TiltUnits.RADIANS;

	
	public NLCFilter(RateData gyro) {
		this.gyro = gyro;
		Filter filter = new Filter();
		filter.setDaemon(true);
		filter.start();
	}

	public NLCFilter(RateData gyro, TiltData accel) {
		this(gyro);
		this.accel = accel;
	}

	/**
	 * Adds the absolute error to the absI vector.
	 * This vector can be used to tune the filter. The filter is well tuned if the absolute rror is small. 
	 */
	private void addToAbsI() {
		for (int i = 0; i < 3; i++) {
			absI.set(i, absI.get(i) + Math.abs(rpError.get(i) * dt));
		}
	}

	/**
	 * displays the attitude matrix on the NXT screen.
	 * Used for debugging.
	 */
	public void displayAttitude() {
		displayMatrix(attitude, "Attitude");
	}
	
	public void displayAngles() {
		LCD.drawString("Roll : " + getRoll(),0,0);
		LCD.drawString("Pitch: " + getPitch(),0,1);
		LCD.drawString("Yaw  : " + getYaw(),0,2);
	}

	/**
	 * displays any matrix on the NXT screen.
	 * Used for debugging.
	 * @param a
	 * The matrix to display
	 * @param title
	 * The title to display above the matrix
	 */
	protected void displayMatrix(Matrix a, String title) {
		LCD.drawString(title, 0, 0);
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				LCD.drawString(Formatter.format(a.get(r, c), 2, 2), c * 5, r + 1);
			}
		}
	}

	/**
	 * The filter maintaines the integral of absolute errors. Absolute errors can be used to
	 * tune the filter. The smaller the absolute error is the better the filter is tuned, but 
	 * the noisier the filter output is. 
	 * @return
	 * A vector containing the absolute errors of the three axis.
	 */
	public Vector getAbsI() {
		return absI;
	}

	/**
	 * @return returns a vector containing the current attitude (roll, pitch, yaw) of the robot 
	 * expressed in the selected unit.
	 */
	public Vector getAttitude() {
		Vector ret = new Vector();

		ret.set(0, getRoll());
		ret.set(1, getPitch());
		ret.set(2, getYaw());
		return ret;
	}

	/**
	 * @return 
	 * Returns a copy of the attitude matrix
	 */
	public Matrix getAttitudeMatrix() {
		return attitude.copy();
	}

	/**
	 * @return 
	 * the dotProduct of the Z-row of the attitude matrix and the output
	 * from the accelerometer. This is the error of the filter used in the PI controller.
	 */
	private Vector getErrorFromAccelerometer() {
		// read the accelerometer;
		accel.fetchAllTilt(acceleration.getArray(), TiltUnits.COSINE);
		// get the Z-row from the attitude matrix
		Vector Zrow = new Vector(attitude, 0, 2);
		// the cross product between normalized gravity vector and Z-row is the
		// error
		// return Zrow.cross(acceleration.devide(acceleration.length()));
		acceleration.normalize();
		return acceleration.cross(Zrow);
		//return Zrow.cross(acceleration);
	}

	/**
	 * @return 
	 * Returns the actual freqency the filter runs at. 
	 */
	public float getFrequency() {
		return Frequency;
	}

	/**
	 * @return 
	 * Returns the I-value of the filter
	 */
	public float getKi() {
		return kI;
	}

	/**
	 * @return 
	 * Returns the P-value of the filter
	 */
	public float getKp() {
		return kP;
	}

	/**
	 * @return 
	 * Returns the target frequency for the the filter.
	 */
	public float getMaxFrequency() {
		return targetFrequency;
	}

	/**
	 * @return
	 * returns the pitch, the rotation around the Y axis.
	 */
	public float getPitch() {
		// return convertToUnit(new Vector(attitude,1,0).dot(VERTICAL));
		return TiltUnits.RADIANS.convertTo((float)-Math.asin(attitude.get(2, 0)),  tiltUnit);
	}

	/**
	 * @return
	 * returns the roll, the rotation around the X axis.
	 */
	public float getRoll() {
		// return convertToUnit(new Vector(attitude,1,1).dot(VERTICAL));
		return TiltUnits.RADIANS.convertTo((float)Math.atan2(attitude.get(2, 1), attitude.get(2, 2)), tiltUnit);

	}

	/**
	 * @return
	 * Returns the unit that the filter uses to report pitch, roll, and yaw
	 */
	public TiltUnits getUnit() {
		return tiltUnit;
	}

	/**
	 * @return
	 * returns the yaw, the rotation around the Z axis.
	 */
	public float getYaw() {
		// return convertToUnit(Math.cos(Math.asin(new
		// Vector(attitude,1,0).cross(NORTH).length())));
		return TiltUnits.RADIANS.convertTo((float)Math.atan2(attitude.get(1, 0), attitude.get(0, 0)), tiltUnit);
	}

	/**
	 * @return 
	 * Indicates whether the filter is running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @return
	 * Indicates whether the filter is initializing
	 */
	public boolean isInitializing() {
		return initializing;
	}

	/**
	 * Removes numerical errors in the attitude matrix that build up over time
	 */
	private void normalize() {

		// Normalizes the attitude matrix (remove the build up of small errors)
		Vector X = new Vector(attitude, 0, 0);
		Vector Y = new Vector(attitude, 0, 1);
		Vector Xor, Yor, Zor;
		float error = X.dot(Y);
		if (Math.abs(error) > 0.01) {
			Xor = X.substract(Y.product(error / 2.0f));
			Yor = Y.substract(X.product(error / 2.0f));
			Zor = Xor.cross(Yor);

			Xor.productEquals((3.0f - Xor.dot(Xor)) / 2.0f);
			Yor.productEquals((3.0f - Yor.dot(Yor)) / 2.0f);
			Zor.productEquals((3.0f - Zor.dot(Zor)) / 2.0f);
			for (int i = 0; i < 3; i++)
				attitude.set(0, i, Xor.get(i));
			for (int i = 0; i < 3; i++)
				attitude.set(1, i, Yor.get(i));
			for (int i = 0; i < 3; i++)
				attitude.set(2, i, Zor.get(i));
		}

	}

	/**
	 * Resets the filter, the sensor is assumed to face north and more or less level
	 */

	private void reset() {
		attitude = Matrix.identity(3, 3);
		iCorrection = new Vector();
		piCorrection = new Vector();
		absI = new Vector();
		initializing = true;
		n=0;
//		if (dataLogger != null) dataLogger.writeComment("Reset");
	}

	/**
	 * Resumes running of the filter
	 */
	public void resume() {
		running = true;
//		if (dataLogger != null) dataLogger.writeComment("Resume");

	}

	/**
	 * The filter can log data to a datalogger on the PC. Providing a datalogger object with this
	 * methods automaticly starts the logging facility
	 * @param dataLogger
	 * A datalogger object
	 */
//	public void setDataLogger(NXTDataLogger dataLogger) {
//		this.dataLogger = dataLogger;
//		LogColumn[] logColumns = { new LogColumn("Rate Sensor X", LogColumn.DT_FLOAT), new LogColumn("Rate Sensor Y", LogColumn.DT_FLOAT), new LogColumn("Rate Sensor Z", LogColumn.DT_FLOAT),
//				new LogColumn("IMU filter X", LogColumn.DT_FLOAT), new LogColumn("IMU filter Y", LogColumn.DT_FLOAT), new LogColumn("IMU filter Z", LogColumn.DT_FLOAT),
//				new LogColumn("Accelerometer X", LogColumn.DT_FLOAT), new LogColumn("Accelerometer Y", LogColumn.DT_FLOAT), new LogColumn("Accelerometer Z", LogColumn.DT_FLOAT) };
//		dataLogger.setColumns(logColumns);
//	}

	/**
	 * The I-value of the filter controls how fast the filter adjusts to change in gyro offset 
	 * @param ki
	 *          the I-value of the filter
	 */
	public void setKi(float ki) {
		kI = ki;
	}

	/**
	 * The P-value of the filter controls how fast the filter corrects the gyro data with accelerometer data
	 * When the P-value is too high the filter will suffer from noise and acceleration
	 * When it is too low the filter will recover slowly from gyro and integration errors 
	 * @param kp
	 *          the P-value of the filter
	 */
	public void setKp(float kp) {
		kP = kp;
	}

	/**
	 * @param maxFrequency
	 *          the target frequency the filter runs at. Lower this value to save
	 *          CPU time, raise it to increase the quality of the filter on fast
	 *          moveing robots. Set to 0 to run the filter at maximum speed
	 */
	public void setMaxFrequency(float targetFrequency) {
		this.targetFrequency = targetFrequency;
	}

	/**
	 * The filter can report attitude in different units (DEGREES, RADIANS and COSINE)
	 * Use COSINE as input for controllers as it is most efficient
	 * @param unit
	 */
	public void setUnit(TiltUnits unit) {
		this.tiltUnit = unit;
	}

	/**
	 * Starts the filter after a stop. Assumes the sensors facing north 
	 */
	public void start() {
		stop();
		reset();
		running = true;
//		if (dataLogger != null) dataLogger.writeComment("Start");

	}

	/**
	 * Stops the filter from running
	 */
	public void stop() {
		running = false;
//		if (dataLogger != null) dataLogger.writeComment("Stop");

	}

	/**
	 * Suspends running of the filter without resetting internal state
	 */
	public void suspend() {
		running = false;
//		if (dataLogger != null) dataLogger.writeComment("Suspend");

	}
	
	public Matrix transformToWorld(Matrix in) {
		return attitude.times(in);
	}
	

	/**
	 * Updates the attitude. Contains of the filter algorithm.
	 */
	private void update() {
		// fetch rate sensor (=gyro) data
		gyro.fetchAllRate(rate.getArray(), RateUnits.RPS);
		// get time period since last iteration
		dt = ((float) timer.nanoElapsed()) / 1000000000.0f;
		timer.reset();
		// add correction (correction was calculated during last update)
		rate.sumEquals(piCorrection);
		// convert rate to displacement (rad/sec -> rad);
		rate.productEquals(dt);
		// convert to antisymmetric matrix
		movement.set(0, 1, -rate.get(2));
		movement.set(0, 2, rate.get(1));
		movement.set(1, 0, rate.get(2));
		movement.set(1, 2, -rate.get(0));
		movement.set(2, 0, -rate.get(1));
		movement.set(2, 1, rate.get(0));
		// Update attitude matrix with movement matrix;
		attitude = attitude.times(movement);
		// normalize matrix to remove numerical errors
		normalize();
		// calculate correction;
		if (accel != null) calculateCorrection();
	}
	
	private void calculateCorrection ()  {
		rpError = getErrorFromAccelerometer();
		if (initializing) {
			// if the filter is initializing the integral is ignored
			piCorrection = rpError.product(kPSettle);
			if ((rpError.length()) < settlingTreshHold) {
				// if the error is small enough for ten times then initialization phase is ended
				if (n++>10) {
					initializing = false;
//					if (dataLogger != null) dataLogger.writeComment("Initialized");
				}
			}
		}
		else {
			pCorrection = rpError.product(kP);
			iCorrection.sumEquals(rpError.product(kI * dt));
			addToAbsI();
			piCorrection = pCorrection.sum(iCorrection);
		}
	}


	@Override
	public TiltUnits getTiltUnit() {
		return tiltUnit;
	}

	@Override
	public void setTiltUnit(TiltUnits unit) {
		this.tiltUnit=unit;
	}

	@Override
	public void fetchAllTilt(float[] ret) {
		fetchAllTilt(ret, tiltUnit);
	}

	@Override
	public void fetchAllTilt(float[] ret, TiltUnits unit) {
		ret[0]=TiltUnits.RADIANS.convertTo((float)Math.atan2(attitude.get(2, 1), attitude.get(2, 2)), unit);
		ret[1]=TiltUnits.RADIANS.convertTo((float)-Math.asin(attitude.get(2, 0)), unit);
		ret[2]=TiltUnits.RADIANS.convertTo((float)Math.atan2(attitude.get(1, 0), attitude.get(0, 0)), unit);
	}

}
