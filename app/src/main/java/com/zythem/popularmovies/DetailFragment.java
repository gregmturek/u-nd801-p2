package com.zythem.popularmovies;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.zythem.youtubepreview.YouTubePreview;

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

import static com.zythem.popularmovies.R.id.app_bar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_MOVIE_INFO = "THE_DATA";
    private static final String ARG_TWO_PANE = "two_pane";
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private MovieDataToPass mMovieInfo;
    private boolean mTwoPane;
    private boolean mImages;

    private String[][] mMovieVideos = new String[0][0];
    private String[][] mMovieReviews = new String[0][0];

    private View mView;
    private int mDivisor;
    private int mScreenHeightDp;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param args Parameter 1.
     * @return A new instance of fragment DetailFragment.
     */
    public static DetailFragment newInstance(Bundle args, boolean twoPane) {
        DetailFragment fragment = new DetailFragment();
        args.putBoolean(ARG_TWO_PANE, twoPane);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovieInfo = Parcels.unwrap(getArguments().getParcelable(ARG_MOVIE_INFO));
            mTwoPane = getArguments().getBoolean(ARG_TWO_PANE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    public void reInit() {
        fetchVideosAndReviews();
        loadContent();
    }

    public void fetchVideosAndReviews() {
        FetchVideoTask fetchVideoTask = new FetchVideoTask();
        fetchVideoTask.execute(mMovieInfo.mId);

        FetchReviewTask fetchReviewTask = new FetchReviewTask();
        fetchReviewTask.execute(mMovieInfo.mId);
    }

    public void loadContent() {
        SharedPreferences sharedPref =  PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean defaultValue = getResources().getBoolean(R.bool.images_switch_default);
        mImages = sharedPref.getBoolean("images_switch", defaultValue);

        final ImageView ivImagepath2 = (ImageView) mView.findViewById(R.id.detail_imagepath2);
        if (mMovieInfo.mImagepath2 != null && !mMovieInfo.mImagepath2.isEmpty() && mImages) {
            Picasso.with(getContext())
                    .load(mMovieInfo.mImagepath2)
                    .into(ivImagepath2, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (isAdded()) {
                                ivImagepath2.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onError() {
                            if (isAdded()) {
                                ivImagepath2.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        }

        ImageView ivImagepath = (ImageView) mView.findViewById(R.id.detail_imagepath);
        float marginValue = getResources().getDimension(R.dimen.normal_layout_margin) * 2
                / getResources().getDisplayMetrics().density;
        int ivImagepathHeight = (mScreenHeightDp / mDivisor) - (Math.round(marginValue) * 2);
        ivImagepathHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ivImagepathHeight, getResources().getDisplayMetrics());
        if(mDivisor == 1) {
            final TypedArray styledAttributes = getActivity().getTheme().obtainStyledAttributes(
                    new int[]{R.attr.actionBarSize}
            );
            int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
            styledAttributes.recycle();
            ivImagepathHeight -= actionBarHeight;
        }
        ivImagepath.getLayoutParams().width = (int) Math.round(ivImagepathHeight / 1.5);
        ivImagepath.getLayoutParams().height = ivImagepathHeight;

        final TextView tvDate = (TextView) mView.findViewById(R.id.detail_date);
        final TextView tvRating = (TextView) mView.findViewById(R.id.detail_rating);
        final CardView cardView = (CardView) mView.findViewById(R.id.detail_card_view);

        if (mMovieInfo.mImagepath != null && !mMovieInfo.mImagepath.isEmpty() && mImages && !mTwoPane) {
            Picasso.with(getContext())
                    .load(mMovieInfo.mImagepath)
                    .noFade()
                    .into(ivImagepath, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (isAdded()) {
                                cardView.setVisibility(View.VISIBLE);

                                TextView tvOverviewHeading = (TextView) mView.findViewById(R.id.detail_overview_heading);
                                RelativeLayout.LayoutParams tvOverviewHeadingParams = (RelativeLayout.LayoutParams)
                                        tvOverviewHeading.getLayoutParams();
                                tvOverviewHeadingParams.addRule(RelativeLayout.BELOW, R.id.detail_card_view);
                                tvOverviewHeading.setLayoutParams(tvOverviewHeadingParams);
                            }
                        }

                        @Override
                        public void onError() {
                            if (isAdded()) {
                                cardView.setVisibility(View.GONE);

                                TextView tvOverviewHeading = (TextView) mView.findViewById(R.id.detail_overview_heading);
                                RelativeLayout.LayoutParams tvOverviewHeadingParams = (RelativeLayout.LayoutParams) tvOverviewHeading.getLayoutParams();
                                tvOverviewHeadingParams.addRule(RelativeLayout.BELOW, R.id.detail_rating);
                                tvOverviewHeading.setLayoutParams(tvOverviewHeadingParams);
                            }
                        }
                    });
        }
        else {
            cardView.setVisibility(View.GONE);

            TextView tvOverviewHeading = (TextView) mView.findViewById(R.id.detail_overview_heading);
            RelativeLayout.LayoutParams tvOverviewHeadingParams = (RelativeLayout.LayoutParams) tvOverviewHeading.getLayoutParams();
            tvOverviewHeadingParams.addRule(RelativeLayout.BELOW, R.id.detail_rating);
            tvOverviewHeading.setLayoutParams(tvOverviewHeadingParams);
        }

        tvDate.setText(mMovieInfo.mDate);

        tvRating.setText(mMovieInfo.mRating);

        TextView tvOverview = (TextView) mView.findViewById(R.id.detail_overview);
        tvOverview.setText(mMovieInfo.mOverview);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;

        fetchVideosAndReviews();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mDivisor = 2;
        }
        else{
            mDivisor = 1;
        }

        Configuration config = getResources().getConfiguration();
        mScreenHeightDp = config.screenHeightDp;
        if(mTwoPane) {
            mScreenHeightDp /= 2;
        }

        int appbarImageHeight = mScreenHeightDp / mDivisor;
        appbarImageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, appbarImageHeight, getResources().getDisplayMetrics());
        AppBarLayout appbar = (AppBarLayout) getActivity().findViewById(app_bar);
        appbar.getLayoutParams().height = appbarImageHeight;

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        Cursor c = null;
        String[] projection = {FavoriteColumns.MOVIE_ID};
        String selectionClause = FavoriteColumns.MOVIE_ID + " = ?";
        String[] selectionArgs = {mMovieInfo.mId};
        try {
            c = getActivity().getApplicationContext().getContentResolver().query(MovieContentProvider.Favorite.MOVIES,
                    projection, selectionClause, selectionArgs, null);
            if (c != null && c.getCount() > 0) {
                fab.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), android.R.drawable.btn_star_big_on));
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
                String[] selectionArgs = {mMovieInfo.mId};
                try {
                    c = getActivity().getApplicationContext().getContentResolver().query(MovieContentProvider.Favorite.MOVIES,
                            projection, selectionClause, selectionArgs, null);
                    if (c == null || c.getCount() == 0) {
                        ContentValues newValues = new ContentValues();
                        newValues.put(FavoriteColumns.MOVIE_TITLE, mMovieInfo.mTitle);
                        newValues.put(FavoriteColumns.MOVIE_IMAGEPATH, mMovieInfo.mImagepath);
                        newValues.put(FavoriteColumns.MOVIE_DATE, mMovieInfo.mDate);
                        newValues.put(FavoriteColumns.MOVIE_RATING, mMovieInfo.mRating);
                        newValues.put(FavoriteColumns.MOVIE_ID, mMovieInfo.mId);
                        newValues.put(FavoriteColumns.MOVIE_OVERVIEW, mMovieInfo.mOverview);
                        newValues.put(FavoriteColumns.MOVIE_IMAGEPATH_2, mMovieInfo.mImagepath2);
                        getActivity().getApplicationContext().getContentResolver().insert(MovieContentProvider.Favorite.MOVIES, newValues);

                        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
                        fab.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), android.R.drawable.btn_star_big_on));

                        Snackbar.make(view, "Added to the Favorite tab!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        c.moveToFirst();
                        long _id = c.getLong(c.getColumnIndex(FavoriteColumns._ID));
                        getActivity().getApplicationContext().getContentResolver().delete(MovieContentProvider.Favorite.withId(_id), null, null);

                        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
                        fab.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), android.R.drawable.btn_star_big_off));
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

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) view.findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitle(mMovieInfo.mTitle);

        loadContent();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        AppCompatActivity appCompatActivity = ((AppCompatActivity) getActivity());
        appCompatActivity.setSupportActionBar(toolbar);
        ActionBar actionBar = appCompatActivity.getSupportActionBar();
        if(actionBar != null && !mTwoPane) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public class FetchVideoTask extends AsyncTask<String, Void, String[][]> {
        ProgressDialog dialog;

        private final String LOG_TAG = FetchVideoTask.class.getSimpleName();

        private String[][] getVideoDataFromJson(String videoJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_KEY = "key";
            final String TMDB_NAME = "name";
            final String TMDB_SITE = "site";
//            final String TMDB_TYPE = "type";

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
        protected void onPreExecute() {
            dialog = new ProgressDialog(getContext());
            dialog.setMessage(getString(R.string.download_dialog));
            dialog.setIndeterminate(true);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String[][] result) {
            if (result != null) {
                mMovieVideos = result;
                // New data is back from the server.  Hooray!
            }
            showVideos();
            dialog.dismiss();
        }
    }

    private void showVideos() {
        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.detail_videos_layout);
        TextView tvEmpty = (TextView) getActivity().findViewById(R.id.detail_videos_empty);

        if (mMovieVideos.length == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            for (int i = 0; i < mMovieVideos.length; i++) {
                if (mMovieVideos[i][2].equals("YouTube")) {
                    if (mImages) {
                        CardView cardView = new CardView(getContext());
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        int margin = getResources().getDimensionPixelSize(R.dimen.card_layout_margin);
                        lp.setMargins(margin, margin, margin, margin);
                        cardView.setLayoutParams(lp);
                        ViewCompat.setElevation(cardView, getResources().getDimensionPixelSize(R.dimen.cardview_default_elevation));
                        cardView.setRadius(getResources().getDimensionPixelSize(R.dimen.cardview_default_radius));
                        linearLayout.addView(cardView);

                        YouTubePreview youTubePreview = new YouTubePreview(getContext());
                        CardView.LayoutParams lp2 = new CardView.LayoutParams(
                                CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                        int margin2 = getResources().getDimensionPixelSize(R.dimen.normal_layout_margin);
                        lp2.setMargins(margin2, margin2, margin2, margin2);
                        youTubePreview.setLayoutParams(lp2);
                        youTubePreview.setContent(mMovieVideos[i][0], mMovieVideos[i][1],
                                getResources().getDimension(R.dimen.subheading_text_size));
                        cardView.addView(youTubePreview);
                    } else {
                        final String key = mMovieVideos[i][0];
                        Button bVideo = new Button(getContext());
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        int margin = getResources().getDimensionPixelSize(R.dimen.card_layout_margin);
                        lp.setMargins(margin, margin, margin, margin);
                        bVideo.setLayoutParams(lp);
                        bVideo.setText(mMovieVideos[i][1]);
                        bVideo.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                YouTubePreview ytp = new YouTubePreview(getContext());
                                ytp.youTube(key);
                            }
                        });
                        linearLayout.addView(bVideo);
                    }
                }
            }
        }
    }

    public class FetchReviewTask extends AsyncTask<String, Void, String[][]> {
        ProgressDialog dialog;

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
        protected void onPreExecute() {
            dialog = new ProgressDialog(getContext());
            dialog.setMessage(getString(R.string.download_dialog));
            dialog.setIndeterminate(true);
            dialog.show();
        }
        @Override
        protected void onPostExecute(String[][] result) {
            if (result != null) {
                mMovieReviews = result;
                // New data is back from the server.  Hooray!
            }
            showReviews();
            dialog.dismiss();
        }
    }

    private void showReviews(){
        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.detail_reviews_layout);
        TextView tvEmpty = (TextView) getActivity().findViewById(R.id.detail_reviews_empty);

        if (mMovieReviews.length == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            for (int i = 0; i < mMovieReviews.length; i++) {
                if (i > 0) {
                    ImageView iv = new ImageView(getActivity());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            getResources().getDimensionPixelSize(R.dimen.text_divider_height));
                    int margin = getResources().getDimensionPixelSize(R.dimen.card_layout_margin);
                    lp.setMargins(margin, margin , margin, margin * 2);
                    iv.setLayoutParams(lp);
                    iv.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                    linearLayout.addView(iv);
                }
                TextView tv = new TextView(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                int margin = getResources().getDimensionPixelSize(R.dimen.card_layout_margin);
                lp.setMargins(margin, 0, margin, margin);
                tv.setLayoutParams(lp);
                tv.setText(mMovieReviews[i][1] + "\n--" + mMovieReviews[i][0]);
                linearLayout.addView(tv);
            }
        }
    }
}
