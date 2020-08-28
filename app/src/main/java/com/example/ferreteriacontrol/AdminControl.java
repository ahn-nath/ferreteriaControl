package com.example.ferreteriacontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AdminControl extends AppCompatActivity {
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private CollectionReference productRef = mStore.collection("Productos");
    private DocumentReference docRef;
    private ProductAdapter adapter;
    EditText searchText;
    FloatingActionButton buttonAddProduct;
    private Handler mainHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        searchText = findViewById(R.id.searchText);
        //Make add product button visible for this activity
        buttonAddProduct = findViewById(R.id.button_add_product);
        buttonAddProduct.setVisibility(View.VISIBLE);
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminControl.this, SingleProductAdmin.class));
            }
        });


        setUpProductsView(productRef);

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

                    //create query
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
                    FirestoreRecyclerOptions<Product> emptyOptions = new FirestoreRecyclerOptions.Builder<Product>()
                            .setQuery(productRef, Product.class)
                            .build();

                    adapter.updateOptions(emptyOptions);

                }
            }
        });


    }


    private void setUpProductsView(Query query) {
        //set view

        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new ProductAdapter(options, false);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();//change data again

        //delete single item with swipe action
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            //direction to delete
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                adapter.deleteItem(viewHolder.getAdapterPosition());
            }

            //on Swipe change color
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


        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position, int caseView) {
                Product product = documentSnapshot.toObject(Product.class);
                String id = documentSnapshot.getId();
                String path = documentSnapshot.getReference().getPath();


                switch (caseView) {
                    case 1:
                        //UPDATE PRODUCT
                        //send document reference to update method in another activity
                        Toast.makeText(AdminControl.this,
                                "Position: " + position + " ID: " + id + " " + caseView, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), SingleProductAdmin.class);
                        intent.putExtra("path", path);
                        startActivity(intent);
                        break;

                    case 2:
                        //Show price in dollars
                        docRef = mStore.document(path); //get document data
                        docRef.get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            final Double price = documentSnapshot.getDouble("price");
                                            Toast.makeText(AdminControl.this,
                                                    "Calculando precio en doláres...", Toast.LENGTH_LONG).show();


                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    final Double calculo_precio, current_price;
                                                    //set variable
                                                    Document document;
                                                    String send;
                                                    Double result;
                                                    try {
                                                        document = Jsoup.connect("https://monitordolarvenezuela.com/").get();
                                                        final String dolarData = document.select("div.back-white-tabla h6.text-center").text();
                                                        send = dolarData.replaceAll("[^\\d.]+|\\.(?!\\d)", "");
                                                        result = Double.parseDouble(send) * price;
                                                    } catch (IOException e) {
                                                        send = String.valueOf(e);
                                                        result = 0.0;
                                                        e.printStackTrace();
                                                    }

                                                    calculo_precio = result; //pass only result
                                                    current_price = Double.parseDouble(send);


                                                    mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            //call dialog box and set text
                                                            Bundle bundle = new Bundle();
                                                            bundle.putDouble("price_dollars", calculo_precio);
                                                            bundle.putDouble("current_dollar", current_price);
                                                            DialogBox dialogMessage = new DialogBox();
                                                            dialogMessage.setArguments(bundle);
                                                            dialogMessage.show(getSupportFragmentManager(), "Dialog box");
                                                        }
                                                    });

                                                }
                                            }).start();
                                        }
                                    }
                                });

                        break;
                    default:
                        Toast.makeText(AdminControl.this, "Error", Toast.LENGTH_SHORT).show();
                }

            }

        });
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
