package ir.afshin.netup.DownloadManager;

import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.OnConnectionResultListener;

/**
 * Created by Afshin on 8/2/2015.
 */
interface OnPartDownloadListener {

    public void onStart(DownloadPart downloadPart);
    public void onProgress(DownloadPart downloadPart, int downloadedBytes, int size);
    public void onFinish(DownloadPart downloadPart, boolean success, ConnectionStatus status);

}
