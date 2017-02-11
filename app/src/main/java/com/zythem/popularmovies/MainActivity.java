package com.zythem.popularmovies;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.zythem.popularmovies.MovieContentProvider.AUTHORITY;
import static com.zythem.popularmovies.R.id.container;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    public Boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if (findViewById(R.id.fragment_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
        } else {
            mTwoPane = false;
        }


    }

    public void loadTabletDetailFragment(MovieDataToPass movieInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("THE_DATA", Parcels.wrap(movieInfo));

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, DetailFragment.newInstance(bundle, true))
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TabFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        /**
         * The fragment argument representing the section number for    this
         * fragment.
         */
        private static final String LOG_TAG = TabFragment.class.getSimpleName();
        private static final int CURSOR_LOADER_ID = 0;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_TWO_PANE = "two_pane";


        private int mTabNum;
        private boolean mTwoPane;

        private MoviesAdapter mCursorAdapter;
        private RecyclerView mRv;
        private TextView emptyView;

        private PreCachingGridLayoutManager mGlm;

        private String[][] mMovieData;

        public TabFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static TabFragment newInstance(int sectionNumber, boolean twoPane) {
            TabFragment fragment = new TabFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putBoolean(ARG_TWO_PANE, twoPane);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int cardsInRowPortrait = getResources().getInteger(R.integer.cards_in_row_portrait);
            int cardsInRowLandscape = getResources().getInteger(R.integer.cards_in_row_landscape);
            int cardsInRow;

            mTabNum = getArguments().getInt(ARG_SECTION_NUMBER);
            mTwoPane = getArguments().getBoolean(ARG_TWO_PANE);

            if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                cardsInRow = cardsInRowPortrait;
            }
            else{
                cardsInRow = cardsInRowLandscape;
            }
            if(mTwoPane) {
                cardsInRow /= 2;
            }

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mRv = (RecyclerView) rootView.findViewById(R.id.rv_recycler_view);
            mRv.setHasFixedSize(true);

            mGlm = new PreCachingGridLayoutManager(getActivity(), cardsInRow);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowmanager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
            int deviceWidth = displayMetrics.widthPixels;
            int deviceHeight = displayMetrics.heightPixels;
            int extraPixels = Math.max(deviceHeight, deviceWidth);
            mGlm.setExtraLayoutSpace(extraPixels);

            mRv.setLayoutManager(mGlm);

            mCursorAdapter = new MoviesAdapter(getActivity(), null, mTabNum, mTwoPane);

            emptyView = (TextView) rootView.findViewById(R.id.rv_grid_empty);

            mCursorAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    checkIfAdapterIsEmpty();
                }
            });

            mRv.setAdapter(mCursorAdapter);

            checkIfAdapterIsEmpty();

            return rootView;
        }

        private void checkIfAdapterIsEmpty() {
            if (mCursorAdapter.getItemCount() == 0) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }

        /**
         * Returns true if the network is available or about to become available.
         *
         * @param c Context used to get the ConnectivityManager
         * @return
         */
        static public boolean isNetworkAvailable(Context c) {
            ConnectivityManager cm =
                    (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }

        private void updateEmptyView() {
            if (mCursorAdapter.getItemCount() == 0) {
                if (emptyView != null) {
                    // if cursor is empty, why?
                    if (!isNetworkAvailable(getActivity()) ) {
                        emptyView.setText(R.string.empty_grid_no_network);
                    }
                }
            }
        }

        public class PreCachingGridLayoutManager extends GridLayoutManager {
            private int extraLayoutSpace;

            public PreCachingGridLayoutManager(Context context, int spanCount) {
                super(context, spanCount);
            }

            public void setExtraLayoutSpace(int extraLayoutSpace) {
                this.extraLayoutSpace = extraLayoutSpace;
            }

            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return extraLayoutSpace;
            }
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState){
            switch (mTabNum) {
                case 1:
                    refetchDataIfNecessary(MovieContentProvider.MostPopular.MOVIES, MovieContentProvider.Path.MOST_POPULAR);
                    break;
                case 2:
                    refetchDataIfNecessary(MovieContentProvider.TopRated.MOVIES, MovieContentProvider.Path.TOP_RATED);
                    break;
            }

            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onStart() {
            super.onStart();
            if (mTwoPane && mTabNum == 1 && getActivity().findViewById(R.id.detail_videos_layout) == null) {
                MovieDataToPass movieInfo = new MovieDataToPass();
                Cursor c = null;
                try {
                    c = getActivity().getContentResolver().query(MovieContentProvider.MostPopular.MOVIES,
                            null, null, null, null);

                    if(c != null) {
                        c.moveToFirst();

                        movieInfo.mTitle = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_TITLE));
                        movieInfo.mImagepath = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH));
                        movieInfo.mDate = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_DATE));
                        movieInfo.mRating = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_RATING));
                        movieInfo.mId = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_ID));
                        movieInfo.mOverview = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_OVERVIEW));
                        movieInfo.mImagepath2 = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH_2));

                        ((MainActivity)getActivity()).loadTabletDetailFragment(movieInfo);
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error ", e);
                } finally {
                    if(c != null){
                        c.close();
                    }
                }
            }
        }

        public void refetchDataIfNecessary(Uri uriType, String pathType) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String defaultValue = getResources().getString(R.string.number_of_movies_to_list_as_pages_default);
            String pages = sharedPref.getString("number_of_movies_to_list_as_pages", defaultValue);

            long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;
            long updateDate = sharedPref.getLong("last_update", 0);
            long currentDate = System.currentTimeMillis() / MILLIS_PER_DAY;
            Boolean needUpdate = false;
            if (updateDate != currentDate) {
                needUpdate = true;
            }

            Cursor c = null;

            try {
                c = getActivity().getContentResolver().query(uriType,
                        null, null, null, null);

                if (c == null || c.getCount() == 0 || c.getCount() != Integer.parseInt(pages) * 20 || needUpdate) {
                    getActivity().getContentResolver().delete(MovieContentProvider.MostPopular.MOVIES, null, null);
                    getActivity().getContentResolver().delete(MovieContentProvider.TopRated.MOVIES, null, null);

                    FetchMovieTask fetchMovieTask = new FetchMovieTask();
                    fetchMovieTask.execute(pathType, pages);

                    SharedPreferences.Editor edit = sharedPref.edit();
                    edit.putLong("last_update", currentDate);
                    edit.apply();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if(c != null){
                    c.close();
                }
            }
        }

        public void storeAllData(String[][] movieData) {
            Log.d(LOG_TAG, "insert all");

            ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>(movieData.length);
            Uri uriType = null;

            switch (mTabNum) {
                case 1:
                    uriType = MovieContentProvider.MostPopular.MOVIES;
                    break;
                case 2:
                    uriType = MovieContentProvider.TopRated.MOVIES;
                    break;
            }

            for (String[] movie : movieData) {
                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                        uriType);
                builder.withValue(MostPopularColumns.MOVIE_TITLE, movie[0]);
                builder.withValue(MostPopularColumns.MOVIE_IMAGEPATH, movie[1]);
                builder.withValue(MostPopularColumns.MOVIE_DATE, movie[2]);
                builder.withValue(MostPopularColumns.MOVIE_RATING, movie[3]);
                builder.withValue(MostPopularColumns.MOVIE_ID, movie[4]);
                builder.withValue(MostPopularColumns.MOVIE_OVERVIEW, movie[5]);
                builder.withValue(MostPopularColumns.MOVIE_IMAGEPATH_2, movie[6]);
                batchOperations.add(builder.build());
            }

            try{
                getActivity().getContentResolver().applyBatch(AUTHORITY, batchOperations);
            } catch(RemoteException | OperationApplicationException e){
                Log.e(LOG_TAG, "Error applying batch insert all", e);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d(LOG_TAG, "resume called");
            getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        }


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args){
            Uri uri = null;
            switch (mTabNum) {
                case 1:
                    uri = MovieContentProvider.MostPopular.MOVIES;
                    break;
                case 2:
                    uri = MovieContentProvider.TopRated.MOVIES;
                break;
                case 3:
                    uri = MovieContentProvider.Favorite.MOVIES;
                break;
            }
            return new CursorLoader(getActivity(), uri,
                    null,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data){
            mCursorAdapter.swapCursor(data);
            updateEmptyView();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader){
            mCursorAdapter.swapCursor(null);
        }



        public class FetchMovieTask extends AsyncTask<String, Void, String[][]> {

            private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

            private String[][] getMovieDataFromJson(String[] movieJsonStrs)
                    throws JSONException {

                // These are the names of the JSON objects that need to be extracted.
                final String TMDB_RESULTS = "results";
                final String TMDB_TITLE = "original_title";
                final String TMDB_IMAGEPATH = "poster_path";
                final String TMDB_DATE = "release_date";
                final String TMDB_RATING = "vote_average";
                final String TMDB_ID = "id";
                final String TMDB_OVERVIEW = "overview";
                final String TMDB_IMAGEPATH2 = "backdrop_path";

                int movieJsonStrsLength = movieJsonStrs.length;

                JSONObject[] movieJson = new JSONObject[movieJsonStrsLength];
                JSONArray[] movieArray = new JSONArray[movieJsonStrsLength];

                for(int j = 0; j < movieJsonStrsLength; j++) {
                    movieJson[j] = new JSONObject(movieJsonStrs[j]);
                    movieArray[j] = movieJson[j].getJSONArray(TMDB_RESULTS);
                }

                int movieArrayLength = movieArray[0].length();

                String[][] resultStrs = new String[movieJsonStrsLength*movieArrayLength][7];

                for(int j = 0; j < movieJsonStrsLength; j++) {
                    for (int i = 0; i < movieArrayLength; i++) {
                        // Get the JSON object representing an individual movie
                        JSONObject individualMovie = movieArray[j].getJSONObject(i);

                        resultStrs[(j*movieArrayLength)+i][0] = individualMovie.getString(TMDB_TITLE);
                        resultStrs[(j*movieArrayLength)+i][1] = "http://image.tmdb.org/t/p/w342/" + individualMovie.getString(TMDB_IMAGEPATH);

                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-mm-dd", Locale.US);
                        String date = individualMovie.getString(TMDB_DATE);
                        try {
                            Date parsedDate = inputFormat.parse(date);
                            date = DateFormat.getDateInstance(DateFormat.SHORT).format(parsedDate);
                        } catch (ParseException e) {
                            Log.e(LOG_TAG, "Error ", e);
                        }

                        resultStrs[(j*movieArrayLength)+i][2] = date;
                        resultStrs[(j*movieArrayLength)+i][3] = individualMovie.getString(TMDB_RATING) + "/10";
                        resultStrs[(j*movieArrayLength)+i][4] = individualMovie.getString(TMDB_ID);
                        resultStrs[(j*movieArrayLength)+i][5] = individualMovie.getString(TMDB_OVERVIEW);
                        resultStrs[(j*movieArrayLength)+i][6] = "http://image.tmdb.org/t/p/w1280/" + individualMovie.getString(TMDB_IMAGEPATH2);
                    }
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
                String[] movieJsonStrs = new String[Integer.parseInt(params[1])];

                for(int i=0; i<Integer.parseInt(params[1]); i++) {
                    try {
                        // Construct the URL
                        final String MOVIE_BASE_URL =
                                "http://api.themoviedb.org/3/movie/" + params[0] + "?";
                        final String APIKEY_PARAM = "api_key";
                        final String PAGE_PARAM = "page";

                        Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                                .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                                .appendQueryParameter(PAGE_PARAM, Integer.toString(i+1))
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
                        movieJsonStrs[i] = buffer.toString();
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
                }

                try {
                    return getMovieDataFromJson(movieJsonStrs);
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
                    mMovieData = result;
                    // New data is back from the server.  Hooray!
                    storeAllData(mMovieData);
                }
            }

        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a TabFragment (defined as a static inner class below).
            return TabFragment.newInstance(position + 1, mTwoPane);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Most Popular";
                case 1:
                    return "Top Rated";
                case 2:
                    return "Favorite";
            }
            return null;
        }

    }

}
