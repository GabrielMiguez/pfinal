package com.example.gabys.notsound;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SonidosActivity extends Menu {

    private AdaptadorSonidos adaptador;
    private FloatingActionButton fab;

    private Sonidos sonidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonidos);
        super.CreateMenu();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_SonidoNuevo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SonidosEdicionActivity.class);
                //i.putExtra("sonidos", sonidos);
                i.putExtra("sonidoSeleccionado", -1);
                startActivity(i);
            }
        });

        sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());

        adaptador = new AdaptadorSonidos(this);
        ListView lv1 = (ListView)findViewById(R.id.list1);
        lv1.setAdapter(adaptador);

        registerForContextMenu(lv1);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.menu_ctx_sonido, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final int itemSelected;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        itemSelected = info.position;

        switch (item.getItemId()) {
            case R.id.CtxOpEditar:
                Intent i = new Intent(this, SonidosEdicionActivity.class);
                i.putExtra("sonidoSeleccionado", itemSelected);
                startActivity(i);
                break;
            case R.id.CtxOpBorrar:
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(SonidosActivity.this);
                dialogo1.setTitle("Importante");
                dialogo1.setMessage("¿Está seguro que desea eliminar este Sonido?");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        sonidos.removeSonido(getApplicationContext(), itemSelected);
                        adaptador.notifyDataSetChanged();
                    }
                });
                dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                    }
                });

                dialogo1.show();

                break;
        }

        return super.onContextItemSelected(item);
    }


    class AdaptadorSonidos extends ArrayAdapter<Sonido> {

        AppCompatActivity appCompatActivity;

        AdaptadorSonidos(AppCompatActivity context) {
            super(context, R.layout.sonido, sonidos.getListasonidos());
            appCompatActivity = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = appCompatActivity.getLayoutInflater();
            View item = inflater.inflate(R.layout.sonido, null);

            Sonido sonido = sonidos.getSonidoByPosition(position);

            TextView textView1 = (TextView)item.findViewById(R.id.textView);
            textView1.setText(sonido.getNombre());

            ImageView imageView1 = (ImageView)item.findViewById(R.id.imageView);
            imageView1.setImageBitmap(sonido.getImagen());

            return(item);
        }
    }
}