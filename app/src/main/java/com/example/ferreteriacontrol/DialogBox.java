package com.example.ferreteriacontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Locale;

public class DialogBox extends AppCompatDialogFragment {
    AlertDialog dialog;
    Activity activity;

    public DialogBox(Activity activity) {
        this.activity = activity;
    }


    public DialogBox() {
        //empty constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String priceinDollars = "El precio no está disponible";
        Double price = 0.0, current_dollar = null;
        //get price in dollars
        if (getArguments() != null) {
            price = getArguments().getDouble("price_dollars");
            current_dollar = getArguments().getDouble("current_dollar");
            priceinDollars = price.toString() + " BsS";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Precio en Bolívares");
        builder.setMessage("Referencia: " + String.format(Locale.GERMAN, "%,.2f", current_dollar) + "\n" + "Producto: " + String.format(Locale.GERMAN, "%,.2f", price));
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }

    void loginLoadDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        //set the view for the activity
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog, null));
        builder.setCancelable(false);
        //display alert box
        dialog = builder.create();
        dialog.show();
    }

    void dismissDialog(){
        dialog.dismiss();
    }
}
