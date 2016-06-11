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

public class StringRequest extends Request {

    private OnStringResponse onStringResponseListener = null;

// ____________________________________________________________________
    public StringRequest(Context ctx, OnStringResponse listener)
    {
        super(ctx);
        setOnStringResponseListener(listener);
    }
// ____________________________________________________________________

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

    public void setOnStringResponseListener(OnStringResponse listener) {

        this.onStringResponseListener = listener;
    }

// ____________________________________________________________________
    /**
     * Gets a response from server which contains string.
     */
    public void startJob()
    {

        this.listener = new OnConnectionResultListener() {

            private boolean cancelWork = false;

            public void onStart(ConnectionStatus status, int size, int reqCode) {
                if(onStringResponseListener != null)
                    onStringResponseListener.onStart(StringRequest.this);
            }

            public void onProgressChanged(int progress, int size, int reqCode, streamingStatus streamingStatus) {}

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
    /**
     * Read data from inputstream.
     * @param inStream
     * @return
     * @throws Exception While some errors occurs, usually because of connection leaks.
     * @author afshin - Apr 28, 2013 at 8:51:02 AM
     */
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
    public interface OnStringResponse {

        public void onStart(Request request);

        public void onFinish(Request request, String response, boolean success, HttpURLConnection connection, ConnectionStatus status);
    }

// ____________________________________________________________________

}
