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

// ____________________________________________________________________
        public static ConnectionStatus toEquivalentStatus(int serverResponseCode)
        {
                ConnectionStatus status = null;

                if(serverResponseCode >= 200 && serverResponseCode < 300)
                {
                        status = ConnectionStatus.SUCCESSFUL;
                }
                else if(serverResponseCode >= 300 && serverResponseCode < 400)
                {
                        status = ConnectionStatus.UNSUCCESSFUL;
                }
                else if(serverResponseCode == 401) {

                        status = ConnectionStatus.UserNotAuthenticated;
                }
                else if(serverResponseCode >= 400 && serverResponseCode < 600)
                {
                        status = ConnectionStatus.UNSUCCESSFUL;
                }


                return status;
        }
// ____________________________________________________________________

}
