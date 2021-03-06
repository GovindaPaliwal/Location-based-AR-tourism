package com.marchengraffiti.nearism.nearism.place;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.marchengraffiti.nearism.nearism.R;
import com.marchengraffiti.nearism.nearism.firebase.FirebaseRead;
import com.marchengraffiti.nearism.nearism.firebase.MyCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class placesActivity extends AppCompatActivity implements OnMapReadyCallback {

    /*String ogImage;
    ArrayList<PlaceItem> data = new ArrayList<>();
    BufferedReader br = null;
    URL url;*/

    WebView webView;
    WebSettings webSettings;

    GoogleMap mMap;
    String[] mapValue;
    String placeTitle, addr1, firstimage;

    ImageView mainImage;
    Bitmap bitmap;
    TextView title, address;
    ImageButton back;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_detail);

        mainImage = findViewById(R.id.main_photo);
        title = findViewById(R.id.title);
        address = findViewById(R.id.place_addr);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        new task().execute();

        // Google Map API Fragment
        FragmentManager fragmentManager = getFragmentManager();
        final MapFragment mapFragment = (MapFragment)fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.800844, 128.141912), 14));
    }

    private class task extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params){
            FirebaseRead firebaseRead = new FirebaseRead();
            firebaseRead.ReadDB(new MyCallback() {
                @Override
                public void onCallback(String value) {
                    publishProgress(value);
                    Log.d("placeDB", value);
                }
            });
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values){
            mapValue = values[0].split(",");
            placeTitle = mapValue[2];
            addr1 = mapValue[3];
            firstimage = mapValue[4];

            final Intent intent = getIntent();

            double lat, lng;
            lat = Double.valueOf(intent.getStringExtra("lat"));
            lng = Double.valueOf(intent.getStringExtra("lng"));

            if(placeTitle.equals(intent.getStringExtra("title"))) {
                Log.d("webViewList", placeTitle);

                webView = findViewById(R.id.webview);
                webView.setWebViewClient(new WebViewClient());
                webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setSupportMultipleWindows(false);
                webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
                webSettings.setLoadWithOverviewMode(true);
                webSettings.setUseWideViewPort(true);
                webSettings.setSupportZoom(false);
                webSettings.setBuiltInZoomControls(false);
                webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                webSettings.setDomStorageEnabled(true);
                //webView.loadUrl("https://www.google.com/search?q=" + placeTitle +
                //        "&source=lnms&tbm=isch");

                webView.loadUrl("https://www.instagram.com/explore/tags/" + placeTitle + "/");

                // gallary
                //imageJsonParsing(placeTitle);

                // map fragment
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(intent.getStringExtra("title")));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14));

                // text
                title.setText(placeTitle);
                address.setText(addr1);

                setMainImage();
            }

            //mMap.addMarker(markerOptions);
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }

    public void setMainImage() {
        Thread mThread = new Thread() {
            @Override
            public void run() {
                try {
                    URL imgurl = new URL(firstimage);
                    HttpURLConnection connection = (HttpURLConnection) imgurl.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream ist = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(ist);
                } catch (IOException ex) {

                }
            }
        };

        mThread.start();

        try {
            mThread.join();
            mainImage.setImageBitmap(bitmap);
        } catch (InterruptedException e) {

        }
    }

    /*
    public void imageJsonParsing(String keyword) {
        Log.d("jsonResult222", keyword);

        ListView listView = findViewById(R.id.gallaryList);

        final String u = "https://www.googleapis.com/customsearch/v1?" +
                "key=AIzaSyCJrFlOU7R5t42RwRwwOUXbQ8IDO9Gh3AA&" +
                "cx=007829791969461891809:h3bcntx7vg2" +
                "&q=\"" + keyword + "\"&num=10";

        new Thread() {
            public void run() {
                try {
                    url = new URL(u);
                    final HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();

                    // GET 방식 request
                    urlconnection.setRequestMethod("GET");
                    br = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(), "UTF-8"));
                    String result = "";
                    String line;
                    while ((line = br.readLine()) != null) {
                        result = result + line + "\n";
                    }

                    JSONObject jsonObject = new JSONObject(result);
                    String items = jsonObject.getString("items");
                    JSONArray jsonArray = new JSONArray(items);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject subJsonObject = jsonArray.getJSONObject(i);
                        String pagemap = subJsonObject.getString("pagemap");
                        JSONObject subJsonObject2 = new JSONObject(pagemap);
                        String metatags = subJsonObject2.getString("metatags");
                        JSONArray jsonArray2 = new JSONArray(metatags);

                        for (int j = 0; j < jsonArray2.length(); j++) {
                            JSONObject subJsonObject3 = jsonArray2.getJSONObject(j);
                            ogImage = subJsonObject3.getString("og:image");
                            Log.d("jsonResult222", "og:image : " + ogImage);
                            PlaceItem placeItem = new PlaceItem(ogImage);
                            data.add(placeItem);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("jsonResult222", "data: " + data);
                //PlaceAdapter adapter = new PlaceAdapter(getApplicationContext(), R.layout.place_detail_item, data);
                //listView.setAdapter(adapter);
            }
        }.start();


    }*/
}

