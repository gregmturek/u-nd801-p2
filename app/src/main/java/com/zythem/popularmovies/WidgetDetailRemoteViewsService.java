package com.zythem.popularmovies;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

public class WidgetDetailRemoteViewsService extends RemoteViewsService {
    private static final String LOG_TAG = WidgetDetailRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {

        return new RemoteViewsFactory() {
            private Cursor c = null;
            private int mWidth, mHeight, mAppWidgetId;
            private AppWidgetManager mAppWidgetManager;

            @Override
            public void onCreate() {
                mAppWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

                Bundle options = mAppWidgetManager.getAppWidgetOptions(mAppWidgetId);

                int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
                int maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);

/*
                int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
                int maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
*/

                if (getResources().getBoolean(R.bool.portrait_orientation)) {
                    mWidth = (int) (minWidth * getResources().getDisplayMetrics().density);
                    mHeight = (int) ((minWidth * 3 / 2) * getResources().getDisplayMetrics().density);
                } else {
                    mWidth = (int) (maxWidth * getResources().getDisplayMetrics().density);
                    mHeight = (int) ((maxWidth * 3 / 2) * getResources().getDisplayMetrics().density);
                }
            }

            @Override
            public void onDataSetChanged() {
                if (c != null) {
                    c.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri uriType = MovieContentProvider.MostPopular.MOVIES;
                c = getContentResolver().query(uriType, null, null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (c != null) {
                    c.close();
                    c = null;
                }
            }

            @Override
            public int getCount() {
                return c == null ? 0 : c.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        c == null || !c.moveToPosition(position)) {
                    return null;
                }
                final RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_item);

                // Extract the data from the Cursor

                final MovieDataToPass data = new MovieDataToPass();
                data.mTitle = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_TITLE));
                data.mImagepath = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH));
                data.mDate = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_DATE));
                data.mRating = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_RATING));
                data.mId = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_ID));
                data.mOverview = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_OVERVIEW));
                data.mImagepath2 = c.getString(c.getColumnIndex(MostPopularColumns.MOVIE_IMAGEPATH_2));

                // Add the data to the RemoteViews

                if (data.mImagepath != null && !data.mImagepath.isEmpty()) {
                    //Run Picasso on the main thread
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    uiHandler.post(new Runnable(){
                        @Override
                        public void run() {
                            Target target = new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    views.setImageViewBitmap(R.id.widget_detail_movie_image, bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                    views.setTextViewText(R.id.widget_detail_movie_title_no_image, data.mTitle);
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    views.setTextViewText(R.id.widget_detail_movie_title, data.mTitle);
                                }
                            };

                            Picasso.with(WidgetDetailRemoteViewsService.this)
                                    .load(data.mImagepath)
                                    .resize(mWidth, mHeight)
                                    .into(target);
                        }
                    });
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra("THE_DATA", Parcels.wrap(data));
                views.setOnClickFillInIntent(R.id.widget_detail_item, fillInIntent);
                return views;
            }

/*
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }
*/

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (c.moveToPosition(position))
                    return c.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
