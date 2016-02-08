package ir.afshin.netup.Requests;

import android.content.Context;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.Pair;
import ir.afshin.netup.base.PostParam;

/**
 * Created by afshinhoseini on 2/8/16.
 */
public class JsonObjectRequest extends StringRequest {


    private OnJsonObjectResponse listener = null;

// ____________________________________________________________________

    public JsonObjectRequest(Context ctx, OnJsonObjectResponse listener) {

        super(ctx, null);
        super.setOnStringResponseListener(onStringResponse);
        setOnJsonResponseListener(listener);
    }

// ____________________________________________________________________

    public JsonObjectRequest(Context ctx, String url, ArrayList<Pair<String, String>> Headers, ArrayList<Pair<String, String>> get_params, ArrayList<PostParam> post_params, OnJsonObjectResponse listener) {

        super(ctx, url, Headers, get_params, post_params, null);
        super.setOnStringResponseListener(onStringResponse);
        setOnJsonResponseListener(listener);
    }

// ____________________________________________________________________

    public void setOnJsonResponseListener(OnJsonObjectResponse listener) {

        this.listener = listener;
    }

// ____________________________________________________________________

    public interface OnJsonObjectResponse {

        void onStart(JsonObjectRequest request);
        void onFinish(JsonObjectRequest request, JSONObject response, boolean success, HttpURLConnection connection, ConnectionStatus status);
    }

// ____________________________________________________________________

    private OnStringResponse onStringResponse = new OnStringResponse() {


        @Override
        public void onStart(Request request) {

            if(listener != null)
                listener.onStart(JsonObjectRequest.this);
        }

        @Override
        public void onFinish(Request request, String response, boolean success, HttpURLConnection connection, ConnectionStatus status) {

            JSONObject jsonObject = null;

            if(success) {

                try {

                    jsonObject = new JSONObject(response);
                } catch (Exception e) {

                    e.printStackTrace();
                    success = false;
                    status = ConnectionStatus.UNSUCCESSFUL;
                }
            }

            if(listener != null)
                listener.onFinish(JsonObjectRequest.this, jsonObject, success, connection, status);

        }
    };
// ____________________________________________________________________

}
