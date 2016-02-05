package ir.afshin.netup.Requests;

/**
 * Created by afshinhoseini on 1/29/16.
 */
public class DownloadCachePolicy {

    public static final long OneDay = 86400000;
    public static final long OneHour = 60000;

    public boolean shouldCache = true;
    public long maxAge = OneDay * 2;

}
