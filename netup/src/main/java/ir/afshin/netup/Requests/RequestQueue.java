package ir.afshin.netup.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import ir.afshin.netup.base.ConnectionStatus;

/**
 * Creates a queue for requests which supports two modes:
 * <ol>
 *     <li>
 *         <b>Normal:</b><p>
 *         All requests will be served sequentially.
 *         <b>Dictator:</b><p>
 *         It is usable when you wanna dispatch many low-priority requests to
 *         server, for example downloading avatar images of posts in a listView or something similar.
 *         This way, the queue will cancel some old requests to feed the newest ones. The number of
 *         requests that queue can serve in dictator mode is determined using
 *         {@link #dictatorCapacity}.
 *
 *     </li>
 * </ol>
 * The queue is also designed to serve requests in a concurrent manner. By setting the <i> max
 * concurrent serving requests</i> in {@link #createNormalQueue(int)} you can determine it. The
 * minimum value is 1 and the maximum value is 8.
 * Created by afshinhoseini on 1/29/16.
 */
public class RequestQueue {

    ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();
    ArrayList<Request> nowServingRequests = new ArrayList<>();
    private boolean isDictator = false;
    private int dictatorCapacity = 20;
    private int maxConcurrentServingRequests = 4;
    boolean pause = false;
    boolean cancelled = false;
    boolean isAdding = false;

// ____________________________________________________________________

    private RequestQueue(int maxConcurrentServingRequests, boolean isDictator, int dictatorCapacity) {

        if(maxConcurrentServingRequests < 1)
            maxConcurrentServingRequests = 1;
        else if(maxConcurrentServingRequests > 8)
            maxConcurrentServingRequests = 8;

        this.maxConcurrentServingRequests = maxConcurrentServingRequests;
        this.isDictator = isDictator;
        this.dictatorCapacity = dictatorCapacity;
    }

// ____________________________________________________________________

    /**
     * Creates a normal request queue.
     * @param maxConcurrentServingRequests The maximum concurrent requests.
     * @return An instance of {@link RequestQueue}.
     */
    public static RequestQueue createNormalQueue(int maxConcurrentServingRequests) {

        return new RequestQueue(maxConcurrentServingRequests, false, 0);
    }

// ____________________________________________________________________

    /**
     * Creates a normal request queue.
     * @return An instance of {@link RequestQueue} with 4 concurrent serving requests.
     */
    public static RequestQueue createNormalQueue() {

        return createNormalQueue(4);
    }

// ____________________________________________________________________

    /**
     * Creates a dictator request queue.
     * @param maxConcurrentServingRequests The maximum concurrent requests.
     * @param dictatorCapacity The capacity of queue.
     * @return An instance of {@link RequestQueue} with a dictator behavior.
     */
    public static RequestQueue createDictatorQueue(int maxConcurrentServingRequests, int dictatorCapacity) {

        return new RequestQueue(maxConcurrentServingRequests, true, dictatorCapacity);
    }

// ____________________________________________________________________

    /**
     * Creates a dictator request queue, with 4 concurrent serving requests and 20 requests for
     * dictator capacity.
     * @return An instance of {@link RequestQueue} with a dictator behavior.
     */
    public static RequestQueue createDictatorQueue() {

        return createDictatorQueue(4, 20);
    }
// ____________________________________________________________________

    /**
     * Checks if a request is already exists in the list or not. This function checks the url of
     * request to find if already exists or not.
     * @param request The request to check.
     * @return <i>true</i> if the request is available, <i>false</i> otherwise.
     */
    public synchronized boolean isExists(Request request) {

        if(request == null) return true;

        boolean isExists = false;

        Object[] arrObj_requests =  requests.toArray();
        if(arrObj_requests != null && arrObj_requests.length > 0) {


            for (Object req : arrObj_requests) {

                if (((Request) req).equals(request)) {

                    isExists = true;
                    break;
                }
            }

            if (isExists) return true;
        }

        for(Request req : nowServingRequests) {

            if(req.equals(request)) {

                isExists = true;
                break;
            }
        }

        return isExists;

    }

// ____________________________________________________________________

    /**
     * Adds the given request to queue.
     * @param request The reuqest to add.
     */
    public synchronized void add(Request request) {

        isAdding = true;
        requests.add(request);

        if(isDictator) {

            if(requests.size() > dictatorCapacity) {

                Object[] arr_requests = requests.toArray();
                int itemsToDelete = requests.size() - dictatorCapacity;

                Log.e("Items to Delete", "" + itemsToDelete);

                for(int i =0; i < itemsToDelete; i++) {

                    requests.remove(arr_requests[i]);
                    Log.e("Dictator", "Removed request with tag: " + (String)((Request) arr_requests[i] ).getTag());
                }

            }
        }

        isAdding = false;
        serve();
    }

// ____________________________________________________________________

