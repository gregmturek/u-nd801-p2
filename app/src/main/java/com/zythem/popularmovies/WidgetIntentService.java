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
import android.view.View;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import java.util.Random;

public class WidgetIntentService extends IntentService {
    private static final String LOG_TAG = WidgetIntentService.class.getSimpleName();

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String movieTitle = null;
        String movieImagepathTemp = null;

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                WidgetProvider.class));

        // Add the heading to the RemoteViews
        int layoutId = R.layout.widget;
        final RemoteViews views = new RemoteViews(this.getPackageName(), layoutId);

        views.setTextViewText(R.id.widget_heading, getResources().getString(R.string.app_name));

        // Get data from the ContentProvider

        Uri uriType = MovieContentProvider.MostPopular.MOVIES;
        Cursor c = getContentResolver().query(uriType, null, null, null, null);

        views.setViewVisibility(R.id.widget_empty, View.VISIBLE);

        if (c != null) {
            // Extract the data from the Cursor
            int max = 19;
            int min = 0;
            Random random = new Random();
            int position = random.nextInt(max - min + 1) + min;

            if (c.moveToPosition(position)) {
                movieTitle = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_TITLE));
                movieImagepathTemp = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH));

                views.setViewVisibility(R.id.widget_empty, View.INVISIBLE);
            }

            c.close();
        }

        final String movieImagepath = movieImagepathTemp;

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            // Add the data to the RemoteViews
            if (movieTitle != null) {
                views.setTextViewText(R.id.widget_movie_title, movieTitle);
            }

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