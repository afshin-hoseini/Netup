package ir.afshin.netup.base;

import android.net.NetworkInfo;

/**
 * @author afshin
 * This class will contains some information about internet connection.
 * <br/>
 * @see ConnectionDescriber#isConnected
 * @see ConnectionDescriber#connectUsing
 * @see ConnectionDescriber#connectionStatus
 */
public class ConnectionDescriber {

	/**
	 * Tells devices is connected or not.
	 */
	public boolean isConnected = false;
	/**
	 * Determines the ways this device is connected to Internet.
	 */
	public int connectUsing = 0 ;
	/**
	 * Connection pkgDlStatus detail.
	 */
	public NetworkInfo.DetailedState connectionStatus = NetworkInfo.DetailedState.IDLE;
}
