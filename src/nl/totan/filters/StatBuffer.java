package nl.totan.filters;


/**
 * This class maintains a simple fixed length buffer of floats and provides statistics on its contents
 * The buffer acts as a FIFO buffer, when it is full and a new value is added to the buffer it will 
 * discard the oldest value to make place for the new value
 * <P>
 * Buffer size is specified during construction. Keep in mind that statistics on larger buffers will
 * take more time.
 * <p> This class was designed for building filters for sensor data. 
 * 
 * @author Aswin
 * @version 1.1
 *
 */
public class StatBuffer {
	protected int bufferSize=0;
	protected int actualSize=0;
	protected int start=0;
	protected int end=0;
	private float[] buffer = null;
	
	public StatBuffer(int bufferSize){
		this.bufferSize=bufferSize;
		buffer= new float[bufferSize];
	}
	
	/**
	 * Adds a value to the buffer. And discards the oldest value when the buffer is full
	 * @param value
	 */
	synchronized public void add(float value) {
		buffer[(start+actualSize) % bufferSize]=value;
		if (actualSize==bufferSize) 
			start = (start + 1) % bufferSize;
		else 
			actualSize++;
		
	}
	
	synchronized public void reset() {
		actualSize=0;
	}
	
	/**
	 * @return
	 * The sum of all the values in the buffer
	 */
	synchronized public float getSum() {
		float ret=0;
		for (int i=start;i<=start+actualSize;i++) 
			ret+=buffer[i % bufferSize];
		return ret;
	}
	
	/**
	 * @return
	 * The mean of all values in the buffer 
	 */
	synchronized public float getMean() {
		if (actualSize>0) 
			return getSum()/actualSize;
		else return Float.NaN;
	}
	
	/**
	 * @return
	 * The number f values in the buffer
	 */
	synchronized public float getN() {
		return actualSize;
	}
	
	/**
	 * @return
	 * The biggest value in the buffer
	 */
	synchronized public float getMax(){
		return getRanked(actualSize);
	}
	
	/**
	 * @return
	 * The smallest value in the buffer
	 */
	synchronized public float getMin(){
		return getRanked(0);
	}

	/**
	 * @return
	 * The median value in the buffer
	 * The median value is the value that is in the middle after sorting all the values.
	 * It is a great alternative for Mean as it is not influenced by occasional extreme values. 
	 */
	synchronized public float getMedian(){
		return getRanked((int)Math.floor(actualSize/2));
	}

	
	
	/**
	 * @param rank
	 * @return
	 * The value of the N biggest item. This is used for other statistics like median, min and max,  
	 */
	protected float getRanked(int rank) {
		// copy buffer to array
		float[] temp=new float[actualSize];
		for (int i=start;i<=start+actualSize;i++) 
			temp[i-start]=buffer[i % bufferSize];
		
		// sort the array
		int out, in;
		float hold;
		for(out=actualSize-1; out>1; out--)  
	     for(in=0; in<out; in++)    
	      if( temp[in] > temp[in+1] ) {
	      	hold=temp[in];
	      	temp[in]=temp[in+1];
	      	temp[in+1]=hold;
	      }

		return temp[rank];
	}
	
	 

}
