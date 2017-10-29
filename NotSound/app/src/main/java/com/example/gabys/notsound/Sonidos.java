package com.example.gabys.notsound;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by VirtualBox on 21/08/2017.
 */

public class Sonidos extends Application{
    public static int ID_SONIDO_ALERTA_EXTERNA = 99;
    public static int POSICION_SONIDO_ALERTA_EXTERNA = 0;

    private ArrayList<Sonido> listasonidos;
    private String file = "notas.dat";

    public Sonidos(){
        listasonidos = new ArrayList<Sonido>();
    }

    public ArrayList<Sonido> getListaSonidos(Boolean incluirAlertaExterna){
        if(incluirAlertaExterna){
            return listasonidos;
        }
        else{
            ArrayList<Sonido> listasonidosSinAlertaExterna = new ArrayList<Sonido>();
            for (Sonido sonido: listasonidos) {
                if(sonido.getID() != ID_SONIDO_ALERTA_EXTERNA){
                    listasonidosSinAlertaExterna.add(sonido);
                }
            }
            return listasonidosSinAlertaExterna;
        }
    }

    public int sizeSonidos(){
        return listasonidos.size();
    }

    public Sonido getSonidoByPosition(int itemIndex){

        return listasonidos.get(itemIndex);
    }

    public Sonido getSonidoByID(int sonidoID){

        if (listasonidos.size() != 0){
            int itemIndex=0;

            for (Sonido sonido: listasonidos) {
                if(sonido.getID() == sonidoID){
                    break;
                }
                itemIndex++;
            }
            return listasonidos.get(itemIndex);
        }
        else{
            return null;
        }

    }

    public int getAvailableSonidoID(){
        int vSonidoID = 0; //simepre el ID=0, significa que nunca guardo en arduino
        /*
        Boolean repetirBusqueda = true;
        int vSonidoID = 0;

        while (repetirBusqueda){
            repetirBusqueda = false;
            vSonidoID++;
            for (Sonido sonido: listasonidos) {
                if(sonido.getID() == vSonidoID){
                    repetirBusqueda = true;
                }
            }
        }
        */
        return vSonidoID;
    }

    public void setSonido(int itemIndex, Sonido sonido){
        listasonidos.set(itemIndex, sonido);
    }

    public void loadSonidos(Context context) {
        String ser = SerializeObject.ReadSettings(context, file);
        if (ser != null && !ser.equalsIgnoreCase("")) {
            this.listasonidos = (ArrayList<Sonido>)SerializeObject.stringToObject(ser);
        }
    }

    public void saveSonidos(Context context) {
        String ser  = SerializeObject.objectToString(this.listasonidos);
        if (ser != null && !ser.equalsIgnoreCase("")) {
            SerializeObject.WriteSettings(context, ser, file);
            //Toast t = Toast.makeText(context, "Guardado exitoso", Toast.LENGTH_SHORT);
            //t.show();
        } else {
            SerializeObject.WriteSettings(context, "", file);
        }
    }

    public void addSonido(Context context, Sonido sonido){
        int newItemIndex = this.sizeSonidos(); //devuelve la cantidad de elementos +1;

        if (sonido.getID() == ID_SONIDO_ALERTA_EXTERNA){
            newItemIndex = POSICION_SONIDO_ALERTA_EXTERNA;
        }

        listasonidos.add(newItemIndex,sonido);
        this.saveSonidos(context);
    }

    public void removeSonidoByPosition(Context context, int itemIndex){

        String rutaFoto = listasonidos.get(itemIndex).getRutaFoto();
        if (rutaFoto != null){
            File file = new File(listasonidos.get(itemIndex).getRutaFoto());
            file.delete();
        }

        listasonidos.remove(itemIndex);

        this.saveSonidos(context);
    }

    public void cleanSonidos(Context context){
        if (listasonidos.size() != 0){
            while(listasonidos.size()>0) {
                this.removeSonidoByPosition(context,0);
            }

        }
    }
}