    /**
     * Cancels whole requests which belong to the given group.
     * @param groupName The group name to cancel.
     */
    public void cancelRequestsOfGroup(String groupName) {

        pause();

        Object[] arrObj_requests = requests.toArray();

        if(arrObj_requests != null) {

            for (Object request : arrObj_requests) {

                if (((Request) request).isMemberOfGroup(groupName)) {

                    requests.remove(request);
                }
            }
        }


        Object[] arrObj_nowServigRequests = nowServingRequests.toArray();

        if(arrObj_nowServigRequests != null) {

            for (Object request : arrObj_nowServigRequests) {

                if (((Request) request).isMemberOfGroup(groupName)) {

                    ((Request) request).cancel();
                }
            }
        }

        System.gc();

        resume();

    }

// ____________________________________________________________________

    /**
     * Cancels whole requests with the given identifier.
     * @param identifier The id of request(s) you wanna cancel.
     */
    public void cancelRequestsWithIdentifier(String identifier) {

        pause();

        Object[] arrObj_requests = requests.toArray();

        if(arrObj_requests != null) {

            for (Object request : arrObj_requests) {

                if (((Request) request).isEqualToIdentifier(identifier)) {

                    requests.remove(request);
                }
            }
        }


        Object[] arrObj_nowServigRequests = nowServingRequests.toArray();

        if(arrObj_nowServigRequests != null) {

            for (Object request : arrObj_nowServigRequests) {

                if (((Request) request).isEqualToIdentifier(identifier)) {

                    ((Request) request).cancel();
                }
            }
        }

        System.gc();

        resume();
    }

// ____________________________________________________________________

    /**
     * Cancels requests which determined by the given {@link RequestFilter} instance.
     * @param deletionFilter An instance of {@link RequestFilter} dtermines which request must be
     *                       cancelled, which shouldn't.
     */
    public synchronized void cancelRequests(RequestFilter deletionFilter) {

        if(deletionFilter == null) return;

        pause();

        Object[] arrObj_requests = requests.toArray();

        if(arrObj_requests != null) {

            for (Object request : arrObj_requests) {

                if (deletionFilter.shouldPerformActionOn((Request) request)) {

                    requests.remove(request);
                }
            }
        }


        Object[] arrObj_nowServigRequests = nowServingRequests.toArray();

        if(arrObj_nowServigRequests != null) {

            for (Object request : arrObj_nowServigRequests) {

                if (deletionFilter.shouldPerformActionOn((Request) request)) {

                    ((Request) request).cancel();
                }
            }
        }

        System.gc();

        resume();

    }

// ____________________________________________________________________

    /**
     * Cancels all requests on this queue.
     */
    public void cancelAll() {

        cancelled = true;

        requests.clear();

        Object[] arrObj_request = nowServingRequests.toArray();

        for(Object request : arrObj_request) {

            ((Request)request).cancel();
        }
    }

// ____________________________________________________________________

    /**
     * Dispatches the requests to server.
     */
    private synchronized void serve() {

        if(pause || isAdding || cancelled) return;

        while(nowServingRequests.size() < maxConcurrentServingRequests) {

            Request request = requests.poll();

            if(request != null) {

                nowServingRequests.add(request);
                request.onRequestFinishListener = this.requestFinishListener;
                request.startJob();
            }
            else
                break;
        }

        Log.e("SERVING COUNT", nowServingRequests.size() + "");
    }

// ____________________________________________________________________

    /**
     * Pauses the queue.
     */
    public void pause() {

        pause = true;
    }

// ____________________________________________________________________

    /**
     * Resumes the queue.
     */
    public void resume() {

        pause = false;
        serve();
    }

// ____________________________________________________________________

    /**
     * Listens to requests for their finish status. Once one become finish, it starts serving to
     * next request.
     */
    private Request.OnRequestFinishListener requestFinishListener = new Request.OnRequestFinishListener() {

        @Override
        public void requestFinished(Request request, ConnectionStatus status) {

            nowServingRequests.remove(request);
            serve();
        }
    };

// ____________________________________________________________________

    /**
     * Filters the request of a queue and determines the pending action should be performed on
     * which requests.
     */
    public interface RequestFilter {

        /**
         * Determines if the pending action can be performed on the given request or not.
         * @param request The given request.
         * @return <i>true</i> means that the pending action could be performed on the given request
         * , <i>false</i> otherwise.
         */
        boolean shouldPerformActionOn(Request request);
    }

// ____________________________________________________________________

}
