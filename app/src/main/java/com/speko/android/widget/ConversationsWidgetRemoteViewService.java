package com.speko.android.widget;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.speko.android.ChatActivityFragment;
import com.speko.android.R;
import com.speko.android.Utility;
import com.speko.android.data.ChatMembersColumns;
import com.speko.android.data.UserPublic;
import com.speko.android.data.UsersProvider;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.IOException;

/**
 * Created by rafaelalves on 23/03/17.
 */

@SuppressWarnings("ALL")
public class ConversationsWidgetRemoteViewService extends RemoteViewsService {
    private final String LOG = getClass().getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i(LOG,"onGetViewFactory");

        return new RemoteViewsFactory() {
            public final String LOG_TAG = "RemoteViewsFactory";
            private Cursor data = null;

            @Override
            public void onCreate() {
                Log.i(LOG_TAG, "onCreate");

            }

            @Override
            public void onDataSetChanged() {
                Log.i(LOG_TAG,"onDataSetChanged");

                // if we are offline
                if (!Utility.isNetworkAvailable(getApplication())) {

                    if (data != null) {
                        data.close();
                        data = null;
                    }

                }else{
                    if (data != null) {
                        data.close();
                    }

                    final long identityToken = Binder.clearCallingIdentity();
                    data = getContentResolver().query(UsersProvider.ChatMembers.CHAT_URI,
                            null,
                            null,
                            null,
                            null);
                    Binder.restoreCallingIdentity(identityToken);
                }

            }


            @Override
            public void onDestroy() {
                Log.i(LOG_TAG,"onDestroy");
                if (data != null) {
                    data.close();
                    data = null;
                }

            }

            @Override
            public int getCount() {
                Log.i(LOG_TAG,"getCount");
                return data == null ? 0 : data.getCount();
            }

            @SuppressLint("ObsoleteSdkInt")
            @Override
            public RemoteViews getViewAt(int position) {
                Log.i(LOG_TAG,"getViewAt");
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_conversation_viewholder);


                String userName = data.getString(
                        data.getColumnIndex(ChatMembersColumns.OTHER_MEMBER_NAME)
                );



                views.setTextViewText(R.id.conversation_friend_viewholder_username, userName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.conversation_friend_viewholder_username,
                            getString(R.string.a11y_friend_name_content_description, userName));
                }


                Bitmap profilePicture = null;
                try {
                    profilePicture = Picasso.with(ConversationsWidgetRemoteViewService.this).load(
                            data.getString(
                                    data.getColumnIndex(ChatMembersColumns.OTHER_MEMBER_PHOTO_URL)
                            )).placeholder(R.drawable.ic_placeholder_profile_photo)
                            .transform(new CircleTransform()).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (profilePicture != null) {
                    views.setImageViewBitmap(R.id.conversation_friend_profile_picture,
                            profilePicture);

                } else {
                    views.setImageViewResource(R.id.conversation_friend_profile_picture,
                            R.drawable.ic_placeholder_profile_photo);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.conversation_friend_profile_picture,
                            getString(R.string.a11y_friend_picture_content_description));
                }

                String otherUserId = data.getString(
                        data.getColumnIndex(ChatMembersColumns.OTHER_MEMBER_ID)
                );

                // we need call these binder methods in orther to get permissions
                final long identityToken = Binder.clearCallingIdentity();
                UserPublic otherUserComplete = Utility.
                        getOtherUserWithIdFromConversationsDB(ConversationsWidgetRemoteViewService.this, otherUserId);
                Binder.restoreCallingIdentity(identityToken);

                String fluentLanguage = data.getString(
                        data.getColumnIndex(ChatMembersColumns.OTHER_MEMBER_FLUENT_LANGUAGE));

                views.setImageViewResource(R.id.conversation_friend_fluent_language_profile_picture,
                        Utility.getDrawableUriForLanguage(fluentLanguage,
                                ConversationsWidgetRemoteViewService.this)
                );
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.conversation_friend_fluent_language_profile_picture,
                            getString(R.string.a11y_friend_fluent_language_content_description,
                                    fluentLanguage));

                }
                final Intent fillIntent = new Intent();

                String chatId = data.getString(
                        data.getColumnIndex(ChatMembersColumns.FIREBASE_CHAT_ID)
                );

                fillIntent.putExtra(ChatActivityFragment.CHAT_ID, chatId);
                fillIntent.putExtra(ChatActivityFragment.FRIEND_ID, otherUserId);
                views.setOnClickFillInIntent(R.id.widget_conversation_viewholder,fillIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                Log.i(LOG_TAG,"getLoadingView");
                // using the same view from conversations viewholder
                return new RemoteViews(getPackageName(), R.layout.widget_conversation_viewholder);
            }

            @Override
            public int getViewTypeCount() {
                Log.i(LOG_TAG,"getViewTypeCount");
                return 1;
            }



            @Override
            public long getItemId(int position) {
                // doesn't matter
                Log.i(LOG_TAG,"getIdtemId");
                return position;
            }

            @Override
            public boolean hasStableIds() {
                Log.i(LOG_TAG,"hasStableIds");
                return true;
            }
        };
    }

    private class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }
}
