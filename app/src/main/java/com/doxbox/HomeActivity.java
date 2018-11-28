package com.doxbox;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.doxbox.http.SingletonRequestQueue;
import com.squareup.picasso.Picasso;

import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private ArrayList<String> mTitles = new ArrayList<>();
    private ArrayList<String> mImagesUrls = new ArrayList<>();
    private ArrayList<String> mShortTitle = new ArrayList<>();

    //private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_movies:
                    searchMovies();
                    return true;
                case R.id.navigation_series:
                    searchSeries();
                    return true;
                case R.id.navigation_twitch:
                    //mTextMessage.setText(R.string.title_twitch);
                    return true;
                case R.id.navigation_youtube:
                    searchMediaYoutube();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Log.d(TAG, "onCreate: started.");

        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        searchMovies();
    }


    protected void searchMovies() {
        searchMediaVubiquity("movie", "boxCover");
    }

    protected void searchSeries() {
        searchMediaVubiquity("series", "thumbnail");
    }

    protected void searchMediaYoutube(){
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&key=AIzaSyD5EixcAnWnY79VcQaM7L8p6GJqfQOdrqk&maxResults=10";
        SingletonRequestQueue queue = SingletonRequestQueue.getInstance(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response){

                try {
                    mTitles = new ArrayList<>();
                    mImagesUrls = new ArrayList<>();
                    mShortTitle = new ArrayList<>();
                    for(int i=0;i<response.getJSONArray("items").length();i++) {
                        String title = (((JSONObject) response.getJSONArray("items").get(i)).getJSONObject("snippet").getString("title"));
                        String shortTitle = (((JSONObject) response.getJSONArray("items").get(i)).getJSONObject("snippet").getString("description"));
                        String imgUrl = (((JSONObject) response.getJSONArray("items").get(i)).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("medium").getString("url"));

                        imgUrl = imgUrl.replace("\\", "");

                        mImagesUrls.add(imgUrl);
                        mTitles.add(title);
                        mShortTitle.add(shortTitle);
                        System.out.println("Response: " + response);

                        initRecyclerView();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("ERRO NA CHAMADA DO SERVIÇO DE BUSCA DE ITENS");
                    }
                }
        );
        DefaultRetryPolicy policy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        queue.addToRequestQueue(jsonObjectRequest);
    }

        private void searchMediaVubiquity(String mediaType, final String imageType){

            String url = "https://ccsearch-q003.azureedge.net/indexes/0000d-"+mediaType+"-index/docs?api-version=2017-11-11&api-key=9454520FF92761E7FAABADB84FFBD150&search=*&$select=titleLong,summaryMedium,"+imageType+"&$top=5";
            SingletonRequestQueue queue = SingletonRequestQueue.getInstance(this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response){

                    try {
                        mTitles = new ArrayList<>();
                        mImagesUrls = new ArrayList<>();
                        mShortTitle = new ArrayList<>();
                        for(int i=0;i<response.getJSONArray("value").length();i++) {
                            String imgUrl;
                            if(imageType.equals("boxCover")) {
                                imgUrl = (((JSONObject) response.getJSONArray("value").get(i)).getJSONArray("boxCover").getString(0));
                            }else{
                                imgUrl = (((JSONObject) response.getJSONArray("value").get(i)).getJSONArray("thumbnail").getString(0));
                            }
                            imgUrl = imgUrl.replace("\\", "");

                            mImagesUrls.add(imgUrl+"&q=60&w=100");
                            mTitles.add(((((JSONObject) response.getJSONArray("value").get(i)).getString("titleLong"))));
                            mShortTitle.add(((((JSONObject) response.getJSONArray("value").get(i)).getString("summaryMedium"))));
                            System.out.println("Response: " + response);

                            initRecyclerView();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("ERRO NA CHAMADA DO SERVIÇO DE BUSCA DE ITENS");
                        }
                    }
            );
            DefaultRetryPolicy policy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjectRequest.setRetryPolicy(policy);
            queue.addToRequestQueue(jsonObjectRequest);
        }


        private void initRecyclerView() {
            Log.d(TAG, "initRecyclerView: init recycleview.");
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mTitles, mImagesUrls, mShortTitle);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }


    }
