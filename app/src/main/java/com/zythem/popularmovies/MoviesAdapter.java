package com.zythem.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

class MoviesAdapter extends CursorRecyclerViewAdapter<MoviesAdapter.ViewHolder> {
    private Context mContext;
    private int mTabNum;
    private static boolean mTwoPane;

    private int mCardImageWidth;
    private int mCardImageHeight;

    private boolean mImages;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        TextView mTextViewTitle;
        ImageView mImageView;
        TextView mTextViewDate;
        TextView mTextViewRating;

        ViewHolder(View v) {
            super(v);

            mCardView = (CardView) v.findViewById(R.id.card_view);
            mTextViewTitle = (TextView) v.findViewById(R.id.tv_title);
            mImageView = (ImageView) v.findViewById(R.id.iv_image);
            mTextViewDate = (TextView) v.findViewById(R.id.tv_date);
            mTextViewRating = (TextView) v.findViewById(R.id.tv_rating);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mTwoPane) {
                        MovieDataToPass movieInfo = (MovieDataToPass) mCardView.getTag();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("THE_DATA", Parcels.wrap(movieInfo));

                        ((FragmentActivity) v.getContext()).getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, DetailFragment.newInstance(bundle, true))
                                .commit();
                    } else {
                        Intent intent = new Intent(v.getContext(), DetailActivity.class);
                        MovieDataToPass movieInfo = (MovieDataToPass) mCardView.getTag();
                        intent.putExtra("THE_DATA", Parcels.wrap(movieInfo));
                        v.getContext().startActivity(intent);
                    }
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    MoviesAdapter(Context context, Cursor cursor, int tabNum, boolean twoPane) {
        super(context, cursor);
        mContext = context;
        mTabNum = tabNum;
        mTwoPane = twoPane;

        int cardsInRowPortrait = context.getResources().getInteger(R.integer.cards_in_row_portrait);
        int cardsInRowLandscape = context.getResources().getInteger(R.integer.cards_in_row_landscape);
        int cardsInRow;

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            cardsInRow = cardsInRowPortrait;
        } else {
            cardsInRow = cardsInRowLandscape;
        }
        Configuration config = context.getResources().getConfiguration();
        int screenWidthDp = config.screenWidthDp;

        mCardImageWidth = (screenWidthDp - (16 * cardsInRow) - (8 * (cardsInRow - 1))) / cardsInRow;
        mCardImageHeight = (int) Math.round(mCardImageWidth * 1.5);

        //convert dp to pixels
        mCardImageWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mCardImageWidth, context.getResources().getDisplayMetrics());
        mCardImageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mCardImageHeight, context.getResources().getDisplayMetrics());

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean defaultValue = context.getResources().getBoolean(R.bool.images_switch_default);
        mImages = sharedPref.getBoolean("images_switch", defaultValue);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        holder.mTextViewTitle.getLayoutParams().width = mCardImageWidth;
        holder.mTextViewTitle.getLayoutParams().height = mCardImageHeight;
        holder.mImageView.getLayoutParams().width = mCardImageWidth;
        holder.mImageView.getLayoutParams().height = mCardImageHeight;

        MovieDataToPass data = new MovieDataToPass();

        switch (mTabNum) {
            case 1:
                holder.mTextViewTitle.setText(cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_TITLE)));
                if (cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH)) != null
                        && !cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH)).isEmpty()
                        && mImages) {
                    Picasso.with(mContext)
                            .load(cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH)))
                            .noFade()
                            .into(holder.mImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    holder.mImageView.setVisibility(View.INVISIBLE);
                                }
                            });
                }
                holder.mTextViewDate.setText(cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_DATE)));
                holder.mTextViewRating.setText(cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_RATING)));

                data.mTitle = cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_TITLE));
                data.mImagepath = cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH));
                data.mDate = cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_DATE));
                data.mRating = cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_RATING));
                data.mId = cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_ID));
                data.mOverview = cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_OVERVIEW));
                data.mImagepath2 = cursor.getString(cursor.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH_2));
                break;
            case 2:
                holder.mTextViewTitle.setText(cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_TITLE)));
                if (cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_IMAGEPATH)) != null
                        && !cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_IMAGEPATH)).isEmpty()
                        && mImages) {
                    Picasso.with(mContext)
                            .load(cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_IMAGEPATH)))
                            .noFade()
                            .into(holder.mImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    holder.mImageView.setVisibility(View.INVISIBLE);
                                }
                            });
                }
                holder.mTextViewDate.setText(cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_DATE)));
                holder.mTextViewRating.setText(cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_RATING)));

                data.mTitle = cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_TITLE));
                data.mImagepath = cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_IMAGEPATH));
                data.mDate = cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_DATE));
                data.mRating = cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_RATING));
                data.mId = cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_ID));
                data.mOverview = cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_OVERVIEW));
                data.mImagepath2 = cursor.getString(cursor.getColumnIndex(TopRatedColumns.MOVIE_IMAGEPATH_2));
                break;
            case 3:
                holder.mTextViewTitle.setText(cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_TITLE)));
                if (cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_IMAGEPATH)) != null
                        && !cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_IMAGEPATH)).isEmpty()
                        && mImages) {
                    Picasso.with(mContext)
                            .load(cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_IMAGEPATH)))
                            .noFade()
                            .into(holder.mImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    holder.mImageView.setVisibility(View.INVISIBLE);
                                }
                            });
                }
                holder.mTextViewDate.setText(cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_DATE)));
                holder.mTextViewRating.setText(cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_RATING)));

                data.mTitle = cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_TITLE));
                data.mImagepath = cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_IMAGEPATH));
                data.mDate = cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_DATE));
                data.mRating = cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_RATING));
                data.mId = cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_ID));
                data.mOverview = cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_OVERVIEW));
                data.mImagepath2 = cursor.getString(cursor.getColumnIndex(FavoriteColumns.MOVIE_IMAGEPATH_2));
                break;
        }

        holder.mCardView.setTag(data);
    }

}
