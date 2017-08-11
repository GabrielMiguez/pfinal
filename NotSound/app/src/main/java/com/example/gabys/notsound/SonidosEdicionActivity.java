package com.example.gabys.notsound;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.content.Intent;

import java.util.ArrayList;

public class SonidosEdicionActivity extends AppCompatActivity {

    private ArrayList<Sonido> sonidos;
    private int itemSeleccionado = -1;
    private String ser;

    ImageButton botonGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonidos_edicion);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText txt_sonidoNombre = (EditText)findViewById(R.id.edtxt_sonidoNombre);

        //Levanto los parametros con los datos del Sonido a Editar
        sonidos = (ArrayList<Sonido>) getIntent().getSerializableExtra("sonidos");
        itemSeleccionado = (int) getIntent().getSerializableExtra("sonidoSeleccionado");

        if (itemSeleccionado != -1){
            //Cargo los parametros en los objetos de la pantalla
            txt_sonidoNombre.setText(sonidos.get(itemSeleccionado).getNombre());
        }

        botonGuardar = (ImageButton) findViewById(R.id.btn_Guardar);
        botonGuardar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            if (itemSeleccionado != -1) {
                sonidos.set(itemSeleccionado,(new Sonido(txt_sonidoNombre.getText().toString(),'m')));
                grabarSonidos();
            }
            else {
                int indice = sonidos.size();
                Sonido sonidoNuevo = new Sonido(txt_sonidoNombre.getText().toString(),'m');
                sonidos.add(indice,sonidoNuevo);
                grabarSonidos();
            }
            }
        });
        /*
        String sonidosConcatenados = "";
        for (Sonido item: sonidos) {
            sonidosConcatenados = sonidosConcatenados + "|" + item.getNombre();
        }
        txt_sonidoNombre.setText(sonidosConcatenados);
        */
    }

    class AdaptadorSonidos extends ArrayAdapter<Sonido> {

        AppCompatActivity appCompatActivity;

        AdaptadorSonidos(AppCompatActivity context) {
            super(context, R.layout.sonido, sonidos);
            appCompatActivity = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = appCompatActivity.getLayoutInflater();
            View item = inflater.inflate(R.layout.sonido, null);

            TextView textView1 = (TextView)item.findViewById(R.id.textView);
            textView1.setText(sonidos.get(position).getNombre());

            ImageView imageView1 = (ImageView)item.findViewById(R.id.imageView);
            if (sonidos.get(position).getGenero()=='m')
                imageView1.setImageResource(R.mipmap.ic_launcher);
            else
                imageView1.setImageResource(R.mipmap.ic_launcher_round);
            return(item);
        }
    }

    public void grabarSonidos() {
        ser = SerializeObject.objectToString(this.sonidos);

        if (ser != null && !ser.equalsIgnoreCase("")) {
            SerializeObject.WriteSettings(this, ser, "notas.dat");
            Toast t = Toast.makeText(this, "Guardado exitoso", Toast.LENGTH_SHORT);
            t.show();
            startActivity(new Intent(this, SonidosActivity.class));
        } else {
            SerializeObject.WriteSettings(this, "", "notas.dat");
        }
    }
}
