package ir.afshin.netup.Requests;

/**
 * Created by afshinhoseini on 1/29/16.
 */
public class DownloadCachePolicy {

    /**
     * An constant representing the number of milliseconds of a single day.
     */
    public static final long OneDay = 86400000;
    /**
     * An constant representing the number of milliseconds of a single hour.
     */
    public static final long OneHour = 60000;


    /**
     * Determines if the downloading content must be kept in cached places or not.
     */
    public boolean shouldCache = true;
    /**
     * Determines the maximum age of cached file, the default value is 2 days.
     */
    public long maxAge = OneDay * 2;

}
