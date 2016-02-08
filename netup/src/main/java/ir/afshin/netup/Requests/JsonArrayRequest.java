package ir.afshin.netup.Requests;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.Pair;
import ir.afshin.netup.base.PostParam;

/**
 * Created by afshinhoseini on 2/8/16.
 */
public class JsonArrayRequest extends StringRequest {


    private OnJsonArrayResponse listener = null;

// ____________________________________________________________________

    public JsonArrayRequest(Context ctx, OnJsonArrayResponse listener) {

        super(ctx, null);
        super.setOnStringResponseListener(onStringResponse);
        setOnJsonResponseListener(listener);
    }

// ____________________________________________________________________

    public JsonArrayRequest(Context ctx, String url, ArrayList<Pair<String, String>> Headers, ArrayList<Pair<String, String>> get_params, ArrayList<PostParam> post_params, OnJsonArrayResponse listener) {

        super(ctx, url, Headers, get_params, post_params, null);
        super.setOnStringResponseListener(onStringResponse);
        setOnJsonResponseListener(listener);
    }

// ____________________________________________________________________

    public void setOnJsonResponseListener(OnJsonArrayResponse listener) {

        this.listener = listener;
    }

// ____________________________________________________________________

    public interface OnJsonArrayResponse {

        void onStart(JsonArrayRequest request);
        void onFinish(JsonArrayRequest request, JSONArray response, boolean success, HttpURLConnection connection, ConnectionStatus status);
    }

// ____________________________________________________________________

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
                    success = false;
                    status = ConnectionStatus.UNSUCCESSFUL;
                }
            }

            if(listener != null)
                listener.onFinish(JsonArrayRequest.this, jsonObject, success, connection, status);

        }
    };
// ____________________________________________________________________

}
