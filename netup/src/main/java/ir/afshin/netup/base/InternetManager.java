package ir.afshin.netup.base;

import java.io.FileInputStream;
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
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
	protected ArrayList<Pair<String, String>> getParams;
	protected ArrayList<PostParam> postParams;
	protected OnConnectionResultListener listener;
	protected String url = "";

	protected boolean cancelWork = false;

	private final String dashes = "--";
	private final String crlf = Character.toString((char)0x0d) + Character.toString((char)0x0a);//"\r\n";
	private final String boundary = "----A35cxyzA35cxyzA35cxyzA35cxyz";
	private final String multiPartPostHDR = "Content-type: multipart/form-data; boundary=" + boundary + crlf;
	/**
	 * Parameters:
	 * 1- form object name.
	 */
	private String formDataHeader = crlf+dashes+boundary + crlf + "Content-Disposition: form-data; name=\"%1$s\"";

	/**
	 * Parameters:<p>
	 * 1- form object name.<p>
	 * 2- form object value.<p>
	 */
	private String formDataContent = formDataHeader /*+ crlf + "Content-Type:application/octet-stream" */+ crlf + crlf + "%2$s" ;

	/**
	 * Parameters:<p>
	 * 1- form object name.<p>
	 * 2- filename.<p>
	 * 3- content type.<p>
	 */
	private String fileHeader = formDataHeader +  "; filename=\"%2$s\"" + crlf + "Content-type:%3$s" + crlf + crlf;

	private boolean methodManuallySet = false;
	private String methodType = "";

	public enum Methods{
		GET, POST, PUT, DELETE, HEAD, Auto
	}

	int timeOut = 20000;

	private boolean mustPostParamSent_multiParted = false;

// ____________________________________________________________________
	/**
	 * Starts a connection to given url address with given parameters
	 * @param ctx An instance of Context.
	 * @param url Url address.
	 * @param reqCode A request code to distinguish responses.
	 * @param Headers An array of {@link Pair} including needed headers to send to server
	 * @param get_params An array of {@link Pair} including all path parameters to send to server
	 * @param post_params An array of {@link PostParam} including all post parameters must be sent
	 *                    to server
	 * @param listener An instance of {@link OnConnectionResultListener} which listens to
	 *                    communication activities.
	 * @param tag Any object to tag to response.
	 */
	public void connect(final Context ctx, String url, final int reqCode, final ArrayList<Pair<String, String>> Headers, final ArrayList<Pair<String, String>> get_params,final ArrayList<PostParam> post_params, final OnConnectionResultListener listener, Object tag)
	{

		this.ctx = ctx;
		this.url = url;
		this.reqCode = reqCode;
		this.Headers = Headers;
		this.getParams = get_params;
		this.postParams = post_params;
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
				case HEAD: methodType = "HEAD"; break;
			}
		}

	}

// ____________________________________________________________________

	/**
	 *
	 * @param multiPart If <i>true</i>, post parameters will be sent in a multipart manner, if
	 *                     <i>false</i>, the internet manager will seek your post params and if
	 *                  could find at least one post poarameter including file, then automatically
	 *                  send your request body in a multipart manner, otherwise will send your
	 *                  request body in a simple key-value format.
     */
	public void forceMultiPart(boolean multiPart) {

		mustPostParamSent_multiParted = multiPart;
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
	 * Disconnects connection and also informs listener about disconnection.
	 * <h1>Note:</h1>
	 * Calling this function perfectly cancels upload actions, but if streaming is in download status, this method just tell the listener
	 * to cancel work. The listener could response the cancellation depend on it's situation. <b>That means the listener HAVE TO handle
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
	 * @param ctx An instance of {@link Context}
	 * @return An instance of {@link ConnectionDescriber} which describes the connectivity state.
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

			connection.addRequestProperty(header.getFirst(), header.getSecondWithoutEncoding());
		}



	}
