package com.example.gabys.notsound;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by VirtualBox on 21/08/2017.
 */

public class Sonidos extends Application{
    private ArrayList<Sonido> listasonidos;
    private String file = "notas.dat";

    public Sonidos(){
        listasonidos = new ArrayList<Sonido>();
    }

    public ArrayList<Sonido> getListasonidos(){ return listasonidos; }

    public int sizeSonidos(){
        return listasonidos.size();
    }

    public Sonido getSonido(int itemIndex){
        return listasonidos.get(itemIndex);
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
            Toast t = Toast.makeText(context, "Guardado exitoso", Toast.LENGTH_SHORT);
            t.show();
        } else {
            SerializeObject.WriteSettings(context, "", file);
        }
    }

    public void addSonido(Context context, Sonido sonido){
        int newItemIndex = this.sizeSonidos(); //devuelve la cantidad de elementos +1;
        listasonidos.add(newItemIndex,sonido);
        this.saveSonidos(context);
    }

    public void removeSonido(Context context, int itemIndex){
        listasonidos.remove(itemIndex);
        this.saveSonidos(context);
    }

}
