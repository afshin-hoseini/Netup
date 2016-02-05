package ir.afshin.netup.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import ir.afshin.netup.base.PostParam.ParamType;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 
 * @author afshin - May 8, 2013 at 8:12:07 AM
 * Handles internet connections and also uploads any data type in any format.
 *
 */
public class InternetManager extends Thread {

	
	HttpURLConnection connection = null;
	protected Context ctx = null;
	protected int reqCode;
	protected ArrayList<Pair<String, String>> Headers;
	protected ArrayList<Pair<String, String>> get_params;
	protected ArrayList<PostParam> post_params;
	protected OnConnectionResultListener listener;
	protected String url = "";
	
	protected boolean cancelWork = false;
	
	String dashes = "--";
	String crlf = Character.toString((char)0x0d) + Character.toString((char)0x0a);//"\r\n";
	String boundary = "----A35cxyzA35cxyzA35cxyzA35cxyz";
	
	String multiPartPostHDR = "Content-type: multipart/form-data; boundary=" + boundary + crlf;
	/**
	 * Parameters:
	 * 1- form object name.
	 */
	String formDataHeader = crlf+dashes+boundary + crlf + "Content-Disposition: form-data; name=\"%1$s\"";
	
	/**
	 * Parameters:<br/>
	 * 1- form object name.<br/>
	 * 2- form object value.<br/>
	 */
	String formDataPost = formDataHeader /*+ crlf + "Content-Type:application/octet-stream" */+ crlf + crlf + "%2$s" ;
	
	/**
	 * Parameters:<br/>
	 * 1- form object name.<br/>
	 * 2- filename.<br/>
	 * 3- content type.<br/>
	 */
	String fileHeader = formDataHeader +  "; filename=\"%2$s\"" + crlf + "Content-type:%3$s" + crlf + crlf;

	private boolean methodManuallySet = false;
	private String methodType = "";

	public enum Methods{
		GET, POST, PUT, DELETE, Auto
	}

	int timeOut = 20000;

// ____________________________________________________________________
	/**
	 * Starts a connection to given url address with given parameters
	 * @param ctx
	 * @param url Url address.
	 * @param reqCode A request code to distinguish responses.
	 * @param Headers 
	 * @param get_params 
	 * @param post_params 
	 * @param listener 
	 * @param tag Any object to tag to response.
	 */
	public void connect(final Context ctx, String url, final int reqCode, final ArrayList<Pair<String, String>> Headers, final ArrayList<Pair<String, String>> get_params,final ArrayList<PostParam> post_params, final OnConnectionResultListener listener, Object tag)
	{
		
		this.ctx = ctx;
		this.url = url;
		this.reqCode = reqCode;
		this.Headers = Headers;
		this.get_params = get_params;
		this.post_params = post_params;
		this.listener = listener;
		
		cancelWork = false;
		start();	
		
	}
// ____________________________________________________________________
	public void setMethod(Methods method)
	{
		if(method == Methods.Auto)
			methodManuallySet = false;
		else {
			methodManuallySet = true;
			switch (method)
			{
				case GET: methodType = "GET"; break;
				case POST: methodType = "POST"; break;
				case PUT: methodType = "PUT"; break;
				case DELETE: methodType = "DELETE"; break;
			}
		}

	}
// ____________________________________________________________________
	public void connect(Context ctx, OnConnectionResultListener listener)
	{
		this.ctx = ctx;
		cancelWork = false;
		this.listener = listener;
		start();
	}

// ____________________________________________________________________

	@Override
	public synchronized void start() {

		setDaemon(true);
		super.start();
	}

// ____________________________________________________________________

