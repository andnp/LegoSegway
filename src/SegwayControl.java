import lejos.nxt.comm.RConsole;


public class SegwayControl {
	private static Balancing balancer = new Balancing();
	public static void main(String[] args){
		RConsole.openBluetooth(10000); // open Remote Console
		balancer.start();
	}
}
