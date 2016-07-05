package ir.afshin.netup.base;

import android.content.Context;

import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * @author afshin
 * Objects and/or classes which want to use @InternetManager should use this listener to get responses.
 */
public interface OnConnectionResultListener {



	public static enum streamingStatus {UPLOAD,DOWNLOAD}
	
	public boolean cancelWork = false;
	
	/**
	 * Will be raised while upload or downloading progress changes.
	 * @param progress Amount of bytes up/downloaded.
	 * @param size Size of data to up/download.
	 * @param reqCode An unique identifying code.
	 * @param streamingStatus Determines that this progress changes is related to upload or download, using enum {@link streamingStatus}
	 * @see streamingStatus
	 */
	public void onProgressChanged(int progress, int size, int reqCode, streamingStatus streamingStatus);
	/**
	 * Will be raised while connection starts.
	 * @param status Status of connection, enum {@link ConnectionStatus}
	 * @param size Size of uploading data.
	 * @param reqCode An unique identifying code.
	 */
	public void onStart(ConnectionStatus status, int size, int reqCode);
	/**
	 * Will be raised While disconnects.
	 * @param serverResponseCode The response code received from server.
	 * @param status Status of connection, enum {@link ConnectionStatus}
	 * @param size Size of downloaded data.
	 * @param reqCode An unique identifying code.
	 * @param inStream Response inputStream.
	 * @param connection Connection object.
	 * @param streamingStatus Determines that this call of onFinish is related to upload or download, using enum {@link streamingStatus}
	 */
	public void onFinish(int serverResponseCode, ConnectionStatus status, int size, int reqCode, InputStream inStream, HttpURLConnection connection, streamingStatus streamingStatus);
	
	/**
	 * Will be raised while connection pkgDlStatus changed.
	 * 
	 * <h2>Note:</h2> While a cancellation has been took place, this method will be invoked with <i>ConnectionStatus.CANCELED</i>.<p>
	 * This mean that user has canceled operation, {@link InternetManager} has canceled it's own job, but it can't controls this interface
	 * operations. So that while this method has been raised, you have to stop any operation cause the connection will be disconnected.
	 * @param status Status of connection, enum {@link ConnectionStatus}
	 */
	public void onConnectionStatusChanged(ConnectionStatus status);
	
}
