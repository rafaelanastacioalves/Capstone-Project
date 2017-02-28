package com.speko.android;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
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
public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int USER_LOADER = 2;
    private final String LOG_TAG = getClass().getSimpleName();


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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this,view);

        SpekoSyncAdapter.syncImmediatly(getContext());



        return view;
    }

    @OnClick(R.id.fragment_button_profile_change)
    public void changeProfile(View v){

        User user = Utility.getUser(getContext());
        user.setAge(ageEditText.getText().toString());
        user.setFluentLanguage(spinner_fluent_language.getSelectedItem().toString());
        user.setLearningLanguage(spinner_learning_language.getSelectedItem().toString());

        if (userDescription.getText() == null || userDescription.getText().toString().isEmpty() ) {
            Log.i(LOG_TAG, "Text is null or empty, setting from hint: " +
                    userDescription.getHint().toString());
            user.setUserDescription(userDescription.getHint().toString());

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

    @OnClick(R.id.signup_upload_picture)
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

        String spinnerValue = Utility.getUser(getActivity()).getFluentLanguage();
        Log.i(LOG_TAG,"Fluent Langauge: " + spinnerValue);

        spinner_fluent_language.setSelection(
                ((ArrayAdapter)spinner_fluent_language.getAdapter()).getPosition(
                        spinnerValue
                )
        );


        spinnerValue = Utility.getUser(getActivity()).getLearningLanguage();
        Log.i(LOG_TAG,"Learning Langauge: " + spinnerValue);

        spinner_learning_language.setSelection(
                ((ArrayAdapter) spinner_learning_language.getAdapter()).getPosition(
                        spinnerValue
                )
        );

        ageEditText.setText(Utility.getUser(getContext()).getAge());

        userDescription.setHint(Utility.getUser(getContext()).getUserDescription());
        Log.i(LOG_TAG,"Age: " + Utility.getUser(getContext()).getAge());

        if(Utility.getUser(getActivity()).getProfilePicture() != null){

            showPhoto(Utility.getUser(getActivity()).getProfilePicture());
        }


    }

    @Override
    public void onStart() {
        Log.i(LOG_TAG,"Initloader");
        getLoaderManager().initLoader(USER_LOADER, null, this);
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

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
