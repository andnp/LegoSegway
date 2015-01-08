import lejos.nxt.*;
import lejos.nxt.comm.RConsole;
import nl.totan.sensors.*;


public class Balancing extends Thread{
	public static float MAX_SPEED = Motor.A.getMaxSpeed();
	
	public void run(){
		
		SensorPort.S4.i2cEnable(I2CPort.HIGH_SPEED);
		//MMA7455L accel = new MMA7455L(SensorPort.S4);
		L3G4200D gyro = new L3G4200D(SensorPort.S4);
		
//		float[] startingTilt = getAverageTilt(accel, 15); // Find the starting angle between bot and ground
//		float[] startingRate = getAverageRate(gyro, 15);

		
		float error = 0; // Error sum which should approach zero
		float proportional_error = 0; // Error based on the Proportional part of the PID
		float derivative_error = 0; // Error based on the Derivative part of the PID
		float raw_gyro = 0;
		float estimated_tilt = 0;
		float dt = 0; // change in time
		long start_time = 0;
		
//		float[] tilt = new float[3]; // array containing {x_tilt,y_tilt,z_tilt} in degrees
		float[] rate = new float[3]; // array containing {x_rate,y_rate,z_rate} in degrees/second

		float kp = (float) .50; // Gain (constant) of the proportional portion of the PID
		float kd = (float) .0; // Gain (constant) of the proportional portion of the PID
		
		while(Button.ENTER.isUp()){ // controller loop
			start_time = System.nanoTime();
			
			gyro.fetchAllRate(rate);  // update rate array
//			accel.fetchAllTilt(tilt); // update tilt array
			
//			raw_tilt = tilt[0]; // Read the raw x compenent of the accelerometer in degrees
			raw_gyro = rate[0]; // Read the raw x component of the gyroscope in degrees/sec
			
			derivative_error = raw_gyro;
			proportional_error = estimated_tilt;
			
			error = kp * proportional_error + kd * derivative_error;
			
			//RConsole.println("x_tilt:" + estimated_tilt + "   x_rate:" + raw_gyro);
			setMotorSpeed(error);
			dt = (long) (System.nanoTime() - start_time) / 1000000; // change in time in milliseconds
			estimated_tilt += rate[0] * (dt / 1000);
			RConsole.println("dt: " + dt);
		}
	}
	
	private static float[] getAverageTilt(MMA7455L accel, int trials){
		float[] ret = {0,0,0};
		float[] tilt = new float[3];
		for(int i = 0; i <= trials; i++){
			accel.fetchAllTilt(tilt);
			ret[0] += tilt[0];
			ret[1] += tilt[1];
			ret[2] += tilt[2];
		}
		ret[0] = ret[0] / trials;
		ret[1] = ret[1] / trials;
		ret[2] = ret[2] / trials;
		return ret;
	}
	
	private static void setMotorSpeed(float percent){
		if(percent > 1) percent = 1;
		if(percent < -1) percent = -1;
		if(!Float.isNaN(percent)){
			float motor_speed = MAX_SPEED * percent;
			Motor.A.setSpeed(Math.abs(motor_speed));
			Motor.B.setSpeed(Math.abs(motor_speed));
			if(motor_speed > 0){
				Motor.A.backward();
				Motor.B.backward();
			} else if(motor_speed < 0){
				Motor.A.forward();
				Motor.B.forward();
			}
			//RConsole.println("motor_speed: " + motor_speed);
		}
	}
	
	private static float[] getAverageRate(L3G4200D gyro, int trials){
		float[] ret = {0,0,0};
		float[] rate = new float[3];
		for(int i = 0; i <= trials; i++){
			gyro.fetchAllRate(rate);
			ret[0] += rate[0];
			ret[1] += rate[1];
			ret[2] += rate[2];
		}
		ret[0] = ret[0] / trials;
		ret[1] = ret[1] / trials;
		ret[2] = ret[2] / trials;
		return ret;
	}
}
