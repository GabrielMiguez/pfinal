package com.example.gabys.notsound;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by VirtualBox on 21/08/2017.
 */

public class Sonidos extends Application{
    private ArrayList<Sonido> sonidos;
    private String ser;
    private String file = "notas.dat";

    public ArrayList<Sonido> getSonidos(){ return sonidos; }

    public int sizeSonidos(){
        return sonidos.size();
    }

    public Sonido getSonido(int itemIndex){
        return sonidos.get(itemIndex);
    }

    public void setSonido(int itemIndex, Sonido sonido){
        sonidos.set(itemIndex, sonido);
    }

    public void loadSonidos(Context context) {
        ser = SerializeObject.ReadSettings(context, file);
        if (ser != null && !ser.equalsIgnoreCase("")) {
            this.sonidos = (ArrayList<Sonido>)SerializeObject.stringToObject(ser);
        }
    }

    public void saveSonidos(Context context) {
        ser = SerializeObject.objectToString(this.sonidos);
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
        sonidos.add(newItemIndex,sonido);
        this.saveSonidos(context);
    }

    public void removeSonido(Context context, int itemIndex){
        sonidos.remove(itemIndex);
        this.saveSonidos(context);
    }

}
