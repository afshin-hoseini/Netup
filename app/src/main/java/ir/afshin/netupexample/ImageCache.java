package ir.afshin.netupexample;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * Created by afshinhoseini on 2/2/16.
 */
public class ImageCache extends LruCache<String, Bitmap> {

    private ImageCache(int size) {
        super(size);
    }

    public static ImageCache getInstance(Context ctx) {

        ActivityManager am = (ActivityManager) ctx.getSystemService(ctx.ACTIVITY_SERVICE);
        int availMem_bytes = am.getMemoryClass()*1024*1024;
        int cacheSize = availMem_bytes / 8;

        return new ImageCache(cacheSize);
    }


    @Override
    protected int sizeOf(String key, Bitmap value) {

        Log.e("size", key + ": "+ value.getByteCount());
        return value.getByteCount();
    }



}
