package com.speko.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.speko.android.data.UserComplete;
import com.speko.android.sync.SpekoSyncAdapter;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.speko.android.Utility.RC_PHOTO_PICKER;

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

    public static final String BUNDLE_ARGUMENT_IS_SYNCABLE = "bundle_argument_is_syncable";
    private boolean BUNDLE_VALUE_IS_SYNCABLE = false;

    public static final String BUNDLE_ARGUMENT_FIRST_TIME_ENABLED =
            "bundle_argument_first_time_enabled";
    private boolean BUNDLE_VALUE_FIRST_TIME_ENABLED = false;



    private boolean mIsAvatarShown = true;

    @Nullable
    @BindView(R.id.log_out)
    Button logOut;
    @BindView(R.id.sync_button)
    Button sync_button;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @BindView(R.id.signup_edittext_input_age)
    EditText ageEditText;

    @BindView(R.id.signup_edittext_input_name)
    EditText nameEditText;

    @Nullable
    @BindView(R.id.fragment_button_profile_change)
    AppCompatButton signupButton;

    @BindView(R.id.signup_user_description)
    AppCompatEditText userDescription;

    @BindView(R.id.signup_imageview_profile_picture)
    CircleImageView profilePicture;

    @BindView(R.id.signup_imageview_profile_picture_container)
    FrameLayout profilePictureContainer;

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

    @Nullable
    @BindView(R.id.profile_options_container_view_stub)
    ViewStub profileOptionsContainerViewStub;

    private UserComplete userComplete;
    private OnFragmentInteractionListener mListener;


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void completeSignup(UserComplete userComplete);
    }



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
        Log.i(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {


            BUNDLE_VALUE_FIRST_TIME_ENABLED = getArguments().
                    getBoolean(BUNDLE_ARGUMENT_FIRST_TIME_ENABLED,false);
            Log.i(LOG_TAG, "BUNDLE_VALUE_FIRST_TIME_ENABLED: " + BUNDLE_VALUE_FIRST_TIME_ENABLED);

            BUNDLE_VALUE_IS_SYNCABLE = getArguments().
                    getBoolean(BUNDLE_ARGUMENT_IS_SYNCABLE,false);

            Log.i(LOG_TAG, "BUNDLE_VALUE_IS_SYNCABLE: " + BUNDLE_VALUE_IS_SYNCABLE);

        }

        Log.i(LOG_TAG,"Initloader");
        getLoaderManager().initLoader(USER_LOADER, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this,view);
        profileListView.setPadding(0,Utility.getStatusBarHeight(getActivity()),0,0);
        setRefreshScreen(true);
        setup(view);
        ButterKnife.bind(this,view);


//        SpekoSyncAdapter.syncImmediatly(getContext());




        return view;
    }

    private void setup(View view) {

        if(BUNDLE_VALUE_FIRST_TIME_ENABLED){
            Log.i(LOG_TAG, "First Time Enabled TRUE!");
            profileOptionsContainerViewStub.setLayoutResource(R.layout.profile_options_signup);
            profileOptionsContainerViewStub.inflate();

        }else{

            Log.i(LOG_TAG, "First Time Enabled FALSE!");
            profileOptionsContainerViewStub.setLayoutResource(R.layout.profile_options_edit);
            profileOptionsContainerViewStub.inflate();
            signupButton = (AppCompatButton) view.findViewById(R.id.fragment_button_profile_change);

        }

        if(BUNDLE_VALUE_IS_SYNCABLE){
            Log.i(LOG_TAG, "Syncable TRUE!");
            sync_button = (Button)  view.findViewById(R.id.sync_button);
            sync_button.setVisibility(View.VISIBLE);

        }
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


    @OnClick(R.id.log_out) @Optional
    public void onClicklogOut(View v) {

        Utility.deleteEverything(getContext());
        FirebaseAuth.getInstance().signOut();
    }


    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        userComplete = null;
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

        if(BUNDLE_VALUE_IS_SYNCABLE){
            // if is syncing or off line
            if (SpekoSyncAdapter.isSyncActive(getContext()) ||
                    !Utility.getIsConnectedStatus(getContext())){
                signupButton.setClickable(false);
            }else{
                signupButton.setClickable(true);
            }
        }


    }

    @OnClick(R.id.fragment_button_profile_register) @Optional
    public void onClickRegisterUser(View v){
        if (populateAndValidateUserObjectCorrectly()){
            mListener.completeSignup(userComplete);
        }


    }

    @OnClick(R.id.fragment_button_profile_change) @Optional
    public void onClickChangeProfile(View v){

        if(populateAndValidateUserObjectCorrectly()){
            final Context applicationContext = getActivity().getApplication();
            OnCompleteListener onCompleteListener = new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    SpekoSyncAdapter.syncImmediatly(applicationContext);
                }
            };
            Utility.setUser(userComplete,getActivity(), onCompleteListener);


            Toast.makeText(getActivity(), "ProfileUpdated!", Toast.LENGTH_SHORT).show();

        }





    }

    private boolean populateAndValidateUserObjectCorrectly(){
        Log.i(LOG_TAG, "PopulateAndValidateUserObject");


        if (userComplete == null) {
            userComplete = Utility.getUser(getContext());
        }

        // About age...
        if (ageEditText.getText() == null || ageEditText.getText().toString().isEmpty()) {
            Log.i(LOG_TAG, "Text is null or empty, so we set nothing");
        }else {
            Log.i(LOG_TAG, "Text is NOT null or empty, setting from text: " +
                    ageEditText.getText().toString());
            userComplete.setAge(ageEditText.getText().toString());

        }
        String ageString = ageEditText.getText().toString();
        if (!Utility.isValidAge(ageString)){
            Toast.makeText(getContext(), R.string.age_not_acceptable_error, Toast.LENGTH_LONG)
                    .show();
            return false;
        }



        // About name...

        // if text view is empty...
        Log.i(LOG_TAG, "populateAndValidateUser...  name: " + nameEditText.getText());
        Log.i(LOG_TAG, "populateAndValidateUser...  user.name: " + userComplete.getName());
        if (nameEditText.getText() == null || nameEditText.getText().toString().isEmpty()) {

                // and even the value already put is empty
                if (userComplete.getName().isEmpty()){

                    Toast.makeText(getActivity(), "You must fill a name.", Toast.LENGTH_SHORT)
                            .show();
                    return false;

                }


        } else if (nameEditText.getText().length() < 3) {
            Toast.makeText(getActivity(), "The name must be at least 3 characters",
                    Toast.LENGTH_SHORT).show();
            return false;

        } else {
            // if text view has some value in it and it is bigger than 3 chars...
            userComplete.setName(nameEditText.getText().toString());
        }
        // at this point, if we din't set any value for name, it is because it is already set by the
        // user before ad he din't made any changes




        // About user description
        if (userDescription.getText() == null || userDescription.getText().toString().isEmpty() ) {
            Log.i(LOG_TAG, "Text is null or empty, so we set nothing ");
        }else{
            Log.i(LOG_TAG, "Text is NOT null or empty, setting from text: " +
                    userDescription.getText().toString());
            userComplete.setUserDescription(userDescription.getText().toString());
        }

        // About user picture URL
        if (downloadUrl != null){
            userComplete.setProfilePicture(downloadUrl.toString());

        }


        // About Language
        Log.i(LOG_TAG, "getLearningLanguage: " + userComplete.getLearningLanguage() );
        if(userComplete.getLearningLanguage() == null || userComplete.getLearningLanguage().isEmpty()){
            Toast.makeText(getActivity(), getString(R.string.error_must_choose_language_of_interest),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        Log.i(LOG_TAG, "getFluentLanguage: " + userComplete.getFluentLanguage() );
        if(userComplete.getFluentLanguage() == null || userComplete.getFluentLanguage().isEmpty()){
            Toast.makeText(getActivity(), getString(R.string.error_must_choose_fluent_language),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        //if fluent language and language of interest are different
        if (userComplete.getFluentLanguage().equals(userComplete.getLearningLanguage()
                )) {
            Toast.makeText(getContext(),
                    R.string.languages_must_be_different_error,
                    Toast.LENGTH_LONG)
                    .show();
            return false;

        }

        userComplete.setLearningCode(userComplete.getFluentLanguage()
                + "|"
                + userComplete.getLearningLanguage());


        return true;
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
                userComplete.setFluentLanguage(systemValue);
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
        builder.setTitle(R.string.select_language_of_interest);
        builder.setItems(entriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]

                String systemValue = valuesArray[which];
                userComplete.setLearningLanguage(systemValue);
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
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
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
        if(userComplete == null){
            userComplete = Utility.getUser(getActivity());
        }
        String spinnerValue = userComplete.getFluentLanguage();
        Log.i(LOG_TAG,"Fluent Langauge: " + spinnerValue);







        spinnerValue = userComplete.getLearningLanguage();
        Log.i(LOG_TAG,"Learning Langauge: " + spinnerValue);


        if(BUNDLE_VALUE_FIRST_TIME_ENABLED){
            nameEditText.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        }else {
            nameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText editText = (EditText) v;
                    if(hasFocus){
                        editText.setHint(userComplete.getName());

                    }else{
                        editText.setHint("");

                    }
                }
            });
        }

        ageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText editText = (EditText) v;
                if(hasFocus){
                    editText.setHint(userComplete.getAge());

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
                    editText.setHint(userComplete.getUserDescription());

                }else{
                    editText.setHint("");

                }
            }
        });

        Log.i(LOG_TAG,"Age: " + userComplete.getAge());



        if(userComplete.getProfilePicture() != null){

            showUserPhoto(userComplete.getProfilePicture());
        }else if (BUNDLE_VALUE_FIRST_TIME_ENABLED){
            String pictureUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
            userComplete.setProfilePicture(pictureUrl);
            showUserPhoto(pictureUrl);
        }

        if(userComplete.getFluentLanguage() != null){
            profileFluentLanguageImageView.setImageResource(Utility.getDrawableUriForLanguage( userComplete.getFluentLanguage(),getActivity()));
            profileFluentLanguageTextView.setText(userComplete.getFluentLanguage());
            fluentLanguageBiggerPictureImageView.setImageResource(
                    Utility.getFluentLangagueBiggerPictureUri(getActivity(),
                            userComplete.getFluentLanguage()));
        }



        Log.i(LOG_TAG,"Age: " + userComplete.getLearningLanguage());

        if (userComplete.getLearningLanguage() != null){
            profileLanguageOfInterestImageView.setImageResource(Utility.getDrawableUriForLanguage( userComplete.getLearningLanguage(),getActivity()));
            profileLanguageOfInterestTextView.setText(userComplete.getLearningLanguage());

        }else{

        }

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//        Log.i(LOG_TAG,"onOffSetChanged: ");
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();
        if (mMaxScrollSize == 0 ){
            return;
        }

        int percentage = (Math.abs(verticalOffset)) * 100 / mMaxScrollSize;
//        Log.i(LOG_TAG,"mMaxScrollSize: " + mMaxScrollSize);
//        Log.i(LOG_TAG,"percentage of Scrolling: " + percentage);


        if (percentage >= TOTAL_SCROLLOING_PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

            profilePictureContainer.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(200)
                    .start();
        }

        if (percentage <= TOTAL_SCROLLOING_PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            profilePictureContainer
                    .animate()
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

            progressBar.show();
            //if onClickSync active, disable list clicking


        } else {
            progressBar.hide();
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








}
