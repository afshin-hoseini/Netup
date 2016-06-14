package ir.afshin.netup.Requests;

import android.content.Context;

import java.util.ArrayList;

import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.InternetManager;
import ir.afshin.netup.base.Pair;
import ir.afshin.netup.base.PostParam;

/**
 * An abstract class for requests.
 *
 * Created by afshinhoseini on 1/29/16.
 */
public abstract class Request extends InternetManager{

    /**
     * Stores a tagged object to which this request returns when finished.
     */
    private Object tag = null;
    /**
     * To identify the request.
     */
    private String identifier = "";
    /**
     * Used for grouping requests.
     */
    private String group = "";
    /**
     * Keeps the time that this request is created.
     */
    long creationTime = 0;
    /**
     * An instance of {@link OnRequestFinishListener} to tell the queue that the request is done.
     */
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
     * Sets needed parameters.
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
        this.getParams = get_params;
        this.postParams = post_params;
    }

// ____________________________________________________________________

    /**
     * Cancels the request.
     */
    public abstract void cancel();

    /**
     * Starts the request.
     */
    public abstract void startJob();
// ____________________________________________________________________

    /**
     * Sets the tag object.
     * @param tag
     * @see #tag
     */
    public void setTag(Object tag) {

        this.tag = tag;
    }

// ____________________________________________________________________

    /**
     * Gets the tag object.
     * @return The value of tagged object.
     */
    public Object getTag() {

        return tag;
    }

// ____________________________________________________________________

    /**
     * Sets the identifier of this request.
     * @param identifier The id of this request.
     * @see #identifier
     */
    public void setIdentifier(String identifier) {

        this.identifier = identifier;
    }

// ____________________________________________________________________

    /**
     * Gets the request identifier string.
     * @return The identifier String.
     * @see #identifier
     */
    public String getIdentifier() {

        return identifier;
    }

// ____________________________________________________________________

    /**
     * Sets the group name, which this request in belonh to.
     * @param group The group name this request is belong to.
     * @see  #group
     */
    public void setGroup(String group) {

        this.group = group;
    }

// ____________________________________________________________________

    /**
     * Gets the group name that this request is belong to.
     * @return The group name.
     * @see #group
     */
    public String getGroup() {

        return group;
    }

// ____________________________________________________________________

    /**
     * Determines that this request is a member of the given group name or not.
     * @param group
     * @return <i>true</i> if this request is a member of the given group name, <i>false</i> otherwise.
     */
    public boolean isMemberOfGroup(String group) {

        return this.group.equals(group);
    }

// ____________________________________________________________________

    /**
     * Checks if request identifier is equals to given identifier or not.
     * @param identifier
     * @return
     */
    public boolean isEqualToIdentifier(String identifier) {

        return this.identifier.equals(identifier);
    }

// ____________________________________________________________________

    /**
     * Tells the queue that this request is done.
     * @param status The status of connection when this request is finished.
     */
    protected void notifyRequestFinishToQueue(ConnectionStatus status) {

        if(onRequestFinishListener != null)
            onRequestFinishListener.requestFinished(this, status);
    }

// ____________________________________________________________________

    /**
     * Listens to request to figure out when finished.
     */
    interface OnRequestFinishListener {

         void requestFinished(Request request, ConnectionStatus status);
    }

// ____________________________________________________________________

    /**
     * Checks the equality of this request against the given request.
     * @param request
     * @return <i>true</i> if the given request object is equals to this request instance.
     */
    @Override
    public boolean equals(Object request) {

        boolean isEqual = false;

        Request req = (Request) request;

        isEqual = url.equals(req.url);

        return isEqual;
    }


// ____________________________________________________________________




}
