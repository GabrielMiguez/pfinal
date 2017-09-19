#include "TimerOne.h"
#include <SoftwareSerial.h>
#include <EEPROM.h>
#define LOG_OUT 1 // use the log output function
#include <FHT.h> // include the library
#define FHT_N 256 // set to 256 point fht
#define BAUDRATE 19200 // set to 256 point fht
#define DELAY_ANCHO_BANDA 74  //74 para B.W=6400Hz y Resolucion=50Hz.   75 para simulacion Proteus
SoftwareSerial BT1(4,2); // RX, TX recorder que se cruzan

String rea="";
String buf="";

bool  modo=0; //modo 0: out, 1: in
bool estado=0;  //0 lecturea, 1 grabando
bool normal=1; //SetNormal or fast

int IDSoundGrab=-1;
int ledPin = 10;
int ledPin1 = 13;
/*char palanca[3]="A0";*/
char mic[3]="A0";
double GLOBAL_ruidoPromedio=0;
double procentajeSuperacionPromedio=30;
char c;

void setNormal(){
  TIMSK0 = 1; // turn off timer0 for lower jitter -->Problemas con el sleep
  ADCSRA |= bit (ADPS1);    
  ADMUX = 0x40; // use adc0
  DIDR0 = 0x01; // turn off the digital input for adc0
  normal=1;
}
void setFast(){
  TIMSK0 = 0; // turn off timer0 for lower jitter -->Problemas con el sleep
  ADCSRA = 0xe5; // set the adc to free running mode -->Problemas con el analogRead
  ADMUX = 0x40; // use adc0
  DIDR0 = 0x01; // turn off the digital input for adc0
  normal=0;
}

void setup()
{
  BT1.begin(9600);
  BT1.flush();
  
  //Serial.begin(9600);
  pinMode(ledPin, OUTPUT);
  pinMode(ledPin1, OUTPUT);
  pinMode(A0,INPUT);
  Timer1.initialize(2000000);
  Timer1.attachInterrupt(callback);

  Serial.begin(BAUDRATE); // use the serial port

 
  //TIMSK0 = 0; // turn off timer0 for lower jitter -->Problemas con el sleep
  //ADCSRA = 0xe5; // set the adc to free running mode -->Problemas con el analogRead
  //ADMUX = 0x40; // use adc0
  //DIDR0 = 0x01; // turn off the digital input for adc0

  //  ADCSRA |= bit (ADPS0);                               //   2  
  //  ADCSRA |= bit (ADPS1);                               //   4  
  //  ADCSRA |= bit (ADPS0) | bit (ADPS1);                 //   8  
  //  ADCSRA |= bit (ADPS2);                               //  16 
  //  ADCSRA |= bit (ADPS0) | bit (ADPS2);                 //  32 
  //  ADCSRA |= bit (ADPS1) | bit (ADPS2);                 //  64 
  //  ADCSRA |= bit (ADPS0) | bit (ADPS1) | bit (ADPS2);   // 128

 // Set A/D prescale factor to 128
    // 16 MHz / 128 = 125 KHz, inside the desired 50-200 KHz range.
    // XXX: this will not work properly for other clock speeds, and
    // this code should use F_CPU to determine the prescale factor.

}



//Esta funcion toma 500 muestras y promedia el ruido para setear nuevo ruido promedio
void callback()
{
  if (modo!=0) return; // modo != OUT => salgo
  if (normal==0) return; //modo fast me voy
  
  Serial.println("-----Inicio callback-----");
  
}

void reccmd(String buf){
        /*
                 'G|'->Comenzar Grabación (x milisegundos máximos)
                'P1|'<-Sonido PRE Guardado con ID
                'G1|'->Guardar Sonido ID
                'G1|'<-Sonido Guardado con ID 1
                'B1|'->Borrar Sonido ID
                'B1|'<-Sonido Borrado ID
                'T0|'->TEST ID
                'T0|'<-TEST ID
                'C1|'->CONFIG ID NIVEL
                'C1|'<-CONFIG ID OK
                ‘C2|'->CONFIG ID NIVEL
                'C2|'<-CONFIG ID OK
                'NA|'<-Notificación Alerta
                'N1|'<-Notificación ID
     */    
  if (buf=="T0"){
    BT1.write("T0|");
  }
  if (buf=="C1"){//out
    modo=0;
    BT1.write("C1|"); //out
  }
  if (buf=="C2"){//in
    modo=1;
    BT1.write("C2|"); // in
  }
  
  if (buf=="CE"){
    cleanEEPROM();
    BT1.write("CE|"); // Format EEPROM
  }

  if (buf=="G"){
    modo=1; //modo 0: out, 1: in
    estado=1;  //0 lecturea, 1 grabando
    IDSoundGrab=-1;
    grabar();
    BT1.write('G');
    BT1.print(IDSoundGrab);
    BT1.write('|');    
    
    IDSoundGrab=-1;
    estado=0;  //0 lecturea, 1 grabando
  }
    
}

void loop()
{
  while(1){
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
    Serial.println("Antes modo 0");
    if (modo==0){ // modo OUT
      Serial.println("modo 0");
      
    }else{ //modo IN
      Serial.println("modo 1");
      modoPatron();
    }
  }
}

int primeraPosVaciaEEPROM(){
   for (int i = 0 ; i < EEPROM.length() ; i++) {
    if(EEPROM.read(i)==0)	return i;
   }
   
   //EEPROM LLENA
   return 0;
}


int ultimaPosLlenaEEPROM(){
  if(EEPROM.read(0)==0)	return 0;

  for (int i = 0 ; i < EEPROM.length() ; i++) {
    if(EEPROM.read(i+1)==0)	return i; 
  }  
  return EEPROM.length();
}

void cleanEEPROM(){
  for (int i = 0 ; i < EEPROM.length() ; i++) {
    EEPROM.write(i, 0);
  }
}

void grabar(){  
}

void modoPatron() {
	Serial.println("----Comienza la escucha-----");
	
	setFast();
	Serial.println("----SetFast-----");
		
	Serial.println("----Ya Tome las Lecturas-----");
	setNormal();  
	Serial.println("----SetNormal-----");  
}
