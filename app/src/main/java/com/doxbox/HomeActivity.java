package com.doxbox;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.doxbox.http.SingletonRequestQueue;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private ArrayList<String> mTitles = new ArrayList<>();
    private ArrayList<String> mImagesUrls = new ArrayList<>();
    private ArrayList<String> mShortTitle = new ArrayList<>();

    //private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            ActionMenuItemView search = findViewById(R.id.search);
            @SuppressLint("RestrictedApi") SearchView searchView = (SearchView) search.getItemData().getActionView();

            switch (item.getItemId()) {
                case R.id.navigation_movies:
                    searchMovies(searchView.getQuery().toString());
                    return true;
                case R.id.navigation_series:
                    searchSeries(searchView.getQuery().toString());
                    return true;
                case R.id.navigation_twitch:
                    //mTextMessage.setText(R.string.title_twitch);
                    return true;
                case R.id.navigation_youtube:
                    searchMediaYoutube(searchView.getQuery().toString());
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_field, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
                switch (bottomNavigationView.getSelectedItemId()) {
                    case R.id.navigation_movies:
                        searchMovies(query);
                        return true;
                    case R.id.navigation_series:
                        searchSeries(query);
                        return true;
                    case R.id.navigation_twitch:
                        //mTextMessage.setText(R.string.title_twitch);
                        return true;
                    case R.id.navigation_youtube:
                        searchMediaYoutube(query);
                        return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Log.d(TAG, "onCreate: started.");

        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        searchMovies("*");
    }


    protected void searchMovies(String filter) {
        searchMediaVubiquity("movie", "boxCover", filter);
    }

    protected void searchSeries(String filter) {
        searchMediaVubiquity("series", "thumbnail", filter);
    }

    protected void searchMediaYoutube(String filter){
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&key=AIzaSyD5EixcAnWnY79VcQaM7L8p6GJqfQOdrqk&maxResults=10";
        if(null!= filter && !filter.isEmpty()) {
            url += "&q="+filter;
        }
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
                        String imgUrl = (((JSONObject) response.getJSONArray("items").get(i)).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").getString("url"));

                        imgUrl = imgUrl.replace("\\", "");

                        mImagesUrls.add(imgUrl);
                        if(title.length()>40) {
                            mTitles.add(title.substring(0, 40));
                        }else{
                            mTitles.add(title);
                        }
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

        private void searchMediaVubiquity(String mediaType, final String imageType, String filter) {

            if(null == filter || filter.isEmpty()) {
                filter = "*";
            }
            String url = "https://ccsearch-q003.azureedge.net/indexes/0000d-"+mediaType+"-index/docs?api-version=2017-11-11&api-key=9454520FF92761E7FAABADB84FFBD150&search="+filter+"&$select=titleLong,summaryMedium,"+imageType+"&$top=10";
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
                        }
                        initRecyclerView();
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
