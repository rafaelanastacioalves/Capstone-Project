package com.speko.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.speko.android.data.User;
import com.speko.android.sync.SpekoSyncAdapter;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.id.list;
import static android.app.Activity.RESULT_OK;
import static com.speko.android.Utility.RC_PHOTO_PICKER;
import static com.speko.android.Utility.getUser;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener, AppBarLayout.OnOffsetChangedListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int USER_LOADER = 2;
    private final String LOG_TAG = getClass().getSimpleName();

    private int mMaxScrollSize;
    private int TOTAL_SCROLLOING_PERCENTAGE_TO_ANIMATE_AVATAR = 60;
    private boolean mIsAvatarShown = true;


    @BindView(R.id.log_out)
    Button logOut;
    @BindView(R.id.sync_button)
    Button sync_button;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @BindView(R.id.signup_edittext_input_age)
    EditText ageEditText;

    @BindView(R.id.fragment_button_profile_change)
    AppCompatButton signupButton;

    @BindView(R.id.signup_user_description)
    AppCompatEditText userDescription;

    @BindView(R.id.signup_imageview_profile_picture)
    CircleImageView profilePicture;
    private Uri downloadUrl = null;

    @BindView(R.id.progress_bar)
    ContentLoadingProgressBar progressBar;

    @BindView(R.id.profile_grid_layout)
    GridLayout gridLayout;

    @BindView(R.id.profile_appbar_layout)
    AppBarLayout appBarLayout;

    @BindView(R.id.fluent_language_bigger_picture_imageview)
    ImageView fluentLanguageBiggerPictureImageView;

    @BindView(R.id.profile_fluent_language_imageview)
    ImageView profileFluentLanguageImageView;

    @BindView(R.id.profile_language_of_interest_imageview)
    ImageView profileLanguageOfInterestImageView;

    @BindView(R.id.profile_fluent_language_textview)
    TextView profileFluentLanguageTextView;

    @BindView(R.id.profile_language_of_interest_textview)
    TextView profileLanguageOfInterestTextView;

    @BindView(R.id.profile_fluent_language_container)
    View profileFluentLanguageContainer;

    @BindView(R.id.profile_language_of_interest_container)
    View profileLanguageOfInterestContainer;

    @BindView(R.id.profile_list_view)
    View profileListView;


    private User user;


    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Log.i(LOG_TAG,"Initloader");
        getLoaderManager().initLoader(USER_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this,view);

        profileListView.setPadding(0,Utility.getStatusBarHeight(getActivity()),0,0);
        setRefreshScreen(true);
