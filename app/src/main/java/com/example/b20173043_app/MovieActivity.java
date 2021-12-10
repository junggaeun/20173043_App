package com.example.b20173043_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.b20173043_app.model.DMMovie;
import com.example.b20173043_app.model.DMMovieList;
import com.example.b20173043_app.model.DMUser;
import com.example.b20173043_app.model.DMUserLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MovieActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, EventListener<DocumentSnapshot> {

    private FirebaseAuth auth;
    private boolean isAuthStateListenerAdded = false;

    private ListenerRegistration userListenerRegistration;

    private static RequestQueue requestQueue;
    private EditText editText;
    private TextView textView;
    private RecyclerView recyclerView;
    private MovieAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_movie);

        auth.addAuthStateListener(this);
        isAuthStateListenerAdded = true;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userListenerRegistration = db.collection(Constants.USERS)
                .document(auth.getUid())
                .addSnapshotListener(this);

        initUi();
    }

    private void initUi() {
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();
            }
        });

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MovieAdapter();
        recyclerView.setAdapter(adapter);

/* DB 에 저장된 사용자 정보를 읽는 방법
        DMUser user = DMUserLiveData.getInstance().getValue();
        if (user != null) {
            user.getEmail();
            user.getName();
            user.getPhoneNumber();
        }
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.actionSignOut) {
            auth.signOut();
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void makeRequest() {
        String url = editText.getText().toString();

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        println("응답 -> " + response);

                        processResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        println("에러 -> " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
        println("요청 보냄.");
    }

    public void println(String data) {
        Log.d("MainActivity", data);
    }

    public void processResponse(String response) {
        Gson gson = new Gson();
        DMMovieList movieList = gson.fromJson(response, DMMovieList.class);

        println("영화정보의 수 : " + movieList.boxOfficeResult.dailyBoxOfficeList.size());

        for (int i = 0; i < movieList.boxOfficeResult.dailyBoxOfficeList.size(); i++) {
            DMMovie movie = movieList.boxOfficeResult.dailyBoxOfficeList.get(i);

            adapter.addItem(movie);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
        if (error != null) {
            error.printStackTrace();
            return;
        }

        if (value.exists()) {
            DMUserLiveData.getInstance().setValue(value.toObject(DMUser.class));
        }
    }

    @Override
    protected void onDestroy() {
        if (isAuthStateListenerAdded) {
            auth.removeAuthStateListener(this);
        }

        if (userListenerRegistration != null) {
            userListenerRegistration.remove();
        }

        super.onDestroy();
    }
}