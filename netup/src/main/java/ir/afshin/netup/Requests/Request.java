package ir.afshin.netup.Requests;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.InternetManager;
import ir.afshin.netup.base.Pair;
import ir.afshin.netup.base.PostParam;

/**
 * Created by afshinhoseini on 1/29/16.
 */
public abstract class Request extends InternetManager{

    private Object tag = null;
    private String identifier = "";
    private String group = "";
    long creationTime = 0;
    OnRequestFinishListener onRequestFinishListener = null;



// ____________________________________________________________________

    public Request(Context ctx) {

        this.ctx = ctx;
    }

// ____________________________________________________________________

    public Request(Context ctx, String url, ArrayList<Pair<String, String>> Headers, ArrayList<Pair<String, String>> get_params, ArrayList<PostParam> post_params)
    {
        this.ctx = ctx;
        setParams(url, Headers, get_params, post_params);
    }

// ____________________________________________________________________

    /**
     * Prepares an instance of {@link InternetManager}, sets it's parameter and also assigns authentication token if needed.
     * @param url The URL to connect to.
     * @param Headers Arbitrary headers to send or <code>null</code>.
     * @param get_params Arbitrary get parameters to send or <code>null</code>.
     * @param post_params Arbitrary post parameters to send or <code>null</code>.
     * @author Afshin - Jun 12, 2013  9:49:21 AM
     */

    public void setParams(String url, ArrayList<Pair<String, String>> Headers, ArrayList<Pair<String, String>> get_params, ArrayList<PostParam> post_params)
    {

        this.url = url;
        this.Headers = Headers;
        this.get_params = get_params;
        this.post_params = post_params;
    }

// ____________________________________________________________________

    public abstract void cancel();
    public abstract void startJob();
// ____________________________________________________________________

    public void setTag(Object tag) {

        this.tag = tag;
    }

// ____________________________________________________________________

    public Object getTag() {

        return tag;
    }

// ____________________________________________________________________

    public void setIdentifier(String identifier) {

        this.identifier = identifier;
    }

// ____________________________________________________________________

    public String getIdentifier() {

        return identifier;
    }

// ____________________________________________________________________

    public void setGroup(String group) {

        this.group = group;
    }

// ____________________________________________________________________

    public String getGroup() {

        return group;
    }

// ____________________________________________________________________

    public boolean isMemberOfGroup(String group) {

        return this.group.equals(group);
    }

// ____________________________________________________________________

    public boolean isEqualToIdentifier(String identifier) {

        return this.identifier.equals(identifier);
    }

// ____________________________________________________________________

    protected void notifyRequestFinishToqueue(ConnectionStatus status) {

        if(onRequestFinishListener != null)
            onRequestFinishListener.requestFinished(this, status);
    }

// ____________________________________________________________________

    interface OnRequestFinishListener {

         void requestFinished(Request request, ConnectionStatus status);
    }

// ____________________________________________________________________

    @Override
    public boolean equals(Object request) {

        boolean isEqual = false;

        Request req = (Request) request;

        isEqual = url.equals(req.url);

        return isEqual;
    }


// ____________________________________________________________________




}
