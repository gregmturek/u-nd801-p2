package com.zythem.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.zythem.popularmovies.R.layout.activity_detail;

public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    String[][] mMovieVideos;
    String[][] mMovieReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final MovieDataToPass movieInfo = Parcels.unwrap(getIntent().getParcelableExtra("THE_DATA"));

        FetchVideoTask fetchVideoTask = new FetchVideoTask();
        fetchVideoTask.execute(movieInfo.mId);

        FetchReviewTask fetchReviewTask = new FetchReviewTask();
        fetchReviewTask.execute(movieInfo.mId);

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
                String[] projection = {FavoriteColumns._ID, FavoriteColumns.MOVIE_ID};
                String selectionClause = FavoriteColumns.MOVIE_ID + " = ?";
                String[] selectionArgs = {movieInfo.mId};
                try {
                    c = getApplicationContext().getContentResolver().query(MovieContentProvider.Favorite.MOVIES,
                            projection, selectionClause, selectionArgs, null);
                    if (c == null || c.getCount() == 0) {
                        ContentValues newValues = new ContentValues();
                        newValues.put(FavoriteColumns.MOVIE_TITLE, movieInfo.mTitle);
                        newValues.put(FavoriteColumns.MOVIE_IMAGEPATH, movieInfo.mImagepath);
                        newValues.put(FavoriteColumns.MOVIE_DATE, movieInfo.mDate);
                        newValues.put(FavoriteColumns.MOVIE_RATING, movieInfo.mRating);
                        newValues.put(FavoriteColumns.MOVIE_ID, movieInfo.mId);
                        newValues.put(FavoriteColumns.MOVIE_OVERVIEW, movieInfo.mOverview);
                        newValues.put(FavoriteColumns.MOVIE_IMAGEPATH_2, movieInfo.mImagepath2);
                        getApplicationContext().getContentResolver().insert(MovieContentProvider.Favorite.MOVIES, newValues);

                        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                        fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_on));

                        Snackbar.make(view, "Added to the Favorite tab!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        c.moveToFirst();
                        long _id = c.getLong(c.getColumnIndex(FavoriteColumns._ID));
                        getApplicationContext().getContentResolver().delete(MovieContentProvider.Favorite.withId(_id), null, null);

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

    public class FetchVideoTask extends AsyncTask<String, Void, String[][]> {

        private final String LOG_TAG = FetchVideoTask.class.getSimpleName();

        private String[][] getVideoDataFromJson(String videoJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_KEY = "key";
            final String TMDB_NAME = "name";
            final String TMDB_SITE = "site";
            final String TMDB_TYPE = "type";

            JSONObject videoJson = new JSONObject(videoJsonStr);
            JSONArray videoArray = videoJson.getJSONArray(TMDB_RESULTS);

            int videoArrayLength = videoArray.length();

            String[][] resultStrs = new String[videoArrayLength][3];

            for (int i = 0; i < videoArrayLength; i++) {
                // Get the JSON object representing an individual movie
                JSONObject individualVideo = videoArray.getJSONObject(i);

                resultStrs[i][0] = individualVideo.getString(TMDB_KEY);
                resultStrs[i][1] = individualVideo.getString(TMDB_NAME);
                resultStrs[i][2] = individualVideo.getString(TMDB_SITE);
            }
            return resultStrs;
        }

        @Override
        protected String[][] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr;

            try {
                // Construct the URL
                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3/movie/" + params[0] + "/videos" + "?";
                final String APIKEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line);
                    buffer.append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getVideoDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing.
            return null;
        }

        @Override
        protected void onPostExecute(String[][] result) {
            if (result != null) {
                mMovieVideos = result;
                // New data is back from the server.  Hooray!
                showVideos();
            }
        }

    }

    private void showVideos() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.detail_videos_layout);

        if (mMovieVideos.length == 0) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            tv.setText(getApplicationContext().getString(R.string.none));
            linearLayout.addView(tv);
        } else {
            for (int i = 0; i < mMovieVideos.length; i++) {
                if (mMovieVideos[i][2].equals("YouTube")) {
                    final int index = i;
                    Button bVideo = new Button(this);
                    bVideo.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    bVideo.setText(mMovieVideos[i][1]);
                    bVideo.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            youTube(index);
                        }
                    });
                    linearLayout.addView(bVideo);
                }
            }
        }
    }

    public void youTube(int index) {
        Uri uri = Uri.parse("vnd.youtube:" + mMovieVideos[index][0]);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public class FetchReviewTask extends AsyncTask<String, Void, String[][]> {

        private final String LOG_TAG = FetchReviewTask.class.getSimpleName();

        private String[][] getReviewDataFromJson(String reviewJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_AUTHOR = "author";
            final String TMDB_CONTENT = "content";

            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(TMDB_RESULTS);

            int reviewArrayLength = reviewArray.length();

            String[][] resultStrs = new String[reviewArrayLength][2];

            for (int i = 0; i < reviewArrayLength; i++) {
                // Get the JSON object representing an individual movie
                JSONObject individualReview = reviewArray.getJSONObject(i);

                resultStrs[i][0] = individualReview.getString(TMDB_AUTHOR);
                resultStrs[i][1] = individualReview.getString(TMDB_CONTENT);
            }
            return resultStrs;
        }

        @Override
        protected String[][] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr;

            try {
                // Construct the URL
                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews" + "?";
                final String APIKEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line);
                    buffer.append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing.
            return null;
        }

        @Override
        protected void onPostExecute(String[][] result) {
            if (result != null) {
                mMovieReviews = result;
                // New data is back from the server.  Hooray!
                showReviews();
            }
        }

    }


    private void showReviews(){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.detail_reviews_layout);

        if (mMovieReviews.length == 0) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            tv.setText(getApplicationContext().getString(R.string.none));
            linearLayout.addView(tv);
        } else {
            for (int i = 0; i < mMovieReviews.length; i++) {
                TextView tv = new TextView(this);
                tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                tv.setText(mMovieReviews[i][1] + "\n--" + mMovieReviews[i][0] + "\n");
                linearLayout.addView(tv);
            }
        }

    }

}
