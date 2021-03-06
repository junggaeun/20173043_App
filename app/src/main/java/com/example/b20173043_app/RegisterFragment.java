package com.example.b20173043_app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.b20173043_app.model.DMUser;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText nameEditText;
    private EditText phoneEditText;
    private ProgressDialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MaterialToolbar) view.findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> {
            getActivity().onBackPressed();
        });

        emailEditText = view.findViewById(R.id.emailText);
        passwordEditText = view.findViewById(R.id.passwordText);
        nameEditText = view.findViewById(R.id.nameText);
        phoneEditText = view.findViewById(R.id.phoneText);
        view.findViewById(R.id.signUpButton).setOnClickListener(v -> {
            signUp();
        });
    }

    private void signUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        boolean validation = true;

        if (email.isEmpty()) {
            emailEditText.setError("????????? ????????? ????????? ?????????.");
            validation = false;
        } else {
            emailEditText.setError(null);
        }

        if (password.isEmpty()) {
            passwordEditText.setError("????????? ????????? ????????? ?????????.");
            validation = false;
        } else {
            passwordEditText.setError(null);
        }

        if (name.isEmpty()) {
            nameEditText.setError("????????? ????????? ?????????.");
            validation = false;
        } else {
            nameEditText.setError(null);
        }

        if (phone.isEmpty()) {
            phoneEditText.setError("??????????????? ????????? ?????????.");
            validation = false;
        } else {
            phoneEditText.setError(null);
        }

        if (!validation) return;

        DMUser user = new DMUser(email, name, phone);

        progressDialog = ProgressDialog.show(getContext(), null, "???????????? ???... ????????? ????????? ?????????.", true, false);

        auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (task.getException() != null) {
                        throw task.getException();
                    }

                    String uid = task.getResult().getUser().getUid();
                    return db.collection(Constants.USERS).document(uid).set(user);
                })
                .addOnCompleteListener(task -> {
                    Exception exception = task.getException();

                    if (exception != null) {
                        progressDialog.dismiss();
                        progressDialog = null;

                        if (exception instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(getContext(), "??????????????? ?????? ????????????. ?????? ????????? ?????????.", Toast.LENGTH_SHORT).show();

                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(getContext(), "????????? ????????? ???????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();

                        } else if (exception instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(getContext(), String.format("%s ????????? ????????? ????????? ????????? ????????????. ?????? ????????? ????????? ????????? ?????????.", email), Toast.LENGTH_SHORT).show();

                        } else {
                            exception.printStackTrace();
                            Toast.makeText(getContext(), "????????? ?????????????????????. ?????? ??? ?????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                        }

                        if (auth.getCurrentUser() != null) {
                            auth.getCurrentUser().delete();
                        }
                    } else {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).startMovieActivity(true);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        super.onDestroy();
    }
}