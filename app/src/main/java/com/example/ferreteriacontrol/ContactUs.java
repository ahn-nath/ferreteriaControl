package com.example.ferreteriacontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ContactUs extends AppCompatActivity {
    EditText contactSubject, contactMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        contactSubject = findViewById(R.id.contactSubject);
        contactMessage = findViewById(R.id.contactMessage);
    }

    public void sendEmail(View view) {
        //get values of input text
        String subject = contactSubject.getText().toString();
        String message = contactMessage.getText().toString();
        String[] recipients = {"hvtoledo@gmail.com"};

        //send email
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(intent, "Elige una aplicación para enviar tu correo"));

    }
}
