package nl.totan.util;


/**
* Elapsed time watch (in nanoseconds) <br>
* To use - construct a new instance.  
*  @author Roger Glassey, modified by Aswin Bouwmeester
*  version 2     
*/
public class Stopwatch
{
/**
records system clock time (in nanoseconds) when reset() was executed
*/
	private	long t0 = System.nanoTime(); 
	
/**
Reset watch to zero
*/
	public void reset()
	{
		t0 = System.nanoTime();
	}
/**
Return elapsed time in milliseconds 
*/
	public int elapsed( )
	{
		return (int)(System.nanoTime() -t0)/1000000;
	}	

	/**
	Return elapsed time in nanoseconds 
	*/	
	public long nanoElapsed( )
	{
		return System.nanoTime() -t0;
	}		

}