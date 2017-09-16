#include "TimerOne.h"
#include <SoftwareSerial.h>
SoftwareSerial BT1(4,2); // RX, TX recorder que se cruzan

String rea="";
String buf="";

bool  modo=0; //modo 0: out, 1: in
bool estado=0;  //0 lecturea, 1 grabando

int ledPin = 10;
int ledPin1 = 13;
/*char palanca[3]="A0";*/
char mic[3]="A0";
double GLOBAL_ruidoPromedio=0;
double procentajeSuperacionPromedio=1.5;
char c ;
double lecturaMic;

void setup()
{
  BT1.begin(9600);
  BT1.flush();
  
  Serial.begin(9600);
  pinMode(ledPin, OUTPUT);
  pinMode(ledPin1, OUTPUT);
  pinMode(A0,INPUT);
  Timer1.initialize(3000000);
  Timer1.attachInterrupt(callback);
}

//Esta funcion toma 500 muestras y promedia el ruido para setear nuevo ruido promedio
void callback()
{
  if (modo!=0) return; // modo != OUT => salgo
  
  //Serial.println("-----Inicio-----");
  
  double arrayOfTops[50];
  int contArrayOfTops=0;
  //Serial.println("--------------Muestras-----------");
  for (int i=0; i<50; i++) {
    //Obtengo todos los valores y los aguardo para tomar lods 20 mas altos
    double lecturaMic=analogRead(A0);
    arrayOfTops[contArrayOfTops]=lecturaMic;
    contArrayOfTops++;
  }

  //Hago burbuja para ordenarlos
  double temp=0;
  for (int i=0; i<50; i++){
    for (int j=0 ; j<50 - 1; j++){
      if (arrayOfTops[j] > arrayOfTops[j+1])
      {
        temp = arrayOfTops[j];
        arrayOfTops[j] = arrayOfTops[j+1];
        arrayOfTops[j+1] = temp;
      }
    }
  }

  //Tomo los ultimos 20 que son los mas altos y saco el promedio en base a esos
  double sum=0;
  for (int i=29; i<50; i++) {
    //Serial.println(arrayOfTops[i]);
    sum=sum+arrayOfTops[i];
  }
  GLOBAL_ruidoPromedio=sum/20;
  Serial.println("PROMEDIO DIO:");
  Serial.println(GLOBAL_ruidoPromedio);
  
}

void reccmd(String buf){
  /*
     'T0|'->TEST ID
      'T0|'<-TEST ID
  
      'C1|'->CONFIG ID NIVEL
      'C1|'<-CONFIG ID OK
  */    
  if (buf=="T0"){
    BT1.write("T0|");
  }
  if (buf=="C1"){
    modo=0;
    BT1.write("C1|"); //out
  }
  if (buf=="C2"){
    modo=1;
    BT1.write("C2|"); // in
  }
}

void loop()
{
   //lectura del blue
   if (BT1.available()){
     c= BT1.read();
     //Serial.write(c);
     if( c != '|')    //Hasta que el caracter sea END_CMD_CHAR
        buf += c;
     else{
        reccmd(buf);
        buf="";
     } 
     delay(25) ;
   }

  if (modo==0){ // modo OUT
    //Vamos leyendo lo que capta el mic
    //si supera valor promedio, prendemos led
    lecturaMic=analogRead(A0);
  
    if(lecturaMic>(GLOBAL_ruidoPromedio*procentajeSuperacionPromedio)){
      //Prendo led
      digitalWrite(ledPin1, HIGH);
      //Envio sms x blue
      BT1.write("NA|");
      
      callback();
      delay(2000);
    }else{
      digitalWrite(ledPin1, LOW);
    }
  }else{ //modo IN
  }

       
  
   
}

