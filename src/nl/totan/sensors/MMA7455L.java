package nl.totan.sensors;


import lejos.nxt.I2CPort;

public class MMA7455L extends  Accelerometer {

	protected static final int	ACCEL	= 0x00;
	protected static final int	MODE_REG	= 0x16;
	protected static final int	DEFAULT_ADDRESS	= 0x3A;
	


	
	
	private byte[] raw= new byte[6];
	private int[]  buf=new int[3];
 

	public MMA7455L(I2CPort port) {
		super(port, DEFAULT_ADDRESS);
		sendData(MODE_REG, (byte) 0x01);
		Settings.load();
		multiplier	= 0.015625f;
	}


  public void fetchRawAccel(int[] ret) {
    getData(ACCEL, raw, 6);
    for(int i=0;i<3;i++) {
    	ret[i]=((raw[i*2+1] ) << 8) | (raw[2*i] & 0xFF) & 0x03ff;
    	if (ret[i]>512) ret[i]-=1024;
    }
    //ret[0]=-ret[0];
    //ret[1]=-ret[1];
  }
  
  @Override
  public String getProductID() {
    return "dIMU";
  }

  public String getSensorType() {
    return "MMA7455L";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

	
  /**
   * Acceleration along 3 axis.
   * @param unit of acceleration
   * @return Acceleration in provided unit
   */
  public void fetchAllAccel(float[] ret,AccelUnits unit) {
    fetchRawAccel(buf);
    for (int i = 0; i < 3; i++) {
      ret[i] = (buf[i] - Settings.offset[i]) * multiplier * Settings.scale[i];
    }
    AccelUnits.G.convertTo(ret, unit);
  }

	
}
