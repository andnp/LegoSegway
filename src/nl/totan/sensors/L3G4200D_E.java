package nl.totan.sensors;

import lejos.nxt.I2CPort;
import nl.totan.util.Formatter;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.util.TextMenu;


/**
 * @author Aswin
 * 
 * Class to access the rate sensor (gyro) from Dexter Industries IMU
 * It provies a user interface via the NXT LCD
 */
public class L3G4200D_E extends L3G4200D implements SensorUserInterface {

	public L3G4200D_E(I2CPort port) {
		super(port);
	}

	public void displayAbout() {
		LCD.clear();
		LCD.drawString(this.getProductID(), 1, 1);
		LCD.drawString(this.getSensorType(), 1, 2);
		LCD.drawString(this.getVersion(), 1, 3);
		LCD.drawString("Escape to return", 0, 7);
		Button.ESCAPE.waitForPressAndRelease();
	}

	public void displaySensorValue() {
		float[] rate = new float[3];
		while (!Button.ESCAPE.isDown()) {
			fetchAllRate(rate);
			LCD.clear();
			LCD.drawString("Rate", 4, 0);
			LCD.drawString("" + getRateUnit(), 4, 1);
			LCD.drawString("X", 1, 2);
			LCD.drawString("Y", 1, 3);
			LCD.drawString("Z", 1, 4);
			LCD.drawString("T", 1, 6);
			LCD.drawString(Float.toString(fetchTemperature()), 4, 6);
			LCD.drawString(" "+getTemperatureUnit(), 8, 6);
			LCD.drawString("Escape to return", 0, 7);
			for (int i = 0; i < 3; i++) {
				LCD.drawString(Formatter.format(rate[i], 3, 1), 4, i + 2);
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException ex) {
			}

		}
		while (Button.ESCAPE.isDown());
	}

	public void runCalibrationMenu() {
		int index = 0;
		String[] menuItems = { "Calculate Offset","Dynamic Offset", "Show Offset" };
		TextMenu menu = new TextMenu(menuItems, 1, getSensorType());
		while (true) {
			LCD.clear();
			LCD.drawString("Escape to return", 0, 7);
			index = menu.select();
			switch (index) {
			case -1:
				return;
			case 0:
				calculateOffset();
				break;
			case 1:
				runDynamicOffsetMenu();
				break;
			case 2:
				displayOffset();
				break;
				default:
					break;
			}
		}
	}

	private void displayOffset() {
		float[] temp={0,0,0};
		LCD.clear();
		LCD.drawString("Auto  Offset", 4, 0);
		LCD.drawString("X", 1, 2);
		LCD.drawString("Y", 1, 3);
		LCD.drawString("Z", 1, 4);
		LCD.drawString("Escape to return", 0, 7);
		while(!Button.ESCAPE.isDown()) {
			for (int i = 0; i < 3; i++) {
				LCD.drawString(Boolean.toString(dynamicOffset[i]), 4, i + 2);
				LCD.drawString(Formatter.format(offset[i], 3, 2), 10, i + 2);
				fetchAllRate(temp);
				try {
					Thread.sleep(50);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		while (Button.ESCAPE.isDown());
	}

	private void runDynamicOffsetMenu() {
		int index = 0;
		while(true) {
			String[] menuItems = { "X  :" + Boolean.toString(dynamicOffset[0]), 
					"Y  :" + Boolean.toString(dynamicOffset[1]),
					"Z  :" + Boolean.toString(dynamicOffset[2])};
			TextMenu menu = new TextMenu(menuItems, 1, "Settings menu");
			LCD.clear();
			LCD.drawString("Escape to return", 0, 7);
			index = menu.select();
			switch (index) {
			case -1:
				return;
			case 0:
				dynamicOffset[0]=!dynamicOffset[0];
				break;
			case 1:
				dynamicOffset[1]=!dynamicOffset[1];
				break;
			case 2:
				dynamicOffset[2]=!dynamicOffset[2];
				break;
			default:
				break;
		}
		}
		
	}

	public void runMenu() {
		int index = 0;
		String[] menuItems = { "View", "Settings", "Calibrate", "About" };
		TextMenu menu = new TextMenu(menuItems, 1, getSensorType());
		while (true) {
			LCD.clear();
			LCD.drawString("Escape to return", 0, 7);
			index = menu.select();
			switch (index) {
			case -1:
				return;
			case 0:
				displaySensorValue();
				break;
			case 1:
				runSettingsMenu();
				break;
			case 2:
				runCalibrationMenu();
				break;
			case 3:
				displayAbout();
			default:
				break;
			}
		}
	}

	public void runSettingsMenu() {
		int index = 0;
		while (true) {
			String[] menuItems = { "Rate  :" + getRateUnit(), "Range :" + getRange(), "Speed:" + getSampleRate() };
			TextMenu menu = new TextMenu(menuItems, 1, "Settings menu");
			LCD.clear();
			LCD.drawString("Escape to return", 0, 7);
			index = menu.select();
			switch (index) {
			case -1:
				return;
			case 0:
				selectRateUnit();
				break;
			case 1:
				selectRange();
				break;
			case 2:
				selectSampleRate();
				break;
			default:
				break;
			}
		}
	}

	public void selectRateUnit() {
		int index = 0;
		String[] menuItems;
		menuItems = new String[RateUnits.values().length];
		int i = 0;
		for (RateUnits p : RateUnits.values()) {
			menuItems[i++] = p.name();
		}
		TextMenu menu = new TextMenu(menuItems, 1, "Select rate unit");
		LCD.clear();
		LCD.drawString("Escape to return", 0, 7);
		RateUnits s;
		s = getRateUnit();
		index = menu.select(s.ordinal());

		if (index != -1) {
			RateUnits[] p;
			p = RateUnits.values();
			setRateUnit(p[index]);
		}
	}
	
	
	public void selectRange() {
			int index = 0;
			String[] menuItems;
			menuItems = new String[Range.values().length];
			int i = 0;
			for (Range p : Range.values()) {
				menuItems[i++] = p.name();
			}
			TextMenu menu = new TextMenu(menuItems, 1, "Select rate unit");
			LCD.clear();
			LCD.drawString("Escape to return", 0, 7);
			Range s;
			s = getRange();
			index = menu.select(s.ordinal());

			if (index != -1) {
				Range[] p;
				p = Range.values();
				setRange(p[index]);
			}
		}
		
	public void selectSampleRate() {
		int index = 0;
		String[] menuItems;
		menuItems = new String[SampleRate.values().length];
		int i = 0;
		for (SampleRate p : SampleRate.values()) {
			menuItems[i++] = p.name();
		}
		TextMenu menu = new TextMenu(menuItems, 1, "Select rate unit");
		LCD.clear();
		LCD.drawString("Escape to return", 0, 7);
		SampleRate s;
		s = getSampleRate();
		index = menu.select(s.ordinal());
 
		if (index != -1) {
			SampleRate[] p;
			p = SampleRate.values();
			setSampleRate(p[index]);
		}
	}
	public void loadCalibration() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void showCalibrationSettings() {
		displayOffset();
	}
	
}
