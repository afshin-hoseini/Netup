package ir.afshin.netup.Requests;

import android.content.Context;

import org.json.JSONArray;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.Pair;
import ir.afshin.netup.base.PostParam;

/**
 * It requests server for an json array response. If you know the api you're calling will return
 * json array, it's the case.
 * Created by afshinhoseini on 2/8/16.
 */
public class JsonArrayRequest extends StringRequest {


    private OnResponse listener = null;

// ____________________________________________________________________

    public JsonArrayRequest(Context ctx, OnResponse listener) {

        super(ctx, null);
        super.setOnStringResponseListener(onStringResponse);
        setOnJsonResponseListener(listener);
    }

// ____________________________________________________________________

    public JsonArrayRequest(Context ctx, String url, ArrayList<Pair<String, String>> Headers, ArrayList<Pair<String, String>> get_params, ArrayList<PostParam> post_params, OnResponse listener) {

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
     * Listens to {@link JsonArrayRequest json array requests} to figure out when a request starts
     * or when finishes.
     */
    public interface OnResponse {

        /**
         * Will be called once the given {@link JsonArrayRequest json array request} is started.
         * @param request The started request.
         */
        void onStart(JsonArrayRequest request);

        /**
         * Will be called once the given {@link JsonArrayRequest json array request} is finished.
         * @param request The finished request.
         * @param response The json array response.
         * @param success <i>true</i> if the request succeeded, <i>false</i> otherwise.
         * @param connection A pointer to connection itself.
         * @param status An instance of {@link ConnectionStatus} describing the status of connection.
         */
        void onFinish(JsonArrayRequest request, JSONArray response, boolean success, HttpURLConnection connection, ConnectionStatus status);
    }

// ____________________________________________________________________

    /**
     * Listens to the result of api string response.
     */
    private OnStringResponse onStringResponse = new OnStringResponse() {


        @Override
        public void onStart(Request request) {

            if(listener != null)
                listener.onStart(JsonArrayRequest.this);
        }

        @Override
        public void onFinish(Request request, String response, boolean success, HttpURLConnection connection, ConnectionStatus status) {

            JSONArray jsonObject = null;

            if(success) {

                try {

                    jsonObject = new JSONArray(response);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            if(listener != null)
                listener.onFinish(JsonArrayRequest.this, jsonObject, success, connection, status);

        }
    };
// ____________________________________________________________________

}
