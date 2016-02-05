package ir.afshin.netup.Requests;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import ir.afshin.netup.base.ConnectionStatus;

/**
 * Created by afshinhoseini on 1/29/16.
 */
public class RequestQueue {

    ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();
    ArrayList<Request> nowServingRequests = new ArrayList<>();
    public boolean isDictator = false;
    public int dictatorCapacity = 20;
    public int maxServingCount = 4;
    boolean pause = false;
    boolean cancelled = false;
    boolean isAdding = false;

// ____________________________________________________________________

    /**
     * Currently just checks for url.
     * @param request
     * @return
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

    public void cancelAll() {

        cancelled = true;

        requests.clear();

        Object[] arrObj_request = nowServingRequests.toArray();

        for(Object request : arrObj_request) {

            ((Request)request).cancel();
        }
    }

// ____________________________________________________________________

    private synchronized void serve() {

        if(pause || isAdding || cancelled) return;

        while(nowServingRequests.size() < maxServingCount) {

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

    public void pause() {

        pause = true;
    }

// ____________________________________________________________________

    public void resume() {

        pause = false;
        serve();
    }

// ____________________________________________________________________

    private Request.OnRequestFinishListener requestFinishListener = new Request.OnRequestFinishListener() {

        @Override
        public void requestFinished(Request request, ConnectionStatus status) {

            nowServingRequests.remove(request);
            serve();
        }
    };

// ____________________________________________________________________

    public interface RequestFilter {

        boolean shouldPerformActionOn(Request request);
    }

// ____________________________________________________________________

}
