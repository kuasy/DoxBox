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
                    //mTextMessage.setText(R.string.title_movies);
                    return true;
                case R.id.navigation_series:
                    //mTextMessage.setText(R.string.title_series);
                    return true;
                case R.id.navigation_twitch:
                    //mTextMessage.setText(R.string.title_twitch);
                    return true;
                case R.id.navigation_youtube:
                    //mTextMessage.setText(R.string.title_youtube);
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
        SingletonRequestQueue queue = SingletonRequestQueue.getInstance(this);
        String url = "https://ccsearch-q003.azureedge.net/indexes/0000d-movie-index/docs?api-version=2017-11-11&api-key=9454520FF92761E7FAABADB84FFBD150&search=*&$select=titleLong,summaryMedium,boxCover&$top=5";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response){

                        /*ImageView iv1 = (ImageView) findViewById(R.id.imageView1);
                        ImageView iv2 = (ImageView) findViewById(R.id.imageView2);
                        ImageView iv3 = (ImageView) findViewById(R.id.imageView3);
                        ImageView iv4 = (ImageView) findViewById(R.id.imageView4);
                        ImageView iv5 = (ImageView) findViewById(R.id.imageView5);


                        ImageView[] img = {iv1, iv2, iv3, iv4, iv5};*/


                        try {
                            for(int i=0;i<response.getJSONArray("value").length();i++) {
                                //img[i].setImageBitmap(); = ((JSONObject)response.getJSONArray("value").get(i)).getString("boxCover");

                                String imgUrl = (((JSONObject) response.getJSONArray("value").get(i)).getJSONArray("boxCover").getString(0));
                                imgUrl = imgUrl.replace("\\", "");

                                //Picasso.get().load(imgUrl).into(img[i]);
                                mImagesUrls.add(imgUrl);
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
