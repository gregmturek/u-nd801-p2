package com.zythem.popularmovies;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SearchResultsActivity extends AppCompatActivity {
    private String[][] mMovieData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        handleIntent(getIntent());

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to action_search your data somehow

            Toast.makeText(getApplication(), "Search: " + query, Toast.LENGTH_SHORT).show();
            Log.d("CHECK_THIS", "Info:" + query);

            FetchSearchTask fetchSearchTask = new FetchSearchTask();
            fetchSearchTask.execute(query);
        }
    }



    public class FetchSearchTask extends AsyncTask<String, Void, String[][]> {
        private ProgressDialog dialog;

        private final String LOG_TAG = FetchSearchTask.class.getSimpleName();

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
//            String[] movieJsonStrs = new String[Integer.parseInt(params[1])];
            // Set to just 1 page for now
            String[] movieJsonStrs = new String[1];

            for(int i = 0; i < movieJsonStrs.length; i++) {
                try {
                    // Construct the URL
                    final String MOVIE_BASE_URL =
                            "http://api.themoviedb.org/3/search/movie?";
                    final String APIKEY_PARAM = "api_key";
                    final String QUERY_PARAM = "query";
                    final String PAGE_PARAM = "page";

                    Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                            .appendQueryParameter(QUERY_PARAM, params[0])
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
        protected void onPreExecute() {
            dialog = new ProgressDialog(SearchResultsActivity.this);
            dialog.setMessage(getString(R.string.download_dialog));
            dialog.setIndeterminate(true);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String[][] result) {
            if (result != null) {
                mMovieData = result;
                // New data is back from the server.  Hooray!
//                storeAllData(mMovieData);
//                showInitialTabletDetailFragment();
            }
            Log.d("CHECK_THIS", "Search Result from API:" + mMovieData[0][5]);
            dialog.dismiss();
        }

    }



}
