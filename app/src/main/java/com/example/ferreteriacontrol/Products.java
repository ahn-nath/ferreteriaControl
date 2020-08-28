package com.example.ferreteriacontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class Products extends AppCompatActivity {
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private CollectionReference productRef = mStore.collection("Productos");
    private ProductAdapter adapter;
    EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        searchText = findViewById(R.id.searchText);

        //get extra intent
        Intent intent = getIntent();
        int rubro = intent.getIntExtra("rubro", -1);
            if(rubro == -1) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else {
                setUpProductsView(rubro);
            }


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
                Toast.makeText(getApplicationContext(), "text after changed", Toast.LENGTH_SHORT).show();
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


    private void setUpProductsView(int rubro) {
        Query query = productRef.whereEqualTo("group", rubro); //rubro
        //**IMPORTANT** create default case to when there are no products
        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new ProductAdapter(options, true);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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
