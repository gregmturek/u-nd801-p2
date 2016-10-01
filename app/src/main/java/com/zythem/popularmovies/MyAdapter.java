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

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private String[][] mMovieData;

    private static class MovieDataToPass {
        String title;
        String imagepath;
        String date;
        String rating;
        String id;
        String overview;
        String imagepath2;
    }

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
                    MovieDataToPass data = (MovieDataToPass) mCardView.getTag();
//                    movieInfo.mId = (String) mCardView.getTag();
//                    movieInfo.mTitle = mTextViewTitle.getText().toString();
                    movieInfo.mTitle = data.title;
                    movieInfo.mImagepath = data.imagepath;
                    movieInfo.mDate = data.date;
                    movieInfo.mRating = data.rating;
                    movieInfo.mId = data.id;
                    movieInfo.mOverview = data.overview;
                    movieInfo.mImagepath2 = data.imagepath2;
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

/*
        int cardsInRowPortrait = 2;
        int cardsInRowLandscape = 3;
        int cardsInRow;

        if(parent.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            cardsInRow = cardsInRowPortrait;
        }
        else{
            cardsInRow = cardsInRowLandscape;
        }
        Configuration config = parent.getContext().getResources().getConfiguration();
        int screenWidthDp = config.screenWidthDp;
//            int screenHightDp = config.screenHeightDp;

        int cardImageWidth = screenWidthDp/cardsInRow;
        int cardImageHeight = (int) Math.round(cardImageWidth * 1.5);

        //convert dp to pixels
        cardImageWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, cardImageWidth, parent.getContext().getResources().getDisplayMetrics());
        cardImageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, cardImageHeight, parent.getContext().getResources().getDisplayMetrics());




//        v.setMinimumWidth(cardImageWidth);
//        v.setMinimumHeight(cardImageHeight);

        v.setLayoutParams(new RelativeLayout.LayoutParams(cardImageHeight, cardImageWidth));
*/


        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        int cardsInRowPortrait = 2;
        int cardsInRowLandscape = 4;
        int cardsInRow;

        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            cardsInRow = cardsInRowPortrait;
        }
        else{
            cardsInRow = cardsInRowLandscape;
        }
        Configuration config = context.getResources().getConfiguration();
        int screenWidthDp = config.screenWidthDp;
//            int screenHightDp = config.screenHeightDp;

        int cardImageWidth = (screenWidthDp - (16 * cardsInRow) - (8 * (cardsInRow - 1))) / cardsInRow;
        int cardImageHeight = (int) Math.round(cardImageWidth * 1.5);

//        Toast.makeText(context, "screenWidthDp and cardImageWidth (in dp): " + screenWidthDp + " and " + cardImageWidth, Toast.LENGTH_SHORT).show();


        //convert dp to pixels
        cardImageWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, cardImageWidth, context.getResources().getDisplayMetrics());
        cardImageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, cardImageHeight, context.getResources().getDisplayMetrics());

        holder.mTextViewTitle.getLayoutParams().width = cardImageWidth;
        holder.mTextViewTitle.getLayoutParams().height = cardImageHeight;
        holder.mImageView.getLayoutParams().width = cardImageWidth;
        holder.mImageView.getLayoutParams().height = cardImageHeight;


//        v.setMinimumWidth(cardImageWidth);
//        v.setMinimumHeight(cardImageHeight);

//        v.setLayoutParams(new RelativeLayout.LayoutParams(cardImageHeight, cardImageWidth));


        holder.mTextViewTitle.setText(mMovieData[position][0]);
        Picasso.with(context).load(mMovieData[position][1]).into(holder.mImageView);
        holder.mTextViewDate.setText(mMovieData[position][2]);
        holder.mTextViewRating.setText(mMovieData[position][3]);

        MovieDataToPass data = new MovieDataToPass();
        data.title = mMovieData[position][0];
        data.imagepath = mMovieData[position][1];
        data.date = mMovieData[position][2];
        data.rating = mMovieData[position][3];
        data.id = mMovieData[position][4];
        data.overview = mMovieData[position][5];
        data.imagepath2 = mMovieData[position][6];

        holder.mCardView.setTag(data);
    }

    @Override
    public int getItemCount() {
        return mMovieData.length;
    }
}
