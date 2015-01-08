package nl.totan.filters;


import nl.totan.sensors.TiltData;

public class OverSample implements TiltData {
	private class Runner extends Thread {
		public void run() {
			while (true) {
				update();
				try {
					Thread.sleep(interval);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	

	TiltData source = null;
	float[] sample=new float[3];
	static int bufferSize=7;
	StatBuffer[] buffer={new StatBuffer(bufferSize),new StatBuffer(bufferSize),new StatBuffer(bufferSize)};
	float[] buf={0,0,0};
	protected int interval=1;
	
	
	

	public OverSample(TiltData source) {
		this.source=source;
		Runner runner = new Runner();
		runner.setDaemon(true);
		runner.start();
	}
	
	synchronized private void update() {
		source.fetchAllTilt(buf);
		for (int i=0;i<3;i++)
			buffer[i].add(buf[i]);
	}



	@Override
	public TiltUnits getTiltUnit() {
		return source.getTiltUnit();
	}



	@Override
	public void setTiltUnit(TiltUnits unit) {
		source.setTiltUnit(unit);
	}



	@Override
	synchronized public void fetchAllTilt(float[] ret) {
		for (int i=0;i<3;i++) {
			ret[i]=buffer[i].getMean();
			buffer[i].reset();
		}
	}



	@Override
	public void fetchAllTilt(float[] ret, TiltUnits unit) {
		fetchAllTilt(ret);
		source.getTiltUnit().convertTo(ret, unit);
	}


}
