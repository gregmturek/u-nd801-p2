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

        // Get data from the ContentProvider

        Uri uriType = MovieContentProvider.MostPopular.MOVIES;
        String pathType = MovieContentProvider.Path.MOST_POPULAR;
        Cursor c = getContentResolver().query(uriType, null, null, null, null);

        if (c == null) {
            Log.d(LOG_TAG, "null cursor");
            return;
        }

        int max = 19;
        int min = 0;
        Random random = new Random();
        int position = random.nextInt(max - min + 1) + min;

        if (!c.moveToPosition(position)) {
            c.close();
            Log.d(LOG_TAG, "not cursor move to position ");
            return;
        }

        // Extract the data from the Cursor

        String movieTitle = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_TITLE));
        final String movieImagepath = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH));

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget;
            final RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews

            views.setTextViewText(R.id.widget_heading, "Popular Movies");
            views.setTextViewText(R.id.widget_movie_title, movieTitle);

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