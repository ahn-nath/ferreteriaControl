package com.example.ferreteriacontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    EditText emailUser, passwordUser;
    String userId;
    DialogBox loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_02);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        emailUser = findViewById(R.id.emailUser);
        passwordUser = findViewById(R.id.passwordUser);
        loadingDialog = new DialogBox(SignUp.this);
    }

    //check if already existing/logged in user
    @Override
    public void onStart() {
        super.onStart();
        //if there's a user logged in redirect
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), waitingRoom.class));
            finish();
        }
    }


    public void registerUser(View v) {
        final String email = emailUser.getText().toString().trim();
        String password = passwordUser.getText().toString().trim();

        //validation
        if (TextUtils.isEmpty(email)) {
            emailUser.setError("Email is Required.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordUser.setError("Password is Required.");
            return;
        }

        if (password.length() < 6) {
            passwordUser.setError("Password Must be >= 6 Characters");
            return;
        }


        loadingDialog.loginLoadDialog();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull final Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //send verification email link
                    loadingDialog.dismissDialog();

                    FirebaseUser fuser = mAuth.getCurrentUser();
                    Objects.requireNonNull(fuser).sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(SignUp.this, "Se ha enviado un email a su correo para verificar la cuenta", Toast.LENGTH_SHORT).show();
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUp.this, "No se ha podido enviar el correo de verificación", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //Create user and add custom fields to database
                    //Get reference to the collection in database and add values
                    userId = mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = mStore.collection("usuarios").document(userId);
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("rol", "3");
                    user.put("access", 0);

                    documentReference.set(user)

                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignUp.this, "Datos agregados de forma exitosa", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), waitingRoom.class));

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUp.this, "Hubo un inconveniente con la base de datos" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });


                } else {
                    Toast.makeText(SignUp.this, "No se pudo registrar su cuenta " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismissDialog();
                }
            }
        });


    }

    public void goLogin(View view) {
        startActivity(new Intent(getApplicationContext(), SignIn.class));
    }


}
