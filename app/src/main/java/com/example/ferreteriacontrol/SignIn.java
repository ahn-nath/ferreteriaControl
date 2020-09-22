package com.example.ferreteriacontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignIn extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private DocumentReference usersRef;
    String userId;
    EditText emailLogin, passwordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_01);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
    }

    //check if already existing/logged in user
    @Override
    public void onStart() {
        super.onStart();
        //if there's a user logged in redirect
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), waitingRoom.class));
            finish();
        }
    }

    public void Login(View v) {
        String email = emailLogin.getText().toString().trim();
        String password = passwordLogin.getText().toString().trim();

        //Validate values
        if (TextUtils.isEmpty(email)) {
            emailLogin.setError("Email is Required.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLogin.setError("Password is Required.");
            return;
        }

        if (password.length() < 6) {
            passwordLogin.setError("Password Must be >= 6 Characters");
            return;
        }

        //progressBar.setVisibility(View.VISIBLE);

        // Authenticate user
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(SignIn.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                    //Store user's role
                    userId = mAuth.getCurrentUser().getUid();
                    usersRef = mStore.document("usuarios/" + userId);//get reference to the current user's document
                    usersRef.get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        int role =  Integer.parseInt(documentSnapshot.get("rol").toString());
                                        boolean isAdmin = (role == 1);
                                        //save role and boolean value to SharedPreferences
                                        SharedPreferences sharedPreferences = getSharedPreferences("MainInfo", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putInt("role", role);
                                        editor.putBoolean("isAdmin", isAdmin);
                                        editor.apply();

                                    }    }
                            });
                   //go to Main activity
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                }

                else {
                    Toast.makeText(SignIn.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    //progressBar.setVisibility(View.GONE);
                }
            }


        });
    }

    public void goRegister(View view) {
        startActivity(new Intent(getApplicationContext(), SignUp.class));
    }
}
