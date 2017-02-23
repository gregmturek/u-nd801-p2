package com.zythem.popularmovies;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

public class WidgetIntentService extends IntentService {
    private static final String LOG_TAG = WidgetIntentService.class.getSimpleName();
/*
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
*/

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                WidgetProvider.class));

        // Get data from the ContentProvider

        Uri uriType = MovieContentProvider.MostPopular.MOVIES;
        String pathType = MovieContentProvider.Path.MOST_POPULAR;
        Cursor c = getContentResolver().query(uriType, null, null, null, null);

        Log.d(LOG_TAG, "service called");


        if (c == null) {
            Log.d(LOG_TAG, "null cursor");
            return;
        }

        if (!c.moveToFirst()) {
            c.close();
            Log.d(LOG_TAG, "not cursor move to first ");
            return;
        }



/*
        String location = Utility.getPreferredLocation(this);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location, System.currentTimeMillis());
        Cursor data = getContentResolver().query(weatherForLocationUri, FORECAST_COLUMNS, null,
                null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }
*/

        // Extract the data from the Cursor

        String movieTitle = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_TITLE));
        final String movieImagepath = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH));

/*
        int weatherId = data.getInt(INDEX_WEATHER_ID);
        int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
        String description = data.getString(INDEX_SHORT_DESC);
        double maxTemp = data.getDouble(INDEX_MAX_TEMP);
        String formattedMaxTemperature = Utility.formatTemperature(this, maxTemp);
        data.close();
*/

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget;
            final RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews

            views.setTextViewText(R.id.widget_heading, "Popular Movies");
            views.setTextViewText(R.id.widget_movie_title, movieTitle);

/*
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    views.setImageViewBitmap(R.id.widget_movie_image , bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
*/

            if (movieImagepath != null && !movieImagepath.isEmpty()) {
                //Run Picasso on the main thread
                Handler uiHandler = new Handler(Looper.getMainLooper());
                uiHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        Picasso.with(getApplicationContext())
                                .load(movieImagepath)
                                .noFade()
                                .into(views, R.id.widget_movie_image, appWidgetIds);
                    }
                });
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}