package com.zythem.popularmovies;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetDetailProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_detail);

            // Set heading text
            views.setTextViewText(R.id.widget_detail_heading, context.getResources().getString(R.string.tab_most_popular));

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("THE_PARENT", "WIDGET");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId + 201, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_detail_heading, pendingIntent);

            // Set up the collection
            setRemoteAdapter(context, views, appWidgetId/*, appWidgetManager*/);
            boolean useDetailActivity = context.getResources()
                    .getBoolean(R.bool.use_detail_activity);
            Intent clickIntentTemplate = useDetailActivity
                    ? new Intent(context, DetailActivity.class)
                    : new Intent(context, MainActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(appWidgetId + 202, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_detail_list, clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_detail_list, R.id.widget_detail_empty);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views, int appWidgetId/*, AppWidgetManager appWidgetManager*/) {
        Intent intent = new Intent(context, WidgetDetailRemoteViewsService.class);
//        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));
        views.setRemoteAdapter(R.id.widget_detail_list, intent);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d("CHECK_THIS", "Called: onAppWidgetOptionsChanged appWidgetId = " + appWidgetId);

//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_detail);
//        appWidgetManager.updateAppWidget(appWidgetId, views);
//        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

//        Intent updateIntent = new Intent(context, WidgetDetailRemoteViewsService.class);
//        context.startService(updateIntent);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_detail_list);
    }

}
