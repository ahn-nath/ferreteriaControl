package com.example.ferreteriacontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    int[] rubrosId = new int[10];
    TextView scr1;
    TableLayout t1;
    TableRow tr;
    LinearLayout l1;
    ImageView img1;
    private Handler mainHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t1 = findViewById(R.id.t1);
        t1.setColumnStretchable(0, true);
        t1.setColumnStretchable(1, true);

        //Values to use for the product's categories table layout
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
        //connect with actual images
        int[] images = new int[]{R.drawable.cat3, R.drawable.cat3};
        goView(rubros, images);
        getCurrentDolarPrice();

    }

    //Top nav bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), AdminControl.class));
                return true;

            case R.id.contact:
                startActivity(new Intent(getApplicationContext(), ContactUs.class));
                return true;

            case R.id.logOut:
                mAuth.signOut();
                //remove hasAccess value to verify again in case is a different user when logged in
                SharedPreferences sharedPreferences = getSharedPreferences("MainInfo", MODE_PRIVATE);
                sharedPreferences.edit().remove("hasAccess").apply();

                startActivity(new Intent(getApplicationContext(), SignIn.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //set TableView with product categories
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
            rubrosId[j] = View.generateViewId(); //generate view id and save it to array
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
            }

        }
        //send product category identifier to Product activity
        Intent intent = new Intent(getApplicationContext(), AdminControl.class);
        intent.putExtra("rubro", position);
        startActivity(intent);
    }

    //get value from webpage (current dollar price)
    public void getCurrentDolarPrice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String CURRENT_PRICE, MSG;
                final boolean IS_NUMERIC;
                Document document;
                String send = null, message;

                try {
                    document = Jsoup.connect("https://monitordolarvenezuela.com/").get();
                    final String dolarData = document.select("div.back-white-tabla h6.text-center").text();
                    send = dolarData.replaceAll("[^\\d.]+|\\.(?!\\d)", "");
                    message = "Precio del dólar: ";

                } catch (UnknownHostException e) {
                    message = "No se pudo obtener la información del URL designado";
                    Log.d("Error:", e.toString());
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();
                    message = e.toString();
                }

                //set values to final variables
                CURRENT_PRICE = send;
                IS_NUMERIC = isNumeric(CURRENT_PRICE); //validate that the value could be converted to number
                MSG = message; //message for each case [successful, failed]

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //if the value [string] is not null and could be converted to number
                        if (CURRENT_PRICE != null && (IS_NUMERIC)) {
                            //sharedPreferences
                            Toast.makeText(getApplicationContext(), MSG + CURRENT_PRICE + IS_NUMERIC, Toast.LENGTH_LONG).show();
                            SharedPreferences sharedPreferences = getSharedPreferences("MainInfo", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("dollarPrice", CURRENT_PRICE);
                            editor.apply();
                        } else {
                            Toast.makeText(getApplicationContext(), "Precio del dólar: " + MSG, Toast.LENGTH_LONG).show();
                        }

                    }
                });

            }
        }).start();
    }

    //check if string could be converted to number
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);

        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


}


