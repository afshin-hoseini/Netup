package ir.afshin.netup.Requests;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import ir.afshin.netup.Coding;

/**
 * Created by afshinhoseini on 2/2/16.
 */
public class CacheManager {

    /**The first item is filename and the second part after dash is expire time.*/
    private final String cacheFileTemplate = "%1$s-%2$s";
    private static String _cacheFolder = null;

// ____________________________________________________________________

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

    public static String createCacheFilename(Context ctx, String url, DownloadCachePolicy cachePolicy) {

        long maxAge = new DownloadCachePolicy().maxAge;
        if (cachePolicy != null)
            maxAge = cachePolicy.maxAge;

        return getCacheFolder(ctx) + getCacheFileNameHash(url) + "-" + (System.currentTimeMillis() + maxAge);
    }

// ____________________________________________________________________

    public static String getCacheFileNameHash(String url) {

        return Coding.makeMD5(url);
    }

// ____________________________________________________________________

    /**
     *
     * @param ctx
     * @param url
     * @return The fileName if found, <b>null</b> if not found.
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

    public static String getCacheFolder(Context ctx) {

        if (_cacheFolder != null) return _cacheFolder;

        /*String privatePath = Environment.getDataDirectory().getAbsolutePath() + "/data/" + ctx.getPackageName();

        if (!privatePath.endsWith("/")) {

            privatePath += "/";
        }

        String cacheDir = Environment.getDownloadCacheDirectory().getPath();
        if (cacheDir.startsWith("/")){

            cacheDir = cacheDir.substring(1);
        }

        _cacheFolder = privatePath + cacheDir + "/";*/

        _cacheFolder = ctx.getCacheDir().getPath();

        if (!_cacheFolder.endsWith("/")) {

            _cacheFolder += "/";
        }

        return _cacheFolder;
    }
// ____________________________________________________________________
}
