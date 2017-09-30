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

import java.util.ArrayList;

public class SonidosActivity extends Menu {
    public static int SONIDO_NUEVO = 98;

    private AdaptadorSonidos adaptador;

    private Sonidos sonidos;
    private ArrayList<Sonido> sonidosSinAlertaExterna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonidos);
        super.CreateMenu();

        String menuAyuda_titulo1="Agregar";
        String menuAyuda_cuerpo1="Presione el botón flotante para añadir un sonido a la lista.";
        String menuAyuda_titulo2="Editar";
        String menuAyuda_cuerpo2="Mantenga presionado el sonido que desea editar y luego elija la opción \"Editar\".";
        String menuAyuda_titulo3="Borrar";
        String menuAyuda_cuerpo3="Mantenga presionado el sonido que desea borrar y luego elija la opción \"Borrar\".";

        super.setAyudaParametros(
                menuAyuda_titulo1,
                menuAyuda_cuerpo1,
                menuAyuda_titulo2,
                menuAyuda_cuerpo2,
                menuAyuda_titulo3,
                menuAyuda_cuerpo3,
                null,
                null);

        sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());
        // Cargo la lista de sonidos sin el Alerta Externa. Este arraylist va a servir solo para mostrar visualmente los sonidos. No actualiza el archivo de sonidos
        sonidosSinAlertaExterna = sonidos.getListaSonidos(false);

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
        final int itemSelected_sonidosCompleto;
        final int itemSelected_sonidosSinAlertaExterna;

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        itemSelected_sonidosCompleto = info.position + 1;
        itemSelected_sonidosSinAlertaExterna = info.position;

        switch (item.getItemId()) {
            case R.id.CtxOpEditar:
                Intent i = new Intent(this, SonidosEdicionActivity.class);
                i.putExtra("sonidoSeleccionado", itemSelected_sonidosCompleto);
                startActivity(i);
                break;
            case R.id.CtxOpBorrar:
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(SonidosActivity.this);
                dialogo1.setTitle("Importante");
                dialogo1.setMessage("¿Está seguro que desea eliminar este Sonido?");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        sonidos.removeSonidoByPosition(getApplicationContext(), itemSelected_sonidosCompleto); // Actualizo la lista completa de los sonidos guardados
                        sonidosSinAlertaExterna.remove(itemSelected_sonidosSinAlertaExterna); // Actualizo la lista de los sonidos que se visualizan
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
            super(context, R.layout.sonido, sonidosSinAlertaExterna);
            appCompatActivity = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = appCompatActivity.getLayoutInflater();
            View item = inflater.inflate(R.layout.sonido, null);

            Sonido sonido = sonidosSinAlertaExterna.get(position);

            TextView txtvw_sonidoNombre = (TextView)item.findViewById(R.id.sonidoNombre);
            txtvw_sonidoNombre.setText(sonido.getNombre());

            ImageView img_sonidoImagen = (ImageView)item.findViewById(R.id.sonidoImagen);
            img_sonidoImagen.setImageBitmap(sonido.getImagen());

            return(item);
        }
    }

    public void CrearNuevoSonido(View view){
        Intent i = new Intent(getApplicationContext(), SonidosEdicionActivity.class);
        i.putExtra("sonidoSeleccionado", SONIDO_NUEVO);
        startActivity(i);
    }
}