package com.speko.android.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.speko.android.ChatActivity;
import com.speko.android.HomeActivity;
import com.speko.android.R;
import com.speko.android.Utility;

import static com.speko.android.sync.SpekoSyncAdapter.ACTION_DATA_UPDATED;

/**
 * Created by rafaelalves on 23/03/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ConversationsWidgetProvider extends AppWidgetProvider {
    private final String LOG =  getClass().getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        Log.i(LOG,"onUpdate");
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_conversations);


            // Create an Intent to launch HomeActivity
            Intent intent = new Intent(context, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.conversation_friend_fluent_language_profile_picture,
                    pendingIntent);


            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }

            Intent clickIntentTemplate = new Intent(context, ChatActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_conversation_list,
                    clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_conversation_list,
                    R.id.widget_empty_list);
            // if we're offline, we update the emptylist view
            if(!Utility.isNetworkAvailable(context)){
                views.setTextViewText(R.id.widget_empty_list,
                        context.getString(R.string.empty_conversations_list_no_network));

            }else{
                views.setTextViewText(R.id.widget_empty_list,
                        context.getString(R.string.no_friend_to_show));
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            Log.i(LOG, "onUpdate - Calling updateAppWidget");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i(LOG,"onReceive");
        if(ACTION_DATA_UPDATED.equals(intent.getAction())
                || intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
                ) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass())
            );

            Log.i(LOG, "Action: " + intent.getAction());



            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_conversation_list);

            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                //we are basically notifying this own class via broadcast in order to update the
                // widget view in case of being offline by making  calling onUpdate()
                onUpdate(context,appWidgetManager,appWidgetIds);


            }


        }

    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        Log.i(LOG, "setRemoteAdapter");
        views.setRemoteAdapter(R.id.widget_conversation_list,
                new Intent(context, ConversationsWidgetRemoteViewService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        Log.i(LOG, "setRemoteAdapterV11");
        views.setRemoteAdapter(0, R.id.widget_conversation_list,
                new Intent(context, ConversationsWidgetRemoteViewService.class));
    }
}
