package com.speko.android;

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
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
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

    @BindView(R.id.signup_spinner_input_age_fluent_language)
    AppCompatSpinner spinner_fluent_language;

    @BindView(R.id.signup_spinner_input_age_language_of_interest)
    AppCompatSpinner spinner_learning_language;

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

    @BindView(R.id.fluent_languge_imageview)
    ImageView fluentLanguageImageView;





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
    public void sync(View v) {
        SpekoSyncAdapter.syncImmediatly(getActivity());
        setRefreshScreen(true);
//        getLoaderManager().restartLoader(FRIENDS_LOADER,null, this);
    }

    @OnClick(R.id.log_out)
    public void logOut(View v) {

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
    public void changeProfile(View v){
        // if fluent language and language of interest are equal
        if(spinner_fluent_language.getSelectedItem().toString()
                .equals(
                        spinner_learning_language.getSelectedItem().toString()
                )){
            Toast.makeText(getContext(),
                    R.string.languages_must_be_different_error,
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        String ageString = ageEditText.getText().toString();

        if (!Utility.isValidAge(ageString)){
            Toast.makeText(getContext(), R.string.age_not_acceptable_error, Toast.LENGTH_LONG)
                    .show();
            return;
        }


        User user = Utility.getUser(getContext());
        if (ageEditText.getText() == null || ageEditText.getText().toString().isEmpty()) {
            Log.i(LOG_TAG, "Text is null or empty, so we set nothing");
        }else {
            Log.i(LOG_TAG, "Text is NOT null or empty, setting from text: " +
                    ageEditText.getText().toString());
            user.setAge(ageEditText.getText().toString());

        }
        user.setFluentLanguage(spinner_fluent_language.getSelectedItem().toString());
        user.setLearningLanguage(spinner_learning_language.getSelectedItem().toString());

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
    public void uploadPicture(View v){
       Utility.call_to_upload_picture(this);


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
                            showPhoto(downloadUrl.toString());


                        }
                    });
        }

    }

    private void showPhoto(String downloadUrl) {
        Picasso.with(getContext()).load(downloadUrl.toString())
                .placeholder(R.drawable.ic_placeholder_profile_photo)
                .resize(profilePicture.getWidth(),profilePicture.getHeight())
                .centerCrop().into(profilePicture);
    }

    private void setView() {

        Log.i(LOG_TAG,"setView");
        User user = Utility.getUser(getActivity());
        String spinnerValue = user.getFluentLanguage();
        Log.i(LOG_TAG,"Fluent Langauge: " + spinnerValue);

        spinner_fluent_language.setSelection(
                ((ArrayAdapter)spinner_fluent_language.getAdapter()).getPosition(
                        spinnerValue
                )
        );

        fluentLanguageImageView.setImageResource(
                Utility.getFluentLangagueBiggerPictureUri(getActivity(), user.getFluentLanguage()));


        spinnerValue =user.getLearningLanguage();
        Log.i(LOG_TAG,"Learning Langauge: " + spinnerValue);

        spinner_learning_language.setSelection(
                ((ArrayAdapter) spinner_learning_language.getAdapter()).getPosition(
                        spinnerValue
                )
        );


        ageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText editText = (EditText) v;
                if(hasFocus){
                    editText.setHint(Utility.getUser(getContext()).getAge());

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
                    editText.setHint(Utility.getUser(getContext()).getUserDescription());

                }else{
                    editText.setHint("");

                }
            }
        });

        Log.i(LOG_TAG,"Age: " + Utility.getUser(getContext()).getAge());

        if(Utility.getUser(getActivity()).getProfilePicture() != null){

            showPhoto(Utility.getUser(getActivity()).getProfilePicture());
        }


    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        Log.i(LOG_TAG,"onOffSetChanged");
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();
        if (mMaxScrollSize == 0 ){
            return;
        }
        int percentage = (Math.abs(verticalOffset)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

            profilePicture.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(200)
                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
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
            //if sync active, disable list clicking


        } else {
            progressBar.hide();
            gridLayout.setVisibility(View.VISIBLE);
            //if sync NOT active, enable list clicking

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(LOG_TAG, "Shared Preferences changed: ");
        if (key.equals(getString(R.string.shared_preference_sync_status_key))) {
            Log.d(LOG_TAG, "Case sync-status");
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
