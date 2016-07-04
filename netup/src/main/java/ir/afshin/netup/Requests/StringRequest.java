package ir.afshin.netup.Requests;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import ir.afshin.netup.base.Pair;
import ir.afshin.netup.base.PostParam;
import ir.afshin.netup.base.OnConnectionResultListener;
import ir.afshin.netup.base.ConnectionStatus;


import android.content.Context;

/**
 * If you are about request server for some strings, regardless that string is json, XML, etc., this
 * is the case fit you needs. If you know your response is json see {@link JsonObjectRequest}
 * or {@link JsonArrayRequest}. If you wanna download some contents like a file or an Image, see
 * {@link DownloadRequest}
 */
public class StringRequest extends Request {

    private OnStringResponse onStringResponseListener = null;
    private UploadProgressListener uploadProgressListener = null;

// ____________________________________________________________________

    /**
     *
     * @param ctx An instance of {@link Context}.
     * @param listener An instance of {@link OnStringResponse} if you wanna listen to request status
     *                 and its response.
     */
    public StringRequest(Context ctx, OnStringResponse listener)
    {
        super(ctx);
        setOnStringResponseListener(listener);
    }
// ____________________________________________________________________

    /**
     *
     * @param ctx An instance of {@link Context}.
     * @param url The url you wanna connect with
     * @param Headers The headers you wanna send to server.
     * @param get_params The get parameters you wanna send through path query.
     * @param post_params The post parameters you wanna send to server inside request body.
     * @param listener An instance of {@link OnStringResponse} if you wanna listen to request status
     *                 and its response.
     */
    public StringRequest( Context ctx, String url, ArrayList<Pair<String, String>> Headers, ArrayList<Pair<String, String>> get_params, ArrayList<PostParam> post_params, OnStringResponse listener)
    {
        super(ctx, url, Headers, get_params, post_params);
        setOnStringResponseListener(listener);
    }
// ____________________________________________________________________

    public void cancel() {

        disconnect();
    }

// ____________________________________________________________________

    /**
     * Sets the given listener as default listener for this request's status.
     * @param listener An instance of {@link OnStringResponse} which listens to responses.
     */
    public void setOnStringResponseListener(OnStringResponse listener) {

        this.onStringResponseListener = listener;
    }
// ____________________________________________________________________

    /**
     * Sets the given listener as default upload progress listener.
     * @param uploadProgressListener An instance of {@link UploadProgressListener} which listens to
     *                               upload progress.
     */
    public void setUploadProgressListener(UploadProgressListener uploadProgressListener) {

        this.uploadProgressListener = uploadProgressListener;
    }

// ____________________________________________________________________

    public void startJob()
    {

        this.listener = new OnConnectionResultListener() {

            private boolean cancelWork = false;

            public void onStart(ConnectionStatus status, int size, int reqCode) {
                if(onStringResponseListener != null)
                    onStringResponseListener.onStart(StringRequest.this);
            }

            public void onProgressChanged(int progress, int size, int reqCode, streamingStatus streamingStatus)
            {
                if(streamingStatus == OnConnectionResultListener.streamingStatus.UPLOAD
                        && uploadProgressListener != null) {
                    uploadProgressListener.onProgress(StringRequest.this, progress, size);
                }
            }

            public void onFinish(int serverResponseCode, ConnectionStatus status, int size, int reqCode,
                                 InputStream inStream, HttpURLConnection connection,
                                 streamingStatus streamingStatus) {


                if(status == ConnectionStatus.SUCCESSFUL)
                {


                    try
                    {

                        String Response = readStringResponse(inStream);


                        if(onStringResponseListener != null)
                            onStringResponseListener.onFinish(StringRequest.this, Response, true, connection, ConnectionStatus.SUCCESSFUL);
                        notifyRequestFinishToQueue(ConnectionStatus.SUCCESSFUL);

                    }catch(Exception e)
                    {
                        if(onStringResponseListener != null)
                            onStringResponseListener.onFinish(StringRequest.this, "", false, null, status);
                        notifyRequestFinishToQueue(status);
                        e.printStackTrace();
                    }
                }
                else
                {
                    if(onStringResponseListener != null)
                        onStringResponseListener.onFinish(StringRequest.this, "", false, connection, status);
                    notifyRequestFinishToQueue(status);
                }


                try{
                    if(inStream != null)
                        inStream.close();

                    if(connection != null)
                        connection.disconnect();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }

            }

            public void onConnectionStatusChanged(ConnectionStatus status) {

                if(status == ConnectionStatus.CANCELED)
                {
                    this.cancelWork = true;

                }

            }
        };

        cancelWork = false;
        try {
            start();
        }catch (Exception e)
        {
            e.printStackTrace();
        }


    }
// ____________________________________________________________________

    private String readStringResponse(InputStream inStream) throws Exception
    {
        String line = "";
        String Response = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));


        while((line = br.readLine()) != null && !cancelWork)
        {
            Response += line;
        }

        try{br.close();}catch(Exception e){e.printStackTrace();}

        return Response;
    }
// ____________________________________________________________________

    /**
     * Listens to status of an {@link StringRequest}.
     */
    public interface OnStringResponse {

        /**
         * Will be invoked once the given {@link Request} is started.
         * @param request The started request.
         */
        void onStart(Request request);

        /**
         * Will be invoked once the given {@link Request} is finished.
         * @param request The request which finished.
         * @param response Includes server response.
         * @param success <i>true</i> if the request succeeded, <i>false</i> otherwise.
         * @param connection The connection object which handles the connection to server.
         * @param status An instance of {@link ConnectionStatus} describing the server response and
         *               reveal the reason of errors.
         */
        void onFinish(Request request, String response, boolean success, HttpURLConnection connection, ConnectionStatus status);
    }

// ____________________________________________________________________

    /**
     * Listens to upload progress.
     */
    public interface UploadProgressListener {

        /**
         * Will be invoked with appropriate parameters, while assigned request made upload progress.
         * @param request Determines which {@link Request} did make progress.
         * @param uploaded Uploaded byte count.
         * @param wholeSize Whole uploading content.
         */
        void onProgress(Request request, int uploaded, int wholeSize);
    }

// ____________________________________________________________________

}
