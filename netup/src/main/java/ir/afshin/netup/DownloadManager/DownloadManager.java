package ir.afshin.netup.DownloadManager;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import ir.afshin.netup.FileSystem.FileManager;
import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.InternetManager;
import ir.afshin.netup.base.OnConnectionResultListener;


/**
 * Created by Afshin on 8/2/2015.
 */
public class DownloadManager {

    DownloadItem downloadItem = null;
    Context ctx = null;
    ArrayList<DownloadPart> downloadParts = new ArrayList<DownloadPart>();
    int parts = 4;
    OnDownloadListener listener;
    boolean isOnStartNotified = false;
    String partsFolder = "";
    int downloadedSize = 0;
    boolean concatingFileParts = false;
    boolean cancelWork = false;

    //Just for logging
    int calls = 0;
    boolean triedToGetFileSize = false;


// ____________________________________________________________________

    public DownloadManager(Context ctx, DownloadItem downloadItem, OnDownloadListener listener)
    {
        this.ctx = ctx;
        this.downloadItem = downloadItem;
        this.listener = listener;

        if(listener == null)
        {
            try {
                throw new Exception("The listener must not be null.");
            }catch (Exception e){e.printStackTrace();}
        }

    }

//_____________________________________________________________________________
    public void cancel()
    {
        cancelWork = true;

        //UPDATE_CANCELLATION
        for(DownloadPart dlPart : downloadParts)
        {
            try{dlPart.cancel();}catch (Exception e){e.printStackTrace();}
        }
    }
// ____________________________________________________________________

    public void startDownload()
    {

        if(downloadItem.fileSize <= 0)
        {
            getFileSize();
            return;
        }


        partsFolder = FileManager.getProjectFolder(ctx) + "temp/dlMan/";

        int eachPartShare = downloadItem.fileSize / parts;
        int lastShareExtra = downloadItem.fileSize % parts;

//        Log.e("DLMAN", "parts = " + parts);
//        Log.e("DLMAN", "Share = " + eachPartShare);
//        Log.e("DLMAN", "extra share = " + lastShareExtra);
//        Log.e("DLMAN", "fileSize = " + downloadItem.fileSize);

        for(int i=0; i<parts; i++)
        {
            DownloadPart downloadPart = new DownloadPart();
            downloadParts.add(downloadPart);

            int partExtraShare = (i==parts-1 ? lastShareExtra : 0);

            downloadPart.startDownload(ctx, downloadItem, i, partsFolder, i* eachPartShare, (i*eachPartShare + eachPartShare + partExtraShare) -1, onPartDownloadListener);

//            Log.e("DLMAN", "part " + i + " from " + i* eachPartShare + " to " + ((i*eachPartShare + eachPartShare + partExtraShare) -1));

        }
    }
// ____________________________________________________________________

    OnPartDownloadListener onPartDownloadListener = new OnPartDownloadListener() {
        @Override
        public synchronized void onStart(DownloadPart downloadPart) {

            if( ! isOnStartNotified)
            {
                isOnStartNotified = true;
                listener.onStart();
            }
        }

        @Override
        public synchronized void onProgress(DownloadPart downloadPart, int downloadedBytes, int size) {

            downloadedSize += downloadedBytes;
            listener.onProgress(downloadItem, downloadParts, downloadItem.fileSize, downloadedSize);

        }

        @Override
        public synchronized void onFinish(DownloadPart downloadPart, boolean success, ConnectionStatus status) {

            int finishedItems = 0;

            for(DownloadPart dlPart : downloadParts)
            {
                if(dlPart.downloadFinished)
                    finishedItems += 1;

            }

            if(finishedItems == downloadParts.size())
                concatParts();

        }
    };

// ____________________________________________________________________

    private void getFileSize()
    {
        InternetManager inetMan = new InternetManager();
        inetMan.connect(ctx, downloadItem.url, 0, null, null, null, new OnConnectionResultListener(){

            boolean canceledManually = false;

            @Override
            public void onProgressChanged(int progress, int size, int reqCode, streamingStatus streamingStatus) {}
            @Override
            public void onStart(ConnectionStatus status, int size, int reqCode) {
                onPartDownloadListener.onStart(null);
            }
            @Override
            public void onConnectionStatusChanged(ConnectionStatus status) {}

            @Override
            public void onFinish(int serverResponseCode, ConnectionStatus status, int size, int reqCode, InputStream inStream, HttpURLConnection connection, streamingStatus streamingStatus) {

                if(canceledManually)
                    return;

                if(status == ConnectionStatus.SUCCESSFUL && size > 0)
                {
                    downloadItem.fileSize = size;
                    canceledManually = true;

                    try{
                        inStream.close();
                        connection.disconnect();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    startDownload();
                }
                else
                {
                    listener.onStart();
                    notifyOnFinish(0,0,false, status);
                }

            }

        }, null);
    }

// ____________________________________________________________________

    private void deleteParts()
    {
        for(DownloadPart downloadPart : downloadParts)
        {
            FileManager.delete(downloadPart.partFilename);
        }
    }

// ____________________________________________________________________

    private void concatParts()
    {

        if(concatingFileParts)
            return;

//        Log.e("DLMAN", "Concat parts calls " +  calls);
        calls++;

        concatingFileParts = true;

        boolean areAllPartsSuccessfullyDownloaded = true;

        for(DownloadPart dlPart : downloadParts)
        {
            if(! dlPart.downloadedSuccessfully) {
                areAllPartsSuccessfullyDownloaded = false;
                break;
            }

        }

        if(! areAllPartsSuccessfullyDownloaded)
        {
            notifyOnFinish(0,0,false, ConnectionStatus.UNSUCCESSFUL);
            deleteParts();
            return;
        }


        FileOutputStream fos_mainFile = null;
        FileInputStream partFileInStream = null;

        try {
            FileManager.makeDir(downloadItem.packagePath);
            fos_mainFile = new FileOutputStream(downloadItem.getDestFilename());

            for(DownloadPart downloadPart : downloadParts)
            {
                partFileInStream = new FileInputStream(downloadPart.partFilename);
                byte[] buffer = new byte[2*1024];
                int readeBytes = -1;

                while(( readeBytes = partFileInStream.read(buffer)) != -1)
                {
                    fos_mainFile.write(buffer, 0, readeBytes);
                }

                try{
                    if(partFileInStream != null)
                    {
                        partFileInStream.close();
                        partFileInStream = null;
                        System.gc();
                    }
                }catch (Exception e){e.printStackTrace();}
            }

            try{
                fos_mainFile.close();
                fos_mainFile = null;
            }catch (Exception e){e.printStackTrace();}

            notifyOnFinish(downloadItem.fileSize, downloadedSize, true, ConnectionStatus.SUCCESSFUL);


        }
        catch (Exception e)
        {
            e.printStackTrace();
            notifyOnFinish(0, 0, false, ConnectionStatus.UNSUCCESSFUL);
        }
        finally {

            try {
                if (fos_mainFile != null)
                    fos_mainFile.close();

                if (partFileInStream != null)
                    partFileInStream.close();
            }catch (Exception e){e.printStackTrace();}
        }

        deleteParts();
    }
// ____________________________________________________________________

    private void notifyOnFinish(final int size, final int downloaded, final boolean success, final ConnectionStatus status)
    {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                listener.onFinish(size, downloaded, success, status);
            }
        });

        t.start();
    }


// ____________________________________________________________________

}
