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



}
