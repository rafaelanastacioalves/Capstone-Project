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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.speko.android.data.UserComplete;
import com.speko.android.sync.SpekoSyncAdapter;
import com.squareup.picasso.Callback;
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
 */
public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        AppBarLayout.OnOffsetChangedListener,
        UpdateFragmentStatus {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final int USER_LOADER = 2;
    private final String LOG_TAG = getClass().getSimpleName();

    private int mMaxScrollSize;
    @SuppressWarnings("FieldCanBeLocal")
    private final int TOTAL_SCROLLED_PERCENTAGE_TO_ANIMATE_AVATAR = 55;

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


    @BindView(R.id.signup_edittext_input_age)
    EditText ageEditText;

    @BindView(R.id.signup_edittext_input_name)
    EditText nameEditText;

    @Nullable
    @BindView(R.id.fragment_button_profile_change)
    AppCompatButton signupButton;

    @BindView(R.id.signup_user_description)
    AppCompatEditText userDescription;

    @BindView(R.id.profile_fragment_imageview_profile_picture)
    CircleImageView profilePicture;

    @BindView(R.id.signup_imageview_profile_picture_container)
    ShimmerFrameLayout profilePictureContainer;

    private Uri downloadUrl = null;

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

    @Override
    public void setLoading(Boolean isLoading) {
        updateScreenState();
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void completeSignup(UserComplete userComplete);
        void signOut();
    }



    public ProfileFragment() {
        // Required empty public constructor
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
        setup(view);
        ButterKnife.bind(this,view);


//        SpekoSyncAdapter.syncImmediatly(getContext());




        return view;
    }

    private void setup(View view) {

        if(BUNDLE_VALUE_FIRST_TIME_ENABLED){
            Log.i(LOG_TAG, "First Time Enabled TRUE!");
            //noinspection ConstantConditions
            profileOptionsContainerViewStub.setLayoutResource(R.layout.profile_options_signup);
            profileOptionsContainerViewStub.inflate();

        }else{

            Log.i(LOG_TAG, "First Time Enabled FALSE!");
            //noinspection ConstantConditions
            profileOptionsContainerViewStub.setLayoutResource(R.layout.profile_options_edit);
            profileOptionsContainerViewStub.inflate();
            signupButton = (AppCompatButton) view.findViewById(R.id.fragment_button_profile_change);

        }

        //noinspection StatementWithEmptyBody
        if(BUNDLE_VALUE_IS_SYNCABLE){
//            Log.i(LOG_TAG, "Syncable TRUE!");
//            sync_button = (Button)  view.findViewById(R.id.sync_button);
//            sync_button.setVisibility(View.VISIBLE);

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        appBarLayout.addOnOffsetChangedListener(this);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        updateScreenState();


    }

    @OnClick(R.id.sync_button)
    public void onClickSync() {
        SpekoSyncAdapter.syncImmediatly(getActivity());
//        getLoaderManager().restartLoader(FRIENDS_LOADER,null, this);
    }


    @OnClick(R.id.log_out) @Optional
    public void onClicklogOut() {
        mListener.signOut();

    }


    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void updateScreenState() {

        if(BUNDLE_VALUE_IS_SYNCABLE){
            // if is syncing or off line
            if (SpekoSyncAdapter.isSyncActive(getContext()) ||
                    !Utility.getIsConnectedStatus(getContext())){
                //noinspection ConstantConditions
                signupButton.setClickable(false);
            }else{
                //noinspection ConstantConditions
                signupButton.setClickable(true);
            }
        }


    }

    @OnClick(R.id.fragment_button_profile_register) @Optional
    public void onClickRegisterUser(){
        if (populateAndValidateUserObjectCorrectly()){
            mListener.completeSignup(userComplete);
        }


    }

    @OnClick(R.id.fragment_button_profile_change) @Optional
    public void onClickChangeProfile(){

        if(populateAndValidateUserObjectCorrectly()){
            final Context applicationContext = getActivity().getApplication();
            OnCompleteListener onCompleteListener = new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    SpekoSyncAdapter.syncImmediatly(applicationContext);
                }
            };
            Utility.setUserIntoFirebase(userComplete,getActivity(), onCompleteListener);


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
                if (userComplete.getName() == null || userComplete.getName().isEmpty()){

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

    @OnClick(R.id.signup_imageview_profile_picture_container)
    public void onClickUploadPicture(){
       Utility.call_to_upload_picture(this);
    }

    @OnClick(R.id.profile_fluent_language_container)
    public void onClickChangeFluentLanguage(){

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
    public void onClickChangeLanguageOfInterest(){

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
            @SuppressWarnings("ConstantConditions") StorageReference photoRef = FirebaseStorage.getInstance().getReference()
                    .child(getString(R.string.user_pictures))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(selectedImageUri.getLastPathSegment());

            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressWarnings("VisibleForTests")
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.i(LOG_TAG, "Result OK from Firebase persistance");

                            // When the image has successfully uploaded, we get its download URL
                            downloadUrl = taskSnapshot.getDownloadUrl();
                            //noinspection ConstantConditions
                            showUserPhoto(downloadUrl.toString());


                        }
                    });
        }

    }

    private void showUserPhoto(String downloadUrl) {
        profilePictureContainer.startShimmerAnimation();

        Picasso.with(getContext()).load(downloadUrl)
                .placeholder(R.drawable.ic_user)
                .resize(getResources().getDimensionPixelSize(R.dimen.profile_user_picture_dimen),
                        getResources().getDimensionPixelSize(R.dimen.profile_user_picture_dimen))
                .centerCrop().into(profilePicture, new Callback() {
            @Override
            public void onSuccess() {
                profilePictureContainer.stopShimmerAnimation();
            }

            @Override
            public void onError() {

            }
        });
    }

    private void setView() {

        Log.i(LOG_TAG,"setView");
        if(userComplete == null){
            userComplete = Utility.getUser(getActivity());

            // if as ultimate case it is null (first time user)
            if (userComplete == null){
                userComplete = new UserComplete();
            }
        }


        if(BUNDLE_VALUE_FIRST_TIME_ENABLED){
            //noinspection ConstantConditions
            nameEditText.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        }else{
            nameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText editText = (EditText) v;
                    if(hasFocus){
                        editText.setHint(userComplete.getName());
                        nameEditText.setContentDescription(
                                getString(R.string.a11y_profile_name_content_description,
                                        userComplete.getName()));

                    }else{

                        editText.setHint("");
                        nameEditText.setContentDescription(
                                getString(R.string.a11y_profile_name_content_description,
                                        getString(R.string.profile_empty_content_description)));

                    }
                }
            });


            ageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText editText = (EditText) v;
                    if(hasFocus){
                        editText.setHint(userComplete.getAge());
                        ageEditText.setContentDescription(
                                getString(R.string.a11y_profile_age_content_description,
                                        userComplete.getAge()));

                    }else{
                        editText.setHint("");
                        ageEditText.setContentDescription(
                                getString(R.string.a11y_profile_age_content_description,
                                        getString(R.string.profile_empty_content_description))
                        );

                    }
                }
            });

            userDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText editText = (EditText) v;
                    if(hasFocus){
                        editText.setHint(userComplete.getUserDescription());
                        userDescription.setContentDescription(
                                getString(R.string.a11y_profile_user_description_content_description,
                                        userComplete.getUserDescription()));

                    }else{
                        editText.setHint("");
                        userDescription.setContentDescription(
                                getString(R.string.a11y_profile_user_description_content_description,
                                        getString(R.string.profile_empty_content_description)));

                    }
                }
            });

            Log.i(LOG_TAG,"Age: " + userComplete.getAge());


        }




        if(userComplete.getProfilePicture() != null){

            showUserPhoto(userComplete.getProfilePicture());
        }else if (BUNDLE_VALUE_FIRST_TIME_ENABLED){
            @SuppressWarnings("ConstantConditions") String pictureUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
            userComplete.setProfilePicture(pictureUrl);
            showUserPhoto(pictureUrl);
        }

        if(userComplete.getFluentLanguage() != null){
            profileFluentLanguageImageView.setImageResource(Utility.getDrawableUriForLanguage( userComplete.getFluentLanguage(),getActivity()));
            profileFluentLanguageContainer.setContentDescription(
                    getString(R.string.profile_fluent_language_content_descritption,
                            Utility.getCompleteLanguageNameString(userComplete.getFluentLanguage(),getActivity())
            ));
            profileFluentLanguageTextView.setText(userComplete.getFluentLanguage());
            fluentLanguageBiggerPictureImageView.setImageResource(
                    Utility.getFluentLangagueBiggerPictureUri(getActivity(),
                            userComplete.getFluentLanguage()));
        }else {
            profileFluentLanguageContainer.setContentDescription(
                    getString(R.string.profile_fluent_language_content_descritption,
                            getString(R.string.profile_empty_content_description))
                    );
        }



        Log.i(LOG_TAG,"Age: " + userComplete.getLearningLanguage());

        if (userComplete.getLearningLanguage() != null){
            profileLanguageOfInterestImageView.setImageResource(Utility.getDrawableUriForLanguage( userComplete.getLearningLanguage(),getActivity()));
            profileLanguageOfInterestTextView.setText(userComplete.getLearningLanguage());
            profileLanguageOfInterestContainer.setContentDescription(
                    getString(R.string.profile_language_of_interest_content_descritption,
                            Utility.getCompleteLanguageNameString(userComplete.getLearningLanguage(),getActivity())
                    ));

        }else{
            profileLanguageOfInterestContainer.setContentDescription(
                    getString(R.string.profile_fluent_language_content_descritption,
                            getString(R.string.profile_empty_content_description))
                    );

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


        if (percentage >= TOTAL_SCROLLED_PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

            profilePictureContainer.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(200)
                    .start();
            profilePictureContainer.setEnabled(false);
            profilePictureContainer.setClickable(false);
            Log.i(LOG_TAG, "Hiding Animation Started");
        }

        if (percentage <= TOTAL_SCROLLED_PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            profilePictureContainer
                    .animate()
                    .scaleY(1).scaleX(1)
                    .start();
            profilePictureContainer.setEnabled(true);
            profilePictureContainer.setClickable(true);
            Log.i(LOG_TAG, "Showing Animation Started");


        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG_TAG,"onStart");
        if(userComplete == null){
            userComplete = Utility.getUser(getActivity());
        }
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
            userComplete = Utility.getUser(getActivity());
            // we only stop the loading framework after we had the minimnum acceptable user info to show
            if (userComplete != null && userComplete.getName() != null && userComplete.getId()!=null ){
                //we destroy the loader, as there is no need for updating the view
                getLoaderManager().destroyLoader(USER_LOADER);
                // now we show and allow edition
                setView();

            }

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
