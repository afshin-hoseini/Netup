package ir.afshin.netup.DownloadManager;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import ir.afshin.netup.FileSystem.FileManager;
import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.InternetManager;
import ir.afshin.netup.base.OnConnectionResultListener;
import ir.afshin.netup.base.Pair;

/**
 * Created by Afshin on 8/2/2015.
 */
public class DownloadPart {

    long startByte = 0;
    long endByte = 0;
    InternetManager inetMan = null;
    String partFilename = "";
    int partNumber = 0;
    String partFolder = "";
    boolean cancelJob = false;
    boolean downloadedSuccessfully = false;
    boolean downloadFinished = false;
    int downloadedBytes = 0;
    int downloadSize = 0;
    OnPartDownloadListener listener = null;


// ____________________________________________________________________

    void startDownload(Context ctx, DownloadItem dlItem, int partNumber, String partFolder, long startByte, long endByte, OnPartDownloadListener listener)
    {
        this.partNumber = partNumber;
        this.partFolder = partFolder;
        this.partFilename = partFolder + dlItem.packageName + "_part"+partNumber;
        this.startByte = startByte;
        this.endByte = endByte;
        this.downloadSize = (int)(endByte - startByte);
        this.listener = listener;

        ArrayList<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
        headers.add(new Pair("Range", "bytes=" + startByte + "-" + endByte));
        inetMan = new InternetManager();
        inetMan.connect(ctx, dlItem.url, 0, headers, null, null, onDownloadListener, null);
    }

// ____________________________________________________________________

    /**
     * UPDATE_CANCELLATION
     */
    void cancel()
    {
        if(inetMan != null)
        {
            inetMan.disconnect();
        }
    }


// ____________________________________________________________________

    OnConnectionResultListener onDownloadListener = new OnConnectionResultListener() {

        boolean cancelWork = false;

        @Override
        public void onProgressChanged(int progress, int size, int reqCode, streamingStatus streamingStatus) {

        }

        @Override
        public void onStart(ConnectionStatus status, int size, int reqCode) {

            listener.onStart(DownloadPart.this);

        }

        @Override
        public void onFinish(int serverResponseCode, ConnectionStatus status, int size, int reqCode, InputStream inStream, HttpURLConnection connection, streamingStatus streamingStatus) {



            if(status == ConnectionStatus.SUCCESSFUL)
            {

                FileOutputStream fos = null;
                BufferedInputStream bis = null;

                try {

                    FileManager.makeDir(partFolder);
                    fos = new FileOutputStream(partFilename);

                    byte[] buffer = new byte[2*1024];
                    int readLen = -1;
                    int totalReadBytes = 0;

                     bis = new BufferedInputStream(inStream);

                    while ((readLen = bis.read(buffer)) != -1 && !cancelWork && !cancelJob)
                    {
                        fos.write(buffer, 0, readLen);
                        totalReadBytes += readLen;
                        downloadedBytes = totalReadBytes;

                        listener.onProgress(DownloadPart.this, readLen, downloadSize);

                    }

                    try {

                        if(fos != null)
                            fos.close();
                        if (bis != null)
                            bis.close();
                        if(connection != null) {
                            connection.disconnect();
                            connection = null;
                        }

                    }catch (Exception e){e.printStackTrace();}


                    downloadedSuccessfully = true;
                    downloadFinished = true;

                    listener.onFinish(DownloadPart.this, true, status);


                }catch (Exception e)
                {
                    downloadFinished = true;
                    listener.onFinish(DownloadPart.this, false, status);
                    e.printStackTrace();
                }
                finally {
                    try {

                        if(fos != null)
                            fos.close();
                        if (bis != null)
                            bis.close();

                    }catch (Exception e){e.printStackTrace();}
                }

            }else {

                downloadFinished = true;
                listener.onFinish(DownloadPart.this, false, status);

            }

            downloadFinished = true;

            try{ if(connection != null)connection.disconnect(); }catch (Exception e) {e.printStackTrace();}

        }

        @Override
        public void onConnectionStatusChanged(ConnectionStatus status) {

            if(status == ConnectionStatus.CANCELED)
            {
                cancelWork = true;
                cancelJob = true;
                //listener.onFinish(DownloadPart.this, false, status);

//                Log.e("DLMAN", "Part " + partNumber + " is canceled");
            }
        }
    };

// ____________________________________________________________________

}
