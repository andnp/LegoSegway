import lejos.nxt.I2CPort;
import lejos.nxt.SensorPort;
import lejos.nxt.comm.RConsole;
import nl.totan.sensors.L3G4200D;


public class Attitude extends Thread {
	
	private static boolean thread_is_running = true;
	private static boolean is_starting = true;
	public static float estimated_tilt = 0;
	public static float raw_gyro = 0; // Raw sensor read from gyroscope
//	float raw_tilt = 0; // Raw sensor read from accelerometer

	public void run(){
		SensorPort.S4.i2cEnable(I2CPort.HIGH_SPEED);
		//MMA7455L accel = new MMA7455L(SensorPort.S4);
		L3G4200D gyro = new L3G4200D(SensorPort.S4);
		
//		float[] tilt = new float[3]; // array containing {x_tilt,y_tilt,z_tilt} in degrees
		float[] rate = new float[3]; // array containing {x_rate,y_rate,z_rate} in degrees/second
		
		float dt = 0; // change in time
		long start_time = 0;
		
		is_starting = false;
		while(thread_is_running){
			start_time = System.nanoTime();
			
//			accel.fetchAllTilt(tilt); // update tilt array
			gyro.fetchAllRate(rate);  // update rate array
			
//			raw_tilt = tilt[0]; // Read the raw x compenent of the accelerometer in degrees
			raw_gyro = rate[0]; // Read the raw x component of the gyroscope in degrees/sec
			
			dt = (long) (System.nanoTime() - start_time) / 1000000; // change in time in milliseconds
			estimated_tilt += rate[0] * (dt / 1000);
			RConsole.println("dt: " + dt);
		}
	}
	
	public void killThread(){
		thread_is_running = false;
	}
	
	public static float getEstimatedTilt(){
		return estimated_tilt;
	}
	
	public static float getRawGyro(){
		return raw_gyro;
	}
	
	public boolean isStarting(){
		return is_starting;
	}
}
