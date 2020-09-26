package com.example.ferreteriacontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class waitingRoom extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private DocumentReference usersRef;
    String userId;
    TextView accessText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        accessText = findViewById(R.id.accessText);

        //check if user has access
        SharedPreferences mainInfo = getApplicationContext().getSharedPreferences("MainInfo", MODE_PRIVATE);
        int hasAccess = mainInfo.getInt("hasAccess", 0);

        //if the status is equal to 1 [approved] send to Main Activity
        if(hasAccess == 1){
            Log.d("AShared", "the user has access");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        //if the status is equal to 2 [declined] set message
        else if(hasAccess == 2){
            Log.d("AShared", "the user doesn't have access'");
            accessText.setText("Su solicitud de acceso ha sido denegada por el administrador");
        }else{
            Log.d("AShared", "null");
        //if there's no status, verify status of the user with database's value
            verifyUser();
        }
    }

    //check if already existing/logged in user
    @Override
    public void onStart() {
        super.onStart();
        //if there's a user logged in redirect
        if(mAuth.getCurrentUser() == null){
            Log.d("Error", "no está creado");
            startActivity(new Intent(getApplicationContext(), SignIn.class));
            finish();
        }
    }



    public void verifyUser(){
        Toast.makeText(getApplicationContext(), "Verificando nuevas actualizaciones...", Toast.LENGTH_SHORT).show();
        //Store user's role
        userId = mAuth.getCurrentUser().getUid();
        usersRef = mStore.document("usuarios/" + userId);//get reference to the current user's document
        usersRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            int access =  Integer.parseInt(documentSnapshot.get("access").toString());
                            if(access == 1){
                             //approved
                               Log.d("AFirebase", "approved");
                               //save to share preferences for further access to this activity
                               SharedPreferences sharedPreferences = getSharedPreferences("MainInfo", MODE_PRIVATE);
                               SharedPreferences.Editor editor = sharedPreferences.edit();
                               editor.putInt("hasAccess", 1);
                               editor.apply();
                               //send to new activity
                               startActivity(new Intent(getApplicationContext(), MainActivity.class));
                           }
                            if(access == 2){
                             //declined
                                Log.d("AFirebase", "declined");
                                SharedPreferences sharedPreferences = getSharedPreferences("MainInfo", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("hasAccess", 2);
                                editor.apply();

                                accessText.setText("Su solicitud de acceso ha sido denegada por el administrador");
                            }
                            Toast.makeText(getApplicationContext(), "Último estatus recibido", Toast.LENGTH_SHORT).show();

                        }    }
                });
    }
}
