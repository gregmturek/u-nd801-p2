package com.zythem.popularmovies;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

/*
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private int mTabNum;

        private MyAdapter mMovieAdapter;
        private RecyclerView mRv;
        private GridLayoutManager mGlm;
        private String[][] mMovieData;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public void showData() {
            mMovieAdapter = new MyAdapter(getActivity(), mMovieData);
            mRv.setAdapter(mMovieAdapter);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            FetchMovieTask movieTask = new FetchMovieTask();
            mTabNum = getArguments().getInt(ARG_SECTION_NUMBER);
            switch (mTabNum) {
                case 1:
                    movieTask.execute("popular");
                    break;
                case 2:
                    movieTask.execute("top_rated");
                    break;
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mRv = (RecyclerView) rootView.findViewById(R.id.rv_recycler_view);
            mRv.setHasFixedSize(false);

            if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                mGlm = new GridLayoutManager(getActivity(), 2);
                mRv.setLayoutManager(mGlm);
            }
            else{
                mGlm = new GridLayoutManager(getActivity(), 4);
                mRv.setLayoutManager(mGlm);
            }

            return rootView;
        }

        public class FetchMovieTask extends AsyncTask<String, Void, String[][]> {

            private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

            private String[][] getMovieDataFromJson(String movieJsonStr)
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

                JSONObject movieJson = new JSONObject(movieJsonStr);
                JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);

                String[][] resultStrs = new String[movieArray.length()][7];

                for(int i = 0; i < movieArray.length(); i++) {
                    // Get the JSON object representing an individual movie
                    JSONObject individualMovie = movieArray.getJSONObject(i);

                    resultStrs[i][0] = individualMovie.getString(TMDB_TITLE);
                    resultStrs[i][1] = "http://image.tmdb.org/t/p/w780/" + individualMovie.getString(TMDB_IMAGEPATH);
                    resultStrs[i][2] = individualMovie.getString(TMDB_DATE);
                    resultStrs[i][3] = individualMovie.getString(TMDB_RATING) + "/10";
                    resultStrs[i][4] = individualMovie.getString(TMDB_ID);
                    resultStrs[i][5] = individualMovie.getString(TMDB_OVERVIEW);
                    resultStrs[i][6] = "http://image.tmdb.org/t/p/w1280/" + individualMovie.getString(TMDB_IMAGEPATH2);
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
                String movieJsonStr = null;

                try {
                    // Construct the URL
                    final String MOVIE_BASE_URL =
                            "http://api.themoviedb.org/3/movie/" + params[0] + "?";
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
                    StringBuffer buffer = new StringBuffer();
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
                        buffer.append(line + "\n");
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
                    return getMovieDataFromJson(movieJsonStr);
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
                    showData();
                }
            }

        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Most Popular";
                case 1:
                    return "Top Rated";
//                case 2:
//                    return "SECTION 3";
            }
            return null;
        }

    }

}
