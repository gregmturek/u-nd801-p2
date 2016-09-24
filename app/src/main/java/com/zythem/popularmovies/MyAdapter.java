package com.zythem.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private String[][] mMovieData;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTextViewTitle;
        public ImageView mImageView;
        public TextView mTextViewDate;
        public TextView mTextViewRating;

        public MyViewHolder(View v) {
            super(v);

            mCardView = (CardView) v.findViewById(R.id.card_view);
            mTextViewTitle = (TextView) v.findViewById(R.id.tv_title);
            mImageView = (ImageView) v.findViewById(R.id.iv_image);
            mTextViewDate = (TextView) v.findViewById(R.id.tv_date);
            mTextViewRating = (TextView) v.findViewById(R.id.tv_rating);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), DetailActivity.class);
                    DetailActivity movieInfo = new DetailActivity();
                    movieInfo.mId = (String) mCardView.getTag();
                    movieInfo.mTitle = mTextViewTitle.getText().toString();
                    intent.putExtra("THE_DATA", movieInfo);
                    v.getContext().startActivity(intent);
//                    Toast.makeText(v.getContext(), "The ID and Title are: " + theId + " and " + theTitle, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context context, String[][] movieData) {
        this.context = context;
        mMovieData = movieData;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTextViewTitle.setText(mMovieData[position][0]);
        Picasso.with(context).load(mMovieData[position][1]).into(holder.mImageView);
        holder.mTextViewDate.setText(mMovieData[position][2]);
        holder.mTextViewRating.setText(mMovieData[position][3]);
        holder.mCardView.setTag(mMovieData[position][4]);
    }

    @Override
    public int getItemCount() {
        return mMovieData.length;
    }
}
