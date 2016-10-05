package com.zythem.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context mContext;
    private String[][] mMovieData;

    private static class MovieDataToPass {
        String mTitle;
        String mImagepath;
        String mDate;
        String mRating;
        String mId;
        String mOverview;
        String mImagepath2;
    }

    private int mCardImageWidth;
    private int mCardImageHeight;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        TextView mTextViewTitle;
        ImageView mImageView;
        TextView mTextViewDate;
        TextView mTextViewRating;

        MyViewHolder(View v) {
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
                    MovieDataToPass data = (MovieDataToPass) mCardView.getTag();
                    movieInfo.mTitle = data.mTitle;
                    movieInfo.mImagepath = data.mImagepath;
                    movieInfo.mDate = data.mDate;
                    movieInfo.mRating = data.mRating;
                    movieInfo.mId = data.mId;
                    movieInfo.mOverview = data.mOverview;
                    movieInfo.mImagepath2 = data.mImagepath2;
                    intent.putExtra("THE_DATA", movieInfo);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    MyAdapter(Context context, String[][] movieData) {
        this.mContext = context;
        mMovieData = movieData;

        int cardsInRowPortrait = context.getResources().getInteger(R.integer.cards_in_row_portrait);
        int cardsInRowLandscape = context.getResources().getInteger(R.integer.cards_in_row_landscape);
        int cardsInRow;

        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            cardsInRow = cardsInRowPortrait;
        }
        else{
            cardsInRow = cardsInRowLandscape;
        }
        Configuration config = context.getResources().getConfiguration();
        int screenWidthDp = config.screenWidthDp;

        mCardImageWidth = (screenWidthDp - (16 * cardsInRow) - (8 * (cardsInRow - 1))) / cardsInRow;
        mCardImageHeight = (int) Math.round(mCardImageWidth * 1.5);

        //convert dp to pixels
        mCardImageWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mCardImageWidth, context.getResources().getDisplayMetrics());
        mCardImageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mCardImageHeight, context.getResources().getDisplayMetrics());
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.mTextViewTitle.getLayoutParams().width = mCardImageWidth;
        holder.mTextViewTitle.getLayoutParams().height = mCardImageHeight;
        holder.mImageView.getLayoutParams().width = mCardImageWidth;
        holder.mImageView.getLayoutParams().height = mCardImageHeight;

        holder.mTextViewTitle.setText(mMovieData[position][0]);
        Picasso.with(mContext).load(mMovieData[position][1]).into(holder.mImageView);
        holder.mTextViewDate.setText(mMovieData[position][2]);
        holder.mTextViewRating.setText(mMovieData[position][3]);

        MovieDataToPass data = new MovieDataToPass();
        data.mTitle = mMovieData[position][0];
        data.mImagepath = mMovieData[position][1];
        data.mDate = mMovieData[position][2];
        data.mRating = mMovieData[position][3];
        data.mId = mMovieData[position][4];
        data.mOverview = mMovieData[position][5];
        data.mImagepath2 = mMovieData[position][6];

        holder.mCardView.setTag(data);
    }

    @Override
    public int getItemCount() {
        return mMovieData.length;
    }
}