//        SpekoSyncAdapter.syncImmediatly(getContext());




        return view;
    }



    @Override
    public void onResume() {
        super.onResume();
        appBarLayout.addOnOffsetChangedListener(this);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        updateScreenState();
    }

    @OnClick(R.id.sync_button)
    public void onClickSync(View v) {
        SpekoSyncAdapter.syncImmediatly(getActivity());
        setRefreshScreen(true);
//        getLoaderManager().restartLoader(FRIENDS_LOADER,null, this);
    }

    @OnClick(R.id.log_out)
    public void onClicklogOut(View v) {

        Utility.deleteEverything(getContext());
        FirebaseAuth.getInstance().signOut();
    }


    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void updateScreenState() {
        if (SpekoSyncAdapter.isSyncActive(getContext())) {
            Log.i(LOG_TAG, "Sync is active");
            setRefreshScreen(true);
        } else {
            Log.i(LOG_TAG, "Sync is NOT active");
            setRefreshScreen(false);
        }

        // if is syncing or off line
        if (SpekoSyncAdapter.isSyncActive(getContext()) ||
                !Utility.getIsConnectedStatus(getContext())){
            signupButton.setClickable(false);
        }else{
            signupButton.setClickable(true);
        }

    }

    @OnClick(R.id.fragment_button_profile_change)
    public void onClickChangeProfile(View v){
        // if fluent language and language of interest are equal


        String ageString = ageEditText.getText().toString();

        if (!Utility.isValidAge(ageString)){
            Toast.makeText(getContext(), R.string.age_not_acceptable_error, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (user == null) {
            user = Utility.getUser(getContext());
        }
        if (ageEditText.getText() == null || ageEditText.getText().toString().isEmpty()) {
            Log.i(LOG_TAG, "Text is null or empty, so we set nothing");
        }else {
            Log.i(LOG_TAG, "Text is NOT null or empty, setting from text: " +
                    ageEditText.getText().toString());
            user.setAge(ageEditText.getText().toString());

        }

        if (userDescription.getText() == null || userDescription.getText().toString().isEmpty() ) {
            Log.i(LOG_TAG, "Text is null or empty, so we set nothing ");
        }else{
            Log.i(LOG_TAG, "Text is NOT null or empty, setting from text: " +
                    userDescription.getText().toString());
            user.setUserDescription(userDescription.getText().toString());
        }

        if (downloadUrl != null){
            user.setProfilePicture(downloadUrl.toString());

        }



        user.setLearningCode(user.getFluentLanguage()
                + "|"
                + user.getLearningLanguage());


        Utility.setUser(user,getActivity());

        SpekoSyncAdapter.syncImmediatly(getActivity());

        Toast.makeText(getActivity(), "ProfileUpdated!", Toast.LENGTH_SHORT).show();




    }

    @OnClick(R.id.signup_imageview_profile_picture)
    public void onClickUploadPicture(View v){
       Utility.call_to_upload_picture(this);
    }

    @OnClick(R.id.profile_fluent_language_container)
    public void onClickChangeFluentLanguage(View v){

        final String[] entriesArray  = getResources().getStringArray(R.array.options_entries_languages);
        final String[] valuesArray  = getResources().getStringArray(R.array.options_values_languages);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_fluent_language);
        builder.setItems(entriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]

                String systemValue = valuesArray[which];
                user.setFluentLanguage(systemValue);
                setView();


            }
        });
        builder.show();
    }

    @OnClick(R.id.profile_language_of_interest_container)
    public void onClickChangeLanguageOfInterest(View v){

        final String[] entriesArray  = getResources().getStringArray(R.array.options_entries_languages);
        final String[] valuesArray  = getResources().getStringArray(R.array.options_values_languages);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_fluent_language);
        builder.setItems(entriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]

                String systemValue = valuesArray[which];
                user.setLearningLanguage(systemValue);
                setView();


            }
        });
        builder.show();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(LOG_TAG,"onActivityResult");
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Log.i(LOG_TAG, "Result OK from profile pick");
            Uri selectedImageUri = data.getData();

            // Get a reference to store file at user_pictures/<UID>/<FILENAME>
            StorageReference photoRef = FirebaseStorage.getInstance().getReference()
                    .child(getString(R.string.user_pictures))
                    .child(getUser(getActivity()).getId())
                    .child(selectedImageUri.getLastPathSegment());

            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.i(LOG_TAG, "Result OK from Firebase persistance");

                            // When the image has successfully uploaded, we get its download URL
                            downloadUrl = taskSnapshot.getDownloadUrl();
                            showUserPhoto(downloadUrl.toString());


                        }
                    });
        }

    }

    private void showUserPhoto(String downloadUrl) {
        Picasso.with(getContext()).load(downloadUrl.toString())
                .placeholder(R.drawable.ic_user)
                .resize(getResources().getDimensionPixelSize(R.dimen.profile_user_picture_dimen),
                        getResources().getDimensionPixelSize(R.dimen.profile_user_picture_dimen))
                .centerCrop().into(profilePicture);
    }

    private void setView() {

        Log.i(LOG_TAG,"setView");
        if(user == null){
            user = Utility.getUser(getActivity());
        }
        String spinnerValue = user.getFluentLanguage();
        Log.i(LOG_TAG,"Fluent Langauge: " + spinnerValue);



        fluentLanguageBiggerPictureImageView.setImageResource(
                Utility.getFluentLangagueBiggerPictureUri(getActivity(), user.getFluentLanguage()));


        spinnerValue =user.getLearningLanguage();
        Log.i(LOG_TAG,"Learning Langauge: " + spinnerValue);




        ageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText editText = (EditText) v;
                if(hasFocus){
                    editText.setHint(user.getAge());

                }else{
                    editText.setHint("");

                }
            }
        });

        userDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText editText = (EditText) v;
                if(hasFocus){
                    editText.setHint(user.getUserDescription());

                }else{
                    editText.setHint("");

                }
            }
        });

        Log.i(LOG_TAG,"Age: " + user.getAge());

        if(user.getProfilePicture() != null){

            showUserPhoto(user.getProfilePicture());
        }

        profileFluentLanguageImageView.setImageResource(Utility.getDrawableUriForLanguage( user.getFluentLanguage(),getActivity()));

        Log.i(LOG_TAG,"Age: " + user.getLearningLanguage());
        profileLanguageOfInterestImageView.setImageResource(Utility.getDrawableUriForLanguage( user.getLearningLanguage(),getActivity()));

        profileFluentLanguageTextView.setText(user.getFluentLanguage());
        profileLanguageOfInterestTextView.setText(user.getLearningLanguage());
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        Log.i(LOG_TAG,"onOffSetChanged: ");
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();
        if (mMaxScrollSize == 0 ){
            return;
        }

        int percentage = (Math.abs(verticalOffset)) * 100 / mMaxScrollSize;
        Log.i(LOG_TAG,"mMaxScrollSize: " + mMaxScrollSize);
        Log.i(LOG_TAG,"percentage of Scrolling: " + percentage);


        if (percentage >= TOTAL_SCROLLOING_PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

            profilePicture.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(200)
                    .start();
        }

        if (percentage <= TOTAL_SCROLLOING_PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            profilePicture.animate()
                    .scaleY(1).scaleX(1)
                    .start();
        }
    }


    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG,"onCreateLoader");
        return Utility.getUserCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == USER_LOADER){
            Log.i(LOG_TAG,"onLoaderFinished");
            setView();
            setRefreshScreen(false);

        }

        //we destroy the loader, as there is no need for updating the view
        getLoaderManager().destroyLoader(USER_LOADER);

    }

    private void setRefreshScreen(Boolean active) {
        //TODO Implement
        Log.i(LOG_TAG, "setRefresh: " + active.toString());
        if (active) {
            gridLayout.setVisibility(View.INVISIBLE);
            progressBar.show();
            //if onClickSync active, disable list clicking


        } else {
            progressBar.hide();
            gridLayout.setVisibility(View.VISIBLE);
            //if onClickSync NOT active, enable list clicking

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(LOG_TAG, "Shared Preferences changed: ");
        if (key.equals(getString(R.string.shared_preference_sync_status_key))) {
            Log.d(LOG_TAG, "Case onClickSync-status");
            updateScreenState();
        }

        if (key.equals(getString(R.string.shared_preference_active_connectivity_status_key))) {
            Log.d(LOG_TAG, "Case connectivity");
            updateScreenState();

        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(User user);
    }







}
