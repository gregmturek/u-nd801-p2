package com.zythem.popularmovies;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import static com.zythem.popularmovies.R.layout.activity_detail;

public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final MovieDataToPass movieInfo = Parcels.unwrap(getIntent().getParcelableExtra("THE_DATA"));

        int divisor;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            divisor = 2;
        }
        else{
            divisor = 1;
        }

        Configuration config = getResources().getConfiguration();
        int screenHeightDp = config.screenHeightDp;

        int appbarImageHeight = screenHeightDp / divisor;

        appbarImageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, appbarImageHeight, getResources().getDisplayMetrics());

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.app_bar);
        appbar.getLayoutParams().height = appbarImageHeight;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        Cursor c = null;
        String[] projection = {FavoriteColumns.MOVIE_ID};
        String selectionClause = FavoriteColumns.MOVIE_ID + " = ?";
        String[] selectionArgs = {movieInfo.mId};
        try {
            c = getApplicationContext().getContentResolver().query(MovieContentProvider.Favorite.MOVIES,
                    projection, selectionClause, selectionArgs, null);
            if (c != null && c.getCount() > 0) {
                fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_on));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if(c != null){
                c.close();
            }
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor c = null;
                String[] projection = {FavoriteColumns.MOVIE_ID};
                String selectionClause = FavoriteColumns.MOVIE_ID + " = ?";
                String[] selectionArgs = {movieInfo.mId};
                try {
                    c = getApplicationContext().getContentResolver().query(MovieContentProvider.Favorite.MOVIES,
                            projection, selectionClause, selectionArgs, null);
                    if (c == null || c.getCount() == 0) {
                        // TODO: add to db
                        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                        fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_on));
                        Snackbar.make(view, "Added to the Favorite tab!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                        fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_off));
                        Snackbar.make(view, "Removed from the Favorite tab!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error ", e);
                } finally {
                    if(c != null){
                        c.close();
                    }
                }
            }
        });

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle(movieInfo.mTitle);

        SharedPreferences sharedPref =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean defaultValue = getResources().getBoolean(R.bool.images_switch_default);
        boolean images = sharedPref.getBoolean("images_switch", defaultValue);

        ImageView ivImagepath2 = (ImageView) findViewById(R.id.detail_imagepath2);
        if (movieInfo.mImagepath2 != null && !movieInfo.mImagepath2.isEmpty() && images) {
            Picasso.with(DetailActivity.this)
                    .load(movieInfo.mImagepath2)
                    .into(ivImagepath2);
        }

        ImageView ivImagepath = (ImageView) findViewById(R.id.detail_imagepath);
        float marginValue = getResources().getDimension(R.dimen.normal_layout_margin) / getResources().getDisplayMetrics().density;
        int ivImagepathHeight = (screenHeightDp / divisor) - (Math.round(marginValue) * 2);
        ivImagepathHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, ivImagepathHeight, getResources().getDisplayMetrics());
        if(divisor == 1) {
            final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                    new int[]{R.attr.actionBarSize}
            );
            int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
            styledAttributes.recycle();
            ivImagepathHeight -= actionBarHeight;
        }
        ivImagepath.getLayoutParams().width = (int) Math.round(ivImagepathHeight / 1.5);
        ivImagepath.getLayoutParams().height = ivImagepathHeight;

        TextView tvDate = (TextView) findViewById(R.id.detail_date);
        TextView tvRating = (TextView) findViewById(R.id.detail_rating);

        if (movieInfo.mImagepath != null && !movieInfo.mImagepath.isEmpty() && images) {
            Picasso.with(DetailActivity.this)
                    .load(movieInfo.mImagepath)
                    .noFade()
                    .into(ivImagepath);
        }
        else {
            ivImagepath.setVisibility(View.GONE);

            TextView tvOverviewHeading = (TextView) findViewById(R.id.detail_overview_heading);
            RelativeLayout.LayoutParams tvOverviewHeadingParams = (RelativeLayout.LayoutParams) tvOverviewHeading.getLayoutParams();
            tvOverviewHeadingParams.addRule(RelativeLayout.BELOW, R.id.detail_rating);

            TextView tvDateLabel = (TextView) findViewById(R.id.detail_date_label);
            RelativeLayout.LayoutParams tvDateLabelParams = (RelativeLayout.LayoutParams) tvDateLabel.getLayoutParams();
            tvDateLabelParams.setMarginStart(0);

            RelativeLayout.LayoutParams tvDateParams = (RelativeLayout.LayoutParams) tvDate.getLayoutParams();
            tvDateParams.setMarginStart(0);

            TextView tvRatingLabel = (TextView) findViewById(R.id.detail_rating_label);
            RelativeLayout.LayoutParams tvRatingLabelParams = (RelativeLayout.LayoutParams) tvRatingLabel.getLayoutParams();
            tvRatingLabelParams.setMarginStart(0);

            RelativeLayout.LayoutParams tvRatingParams = (RelativeLayout.LayoutParams) tvRating.getLayoutParams();
            tvRatingParams.setMarginStart(0);
        }

        tvDate.setText(movieInfo.mDate);

        tvRating.setText(movieInfo.mRating);

        TextView tvOverview = (TextView) findViewById(R.id.detail_overview);
        tvOverview.setText(movieInfo.mOverview);
    }
}
