package com.example.ferreteriacontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private DocumentReference usersRef;
    private String userRole;
    int[] rubrosId = new int[10];
    TextView scr1;
    TableLayout t1;
    TableRow tr;
    LinearLayout l1;
    ImageView img1;
    String userId;
    private boolean authorizeUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t1 = (TableLayout) findViewById(R.id.t1);
        t1.setColumnStretchable(0, true);
        t1.setColumnStretchable(1, true);
        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        usersRef = mStore.document("usuarios/" + userId); //get reference to the current user's document

        String[] rubros = {
                "Cerrajería",
                "Electricidad e iluminación",
                "Seguridad industrial y automotriz",
                "Plomería y griferías",
                "Alambres, clavos y tornillos",
                "Herramientas agrícolas y jardín",
                "Impermeabilizantes y estructuras",
                "Baños y cocinas",
                "Misceláneos",
                "Herramientas manuales y eléctricas"
        };

        int[] images = new int[]{R.drawable.cat3, R.drawable.cat3};
        authorizeUser = getUserRole();
        goView(rubros, images);

    }

    //add admin icon to top bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(authorizeUser) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
            return true;
        }
          return  false; //check if it works

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                //intent
                Intent intent = new Intent(getApplicationContext(), AdminControl.class);
                intent.putExtra("userRole", userRole);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //get role of current user
    private boolean getUserRole () {
        final boolean[] test = {false};
        usersRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //restrict user access based on its role
                            String role = documentSnapshot.getString("rol");
                            userRole = role;
                            Toast.makeText(MainActivity.this, "Tu role es de nivel " + role, Toast.LENGTH_SHORT).show();
                            if(role == "1"){
                              test[0] = true;
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    //handle exception properly
                        // 1- wrong password or inexistent
                    }
                });

        return test[0];

    }

    //set table with product categories
    public void goView(String[] rubros, int[] images) {
        int en = 0, color = 0xFFFFFFFF;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, //width
                LinearLayout.LayoutParams.MATCH_PARENT //height
        );
        //Creation tr
        tr = new TableRow(this);
        tr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));

        //Create two columns per linear layout
        for (int j = 0; j < 10; j++) {
            //choose background color for row column
            color = (color == 0xFFFFFFFF && en != 0) ? 0xD2ECECEC : (color == 0xD2ECECEC && en != 0) ? 0xFFFFFFFF : color;
            //Creation
            l1 = new LinearLayout(this);
            img1 = new ImageView(this);
            scr1 = new TextView(this);

            // Linear Layout
            rubrosId[j] = l1.generateViewId(); //generate view id and save it to array
            l1.setId(rubrosId[j]);
            l1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 5f));
            l1.setOrientation(LinearLayout.VERTICAL);
            l1.setPadding(5, 0, 5, 10);
            l1.setBackgroundColor(color);
            l1.setGravity(Gravity.CENTER);
            l1.setOnClickListener(this);

            //imageView1
            img1.setImageResource(images[0]);
            img1.setLayoutParams(new LinearLayout.LayoutParams(350, 350));

            //textView 1
            scr1.setLayoutParams(params);
            scr1.setText(rubros[j]);
            scr1.setTextSize(12);
            scr1.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            scr1.setTextColor(0xF0000000);
            scr1.setPadding(20, 5, 20, 5);
            scr1.setGravity(Gravity.CENTER);

            //Adding views to linear layout and table row
            l1.addView(img1);
            l1.addView(scr1);
            tr.addView(l1);


            //Create new table row after adding two columns
            en++;
            if (en == 2) {
                en = 0;
                t1.addView(tr);
                tr = new TableRow(this); //new TableRow
                tr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));

            }
        }
    }

    //get position of the view/product category to send it as an indent to the Product activity
    @Override
    public void onClick(View v) {
        int id = v.getId();
        int position = -1;

        for (int i = 0; i < rubrosId.length; i++) {
            //if the id of the view it's in the rubros array, save position
            if (id == rubrosId[i]) {
                Toast.makeText(this, "hello " + id + " " + rubrosId[i], Toast.LENGTH_SHORT).show();
                position = i + 1;
                break;
                //this is a comment
            }

        }
        //send product category identifier to Product activity
        Intent intent = new Intent(getApplicationContext(), Products.class);
        intent.putExtra("rubro", position);
        intent.putExtra("userRole", userRole);
        startActivity(intent);
    }

}
