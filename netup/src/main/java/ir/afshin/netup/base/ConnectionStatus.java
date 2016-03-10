package ir.afshin.netup.base;

import android.content.Context;

/**
 * Created by afshinhoseini on 1/29/16.
 */
public enum ConnectionStatus {

        CACHED,
        SUCCESSFUL,
        UNSUCCESSFUL,
        NO_INTERNET,
        SERVER_ERROR,
        /**
         * The connection canceled by {@link InternetManager#disconnect()}, usually by user.
         */
        CANCELED,
        /**
         * The connection will need user authentication on server, but no user authentication token found.
         */
        NO_AUTH_TOKEN, UserNotAuthenticated, TIMEOUT;

        /**
         * @param ctx Current {@link Context} object.
         * @return
         */
        public String getMessage(Context ctx)
        {
            String statusMsg = "";


//			if(this == OnConnectionResultListener.ConnectionStatus.NO_INTERNET)
//				statusMsg = ctx.getString(R.string.NoInternetConnection);
//			else if(this == ConnectionStatus.SUCCESSFUL)
//				statusMsg = ctx.getString(R.string.successful);
//			else if(this == OnConnectionResultListener.ConnectionStatus.SERVER_ERROR)
//				statusMsg = ctx.getString(R.string.ServerNotRespond);
//			else  if(this == OnConnectionResultListener.ConnectionStatus.CANCELED)
//				statusMsg = ctx.getString(R.string.UserAbortedTheOperation);
//			else  if(this == OnConnectionResultListener.ConnectionStatus.UserNotAuthenticated)
//				statusMsg = ctx.getString(R.string.UserNotAuthenticated);
//			else
//				statusMsg = ctx.getString(R.string.Unsuccessful);

            return statusMsg;
        }


}
