package com.doxbox;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.doxbox.http.SingletonRequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class VideoDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    String search = "dnViaXF1aXR5LmNvLnVrL3RpdGxlL1ZVQkkwMDAwMDAwMDAwMzM0ODYwL09uZVRpbWUvc3RyZWFtL3ByaXZhdGU6U0QvMjAxNy0wMy0wM1QwMDowMDowMC8yMDIwLTAyLTI4VDIzOjU5OjU5";

    String assetIDTrailer = "VUBI0000002215184894";
    String assetIDMovie = "VUBI0000002215184894";
    String offerID = "VUBI0000000001062305";

    int origin;

    TextView tvTitle;
    TextView tvYear;
    TextView tvActor;
    TextView tvDirector;
    TextView tvGenre;
    TextView tvDuration;
    TextView tvSummary;

    WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_video_details);

        loadExtras();

        tvActor = (TextView) findViewById(R.id.textViewActor);
        tvDirector = (TextView) findViewById(R.id.textViewDirector);
        tvDuration = (TextView) findViewById(R.id.textViewDuration);
        tvGenre = (TextView) findViewById(R.id.textViewGenre);
        tvSummary = (TextView) findViewById(R.id.textViewSummary);
        tvTitle = (TextView) findViewById(R.id.textViewTitle);
        tvYear = (TextView) findViewById(R.id.textViewYear);

        webView = (WebView) findViewById(R.id.webViewTrailerPlayback);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                android.util.Log.d("WebView", consoleMessage.message());
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);

        Button btnWatchNow = (Button) findViewById(R.id.btnWatchNow);
        btnWatchNow.setOnClickListener(this);

        switch (origin){
            case R.id.navigation_movies:
                searchVideoDetails("movie");
                break;

            case R.id.navigation_series:
                searchVideoDetails("series");
                break;
        }

    }

    protected void searchVideoDetails(String mediaType) {
        SingletonRequestQueue queue = SingletonRequestQueue.getInstance(this);
        String url = "https://ccsearch-q003.azureedge.net/indexes/0000d-"+ mediaType + "-index/docs?api-version=2017-11-11&api-key=9454520FF92761E7FAABADB84FFBD150&search=" + search + "&$select=titleLong,year,primaryGenre,preview,movie,offer,summaryLong,actor,director,duration&$top=1";
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        try {
                            JSONObject aux = (JSONObject) response.getJSONArray("value").get(0);

                            String actor = getString(R.string.tvActor) + " " + returnArrayInString(aux.getJSONArray("actor"));
                            String director = getString(R.string.tvDirector) + " " + returnArrayInString(aux.getJSONArray("director"));
                            String duration = getString(R.string.tvDuration) + " " + aux.getString("duration");
                            String genre = getString(R.string.tvGenre) + " " + aux.getString("primaryGenre");
                            String summary = getString(R.string.tvSummary) + " " + aux.getString("summaryLong");
                            String year = getString(R.string.tvYear) + " " + aux.getString("year");

                            tvActor.setText(actor);
                            tvDirector.setText(director);
                            tvDuration.setText(duration);
                            tvGenre.setText(genre);
                            tvSummary.setText(summary);
                            tvYear.setText(year);
                            tvTitle.setText(aux.getString("titleLong"));

                            String offerRaw = aux.getJSONArray("offer").getString(0);
                            String assetTrailer = aux.getString("preview");
                            String assetVideo = aux.getString("movie");

                            offerID = offerRaw.replace("vubiquity.co.uk/offer/","");
                            assetIDTrailer = assetTrailer.replace("vubiquity.co.uk/contentgroup/", "");
                            assetIDMovie = assetVideo.replace("vubiquity.co.uk/contentgroup/", "");

                            /*
                            Log.d("Response: " , aux.getString("titleLong"));
                            Log.d("Response: " , aux.getString("year"));
                            Log.d("Response: " , aux.getString("preview"));
                            Log.d("Response: " , aux.getString("movie"));
                            Log.d("Response: " , aux.getJSONArray("offer").getString(0));
                            Log.d("Response: " , aux.getString("summaryLong"));
                            Log.d("Response: " , aux.getJSONArray("actor").getString(0));
                            Log.d("Response: " , aux.getJSONArray("director").getString(0));
                            Log.d("Response: " , aux.getString("duration"));
                            Log.d("Response: " , aux.getString("primaryGenre"));
                            */

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        loadWebView();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Response: " ,"ERRO NA CHAMADA DO SERVIÇO DE BUSCA DE VIDEOS");
                    }
                }
        )
        {};
        DefaultRetryPolicy policy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        postRequest.setRetryPolicy(policy);
        queue.addToRequestQueue(postRequest);


    }

    private String returnArrayInString(JSONArray jsonArray){
        StringBuilder stringBuilder = new StringBuilder();

        try {
            stringBuilder.append(jsonArray.getString(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 1; i < jsonArray.length(); i++){
            try {
                stringBuilder.append(", " + jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return stringBuilder.toString();
    }


    private void loadWebView(){
        webView.loadUrl("file:///android_asset/player/playback.html?assetId=" + assetIDTrailer + "&offerId=" + offerID);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnWatchNow:
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_OFFER, offerID);
                intent.putExtra(PlayerActivity.EXTRA_ASSET, assetIDMovie);
                startActivity(intent);
                break;
        }
    }

    void loadExtras(){
        Intent intent = this.getIntent();
        if(intent!= null){
            search = intent.getStringExtra(RecyclerViewAdapter.VIDEO_ID);
            origin = intent.getIntExtra(RecyclerViewAdapter.ORIGIN,-1);
        }
    }
}