// ____________________________________________________________________

	private String addGetParams(String url, ArrayList<Pair<String, String>> params)
	{
		if(params == null || params.size() == 0)
			return url;

		String getParams = "";
		boolean isNotFirstParam = false;

		try {
			for (Pair<String, String> param : params) {
				getParams += (isNotFirstParam ? "&" : "");
				getParams += (param.getFirst().equals("") ? "" : param.getFirst() + "=") + param.getSecond(url);
				isNotFirstParam = true;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		url += (url.endsWith("?") ? "":"?") + getParams;

		return url;
	}

// ____________________________________________________________________

	private boolean isBodyMultipart(ArrayList<PostParam> params) {

		boolean multiPart = mustPostParamSent_multiParted;

		//If programmer didn't force to use multipart format, we have to check post paramters to see
		//if it's needed to send body in multipart format or not.
		if(!multiPart) {
			//Seeking post parameters to find at list one FILE
			for (PostParam param : params) {
				if (param.type == ParamType.File) {
					multiPart = true;
					break;
				}
			}
		}
		return multiPart;
	}
// ____________________________________________________________________

	/**
	 * Convert post parameters into a key value format as like as : <b>name=value&name=value...</b>
	 * @param postParams Post parameters to send.
	 * @param forUrl The url which this request is going to be sent to.
     * @return An string including all key an value of post params.
     */
	private String createKeyValueBodyFrom(ArrayList<PostParam> postParams, String forUrl) {

		String postData = "";
		boolean isNotFirstParam = false;

		try {
			for (PostParam param : postParams) {
				postData += (isNotFirstParam ? "&" : "");
				postData += (param.name.equals("") ? "" : param.name + "=") + param.getValue(forUrl);
				isNotFirstParam = true;
			}
		}catch (Exception e) {

			e.printStackTrace();
		}

		return postData;
	}

// ____________________________________________________________________

	/**
	 * Calculates the size of request body.
	 * @param params The post parameters.
	 * @return The amount of bytes that must be sent to server.
     */
	private int calculateUploadingDataSize(ArrayList<PostParam> params)
	{
		int size = 0;

		if(params == null || params.size() == 0)
			return 0;

		boolean multiPart = isBodyMultipart(params);

		if(!multiPart)
		{

			String postData = createKeyValueBodyFrom(params, this.url);
			byte data[] = postData.getBytes();
			size += data.length;
		}
		else
		{

			byte data[] = null;

			for(PostParam param : params)
			{
				if(param.type == ParamType.String)
				{
					String paramTxt = String.format(formDataContent, param.name, param.getValueWithoutEncoding());
					data = paramTxt.getBytes();
					size += data.length;
				}
				else if(param.type == ParamType.File)
				{
					data = String.format(fileHeader, param.name, param.fileName, param.mimeType).getBytes();
					size += data.length;

					try {
						if(param.fileToUpload.isFile()) {

							size += param.fileToUpload.length();
						}
					} catch (Exception e) {e.printStackTrace();}
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

		boolean multiPart = isBodyMultipart(params);

		/**
		 * This variable will be used to get some report.
		 */
		String PostCompleteData = "";

		if(!multiPart)
		{

			String postData = createKeyValueBodyFrom(params, this.url);

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

				if(param.type == ParamType.String)
				{
					String paramTxt = String.format(formDataContent, param.name, param.getValueWithoutEncoding());

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

					InputStream inputStream = null;

					try{

						inputStream = new FileInputStream(param.fileToUpload);
						while((readBytes = inputStream.read(buffer)) != -1 && !cancelWork)
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
					finally {

						if(inputStream != null)
							inputStream.close();
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

				uploadedSize += data.length;
				if(listener != null && !cancelWork)
				{
					listener.onProgressChanged(uploadedSize, uploadingDataSize, reqCode, OnConnectionResultListener.streamingStatus.UPLOAD);
				}

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

		System.setProperty("http.keepAlive", "false");

		int uploadingDataSize = calculateUploadingDataSize(postParams);

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
			//if the Internet is connected.
			try{

				url = addGetParams(url, getParams);
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
				if(postParams != null && postParams.size() > 0)
				{
					if(!methodManuallySet)
						connection.setRequestMethod("POST");
					connection.setDoOutput(true);

					//We assumed that post data is not a file, so that the it's nature is not multi part
					boolean multiPart = isBodyMultipart(postParams);

					//Now here we put a header to determine post method.
					if(multiPart)
					{
						connection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
						connection.setFixedLengthStreamingMode(uploadingDataSize);
					}
					else if(!connection.getRequestProperties().containsKey("Content-Type")) {

						connection.addRequestProperty("Content-Type", "application/json");
					}

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



				writePostParams(postParams, uploadingDataSize, listener);


				if(listener != null && !cancelWork)
				{

					connection.setConnectTimeout(timeOut);
//					connection.setReadTimeout(timeOut);

					int responseCode = connection.getResponseCode();

					ConnectionStatus status = ConnectionStatus.toEquivalentStatus(responseCode);

					listener.onFinish(responseCode,	status, connection.getContentLength(), reqCode,
							status == ConnectionStatus.SUCCESSFUL ? connection.getInputStream():(InputStream)null,
							connection, OnConnectionResultListener.streamingStatus.DOWNLOAD
					);

				}

			}
			catch(Exception e)
			{

				e.printStackTrace();
				if(e instanceof SocketTimeoutException)
				{
					if(listener != null)
						listener.onFinish(0,ConnectionStatus.TIMEOUT, 0,0,null, null, OnConnectionResultListener.streamingStatus.DOWNLOAD);
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


					if (responseCode <= 0 && listener != null && !cancelWork) {

						listener.onConnectionStatusChanged(ConnectionStatus.NO_INTERNET);
						listener.onFinish(0, ConnectionStatus.NO_INTERNET, 0, reqCode, null, null, OnConnectionResultListener.streamingStatus.UPLOAD);
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