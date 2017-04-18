package com.speko.android;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.speko.android.data.UserColumns;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafaelalves on 21/01/17.
 */

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.UserViewHolder>  {


    private final String LOG_TAG = getClass().getSimpleName();
    private final Context mContext;
    private Cursor mCursor;
    private FriendsAdapterOnClickHandler mClickHanlder;
    private Boolean viewItensClickable;

    public FriendsListAdapter(Context context, FriendsAdapterOnClickHandler dh){
        Log.i(LOG_TAG, "Contructor");
        mContext = context;
        mClickHanlder = dh;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(LOG_TAG, "OnCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_viewholder,parent,false);
        return new UserViewHolder(view);
    }

    public void swapCursor(Cursor c){
        this.mCursor = c;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        Log.i(LOG_TAG, "onBindViewHolder");
        String id = mCursor.getString(mCursor.getColumnIndex(UserColumns.FIREBASE_ID));
        Log.i(LOG_TAG, "user with id: " + id );

        String userName = mCursor.getString(
                mCursor.getColumnIndex(UserColumns.NAME)
        );
        Log.i(LOG_TAG, "Name: " + userName);
        String userEmail = mCursor.getString(
                mCursor.getColumnIndex(UserColumns.EMAIL)
        );
        Log.i(LOG_TAG, "Email: " + userEmail);



        holder.mNameTextView.setText(userName);
        if(viewItensClickable != null){
            holder.friendViewHolder.setClickable(viewItensClickable);

        }

        Picasso picasso = Picasso.with(mContext);
        picasso.setIndicatorsEnabled(BuildConfig.DEBUG);
        picasso.load(mCursor.getString(
            mCursor.getColumnIndex(UserColumns.USER_PHOTO_URL)
        )).placeholder(R.drawable.ic_placeholder_profile_photo)
                .into(holder.friendProfilePicture) ;

        String fluentLanguage = mCursor.getString(
                mCursor.getColumnIndex(UserColumns.FLUENT_LANGUAGE)
        );
        holder.friendProfileFluentLanguagePicture.setImageResource(
                Utility.getDrawableUriForLanguage(fluentLanguage,mContext)
        );

        Log.i(LOG_TAG, "settingEnabled friendViewHolder: " + viewItensClickable);
        holder.friendViewHolder.setEnabled(viewItensClickable);
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

    public void setViewItensClickable(boolean b) {
        this.viewItensClickable = b;
        notifyDataSetChanged();
    }


    private void updateItemClicking() {

        // if is connected, so are clickable the items
        viewItensClickable =  Utility.getIsConnectedStatus(mContext);
        Log.i(LOG_TAG, "viewItemClickable: " + viewItensClickable.booleanValue());
        notifyDataSetChanged();
    }

    public static interface FriendsAdapterOnClickHandler{
        void onClick(String friendID);
    }


    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.friend_viewholder_username) TextView mNameTextView;
        @BindView(R.id.friend_viewholder) RelativeLayout friendViewHolder;
        @BindView(R.id.friend_profile_picture) CircleImageView friendProfilePicture;
        @BindView(R.id.friend_profile_fluent_language_picture)
        CircleImageView friendProfileFluentLanguagePicture;


        public UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            String friendID = mCursor.getString(mCursor.getColumnIndex(UserColumns.FIREBASE_ID));
            Log.i(LOG_TAG, "Friend List Adapter -> friendId " + friendID);
            mClickHanlder.onClick(friendID);
        }
    }



}
