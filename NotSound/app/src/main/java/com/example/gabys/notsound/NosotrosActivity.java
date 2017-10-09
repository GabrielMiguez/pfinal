package com.example.gabys.notsound;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class NosotrosActivity extends Menu {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nosotros);
        super.CreateMenu();

        TextView txtMauro = (TextView) findViewById(R.id.txtvw_mauro);
        TextView txtMauricio = (TextView) findViewById(R.id.txtvw_mauricio);
        TextView txtLucas = (TextView) findViewById(R.id.txtvw_lucas);
        TextView txtGabriel = (TextView) findViewById(R.id.txtvw_gabriel);
        TextView txtMaximiliano = (TextView) findViewById(R.id.txtvw_maximiliano);
        TextView txtMail = (TextView) findViewById(R.id.txtvw_mail);

        txtMauro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.linkedin.com/in/mauro-gonzález-81392733/");

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        txtMauricio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.linkedin.com/in/mauriciojfernandez/");

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        txtLucas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.linkedin.com/in/lucas-nahuel-lavrencic-32b96a6b");

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        txtGabriel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        txtMaximiliano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.linkedin.com/in/maximiliano-sarli-3856a35b/");

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        txtMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "MesaDeAyuda@NotSound.com.ar" });
                intent.putExtra(Intent.EXTRA_SUBJECT, "Consultas y Sugerencias");
                intent.putExtra(Intent.EXTRA_TEXT, "Estimado Equipo de NotSound,\n \t \t");
                startActivity(Intent.createChooser(intent, ""));
            }
        });
    }





}
