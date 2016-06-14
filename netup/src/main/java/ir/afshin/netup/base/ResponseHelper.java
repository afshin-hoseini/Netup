package ir.afshin.netup.base;

/**
 * Created by afshin on 6/9/15.
 */
class ResponseHelper {

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
        else if((serverResponseCode >= 400 && serverResponseCode < 403) ||  (serverResponseCode >= 405 && serverResponseCode < 407))
        {
            status = ConnectionStatus.UserNotAuthenticated;
        }
        else if(serverResponseCode >= 407 && serverResponseCode < 600)
        {
            status = ConnectionStatus.UNSUCCESSFUL;
        }


        return status;
    }
}
