package com.speko.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConversationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConversationsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER_ID = "param1";
    private static final int CONVERSATIONS_LOADER = 2;
    private ConversationsListAdapter mAdapter;
    private final String LOG_TAG = getClass().getSimpleName();

    @BindView(R.id.conversations_list)
    RecyclerView conversationsList;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public ConversationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ConversationsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConversationsFragment newInstance(String param1) {
        ConversationsFragment fragment = new ConversationsFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(USER_ID);
        }
    }

    @Override
    public void onStart() {
        Log.i(LOG_TAG,"Initloader");
        getLoaderManager().initLoader(CONVERSATIONS_LOADER, null, this);
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversations,container,false);
        ButterKnife.bind(this,view);

        conversationsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new ConversationsListAdapter(getActivity(), new ConversationsListAdapter.ConversationsAdapterOnClickHandler() {
            @Override
            public void onClick(String friendID) {
                Log.d(LOG_TAG,"onClick");
                String chatId = Utility.getFirebaseRoomIdWithUserID(friendID, getActivity());
                Intent i = new Intent(getActivity(), ChatActivity.class);
                i.putExtra(ChatActivityFragment.CHAT_ID, chatId);
                i.putExtra(ChatActivityFragment.FRIEND_ID, friendID);
                startActivity(i);
            }
        });

        Log.i(LOG_TAG, "setting adapter");
        conversationsList.setAdapter(mAdapter);

        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG,"onCreateLoader");
        return Utility.getUserConversationsCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        Log.i(LOG_TAG, "onLoaderFinished with total data: " + data.getCount());
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.i(LOG_TAG,"onLoaderReset");
        mAdapter.swapCursor(null);
    }
}
