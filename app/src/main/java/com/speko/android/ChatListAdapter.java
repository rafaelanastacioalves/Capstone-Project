package com.speko.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.speko.android.data.MessageLocal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by rafaelalves on 21/01/17.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.UserViewHolder>  {


    private final String LOG_TAG = getClass().getSimpleName();
    private final Context mContext;
    private List<MessageLocal> mMessageList;

    public ChatListAdapter(Context context, FriendsAdapterOnClickHandler dh){
        this(context);
        Log.i(LOG_TAG, "Constructor");



    }
    public ChatListAdapter(Context context){
        if(mMessageList == null){
            mMessageList = new ArrayList<>();

        }
        mContext = context;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(LOG_TAG, "OnCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_viewholder,parent,false);
        return new UserViewHolder(view);
    }

    public void swapCursor(List<MessageLocal> c){
        this.mMessageList = c;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
          MessageLocal mMessage = mMessageList.get(position);

        Log.i(LOG_TAG, "onBindViewHolder");

        //TODO modify cursor and name variables
//        String senderUserName = mMessage.getName();
//
//        Log.i(LOG_TAG, "Name: " + senderUserName);
//        String mMessageText = mMessage.getText();
//        Log.i(LOG_TAG, "Text: " + mMessageText);
//
//
//
//        holder.mSenderUsername.setText(senderUserName);
//        holder.mMessage.setText(mMessageText);
    }


    @Override
    public int getItemCount() {
        if(mMessageList == null){
            Log.i(LOG_TAG, "getItemCount with 0 itens");
            return 0;
        }else{
            Log.i(LOG_TAG, "getItemCount with" + mMessageList.size()+ " itens");
            return mMessageList.size();
        }
    }

    public void add(MessageLocal m) {
        mMessageList.add(m);
        notifyDataSetChanged();
    }

    public static interface FriendsAdapterOnClickHandler{
        void onClick(String friendID);
    }


    public class UserViewHolder extends RecyclerView.ViewHolder  {

        @BindView(R.id.message_viewholder_sender_username) TextView mSenderUsername;
        @BindView(R.id.message_viewholder_message) TextView mMessage;



        public UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

        }


    }



}
