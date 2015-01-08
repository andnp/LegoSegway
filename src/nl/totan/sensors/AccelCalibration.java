package nl.totan.sensors;

import java.io.*;

import nl.totan.sensors.AccelerationData.AccelUnits;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.util.TextMenu;


/**
 * This class provides functionallity to calibrate accelerometers.
 * Calibration consist of offset and scale correction.
 * Calibration is done from a calibration menu that is invoked by <code>{@link #runCalibrationMenu}</code>.
 * <p>
 * Calibration settings can be stored in flash memory using the <code>{@link #save}</code> method and retreived from flash by the <code>{@link #load}</code> method. If the <code>load</code> method cannot
 * find a set of stored settings it will use default settings (offset=0, scale=1). 
 * The settings are stored with a filename that is equal to the name of the sensor, as returned by the <code>{@link #getProductId}</code> method. Only one set of settings can be stored per sensor.
 * 
 * @author Aswin
 * @version 1.0
 *
 */
public class AccelCalibration {

	/**
	 * Offset is used for offset correction. It corresponds to the value returned by the accelerometer while zero acceleration.
	 */
	protected float[]	offset	= { 0, 0, 0 };
	/**
	 * Scale is used for scale correction. it is used as a factor in correction values returned by the accelerometer. 
	 */
	protected float[]	scale		= { 1, 1, 1 };
	
	protected int samples=400;
	
	protected Accelerometer accel = null;
	
	protected String name;
	
	int[] raw=new int[3];
	float[] value=new float[3]; 
	
	
	/**
	 * Constructor of the <code>AccelCalibration</code> class.
	 * @param accel
	 * Accel should have the pointer to the accelerometer class that uses the calibration settings.
	 * When invoked from within the accelerometer class (as is normal behaviour) the invokation looks like this
	 * <code>AccelCalibration settings=new AccelCalibration(this);</code>
	 */
	AccelCalibration(Accelerometer accel) {
		this.accel=accel;
		//name=accel.getSensorType();

	}

	/**
	 * Saves the offset and scale factors in Flash memory.
	 */
	public void save() {
		File store = new File(name);
		FileOutputStream out = null;
		if (store.exists())
			store.delete();
		try {
			out = new FileOutputStream(store);
		}
		catch (FileNotFoundException e) {
			System.err.println("Failed to save calibration");
			System.exit(1);
		}
		DataOutputStream dataOut = new DataOutputStream(out);
		try {
			for (int i = 0; i < 3; i++) {
				dataOut.writeFloat(offset[i]);
				dataOut.writeFloat(scale[i]);
			}
			out.close();
		}
		catch (IOException e) {
			System.err.println("Failed to save calibration");
			System.exit(1);
		}
	}

	/**
	 * Loads saved offset and scale values from flash. If no saved values are found it will load default values for offset=0 and scale =1;
	 */
	public void load() {
		File store = new File(name);
		FileInputStream in = null;
		if (store.exists()) {
			try {
				in = new FileInputStream(store);
				DataInputStream din = new DataInputStream(in);
				for (int i = 0; i < 3; i++) {
					offset[i] = din.readFloat();
					scale[i] = din.readFloat();
				}
				din.close();
			}
			catch (IOException e) {
				System.err.println("Failed to load calibration");
				Sound.beep();
			}
		}
		else {
			for (int i = 0; i < 3; i++) {
				offset[i] = 0;
				scale[i] = 1;
			}
		}
	}
	
  /**
   * Runs the calibration interface of the sensor.
   * The interface allows to calibrate different axis and to view, save and load calibration settings.
   */
  public void runCalibrationMenu() {
    int index = 0;
    String[] menuItems = {"X-axis", "Y-axis", "Z-axis", "Show settings", "Load settings","Save settings"};
    TextMenu menu = new TextMenu(menuItems, 1, "Calibrate");
    while (true) {
      index = menu.select();
      switch (index) {
        case -1:
          return;
        case 0:
          calibrateAxis('X');
          break;
        case 1:
          calibrateAxis('Y');
          break;
        case 2:
          calibrateAxis('Z');
          break;
        case 3:
          showCalibrationSettings();
          break;
        case 4:
          this.load();
          break;
        case 5:
          this.save();

        default:
          break;
      }
    }
  }

