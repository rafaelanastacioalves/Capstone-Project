package com.speko.android;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by rafaelalves on 21/01/17.
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.UserViewHolder> {


    private final String LOG_TAG = getClass().getSimpleName();
    private final Context mContext;
    private Cursor mCursor;

    public FriendsAdapter(Context context){
        Log.i(LOG_TAG, "Contructor");
        mContext = context;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(LOG_TAG, "OnCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_viewholder,parent,false);
        return new UserViewHolder(view);
    }

    public void swapCursor(Cursor c){
        this.mCursor = c;
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        Log.i(LOG_TAG, "onBindViewHolder");
        String userName = mCursor.getString(HomeActivityFragment.COL_USER_NAME);
        Log.i(LOG_TAG, "Name: " + userName);
        String userEmail = mCursor.getString(HomeActivityFragment.COL_EMAIL);
        Log.i(LOG_TAG, "Email: " + userEmail);



        holder.mNameTextView.setText(userName);
        holder.mUserEmailTextView.setText(userEmail);
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

    public class UserViewHolder extends RecyclerView.ViewHolder {

        public final TextView mNameTextView;
        public final TextView mUserEmailTextView;


        public UserViewHolder(View itemView) {
            super(itemView);

            mNameTextView = (TextView) itemView.findViewById(R.id.friend_viewholder_username);
            mUserEmailTextView = (TextView) itemView.findViewById(R.id.friend_viewholder_useremail);
        }
    }



}