	public void setTimeOut(int timeOut)
	{
		this.timeOut = timeOut;
	}


// ____________________________________________________________________
	/**
	 * Disconnects connection and also informs listener about disconnection.<br/>
	 * <h1>Note:</h1>
	 * Calling this function perfectly cancels upload actions, but if streaming is in download pkgDlStatus, this method just tell the listener
	 * that cancel notified. The listener could response the cancellation depend on it's situation. <b>That means the listener HAVE TO handle
	 * download action itself...</b>
	 */
	public void disconnect()
	{
		cancelWork = true;
		
		if(listener != null) {
			listener.onConnectionStatusChanged(ConnectionStatus.CANCELED);
			listener.onFinish(0, ConnectionStatus.CANCELED, 0, reqCode, null, null, OnConnectionResultListener.streamingStatus.UPLOAD);
		}
		
		if(connection != null)
			try{
				connection.disconnect();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		
	}

	
// ____________________________________________________________________
	/**
	 * Checks if device is connected or not.
	 * @param ctx
	 * @return
	 */
	public static ConnectionDescriber isConnected(Context ctx)
	{
		ConnectionDescriber desc = new ConnectionDescriber();
		
		ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if(activeNetwork == null)
			desc.isConnected = false;
		else
		{
			desc.isConnected = activeNetwork.isConnectedOrConnecting();
			desc.connectUsing = activeNetwork.getType();
			desc.connectionStatus = activeNetwork.getDetailedState();
		}
		
		return desc;
	}
// ____________________________________________________________________
	private void addHeaders(ArrayList<Pair<String, String>> headers)
	{
		//connection.getRequestProperties().clear();

		if(connection == null)
			return;
		
		//Attaches session id if any, from last 
		if(ConnectionInfo.cookie != null && ConnectionInfo.cookie.length() > 0)
		{
			if(headers == null)
				headers = new ArrayList<Pair<String,String>>();
			headers.add(new Pair<String, String>("Set-Cookie", ConnectionInfo.cookie));
		}
		
		if(headers == null || headers.size() == 0)
			return;
		
		for (Pair<String, String> header : headers) {

			connection.addRequestProperty(header.first, header.second);


		}


		
	}
// ____________________________________________________________________
	private String addGetParams(String url, ArrayList<Pair<String, String>> params)
	{
		if(params == null || params.size() == 0)
			return url;
		
		String getParams = "";
		boolean isNotFirstParam = false;
		
		for (Pair<String, String> param : params) {
			getParams += (isNotFirstParam ? "&":"");
			getParams += (param.first.equals("") ? "":param.first+"=") + param.second; // StringManager.urlUtf8Encoder(param.second, "%");
			isNotFirstParam = true;
		}
		
		url += (url.endsWith("?") ? "":"?") + getParams;
		
		return url;
	}
// ____________________________________________________________________
	private int calculateUploadingDataSize(ArrayList<PostParam> params)
	{
		int size = 0;
		
		if(params == null || params.size() == 0)
			return 0;
		
		boolean multiPart = false;
		
		for(PostParam param: params)
		{
			if(param.type != ParamType.String)
			{
				multiPart = true;
				break;
			}
		}
		
		if(!multiPart)
		{
			
			String postData = "";
			boolean isNotFirstParam = false;
			
			for (PostParam param : params) {
				postData += (isNotFirstParam ? "&":"");
				postData += (param.name.equals("") ? "":param.name+"=") + param.value;//Strings.urlUtf8Encoder(param.value, "%");
				isNotFirstParam = true;
			}
			
			byte data[] = postData.getBytes();
			size += data.length;
		}
		else
		{
		
			byte data[] = null;
			
			for(PostParam param : params)
			{
				if(param.type == ParamType.String || param.type == ParamType.Form)
				{
					String paramTxt = String.format(formDataPost, param.name, param.value,"%");
					data = paramTxt.getBytes();
					size += data.length;
				}
				else if(param.type == ParamType.File)
				{
					data = String.format(fileHeader, param.name, param.fileName, param.mimeType).getBytes();
					size += data.length;
					
					try {
						size += param.stream.available();
					} catch (IOException e) {e.printStackTrace();}
				}
			}
			
			//Writes the end boundary.
			String end = crlf+dashes+boundary+dashes;
			data = end.getBytes();
			size += data.length;
			
			data = null;
		}
		
		return size;
	}
// ____________________________________________________________________
	private void writePostParams(ArrayList<PostParam> params, int uploadingDataSize, OnConnectionResultListener listener) throws Exception
	{
		if(connection == null || params == null || params.size() == 0)
			return;
		
		boolean multiPart = false;
		
		for(PostParam param: params)
		{
			if(param.type != ParamType.String)
			{
				multiPart = true;
				break;
			}
		}
		
		/**
		 * This variable will be used to get some report.
		 */
		String PostCompleteData = "";
		
		if(!multiPart)
		{
			
			String postData = "";
			boolean isNotFirstParam = false;
			
			for (PostParam param : params) {
				postData += (isNotFirstParam ? "&":"");
				postData += (param.name.equals("") ? "":param.name+"=") + param.value;//StringManager.urlUtf8Encoder(param.value, "%");
				isNotFirstParam = true;
			}

			
			if(!cancelWork)
			{
				byte data[] = postData.getBytes();
				OutputStream outStream = connection.getOutputStream();
				outStream.write(data);
				
				if(listener != null)
					listener.onProgressChanged(postData.length(), postData.length(), reqCode, OnConnectionResultListener.streamingStatus.UPLOAD);
			}
			
		}
		else
		{
		
			byte data[] = null;
			byte[] buffer = new byte[512];
			int readBytes = 0;
			int uploadedSize = 0;
			
			//Actually we have to ignore any access to connection, while a cancellation has been happened.
			if(cancelWork)
				return;
			
			OutputStream outStream = connection.getOutputStream();
			
			PostCompleteData += multiPartPostHDR;
			
			for(PostParam param : params)
			{
				if(cancelWork)
					break;
				
				if(param.type == ParamType.String || param.type == ParamType.Form)
				{
					String paramTxt = String.format(formDataPost, param.name, param.value,"%");
					
					PostCompleteData += paramTxt;
					
					data = paramTxt.getBytes();
					outStream.write(data);
					
					uploadedSize += data.length;
					if(listener != null)
					{
						listener.onProgressChanged(uploadedSize, uploadingDataSize, reqCode, OnConnectionResultListener.streamingStatus.UPLOAD);
					}
					
					
				}
				else if(param.type == ParamType.File)
				{
					data = String.format(fileHeader, param.name, param.fileName, param.mimeType).getBytes();
					outStream.write(data);
					
					
					uploadedSize += data.length;
					if(listener != null)
					{
						listener.onProgressChanged(uploadedSize, uploadingDataSize, reqCode, OnConnectionResultListener.streamingStatus.UPLOAD);
					}
					
					try{
							
						while((readBytes = param.stream.read(buffer)) != -1 && !cancelWork)
						{
							outStream.write(buffer, 0, readBytes);
							
							uploadedSize += readBytes;
							if(listener != null && !cancelWork)
							{
								listener.onProgressChanged(uploadedSize, uploadingDataSize, reqCode, OnConnectionResultListener.streamingStatus.UPLOAD);
							}
						}
						
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			
			if(cancelWork)
				return;
			
			try{
				
			//Writes the end boundary.
			String end = crlf+dashes+boundary+dashes;
			data = end.getBytes();
			outStream.write(data);
			PostCompleteData += end;
			
			}catch(Exception e)
			{
				
				e.printStackTrace();
				
			}
			
		
		}
		
	}
// ____________________________________________________________________
	/**
	 * EVERYTHING will be control from here.
	 */
	public void run()
	{

		//if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO)
		System.setProperty("http.keepAlive", "false");
//		System.setProperty("http.maxConnections", "1");
		
		int uploadingDataSize = calculateUploadingDataSize(post_params);
		
		if(listener != null)
		{
			listener.onStart(ConnectionStatus.SUCCESSFUL, uploadingDataSize, reqCode);
		}
		
		//Checks Internet connection
		if(! isConnected(ctx).isConnected)
		{
			//If not connected.
			if(listener != null)
			{
				listener.onConnectionStatusChanged(ConnectionStatus.NO_INTERNET);
				listener.onFinish(0, ConnectionStatus.NO_INTERNET, 0, reqCode, null, null, OnConnectionResultListener.streamingStatus.UPLOAD);
			}
		}
		else
		{
			//if Internet is connected.
			try{
				
				url = addGetParams(url, get_params);
				connection = null;
				
				if(url.startsWith("https"))
			    {
					connection = (HttpsURLConnection) new URL(url).openConnection();
			    }
			    else
			    {
			    	connection = (HttpURLConnection) new URL(url).openConnection();
			    }


				addHeaders(Headers);
				
				

				
				//In this if block we check about the nature of our post parameters.
				//First, checks if have any post parameters or not...
				if(post_params != null && post_params.size() > 0)
				{
					if(!methodManuallySet)
						connection.setRequestMethod("POST");
					connection.setDoOutput(true);

					//We assumed that post data is not a file, so that the it's nature is not multi part
					boolean multiPart = false;
					//Now we check every post parameter to find at least on file.
					for(PostParam param: post_params)
					{
						//If we could find a file or in other words, a none string parameter type
						if(param.type != ParamType.String)
						{
							//We will realize our post nature is multi part
							multiPart = true;
							break;
						}
					}

					//Now here we put a header to determine post method.
					if(multiPart)
					{
						connection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
						connection.setFixedLengthStreamingMode(uploadingDataSize);
					}
					else
						connection.addRequestProperty("Content-Type", "application/json");
					
					connection.addRequestProperty("Content-Length", Integer.toString(uploadingDataSize));
				}
				else
				{
					if(!methodManuallySet)
						connection.setRequestMethod("GET");
					connection.addRequestProperty("Content-Type", "application/json");
				}

				if(methodManuallySet)
				{
					connection.setRequestMethod(methodType);
				}
				


				writePostParams(post_params, uploadingDataSize, listener);

//				Map<String, List<String>> reqs = connection.getRequestProperties();
//
//				for( List<String> l : reqs.values() )
//				{
//					for(String s : l)
//						Log.e("HITEM", s);
//				}

				if(listener != null && !cancelWork)
				{

					connection.setConnectTimeout(timeOut);
//					connection.setReadTimeout(timeOut);

					int responseCode = connection.getResponseCode();

					ConnectionStatus status = ResponseHelper.toEquivalentStatus(responseCode);

					listener.onFinish(responseCode,	status, connection.getContentLength(), reqCode,
							status == ConnectionStatus.SUCCESSFUL ? connection.getInputStream():(InputStream)null,
										connection, OnConnectionResultListener.streamingStatus.DOWNLOAD
									 );
					
				}
				
			}
			catch(Exception e)
			{

				if(e instanceof SocketTimeoutException)
				{

				}
				else {

					int responseCode = 0;

					try {
						//It doesn't connect to server.
						responseCode = connection.getResponseCode();


						if (responseCode == 401) {
							listener.onFinish(responseCode, ConnectionStatus.UserNotAuthenticated, 0, reqCode, null, null, OnConnectionResultListener.streamingStatus.DOWNLOAD);
						}

						if (responseCode == 400 || responseCode >= 402) {
							listener.onFinish(responseCode, ConnectionStatus.UNSUCCESSFUL, 0, reqCode, null, null, OnConnectionResultListener.streamingStatus.DOWNLOAD);
						}

					} catch (Exception ex) {
						ex.printStackTrace();
						listener.onFinish(responseCode, ConnectionStatus.UNSUCCESSFUL, 0, reqCode, null, null, OnConnectionResultListener.streamingStatus.DOWNLOAD);
					}


					if (responseCode <= 0) {
						if (listener != null && !cancelWork) {
							listener.onConnectionStatusChanged(ConnectionStatus.NO_INTERNET);
							listener.onFinish(0, ConnectionStatus.NO_INTERNET, 0, reqCode, null, null, OnConnectionResultListener.streamingStatus.UPLOAD);
						}
					}
				}
				
				e.printStackTrace();
			}
			finally
			{
				if(listener != null && cancelWork)
				{
					listener.onConnectionStatusChanged(ConnectionStatus.CANCELED);
					listener.onFinish(0, ConnectionStatus.CANCELED, 0, reqCode, null, null, OnConnectionResultListener.streamingStatus.UPLOAD);
				}

				try {
					connection.disconnect();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			
		}//End of Internet is connected if block.
	}	//End of public void run()
// ____________________________________________________________________
	
}