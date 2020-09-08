package com.example.ferreteriacontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AdminControl extends AppCompatActivity {
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private CollectionReference productRef = mStore.collection("Productos");
    private DocumentReference docRef;
    private ProductAdapter adapter;
    private Query queryReference;
    int hello;
    private boolean authorizeUser;
    EditText searchText;
    FloatingActionButton buttonAddProduct;
    FloatingActionButton buttonImport;
    //import products
    private List<Product> CSVSamples = new ArrayList<>();
    private static final int OPEN_REQUEST_CODE = 41;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        searchText = findViewById(R.id.searchText);
        buttonAddProduct = findViewById(R.id.button_add_product);
        buttonImport = findViewById(R.id.button_import);

        //get user's role and position
        SharedPreferences sharedPreferences = getSharedPreferences("MainInfo", MODE_PRIVATE);
        authorizeUser = sharedPreferences.getBoolean("isAdmin", false);
        hello = sharedPreferences.getInt("role", 0);

        if(authorizeUser){
            //Make add product button visible for this activity
            buttonImport.setVisibility(View.VISIBLE);
            buttonAddProduct.setVisibility(View.VISIBLE);
            buttonAddProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminControl.this, SingleProductAdmin.class));
                }
            });
        }

        Toast.makeText(getApplicationContext(), "data" + hello + authorizeUser, Toast.LENGTH_LONG).show();


        //get extra intent to determine product's filter
        Intent intent = getIntent();
        int rubro = intent.getIntExtra("rubro", -1);
        if(rubro == -1) {
            queryReference = productRef;
        } else {
            queryReference = productRef.whereEqualTo("group", rubro);;
        }

        setUpProductsView(queryReference);
        searchView();

        

    }

    private void searchView() {
        //Search view
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString() != null) {

                    //create query with inputted value
                    Query searchQuery = productRef.orderBy("name")
                            .startAt(s.toString())
                            .endAt(s.toString() + "\uf8ff");

                    //new options
                    FirestoreRecyclerOptions<Product> newOptions = new FirestoreRecyclerOptions.Builder<Product>()
                            .setQuery(searchQuery, Product.class)
                            .build();
                    
                    //update options
                    adapter.updateOptions(newOptions);
                    
                } else {
                    //if string is null/empty create simple query
                    FirestoreRecyclerOptions<Product> emptyOptions = new FirestoreRecyclerOptions.Builder<Product>()
                            .setQuery(productRef, Product.class)
                            .build();
                    //update options
                    adapter.updateOptions(emptyOptions);

                }
            }
        });

    }


    private void setUpProductsView(Query query) {
        //set view based on query
        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new ProductAdapter(options, false);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();//change data again

        //Remove item
        if(authorizeUser) {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    adapter.deleteItem(viewHolder.getAdapterPosition());
                }

                //on Swipe action/effect
                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addBackgroundColor(0xFF000000)
                            .addActionIcon(R.drawable.ic_delete)
                            .create()
                            .decorate();
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }).attachToRecyclerView(recyclerView);
        }

        //set onClick methods to views
        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position, int caseView) {
                Product product = documentSnapshot.toObject(Product.class);
                String id = documentSnapshot.getId();
                String path = documentSnapshot.getReference().getPath();

                //Update Product if first heading is touched
                  if((authorizeUser) && caseView == 1) {
                          //send document reference to update method in another activity
                          Toast.makeText(AdminControl.this,
                                  "Position: " + position + " ID: " + id + " " + caseView, Toast.LENGTH_SHORT).show();
                          Intent intent = new Intent(getApplicationContext(), SingleProductAdmin.class);
                          intent.putExtra("path", path);
                          startActivity(intent);

                  }

                  //Get price in BsS if "BsS" tag is touched
                  if(caseView == 2){
                        //Show price in dollars
                        docRef = mStore.document(path); //get document data
                        docRef.get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            final Double productPrice = documentSnapshot.getDouble("price");
                                            Toast.makeText(AdminControl.this,
                                                    "Calculando precio en doláres...", Toast.LENGTH_LONG).show();


                                            final Double calculus_price, current_price;
                                            SharedPreferences sharedPreferences = getSharedPreferences("MainInfo", MODE_PRIVATE);
                                            String price = sharedPreferences.getString("dollarPrice", "");

                                            //calculate price of product in sovereign bolivars
                                            current_price = Double.parseDouble(price);
                                            calculus_price = productPrice * current_price;

                                            //send data to Dialog Box
                                            Bundle bundle = new Bundle();
                                            bundle.putDouble("price_dollars", calculus_price);
                                            bundle.putDouble("current_dollar", current_price);
                                            DialogBox dialogMessage = new DialogBox();
                                            dialogMessage.setArguments(bundle);
                                            dialogMessage.show(getSupportFragmentManager(), "Dialog box");
                                        }



                                        }
                                    });
                                }
                  }


        });
    }




    //Import data to database
    //read data from file and update database
    public void readFile(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, OPEN_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Uri currentUri = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_REQUEST_CODE) {
                if (resultData != null) {
                    currentUri = resultData.getData();
                    getDataFromExcel(currentUri); //if successful
                }
            }
        }
    }

    public void getDataFromExcel(Uri currentUri) {
        String line = "";
        //read each line of document one by one
        try {
            InputStream inputStream = getContentResolver().openInputStream(currentUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            //Skip headers
            reader.readLine();
            while((line = reader.readLine()) != null){
                Log.d("Activity", "Line: " + line); //to debug code
                //Split by semicolon
                String[] tokens = line.split(";");
                //Read data
                Product product = new Product();
                //handle empty values [function]

                product.setName(tokens[0]); //column: name
                product.setBrand(tokens[1]); //column: brand
                product.setAmount(Integer.parseInt(tokens[2])); //column: amount
                product.setUnit(tokens[3]); //column: unit
                product.setPrice(Double.parseDouble(tokens[4])); //column: price
                product.setGroup(Integer.parseInt(tokens[5])); //column: group
                CSVSamples.add(product); //add document to ArrayList

                Log.d("Activity", "Document: " + product);
            }
        } catch (IOException e) {
            Log.wtf("Activity: ", "Error trying to parse document. Line:" + line, e);
            e.printStackTrace();
        }

        //Add documents to Firebase
        if(CSVSamples != null){
            Toast.makeText(getApplicationContext(), "Actualizando base de datos...", Toast.LENGTH_LONG).show();
            CollectionReference notebookRef = mStore.collection("Productos");
            for(int i = 0; i < CSVSamples.size(); i++) {
                notebookRef.add(CSVSamples.get(i));

            }
            Toast.makeText(getApplicationContext(), "Base de datos actualizada", Toast.LENGTH_LONG).show();

        }


    }



    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
