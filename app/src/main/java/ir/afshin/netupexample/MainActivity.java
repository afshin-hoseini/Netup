package ir.afshin.netupexample;

import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import ir.afshin.netup.Requests.CacheManager;
import ir.afshin.netup.Requests.DownloadCachePolicy;
import ir.afshin.netup.Requests.DownloadRequest;
import ir.afshin.netup.Requests.Request;
import ir.afshin.netup.Requests.RequestQueue;
import ir.afshin.netup.Requests.StringRequest;
import ir.afshin.netup.base.ConnectionStatus;
import ir.afshin.netup.base.InternetManager;
import ir.afshin.netup.base.Pair;
import ir.afshin.netup.base.PostParam;

public class MainActivity extends AppCompatActivity {


    ListView lv_pics = null;
    ImageCache imageCache = null;
    RequestQueue requestQueue = new RequestQueue();
    PictureAdapter pictureAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ArrayList<PostParam> params = new ArrayList<>();
        params.add(new PostParam("email", "jel.ranjbar.saber@live.com", PostParam.ParamType.String));
        params.add(new PostParam("password", "a150091", PostParam.ParamType.String));

        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<String, String>("content-type" ,"application/x-www-form-urlencoded"));

        StringRequest stringRequest = new StringRequest(this, "http://ngad.ir/instime/index.php/instime/userLogin", headers, null, params, new StringRequest.OnStringResponse() {
            @Override
            public void onStart(Request request) {

            }

            @Override
            public void onFinish(Request request, String response, boolean success, HttpURLConnection connection, ConnectionStatus status) {

                Log.e("Server Resp", response);
            }
        });

        stringRequest.setMethod(InternetManager.Methods.POST);
        stringRequest.startJob();

//        lv_pics = (ListView) findViewById(R.id.lv_pictures);
//
//        //requestQueue.isDictator = true;
//        requestQueue.dictatorCapacity = 2;
//        imageCache = ImageCache.getInstance(this);
//
//        pictureAdapter = new PictureAdapter();
////        lv_pics.setAdapter(pictureAdapter);
//        CacheManager.clearExpiredFiles(this);
//        CacheManager.clearAllCachedFiles(this);
//
//        for(int i=0; i<50; i++) {
//
//            loadImage("pic"+i+".png");
//        }
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                requestQueue.pause();
//            }
//        }, 800);
//
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                requestQueue.resume();
//            }
//        }, 5000);

    }




    private void loadImage(String fileName) {

        String url = "http://www.tinypro.ir/IconSetO/" + fileName;
        DownloadRequest request = new DownloadRequest(this, url, null, downloadProgress);
        request.setTag(fileName);

//        DownloadCachePolicy dlcp = new DownloadCachePolicy();
//        dlcp.maxAge = 500;
//
//        request.setDownloadCachePolicy(dlcp);

        if( ! requestQueue.isExists(request)) {

            Log.e("Adding to queue", fileName);
            requestQueue.add(request);
        }
    }


    DownloadRequest.OnDownloadRequestProgress downloadProgress = new DownloadRequest.OnDownloadRequestProgress() {

        @Override
        public void onStart(DownloadRequest request) {

        }

        @Override
        public void onProgress(DownloadRequest request, long fileSize, long downloadedSize, float percent) {

        }

        @Override
        public void onFinish(DownloadRequest request, boolean success, ConnectionStatus status, String downloadedFilename) {

            if(success) {

                Bitmap bmp = BitmapFactory.decodeFile(downloadedFilename);
                String key = (String) request.getTag();

                if(key != null && bmp != null) {

                    imageCache.put(key , bmp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            pictureAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            else {

                Log.e("Err", status + "");
            }

        }
    };


    class PictureAdapter extends ArrayAdapter {

        public PictureAdapter() {

            super(MainActivity.this, 0);
        }

        @Override
        public int getCount() {
            return 11;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {


            View row = view;

            if (row == null) {

                row = getLayoutInflater().inflate(R.layout.row, viewGroup, false);
            }

            ImageView img = (ImageView) row.findViewById(R.id.img);
            TextView txt = (TextView) row.findViewById(R.id.txt_picName);

            txt.setText("Picture no: " + (i + 1));

            String picName = "pic" + (i + 1) + ".png";
            Bitmap bmpImage = imageCache.get(picName);

            if(bmpImage != null) {

                img.setImageBitmap(bmpImage);
            }
            else {

                img.setImageResource(R.mipmap.ic_launcher);
                loadImage(picName);
            }



            return row;
        }

    }



}
