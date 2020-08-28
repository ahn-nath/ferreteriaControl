package com.example.ferreteriacontrol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DialogBox extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String priceinDollars = "El precio no está disponible";
        Double price, current_dollar = null;
        //get price in dollars
        if (getArguments() != null) {
            price = getArguments().getDouble("price_dollars");
            current_dollar = getArguments().getDouble("current_dollar");
            priceinDollars = price.toString() + " BsS";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Precio en Bolívares");
        builder.setMessage("Precio del dólar: " + current_dollar + "\n" + "Producto: " + priceinDollars);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }
}
