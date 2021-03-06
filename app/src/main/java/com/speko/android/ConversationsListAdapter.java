package com.speko.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.speko.android.data.ChatMembersColumns;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafaelalves on 21/01/17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class ConversationsListAdapter extends RecyclerView.Adapter<ConversationsListAdapter.UserChatViewHolder> implements SharedPreferences.OnSharedPreferenceChangeListener {


    private final String LOG_TAG = getClass().getSimpleName();
    private final Context mContext;
    private Cursor mCursor;
    private final ConversationsAdapterOnClickHandler mClickHanlder;
    private boolean itemsClickable;
    @SuppressWarnings("unused")
    private boolean viewItensClickable;

    public ConversationsListAdapter(Context context, ConversationsAdapterOnClickHandler dh){
        Log.i(LOG_TAG, "Contructor");
        mContext = context;
        mClickHanlder = dh;

    }

    private void updateItemClicking() {

        // if is connected, so are clickable the items
        viewItensClickable =  Utility.getIsConnectedStatus(mContext);

        notifyDataSetChanged();
    }


    @Override
    public UserChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(LOG_TAG, "OnCreateViewHolder");
        itemsClickable = true;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_viewholder,parent,false);
        return new UserChatViewHolder(view);
    }

    public void swapCursor(Cursor c){
        this.mCursor = c;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(UserChatViewHolder holder, int position) {

        if(mCursor.moveToPosition(position)) {


            Log.i(LOG_TAG, "onBindViewHolder");
            String otherUserName = mCursor.getString(
                    mCursor.getColumnIndex(ChatMembersColumns.OTHER_MEMBER_NAME)
            );


            holder.mNameTextView.setText(otherUserName);
            holder.mNameTextView.setContentDescription(
                    mContext.getString(R.string.a11y_friend_name_content_description, otherUserName)
            );

            Picasso.with(mContext).load(
                    mCursor.getString(
                            mCursor.getColumnIndex(ChatMembersColumns.OTHER_MEMBER_PHOTO_URL)
                    )).placeholder(R.drawable.ic_placeholder_profile_photo)
                    .into(holder.mProfilePicture);
            holder.mProfilePicture.setContentDescription(mContext.getString(
                        R.string.a11y_friend_picture_content_description
                    )
            );


            String fluentLanguage = mCursor.getString(
                    mCursor.getColumnIndex(ChatMembersColumns.OTHER_MEMBER_FLUENT_LANGUAGE));

                    holder.conversationProfileFluentLanguagePicture.setImageResource(
                    Utility.getDrawableUriForLanguage(fluentLanguage,mContext)
            );

                    holder.conversationProfileFluentLanguagePicture.setContentDescription(
                        mContext.getString(R.string.a11y_friend_fluent_language_content_description,
                                Utility.getCompleteLanguageNameString(fluentLanguage, mContext))
                    );

            holder.conversationViewHolderContainer.setEnabled(itemsClickable);
            Log.i(LOG_TAG, "end of onBindViewHolder");
        }
    }


    @Override
    public int getItemCount() {
        if(mCursor == null){
            Log.i(LOG_TAG, "getItemCount with 0 itens");
            return 0;
        }else{
            Log.i(LOG_TAG, "getItemCount with" + mCursor.getCount()+ " itens");
            return mCursor.getCount();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(mContext.getString(R.string.shared_preference_active_connectivity_status_key))){
            updateItemClicking();
        }
    }

    public void setViewItensClickable(boolean b) {
        this.viewItensClickable = b;
        notifyDataSetChanged();
    }

    public interface ConversationsAdapterOnClickHandler {
        void onClick(String friendID);

    }


    public class UserChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.conversation_friend_profile_picture) CircleImageView mProfilePicture;
        @BindView(R.id.conversation_friend_viewholder_username) TextView mNameTextView;
        @BindView(R.id.conversation_friend_fluent_language_profile_picture) CircleImageView
                conversationProfileFluentLanguagePicture;
        @BindView(R.id.conversation_viewholder) View conversationViewHolderContainer;



        public UserChatViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            String friendID = mCursor.getString(mCursor.getColumnIndex(ChatMembersColumns.OTHER_MEMBER_ID));
            mClickHanlder.onClick(friendID);
        }
    }



}
