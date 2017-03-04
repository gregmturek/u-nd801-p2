package com.zythem.popularmovies;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.Random;

public class WidgetIntentService extends IntentService {
    private static final String LOG_TAG = WidgetIntentService.class.getSimpleName();

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                WidgetProvider.class));

        // Add the heading to the RemoteViews
        int layoutId = R.layout.widget;
        final RemoteViews views = new RemoteViews(this.getPackageName(), layoutId);

        views.setTextViewText(R.id.widget_heading, getResources().getString(R.string.app_name));

        // Get cursor
        Uri uriType = MovieContentProvider.MostPopular.MOVIES;
        Cursor c = getContentResolver().query(uriType, null, null, null, null);

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            MovieDataToPass data = new MovieDataToPass();

            views.setViewVisibility(R.id.widget_movie_image, View.INVISIBLE);
            views.setViewVisibility(R.id.widget_movie_title, View.INVISIBLE);
            views.setTextViewText(R.id.widget_empty, getResources().getString(R.string.empty_grid));
            views.setViewVisibility(R.id.widget_empty, View.VISIBLE);

            // Get data from content provider
            if (c != null) {
                // Extract the data from the Cursor
                int max = 19;
                int min = 0;
                Random random = new Random();
                int position = random.nextInt(max - min + 1) + min;

                if (c.moveToPosition(position)) {
                    data.mTitle = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_TITLE));
                    data.mImagepath = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH));
                    data.mDate = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_DATE));
                    data.mRating = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_RATING));
                    data.mId = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_ID));
                    data.mOverview = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_OVERVIEW));
                    data.mImagepath2 = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH_2));

                    views.setViewVisibility(R.id.widget_empty, View.INVISIBLE);

                    // Add the data to the RemoteViews
                    if (data.mTitle != null) {
                        views.setTextViewText(R.id.widget_movie_title, data.mTitle);
                        views.setViewVisibility(R.id.widget_movie_title, View.VISIBLE);
                    }

                    Bitmap bitmap = null;
                    if (data.mImagepath != null && !data.mImagepath.isEmpty()) {
                        //Run Picasso on the main thread
                        try {
                            bitmap = Picasso.with(WidgetIntentService.this)
                                    .load(data.mImagepath)
                                    .get();
                            if (bitmap != null) {
                                views.setImageViewBitmap(R.id.widget_movie_image, bitmap);
                                views.setViewVisibility(R.id.widget_movie_image, View.VISIBLE);
                            } else if (data.mTitle != null) {
                                views.setTextViewText(R.id.widget_empty, data.mOverview);
                                views.setViewVisibility(R.id.widget_empty, View.VISIBLE);
                            }
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Error retrieving image from " + data.mImagepath, e);
                        }
                    }

                    // Create an Intent to launch specific movie
                    boolean useDetailActivity = getResources()
                            .getBoolean(R.bool.use_detail_activity);
                    Intent launchIntentSpecific = useDetailActivity
                            ? new Intent(this, DetailActivity.class)
                            : new Intent(this, MainActivity.class);
                    launchIntentSpecific.putExtra("THE_DATA", Parcels.wrap(data));
                    PendingIntent pendingIntentSpecific = PendingIntent.getActivity(this, appWidgetId,
                            launchIntentSpecific, PendingIntent.FLAG_UPDATE_CURRENT);
                    views.setOnClickPendingIntent(R.id.widget_movie_image, pendingIntentSpecific);
                    views.setOnClickPendingIntent(R.id.widget_movie_title, pendingIntentSpecific);
                }
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_heading, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        // Close cursor
        if (c != null) {
            c.close();
        }
    }
}