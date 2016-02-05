package ir.afshin.netup.base;

import java.util.ArrayList;

import android.util.Pair;

/**
 * @author afshin
 * Not used yet, but will be use soon. This class just keeps datas which are usable to comunicate
 * with server.
 */
public class RequestInfo {

	public static enum Status{DONE, SUSPENDED, SENDING}
	
	public int ID = 0;
	public int type = 0;
	public Status status = Status.SUSPENDED;
	public String title = "";
	public String server = "";
	public ArrayList<Pair<String, String>> getParams = null;
	public ArrayList<PostParam> postParams = null;
	public ArrayList<Pair<String, String>> headers = null;
	
}
