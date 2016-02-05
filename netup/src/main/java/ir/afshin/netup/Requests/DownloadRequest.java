package ir.afshin.netup.Requests;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.OnConnectionResultListener;
import ir.afshin.netup.base.Pair;

/**
 * Created by afshinhoseini on 1/29/16.
 */
public class DownloadRequest extends Request {


    private DownloadCachePolicy dlCachePolicy = null;
    private OnDownloadRequestProgress downloadListener = null;
    private boolean cancelWork = false;

    Handler handler = new Handler();

// ____________________________________________________________________

    public DownloadRequest(Context ctx, OnDownloadRequestProgress downloadListener){

        super(ctx);
        setOnDownloadProgressListener(downloadListener);
    }

// ____________________________________________________________________

    public DownloadRequest(Context ctx, String url, ArrayList<Pair<String, String>> Headers, OnDownloadRequestProgress downloadListener) {

        super(ctx, url, Headers, null, null);
        setOnDownloadProgressListener(downloadListener);
    }

// ____________________________________________________________________

    public void setDownloadCachePolicy(DownloadCachePolicy downloadCachePolicy) {

        this.dlCachePolicy = downloadCachePolicy;
    }

// ____________________________________________________________________

    public void setOnDownloadProgressListener(OnDownloadRequestProgress downloadListener) {

        this.downloadListener = downloadListener;
    }
// ____________________________________________________________________

    public void cancel() {

        cancelWork = true;
        disconnect();
    }

// ____________________________________________________________________

    public void startJob() {

        this.listener = new OnConnectionResultListener() {
            @Override
            public void onProgressChanged(int progress, int size, int reqCode, streamingStatus streamingStatus) {

            }

            @Override
            public void onStart(ConnectionStatus status, int size, int reqCode) {

                if(downloadListener != null)
                    downloadListener.onStart(DownloadRequest.this);
            }

            @Override
            public void onFinish(int serverResponseCode, ConnectionStatus status, int size, int reqCode, InputStream inStream, HttpURLConnection connection, streamingStatus streamingStatus) {

                if(status == ConnectionStatus.SUCCESSFUL){

                    if(downloadListener != null)
                        downloadListener.onProgress(DownloadRequest.this, size, 0, (float)0/(float)(size+1));

                    byte[] buffer = new byte[1024];
                    int readBytes = -1;
                    int totalReadBytes = 0;
                    FileOutputStream fos = null;
                    String cacheFileName = CacheManager.createCacheFilename(ctx, url, dlCachePolicy);

                    try {

                        fos = new FileOutputStream(cacheFileName);

                        while (!cancelWork && !DownloadRequest.this.cancelWork && (readBytes = inStream.read(buffer)) > -1) {

                            fos.write(buffer, 0, readBytes);
                            totalReadBytes += readBytes;

                            if(totalReadBytes % 2048 == 0)
                                fos.flush();

                            if(downloadListener != null)
                                downloadListener.onProgress(DownloadRequest.this, size, totalReadBytes, (float) totalReadBytes / (float) (size+1));
                        }

                        if (fos != null) {

                            fos.flush();
                            fos.close();
                            fos = null;
                        }

                        Log.e("DL-Req","Internet: " + url);

                        if(downloadListener != null)
                            downloadListener.onFinish(DownloadRequest.this, true, status, cacheFileName);
                        notifyRequestFinishToqueue(status);


                    }catch (Exception e) {

                        e.printStackTrace();

                        if(DownloadRequest.this.cancelWork) {

                            if(downloadListener != null)
                                downloadListener.onFinish(DownloadRequest.this, false, ConnectionStatus.CANCELED, null);
                            notifyRequestFinishToqueue(ConnectionStatus.CANCELED);
                        }
                        else {

                            if(downloadListener != null)
                                downloadListener.onFinish(DownloadRequest.this, false, ConnectionStatus.UNSUCCESSFUL, null);
                            notifyRequestFinishToqueue(ConnectionStatus.UNSUCCESSFUL);
                        }

                    }
                    finally {

                        try {

                            if (inStream != null)
                                inStream.close();
                            if (fos != null)
                                fos.close();
                        }catch(Exception e) {

                            e.printStackTrace();
                        }

                    }

                }
                else {

                    if(downloadListener != null) {
                        downloadListener.onFinish(DownloadRequest.this, false, status, null);
                        notifyRequestFinishToqueue(status);
                    }
                }

            }

            @Override
            public void onConnectionStatusChanged(ConnectionStatus status) {

                if(status == ConnectionStatus.CANCELED)
                    DownloadRequest.this.cancelWork = true;
            }
        };

        String validCachedFilename = CacheManager.getCachedFileName(ctx, url);

        if(validCachedFilename == null) {
            cancelWork = false;

            try {

                start();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        else{

            if(downloadListener != null) {

                downloadListener.onStart(DownloadRequest.this);
                downloadListener.onFinish(DownloadRequest.this, true, ConnectionStatus.SUCCESSFUL, validCachedFilename);
                notifyRequestFinishToqueue(ConnectionStatus.SUCCESSFUL);
                Log.e("DL-Req","File: " + validCachedFilename);
            }
        }
    }

// ____________________________________________________________________

    public interface OnDownloadRequestProgress {

        public void onStart(DownloadRequest request);
        public void onProgress(DownloadRequest request, long fileSize, long downloadedSize, float percent);
        public void onFinish(DownloadRequest request, boolean success, ConnectionStatus status, String downloadedFilename);
    }

// ____________________________________________________________________




}
