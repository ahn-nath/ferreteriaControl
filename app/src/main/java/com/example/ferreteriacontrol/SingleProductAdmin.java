package com.example.ferreteriacontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.FillResponse;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SingleProductAdmin extends AppCompatActivity {
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private DocumentReference docRef;
    boolean update = false;
    EditText addName, addBrand, addPrice, addAmount;
    TextView headingSingleProduct;
    NumberPicker addRubro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product_admin);
        addName = findViewById(R.id.addName);
        addBrand = findViewById(R.id.addBrand);
        addPrice = findViewById(R.id.addPrice);
        addAmount = findViewById(R.id.addAmount);
        addRubro = findViewById(R.id.addRubro);
        String[] rubros = { //shall this go here, don't think so
                "Cerrajería",
                "Electricidad, iluminación y conductores",
                "Seguridad Industrial y automotriz",
                "Plomería y griferías",
                "Alambres, clavos y tornillos",
                "Herramientas agrícolas y jardín",
                "Impermeabilizantes y estructuras",
                "Baños y cocinas",
                "Misceláneos",
                "Herramientas manuales y eléctricas",
                "Remate"};

        addRubro.setDisplayedValues(rubros);
        addRubro.setMinValue(1);
        addRubro.setMaxValue(11); //in the future they will be eleven categories
        headingSingleProduct = findViewById(R.id.headingSingleProduct);

        //get extra intent
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        //if value different from empty get reference to document
        if (path != null) {
            headingSingleProduct.setText("Actualizar Producto");
            update = true;
            docRef = mStore.document(path);
            getDocumentData(docRef);
        }
    }

    private void getDocumentData(DocumentReference docRef) {
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String brand = documentSnapshot.getString("brand");
                            String price = documentSnapshot.getDouble("price").toString();
                            String amount = documentSnapshot.get("amount").toString();
                            addName.setText(name, TextView.BufferType.EDITABLE);
                            addBrand.setText(brand, TextView.BufferType.EDITABLE);
                            addPrice.setText(price, TextView.BufferType.EDITABLE);
                            addAmount.setText(amount, TextView.BufferType.EDITABLE);
                        }
                    }
                });
    }
    //Set save icon in top nav-bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.product_admin, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_product:
                saveProduct();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void saveProduct() {
        String name = addName.getText().toString();
        String brand = addBrand.getText().toString();
        String image = "1gzBdMzE-d-JWsIHOUl4ofW4GlLv0LVmH"; //temporary
        double price = Double.parseDouble(addPrice.getText().toString());
        int amount = Integer.parseInt(addAmount.getText().toString());
        int group = addRubro.getValue();

        /*
        //add validation to fields: check if empty
        * */

        //if we received an id to update the product
        if (update) {
            //Update product
            docRef.update("name", name,
                    "brand", brand, "price", price, "amount", amount, "group", group);
            Toast.makeText(this, "Producto Actualizado", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            //Add new product
            CollectionReference notebookRef = mStore.collection("Productos");
            notebookRef.add(new Product(name, brand, price, amount, image, group));
            Toast.makeText(this, "Producto Agregado", Toast.LENGTH_SHORT).show();
            finish();
        }

    }
}