	/**
   * Calibrates a single axis. The calibration process consists of determining static acceleration values due to gravity by placing the axis opposite and in the direction of gravity.
   * This gives a minimum and maximum value. The difference between the two is equal to 2G and used to calculate a scale factor. The mean of the two corresponds to 0G and equals the offset value. 
   * <p>
   * Calibration settings are held in memory. To store these values one should use the <code>{@link #save}</code> method.
   * @param axis 
   * Axis should have the value X,Y or Z
   */
  public void calibrateAxis(char axis) {
    float max = 0;
    float min = 0;
    float minG=0;
    float maxG=0;
    float oldOffset=0;
    float oldScale=0;
    int index = 0;
    while (Button.ENTER.isDown()) ;
    switch (axis) {
      case 'X':
        index = 0;
        break;
      case 'Y':
        index = 1;
        break;
      case 'Z':
        index = 2;
        break;
      default:
        return;
    }
    oldOffset=offset[index];
    oldScale=scale[index];
    offset[index]=0;
    scale[index]=1;
    LCD.clear();
    LCD.drawString("Point " + axis + " up.", 0, 1);
    LCD.drawString("Press enter to start", 0, 2);
    showLowPass(index);
    LCD.drawString("Sampling...         ", 0, 2);
    Sound.beep();
    max = getRawMean(index);
    maxG = getMeanG(index);
    Sound.beep();

    LCD.clear();
    LCD.drawString("Point " + axis + " down.", 0, 1);
    LCD.drawString("Press enter to start", 0, 2);
    showLowPass(index);
    LCD.drawString("Sampling...         ", 0, 2);
    Sound.beep();
    min = getRawMean(index);
    minG = getMeanG(index);
    Sound.beep();

    LCD.clear();
    LCD.drawString("offset range", 4, 0);
    LCD.drawString("old", 0, 2);
    LCD.drawString(Double.toString(oldOffset), 4, 2);
    LCD.drawString(Double.toString(oldScale), 11, 2);
    LCD.drawString("new", 0, 3);
    offset[index] = (min + max) / 2.f;
    scale[index] = 2.f/ (maxG - minG);
    LCD.drawString(Double.toString(offset[index]), 4, 3);
    LCD.drawString(Double.toString(scale[index]), 11, 3);
    LCD.drawString("Press enter", 0, 7);
    Button.ENTER.waitForPressAndRelease();

  }
  
  /**
   * returns the mean of N raw values of aixs indicated by the index number .
   * @param index
   * Indicates the axis (x=0,y=1,z=2).
  * @return
   * The mean value.
   */
  private float getRawMean(int index) {
  	int t=0;
    for (int i = 1; i <= samples; i++) {
    	accel.fetchRawAccel(raw);
      t += raw[index];
      try { Thread.sleep(10);} catch (InterruptedException ex) {}
    }
    return t / samples;
  }
  
  /**
   * returns the mean of samples  values of aixs indicated by the index number .
   * @param index
   * Indicates the axis (x=0,y=1,z=2).
   * @return
   * The mean value.
   */
  private float getMeanG(int index) {
  	float t=0;
    for (int i = 1; i <= samples; i++) {
    	accel.fetchAllAccel(value,AccelUnits.G);
      t += value[index];
      try { Thread.sleep(10);} catch (InterruptedException ex) {}
    }
    return t / samples;
  }
   
  /**
   * Displays the raw value of axis indicated by index. A low pass filter is applied on the raw values to make reading easier.
   * @param index
   * Indicates the axis (x=0,y=1,z=2).
   * 
   */
  private void showLowPass(int index){
    float alpha=0.95f;
    float lowpassed=0;
    while (!Button.ENTER.isDown()) {
      accel.fetchRawAccel(raw);
      lowpassed=alpha*raw[index]+ (1.0f-alpha)*lowpassed;
      LCD.drawString(Double.toString(lowpassed),0,3);
      try { Thread.sleep(150);} catch (InterruptedException ex) {}
    }
    while (Button.ENTER.isDown());

  	
  }

  /**
   * Shows the calibration parameters, ie offset and scale 
   */
  public void showCalibrationSettings() {
    LCD.clear();
    LCD.drawString("offset range", 3, 0);
    LCD.drawString("X", 0, 2);
    LCD.drawString("Y", 0, 3);
    LCD.drawString("Z", 0, 4);
    LCD.drawString("Escape to return", 0, 7);
    for (int i = 0; i < 3; i++) {
      LCD.drawString(Double.toString(offset[i]), 3, i + 2);
      LCD.drawString(" ", 11, i + 2);
      LCD.drawString(Double.toString(scale[i]), 10, i + 2);
    }
    Button.ESCAPE.waitForPressAndRelease();

  }
	

}
