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
 * A request class for download purpose. It also use cached items to avoid redundant downloads.
 * Created by afshinhoseini on 1/29/16.
 */
public class DownloadRequest extends Request {


    private DownloadCachePolicy dlCachePolicy = null;
    private OnDownloadRequestProgress downloadListener = null;
    private boolean cancelWork = false;

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

    /**
     * Sets the given {@link OnDownloadRequestProgress} as the main report listener of this request.
     * @param downloadListener An instance of {@link OnDownloadRequestProgress}, listens to download
     *                         progress.
     */
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
                        notifyRequestFinishToQueue(status);


                    }catch (Exception e) {

                        e.printStackTrace();

                        if(DownloadRequest.this.cancelWork) {

                            if(downloadListener != null)
                                downloadListener.onFinish(DownloadRequest.this, false, ConnectionStatus.CANCELED, null);
                            notifyRequestFinishToQueue(ConnectionStatus.CANCELED);
                        }
                        else {

                            if(downloadListener != null)
                                downloadListener.onFinish(DownloadRequest.this, false, ConnectionStatus.UNSUCCESSFUL, null);
                            notifyRequestFinishToQueue(ConnectionStatus.UNSUCCESSFUL);
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
                        notifyRequestFinishToQueue(status);
                    }
                }

            }

            @Override
            public void onConnectionStatusChanged(ConnectionStatus status) {

                if(status == ConnectionStatus.CANCELED)
                    DownloadRequest.this.cancelWork = true;
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

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
                        notifyRequestFinishToQueue(ConnectionStatus.SUCCESSFUL);
                        Log.e("DL-Req","File: " + validCachedFilename);
                    }
                }
            }
        });

        thread.setDaemon(true);
        thread.start();

    }

// ____________________________________________________________________

    /**
     * Listens to {@link DownloadRequest download requests} and reports their status.
     */
    public interface OnDownloadRequestProgress {

        /**
         * Will be called whenever the given request is starte its job.
         * @param request The started download request.
         */
        void onStart(DownloadRequest request);

        /**
         * Will be called while the given {@link DownloadRequest download request} is making
         * progress.
         * @param request The {@link DownloadRequest download request} made progress.
         * @param fileSize The whole file size.
         * @param downloadedSize The downloaded size.
         * @param percent A float value between 0 and 1 indicating the download progress.
         */
        void onProgress(DownloadRequest request, long fileSize, long downloadedSize, float percent);

        /**
         * Will be called once the given {@link DownloadRequest download request} is finished.
         * @param request The {@link DownloadRequest download request} that is just finished.
         * @param success <i>true</i> if the job was finished successfully and file is downloaded
         *                successfully.
         * @param status An instance of {@link ConnectionStatus} indicating the status of connection
         * @param downloadedFilename The downloaded filename on storage.
         */
        void onFinish(DownloadRequest request, boolean success, ConnectionStatus status, String downloadedFilename);
    }

// ____________________________________________________________________




}
