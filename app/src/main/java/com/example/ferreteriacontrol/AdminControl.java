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
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AdminControl extends AppCompatActivity {
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private CollectionReference productRef = mStore.collection("Productos");
    private DocumentReference docRef;
    private ProductAdapter adapter;
    private Query queryReference;
    int userRole;
    private boolean authorizeUser;
    EditText searchText;
    FloatingActionButton buttonAddProduct;
    FloatingActionButton buttonImport;
    FloatingActionButton buttonExport;
    //import products
    private List<Product> CSVSamples = new ArrayList<>();
    private static final int OPEN_REQUEST_CODE = 41;
    private static final int SAVE_REQUEST_CODE = 42;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        searchText = findViewById(R.id.searchText);
        buttonAddProduct = findViewById(R.id.button_add_product);
        buttonImport = findViewById(R.id.button_import);
        buttonExport = findViewById(R.id.button_export);

        //get user's role and if authorized
        SharedPreferences sharedPreferences = getSharedPreferences("MainInfo", MODE_PRIVATE);
        authorizeUser = sharedPreferences.getBoolean("isAdmin", false);
        userRole = sharedPreferences.getInt("role", 0);

        if (authorizeUser) {
            //Make add product button visible for this activity
            buttonImport.setVisibility(View.VISIBLE);
            buttonExport.setVisibility(View.VISIBLE);
            buttonAddProduct.setVisibility(View.VISIBLE);
            buttonAddProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminControl.this, SingleProductAdmin.class));
                }
            });
        }

        Toast.makeText(getApplicationContext(), "data" + userRole + authorizeUser, Toast.LENGTH_LONG).show();


        //get extra intent from Main Activity to determine what products to show
        Intent intent = getIntent();
        int rubro = intent.getIntExtra("rubro", -1);
        if (rubro == -1) {
            //if the category of the prududct was not specified, show all
            queryReference = productRef;
        } else {
            //if the categiry was specified, get it
            queryReference = productRef.whereEqualTo("group", rubro);
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
                String st;
                if (s.toString() != null) {
                    if (s.toString().length() > 0) {
                        st = s.toString().substring(0, 1).toUpperCase() + s.toString().substring(1);
                    } else {
                        st = s.toString();
                    }

                    //create query with inputted value
                    Query searchQuery = productRef.orderBy("name")
                            .startAt(st)
                            .endAt(st + "\uf8ff");

                    //new options
                    FirestoreRecyclerOptions<Product> newOptions = new FirestoreRecyclerOptions.Builder<Product>()
                            .setQuery(searchQuery, Product.class)
                            .build();

                    //update options
                    adapter.updateOptions(newOptions);

                } else {
                    //if string is null/empty create simple query to show all products
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

        adapter = new ProductAdapter(options, true);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //change data again


        //Remove item/product on swiped
        if (authorizeUser) {
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
                String id = documentSnapshot.getId();
                String path = documentSnapshot.getReference().getPath();

                //Update Product if first heading is touched
                if ((authorizeUser) && caseView == 1) {
                    //send document reference to update method in another activity
                    Toast.makeText(AdminControl.this,
                            "Position: " + position + " ID: " + id + " " + caseView, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), SingleProductAdmin.class);
                    intent.putExtra("path", path);
                    startActivity(intent);

                }

                //Get price in BsS if "BsS" tag is touched
                if (caseView == 2) {
                    //Show price in dollars
                    docRef = mStore.document(path); //get document data/price in dollars
                    docRef.get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        final Double calculusPrice, currentPrice;

                                        Toast.makeText(AdminControl.this,
                                                "Calculando precio en doláres...", Toast.LENGTH_LONG).show();

                                        final Double productPrice = documentSnapshot.getDouble("price");
                                        SharedPreferences sharedPreferences = getSharedPreferences("MainInfo", MODE_PRIVATE);
                                        String price = sharedPreferences.getString("dollarPrice", "");

                                        //calculate price of product in sovereign bolivars
                                        currentPrice = Double.parseDouble(price);
                                        calculusPrice = productPrice * currentPrice; //currency conversion (price in bolivars)

                                        //send data to Dialog Box
                                        Bundle bundle = new Bundle();
                                        bundle.putDouble("price_dollars", calculusPrice);
                                        bundle.putDouble("current_dollar", currentPrice);
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
        //select file to read data from
        Toast.makeText(getApplicationContext(), "Importar base de datos", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, OPEN_REQUEST_CODE);
    }

    //Export data to database
    //read data from file and update database
    public void saveFile(View view) {
        //select file to save data to
        Toast.makeText(getApplicationContext(), "Exportar base de datos", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");

        startActivityForResult(intent, SAVE_REQUEST_CODE);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Uri currentUri = null;
        if (resultCode == Activity.RESULT_OK) {
            //Import data: get data from file
            if (requestCode == OPEN_REQUEST_CODE) {
                if (resultData != null) {
                    currentUri = resultData.getData();
                    getDataFromExcel(currentUri);
                }
            //Export data: save data to file
            } else if (requestCode == SAVE_REQUEST_CODE) {
                if (resultData != null) {
                    currentUri =
                            resultData.getData();
                    writeFileContent(currentUri);
                }
            }
        }
    }

    //Export database: save data to file
    private void writeFileContent(Uri currentUri) {
        try {
            final ParcelFileDescriptor PDF =
                    this.getContentResolver().
                            openFileDescriptor(currentUri, "w");

            final FileOutputStream fileOutputStream =
                    new FileOutputStream(
                            PDF.getFileDescriptor());

            productRef
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            //get list of documents in collection
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                                String textContent = "Id;Nombre;Marca;Cantidad;Unidad;Precio;Grupo" + "\n"; //header of the file

                                for (DocumentSnapshot document : myListOfDocuments) {
                                    Log.d("Firebase doc", document.getId() + " => " + document.toObject(Product.class).toString());

                                    //get the data of each document and add it to textContent [String]
                                    textContent += (document.getId() + "; " + document.toObject(Product.class).toString() + "\n");
                                    //arrayDocs.add(document.toObject(Product.class).toString() + "\n");
                                }
                                Toast.makeText(getApplicationContext(), "Base de datos exportada de forma exitosa", Toast.LENGTH_SHORT).show();
                                Log.d("New string", textContent);
                                //save it to doc
                                try {
                                    //write file with the content of textContent
                                    fileOutputStream.write(textContent.getBytes());
                                    fileOutputStream.close();
                                    PDF.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
    }


    //Import database: read content of file
    public void getDataFromExcel(Uri currentUri) {
        String line = "";
        int totalDoc = 0;
        int f = 0; //failed cases
        int s = 0; //successful cases
        boolean finalResult = false;

        //read each line of document one by one
        try {
            InputStream inputStream = getContentResolver().openInputStream(currentUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

            //Skip headers of the file
            reader.readLine();
            int c; //count each line
            while ((line = reader.readLine()) != null) {
                totalDoc++;
                Log.d("Activity", "Line: " + line); //to debug code
                //Split string by semicolon/comma
                String[] tokens = line.split(";|,");
                c = 0;

                //Read and validate the data
                Product product = new Product();
                boolean result = true; //used to determine if an object should be added to ArrayList
                Log.d("Activity", "alength: " + tokens.length); //to debug code

                if (tokens.length == 6) {
                    while (c < 6) {
                        boolean wasSuccessful = product.readContentExcel(tokens[c], c); //determine if the value was set to attribut
                        Log.d("Success case", "counter:" + c + " result:" + wasSuccessful);

                        if (!wasSuccessful) { //if one of the values were not correct, end the process and return false
                            result = false;
                            break;
                        }
                        c++;
                    }

                    Log.d("Success case", "counter:" + c + " result final product:" + result);
                    if (result) { //if all the values were correct, add new object
                        s++; //successful document
                        CSVSamples.add(product); //add document to ArrayList
                        Log.d("Activity", "Document: " + product);
                    }
                } else { //raise invalid format exception. Check later
                    throw new IllegalArgumentException("El formato del documento es inválido");
                }
            }

            f = totalDoc - s; //total amount of documents less successful cases
            Log.d("Failed", "total " + totalDoc + "succ " + s + "failed " + f);


        } catch (IOException e) {
            Log.d("Activity: ", "Error trying to parse document. Line:" + line, e);
            e.printStackTrace();
        } catch (IllegalArgumentException ex) {
            Log.d("Problem: ", "Error with the format of the doc. Line:" + line, ex);
            Toast.makeText(getApplicationContext(), "Formato inválido", Toast.LENGTH_LONG).show();
        }

        //Add documents to Firebase
        if (CSVSamples.size() > 0) {
            //counter: agregar casos exitosos, formatos inválidos
            Toast.makeText(getApplicationContext(), "Actualizando base de datos...", Toast.LENGTH_LONG).show();
            CollectionReference notebookRef = mStore.collection("Productos");
            for (int i = 0; i < CSVSamples.size(); i++) {
                notebookRef.add(CSVSamples.get(i));

            }
            finalResult = true;

        }
        //Sucess or Failure : client
        if (finalResult) {
            Toast.makeText(getApplicationContext(), "Base de datos actualizada." + " Total:" + totalDoc + " Casos exitosos:" + s + " Casos fállidos:" + f, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "No se pudo actualizar la base de datos. Revise el formato del documento", Toast.LENGTH_LONG).show();
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
