package ir.afshin.netup.Requests;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import ir.afshin.netup.Coding;

/**
 * Manages the private cached items.
 * Created by afshinhoseini on 2/2/16.
 */
public class CacheManager {

    /**The folder that keeps the cached items*/
    private static String _cacheFolder = null;

// ____________________________________________________________________

    /**
     * Wipe off all cached items.
     * @param ctx A context object.
     */
    public static void clearAllCachedFiles(Context ctx) {

        File cacheFolder = new File(getCacheFolder(ctx));

        if(cacheFolder.exists()) {

            File[] files = cacheFolder.listFiles();

            for(File file: files) {

                file.delete();
            }
        }
    }

// ____________________________________________________________________

    /**
     * Wipe off all expired cached items.
     * @param ctx
     */
    public static void clearExpiredFiles(Context ctx) {

        File cacheFolder = new File(getCacheFolder(ctx));
        long currentTime = System.currentTimeMillis();

        if(cacheFolder.exists()) {

            String[] files = cacheFolder.list();

            for(String file: files) {

                int lastDashIndex = file.lastIndexOf('-');

                if( lastDashIndex>-1 ) {

                    long expireTime = stringToLong(file.substring(lastDashIndex + 1));

                    if(expireTime < currentTime) {

                        Log.e("EXPIRED FILE", file);
                        File f = new File(getCacheFolder(ctx)+file);
                        f.delete();
                    }
                }

            }
        }
    }

// ____________________________________________________________________

    /**
     * Creates a cache filename based on file url and given
     * {@link DownloadCachePolicy download cache policy object}.
     * @param ctx An object of context.
     * @param url The url of file.
     * @param cachePolicy The cache policy used for this download.
     * @return The generated cache filename including the complete file path.
     */
    public static String createCacheFilename(Context ctx, String url, DownloadCachePolicy cachePolicy) {

        long maxAge = new DownloadCachePolicy().maxAge;
        if (cachePolicy != null)
            maxAge = cachePolicy.maxAge;

        return getCacheFolder(ctx) + getCacheFileNameHash(url) + "-" + (System.currentTimeMillis() + maxAge);
    }

// ____________________________________________________________________

    /**
     * Prepare a hash from the given url.
     * @param url The url of the file.
     * @return
     */
    public static String getCacheFileNameHash(String url) {

        return Coding.makeMD5(url);
    }

// ____________________________________________________________________

    /**
     * Retrieves the cached filename of the given url.
     * @param ctx An instance of context.
     * @param url The url of the file your looking for.
     * @return The cached <i>filename</i> of the given url if found, <b>null</b> if not found.
     */
    public static String getCachedFileName(Context ctx, String url) {

        String cacheDirectory = getCacheFolder(ctx);
        File cacheFolder = new File(cacheDirectory);
        cacheFolder.mkdirs();

        String searchingFilename = getCacheFileNameHash(url);
        long currentTime = System.currentTimeMillis();
        String foundCacheFile = null;

        String[] cachedFiles = cacheFolder.list();

        for(String cachedFile: cachedFiles) {

            int dashIdx = cachedFile.indexOf('-');
            if(dashIdx < 0) { Log.e("NO DASH", "NO DASH"); continue; }

            String fileNamePart = cachedFile.substring(0, dashIdx);

            if(fileNamePart.equals(searchingFilename)) {

                long expireTime = stringToLong(cachedFile.substring(dashIdx + 1));
                if (expireTime > currentTime) {

                    foundCacheFile = getCacheFolder(ctx) + cachedFile;
                    break;
                }
            }
        }

        return foundCacheFile;
    }

// ____________________________________________________________________

    /**
     * Coverts string to long in a safe manner.
     * @param number
     * @return The convertd long number or <i>0</i>, if couldn't convert.
     */
    private static long stringToLong(String number) {

        long lng = 0;

        try{

            lng = Long.parseLong(number);

        }catch (Exception e) {

            lng = 0;
        }

        return lng;
    }

// ____________________________________________________________________

    /**
     * @param ctx An instance of context.
     * @return The cache folder where all the cached items will go there.
     */
    public static String getCacheFolder(Context ctx) {

        if (_cacheFolder != null) return _cacheFolder;

        _cacheFolder = ctx.getCacheDir().getPath();

        if (!_cacheFolder.endsWith("/")) {

            _cacheFolder += "/";
        }

        return _cacheFolder;
    }
// ____________________________________________________________________
}
