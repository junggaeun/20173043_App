package com.example.b20173043_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText emailEditText;
    private EditText passwordEditText;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startMovieActivity(false);
            return;
        }

        setContentView(R.layout.activity_main);

        initUi();
    }

    private void initUi() {
        emailEditText = findViewById(R.id.emailText);
        passwordEditText = findViewById(R.id.passwordText);

        findViewById(R.id.signInButton).setOnClickListener(v -> {
            signIn();
        });

        findViewById(R.id.registerButton).setOnClickListener(v -> {
            if (getSupportFragmentManager().findFragmentByTag("RegisterFragment") != null) return;

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new RegisterFragment(), "RegisterFragment")
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });
    }

    private void signIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        boolean validation = true;

        if (email.isEmpty()) {
            emailEditText.setError("이메일 주소를 입력해 주세요.");
            validation = false;
        } else {
            emailEditText.setError(null);
        }

        if (password.isEmpty()) {
            passwordEditText.setError("이메일 주소를 입력해 주세요.");
            validation = false;
        } else {
            passwordEditText.setError(null);
        }

        if (!validation) return;

        progressDialog = ProgressDialog.show(this, null, "로그인 중... 잠시만 기다려 주세요.", true, false);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            progressDialog = null;

            Exception exception = task.getException();

            if (exception != null) {
                if (exception instanceof FirebaseAuthInvalidUserException) {
                    Toast.makeText(this, String.format("%s 이메일 주소로 가입된 계정이 없습니다. 이메일 주소를 다시 확인해 주세요.", email), Toast.LENGTH_SHORT).show();

                } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this, "패스워드가 틀렸습니다. 다시 확인해 주세요.", Toast.LENGTH_SHORT).show();

                } else {
                    exception.printStackTrace();
                    Toast.makeText(this, "오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                }
            } else {
                startMovieActivity(true);
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

    public void startMovieActivity(boolean animate) {
        Intent intent = new Intent(this, MovieActivity.class);
        if (!animate) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }

        startActivity(intent);
        finish();

        if (!animate) {
            overridePendingTransition(0, 0);
        }
    }
}