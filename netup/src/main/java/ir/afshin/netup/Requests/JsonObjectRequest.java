package ir.afshin.netup.Requests;

import android.content.Context;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.Pair;
import ir.afshin.netup.base.PostParam;

/**
 * It requests server for an json object response. If you know the api you're calling will return
 * json object, it's the case.
 * <p>
 * Created by afshinhoseini on 2/8/16.
 */
public class JsonObjectRequest extends StringRequest {


    private OnResponse listener = null;

// ____________________________________________________________________

    public JsonObjectRequest(Context ctx, OnResponse listener) {

        super(ctx, null);
        super.setOnStringResponseListener(onStringResponse);
        setOnJsonResponseListener(listener);
    }

// ____________________________________________________________________

    public JsonObjectRequest(Context ctx, String url, ArrayList<Pair<String, String>> Headers, ArrayList<Pair<String, String>> get_params, ArrayList<PostParam> post_params, OnResponse listener) {

        super(ctx, url, Headers, get_params, post_params, null);
        super.setOnStringResponseListener(onStringResponse);
        setOnJsonResponseListener(listener);
    }

// ____________________________________________________________________

    public void setOnJsonResponseListener(OnResponse listener) {

        this.listener = listener;
    }

// ____________________________________________________________________

    /**
     * Listens to {@link JsonObjectRequest json object requests} to figure out when a request starts
     * or when finishes.
     */
    public interface OnResponse {

        /**
         * Will be called once the given {@link JsonObjectRequest json object request} is started.
         * @param request The started request.
         */
        void onStart(JsonObjectRequest request);

        /**
         * Will be called once the given {@link JsonObjectRequest json object request} is finished.
         * @param request The finished request.
         * @param response The json object response.
         * @param success <i>true</i> if the request succeeded, <i>false</i> otherwise.
         * @param connection A pointer to connection itself.
         * @param status An instance of {@link ConnectionStatus} describing the status of connection.
         */
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
                }
            }

            if(listener != null)
                listener.onFinish(JsonObjectRequest.this, jsonObject, success, connection, status);

        }
    };
// ____________________________________________________________________

}
