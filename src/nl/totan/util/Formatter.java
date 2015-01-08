package nl.totan.util;

public class Formatter {

	public static String format(double val, int pre, int post) {
		String ret;
		if (val >= Math.pow(10, pre))
			ret = " ++";
		else {
			if (val <= -Math.pow(10, pre - 1))
				ret = " --";
			else {
				if (val !=0 && Math.abs(val) < Math.pow(10, -(post+1)))
					ret = " <<";
				else {

					ret = Double.toString(val);
					ret.trim();
					int p = ret.indexOf(".");
					int q = ret.indexOf("N");
					if (p > -1 && q == -1) {
						for (int i = p; i < pre; i++)
							ret = " " + ret;
						ret = ret + "000000000";
						ret = ret.substring(0, pre + post + 1);
					}

				}
			}

		}
	return ret;
	}
}
