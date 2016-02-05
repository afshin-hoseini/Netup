package ir.afshin.netup.DownloadManager;

import java.util.ArrayList;

import ir.afshin.netup.base.ConnectionStatus;

/**
 * Created by Afshin on 8/2/2015.
 */
public interface OnDownloadListener {


    public void onStart();
    public void onProgress(DownloadItem downloadItem, ArrayList<DownloadPart> parts, int size, int downloaded);
    public void onFinish(int size, int downloaded, boolean success, ConnectionStatus status);
}
