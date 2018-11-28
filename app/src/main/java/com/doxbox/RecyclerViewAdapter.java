package com.doxbox;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";
    private static final String VIDEO_ID = "videoID";
    private static final String ORIGIN = "origin";

    private ArrayList<String> mTitles = new ArrayList<>();
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> mShortTitle = new ArrayList<>();
    private ArrayList<String> mVidID = new ArrayList<>();
    private Context mContext;
    private int originID;

    public RecyclerViewAdapter(Context mContext, ArrayList<String> mTitles, ArrayList<String> mImages, ArrayList<String> mShortTitle, ArrayList<String> mVidID, int originID) {
        this.mTitles = mTitles;
        this.mImages = mImages;
        this.mContext = mContext;
        this.mShortTitle = mShortTitle;
        this.mVidID = mVidID;
        this.originID = originID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        Picasso.get().load(mImages.get(position)).into(holder.image);

        holder.title.setText(mTitles.get(position));
        holder.shortTitle.setText(mShortTitle.get(position));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: " + mTitles.get(position));

                Intent intent = new Intent(mContext, VideoDetailsActivity.class);
                intent.putExtra(VIDEO_ID, mVidID.get(position));
                intent.putExtra(ORIGIN, originID);

                mContext.startActivity(intent);
                Toast.makeText(mContext, mTitles.get(position), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTitles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView title;
        TextView shortTitle;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView){
            super(itemView);

            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            shortTitle = itemView.findViewById(R.id.shortTitle);
            parentLayout = itemView.findViewById(R.id.parent_layout);

        }
    }
}
