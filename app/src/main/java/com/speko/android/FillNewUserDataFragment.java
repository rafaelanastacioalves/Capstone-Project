package com.speko.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.speko.android.data.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.speko.android.Utility.RC_PHOTO_PICKER;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FillNewUserDataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class FillNewUserDataFragment extends Fragment {

    @BindView(R.id.signup_textview_input_age)
    TextView age;

    @BindView(R.id.signup_spinner_input_age_fluent_language)
    AppCompatSpinner spinner_fluent_language;

    @BindView(R.id.signup_spinner_input_age_language_of_interest)
    AppCompatSpinner spinner_language_of_interest;

    @BindView(R.id.signup_button)
    AppCompatButton signupButton;

    @BindView(R.id.signup_user_description)
    AppCompatEditText userDescription;

    @BindView(R.id.signup_imageview_profile_picture)
    AppCompatImageView imageView;


    private OnFragmentInteractionListener mListener;
    private Uri downloadUrl;

    public FillNewUserDataFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fill_new_user_data, container, false);
        ButterKnife.bind(this,view);
        return view;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    @OnClick(R.id.signup_button)
    public void signup(View view){
        User user = new User();
        user.setAge(age.getText().toString());
        user.setFluentLanguage(spinner_fluent_language.getSelectedItem().toString());
        user.setLearningLanguage(spinner_language_of_interest.getSelectedItem().toString());
        user.setUserDescription(userDescription.getText().toString());




        mListener.onFragmentInteraction(user);
    }

    @OnClick(R.id.signup_upload_picture)
    public void uploadPicture(View v){
        Utility.call_to_upload_picture(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
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
                            // When the image has successfully uploaded, we get its download URL
                            downloadUrl = taskSnapshot.getDownloadUrl();


                        }
                    });
        }
    }
}
