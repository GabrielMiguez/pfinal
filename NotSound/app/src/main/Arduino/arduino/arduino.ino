#include "TimerOne.h"
#include <SoftwareSerial.h>
#include <EEPROM.h>
#define LOG_OUT 1 // use the log output function
#include <FHT.h> // include the library
#define FHT_N 256 // set to 256 point fht
#define BAUDRATE 19200 // set to 256 point fht
#define DELAY_ANCHO_BANDA 74  //74 para B.W=6400Hz y Resolucion=50Hz.   75 para simulacion Proteus
SoftwareSerial BT1(4,7); // RX, TX recorder que se cruzan
//const byte NULO=255;
String rea="";
String buf="";
String sms="";

int delta=1;
bool  modo=0; //modo 0: out, 1: in
bool estado=0;  //0 lecturea, 1 grabando
bool normal=1; //SetNormal or fast
int priPOSeeprom=5;   //los primeros 5 son de config
int ultPOSeeprom=5;   //se actualizara con el maximo puede haber espacios en el medio
        
int IDSoundGrab=-1;
int ledPin = 10;
int ledPin1 = 13;
/*char palanca[3]="A0";*/
char mic[3]="A0";
volatile double GLOBAL_ruidoPromedio=0;  //volatile, por que se cambian en interrupciones
double LOCAL_ruidoPromedio=0;  //sin volatile
double procentajeSuperacionPromedio=80;
char c;

void(* resetFunc) (void) = 0;//declare reset function at address 0
byte oldADCSRA;
byte oldTIMSK0;


void setNormal(){
  noInterrupts();
    TIMSK0 = oldTIMSK0; // turn off timer0 for lower jitter -->Problemas con el sleep
    ADCSRA = oldADCSRA;
    ADMUX = 0x40; // use adc0
    DIDR0 = 0x01; // turn off the digital input for adc0
    normal=1;
  interrupts();
}
void setFast(){
  noInterrupts();
    TIMSK0 = 0; // turn off timer0 for lower jitter -->Problemas con el sleep
    ADCSRA = 0xe5; // set the adc to free running mode -->Problemas con el analogRead
    ADMUX = 0x40; // use adc0
    DIDR0 = 0x01; // turn off the digital input for adc0
    normal=0;
  interrupts();
}
void setModoOut(){
    if (modo==0) return;
    
  modo=0;
  setNormal();    
  EEPROM.write(0, 1);
  
  delay(4000);
 
    
    Timer1.attachInterrupt(callback);
    Timer1.restart();
 
  //Timer1.start()
  //resetFunc();  //call reset
  
}
void setModoIn(){
    if (modo==1) return;    
  
  modo=1;
  
  Timer1.stop();
  Timer1.detachInterrupt();   
  
  delay(2000);
  setFast();
  EEPROM.write(0, 2);
  delay(4000);
  //resetFunc();  //call reset
  
}


void setup()
{
  BT1.begin(9600);
  BT1.flush();
  Serial.begin(BAUDRATE); // use the serial port
  
  pinMode(ledPin, OUTPUT);
  pinMode(ledPin1, OUTPUT);
  pinMode(A0,INPUT);
  
  pinMode(8, OUTPUT);
  pinMode(9, OUTPUT);
  pinMode(10, OUTPUT);
  
  oldADCSRA=ADCSRA;
  oldTIMSK0=TIMSK0;
  Serial.println(F("---- SETUP ----"));
  
  //iniDemo();
  //grabar();
  initEEPROM();
  Serial.println(F("---- SET MODo ----"));
  
  modo=1;
  Timer1.initialize(20000000);//20segundos
  
  if (EEPROM.read(0)==2){
    Serial.println(F("---- INI FASt----"));
    Timer1.detachInterrupt();
    setFast();
    modo=1;
  }else{
    Serial.println(F("---- INI normal ----"));
    setNormal();
    Timer1.attachInterrupt(callback);
    modo=0;
  }


  Serial.print(F("Modo:"));
  Serial.println(modo);
  
}

void loop()
{  
  
  digitalWrite(8, LOW);
  digitalWrite(10, HIGH);
  //digitalWrite(8, HIGH);
  
  Serial.println(F("------------- LOOP -------------- "));
  int cantidadTotales=0;

  
  
/*
cleanEEPROM();
grabar();
*/

  while(1){
    //delay(500);

    while (BT1.available()){
      c= BT1.read();
      Serial.println(c);
      if( c != '|')    //Hasta que el caracter sea END_CMD_CHAR
         buf += c;
      else{
          reccmd(buf);
        buf="";
      }
      delay(25);
    } 
    /*
    if (BT1.available()){
      //Serial.println(F("BT ");
      //delay(100000);
      c= BT1.read();
      
      //Serial.write(c);
      if( c != '|')    //Hasta que el caracter sea END_CMD_CHAR
         buf += c;
      else{
        reccmd(buf);
        buf="";
      } 
      //delay(25);
      //Serial.println(F("BT ");
      Serial.println(F(c);
    }
    */

    //Serial.println(F("Antes modo 0");
    if (modo==0){ // modo OUT
//Serial.println(F("AREAD 1");
      int k=analogRead(A0);
cantidadTotales++;
/*
      
      //Serial.println(F("modo 0");
      //Vamos leyendo lo que capta el mic
      //si supera valor promedio, prendemos led
      while(!(ADCSRA & 0x10)); // wait for adc to be ready
      ADCSRA = 0xf5; // restart adc
      byte m = ADCL; // fetch adc data
      byte j = ADCH;
      int k = (j << 8) | m; // form into an int
      //k -= 0x0200; // form into a signed int
      //k <<= 6; // form into a 16b signed int
      
      */
      
    noInterrupts(); //info de interrupciones: http://www.educachip.com/como-y-por-que-usar-las-interrupciones-en-arduino/
      LOCAL_ruidoPromedio=GLOBAL_ruidoPromedio;
    interrupts();
    
    //if(lecturaMic>(LOCAL_ruidoPromedio*procentajeSuperacionPromedio)){
      if(k>(LOCAL_ruidoPromedio +  procentajeSuperacionPromedio)){

Serial.println(F("---SE SUPERO EL PROMEDIO UNA VEZ---"));
//Serial.println(F(cantidadTotales);
        
        int cantidadSuperaciones=0;
        double acumuladorMuestras=0;
        //Se supero el ruido promedio, una unica vez, entonces voy a ver si las siguientes muestras
        //lo siguen superando, cosa de eliminar impulsos cortos
        for (int i = 0 ; i < 2000 ; i++) {



          /*
          while(!(ADCSRA & 0x10)); // wait for adc to be ready
          ADCSRA = 0xf5; // restart adc
          byte m = ADCL; // fetch adc data
          byte j = ADCH;
          int k = (j << 8) | m; // form into an int

/¿*/
//Serial.println(F("AREAD 2");
k=analogRead(A0);
//acumuladorMuestras=acumuladorMuestras+k;
          //Serial.println(F(k);

          if(k>(LOCAL_ruidoPromedio +  procentajeSuperacionPromedio)){
            cantidadSuperaciones=cantidadSuperaciones+1;
          }
          
        }

//Serial.println(F("---Fin toma muestras---");
        
        if(cantidadSuperaciones>80){
        //if((acumuladorMuestras/100)>=LOCAL_ruidoPromedio +  procentajeSuperacionPromedio){
          //Prende
          //Prendo led
          Serial.println(F("Promedio global-:"));
          Serial.println(LOCAL_ruidoPromedio +  procentajeSuperacionPromedio);
          Serial.println(F("Promedio recien calculado:"));
          Serial.println(cantidadSuperaciones);
          //Serial.println(F(cantidadSuperaciones);
          digitalWrite(ledPin1, HIGH);
          //Envio sms x blue
          BT1.write("NA|");
          delay(500);
          BT1.flush();
          delay(500);
          Serial.println(F("supero"));
          
          callback();
          delay(2000);
        }

      }else{
        digitalWrite(ledPin1, LOW);
      }
    }else{ //modo IN
      //Serial.println(F("modo 1");
      modoPatron();
    }
    
  }
}

//Esta funcion toma 500 muestras y promedia el ruido para setear nuevo ruido promedio
void callback()
{
  if (modo!=0) return; // modo != OUT => salgo
  if (normal==0) return; //modo fast me voy
  int topeMuestras=100;
  double arrayOfTops[topeMuestras];
  int contArrayOfTops=0;
  //Serial.println(F("--------------Muestras-----------");
  double acumuladorPromedio=0;
  for (int i=0; i<topeMuestras; i++) {
    //Obtengo todos los valores y los aguardo para tomar lods 20 mas altos
    
    
    int k=analogRead(A0);
    
    //Serial.println(k);
    /*
    while(!(ADCSRA & 0x10)); // wait for adc to be ready
    ADCSRA = 0xf5; // restart adc
    byte m = ADCL; // fetch adc data
    byte j = ADCH;
    int k = (j << 8) | m; // form into an int
    //k -= 0x0200; // form into a signed int
    //k <<= 6; // form into a 16b signed int
    */
    //if (lecturaMic <=1023) 
      //Serial.println(F(lecturaMic);
    
    
    
    //acumuladorPromedio=acumuladorPromedio+k;
    
    arrayOfTops[contArrayOfTops]=k;
    contArrayOfTops++;
  }

  //Hago burbuja para ordenarlos
  double temp=0;
  for (int i=0; i<topeMuestras; i++){
    for (int j=0 ; j<topeMuestras - 1; j++){
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
  int cantidadAPromediar=topeMuestras-10;
  for (int i=cantidadAPromediar-1; i<topeMuestras; i++) {
    //Serial.println(F(arrayOfTops[i]);
    sum=sum+arrayOfTops[i];
  }
  //GLOBAL_ruidoPromedio=acumuladorPromedio/topeMuestras;

  GLOBAL_ruidoPromedio=sum/10;

  if(GLOBAL_ruidoPromedio<100){
    digitalWrite(8, HIGH);
  }else{
    digitalWrite(8, LOW);
  }
  
  Serial.println(F("PROMEDIO DIO:"));
  Serial.println(GLOBAL_ruidoPromedio);  
}

void reccmd(String sms){
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
  if (sms=="T0"){
    BT1.write("T0|");
    BT1.flush();
  }
  if (sms=="C1"){//out
    setModoOut();
    BT1.write("C1|"); //out
    delay(500);
    BT1.flush();
    delay(500);
  }
  if (sms=="C2"){//in
    setModoIn();
    BT1.write("C2|"); // in
    delay(50000);
    BT1.flush();
    delay(50000);
  }
  
  if (sms=="CE"){
    cleanEEPROM();
    BT1.write("CE|"); // Format EEPROM
    BT1.flush(); 
  }

  if (sms[0]=='B'){
    estado=1;  //0 lecturea, 1 grabando
    
    String ss=sms.substring(1 ,sms.length());
    IDSoundGrab = ss.toInt();
    
    eDeleteSound(IDSoundGrab);
    BT1.write('B');
    BT1.print(IDSoundGrab);
    BT1.write('|');    
    BT1.flush();
    
    IDSoundGrab=-1;
    estado=0;  //0 lecturea, 1 grabando
  }
  
  if (sms[0]=='R'){
    setModoIn();
    estado=1;  //0 lecturea, 1 grabando
    
    String ss=sms.substring(1 ,sms.length());
    IDSoundGrab = ss.toInt();
    
    grabar(IDSoundGrab);
    BT1.write('G');
    BT1.print(IDSoundGrab);
    BT1.write('|');
    delay(50000);  
    BT1.flush();
    delay(50000);
    
    IDSoundGrab=-1;
    estado=0;  //0 lecturea, 1 grabando
    delay(400000);
  }
  
  if (sms=="G"){
    setModoIn();    
    estado=1;  //0 lecturea, 1 grabando
    IDSoundGrab=-1;
    grabar(-1);
    BT1.write('G');
    BT1.print(IDSoundGrab);
    BT1.write('|'); 
    delay(50000);   
    BT1.flush();
    delay(50000);
    
    IDSoundGrab=-1;
    estado=0;  //0 lecturea, 1 grabando
    delay(400000);
  }
    
}

void iniDemo(){
  cleanEEPROM();  
  //Sonido 1 TIMBRE
  //https://www.youtube.com/watch?v=h46qbBy45Gg  
  int i=priPOSeeprom;
  EEPROM.write(i, 94);  
  i++;
  EEPROM.write(i, 48);  
  i++;
  EEPROM.write(i, 55);  
  i++;
  EEPROM.write(i, 122);  
  i++;
  EEPROM.write(i, 124);  
  i++;
  
  
  //Sonido 2 PAVA
  //https://www.youtube.com/watch?v=w1nb1lYfc8o

  
  EEPROM.write(i, 38);  
  i++;
  EEPROM.write(i, 77);  
  i++;
  EEPROM.write(i, 91);  
  i++;
  EEPROM.write(i, 95);  
  i++;
  EEPROM.write(i, 111);  
  i++;
 
  //Sonido 3
  //https://www.youtube.com/watch?v=ErLji-xsnTQ
  EEPROM.write(i, 84);  
  i++;
  EEPROM.write(i, 49);  
  i++;
  EEPROM.write(i, 59);  
  i++;
  EEPROM.write(i, 42);  
  i++;
  EEPROM.write(i, 31);  
  i++;  
}  

//---- Funciones EEPROM ----
        /*
            //1024bytes:
            //arduino value: the value to write, from 0 to 255 (byte)
            //Nosotros guardos el valor de las frecuencias, y son de 0a128
          
            -el grabar , busca la primer posiicon libre"0", cuando la encuentra graba las siguiente 5 (utilizando simempre la primer parte de la memoria)
              -el reemplazar marca los casilleros borrados con el valor 0. y quizas actualiza la ultimaposicionusada de la epprom
              -para recorrer se utilizan todos los valores distintos de (0) y que eesten entre el min y el max de la epprom.
         */
         //int priPOSeeprom=5;   //los primeros 5 son de config
            //0: modo funcionamiento 1, 2
            //1: nivel Sensibilidad  1, 2, 3
            //2: reservado
            //3: reservado
            //4: reservado
 
        //int ultPOSeeprom=5;   //se actualizara con el maximo puede haber espacios en el medio               
 
        int CalcularpriPOSVaciaeeprom(){
           for (int i = priPOSeeprom; i <= EEPROM.length()-1; i++)
           {
                if (EEPROM.read(i) == 0)
                    return i;               
           }          
           return -1;   //EEPROM LLENA
        }
 
        int CalcularultPOSeeprom(){                     
           for (int i = EEPROM.length()-1; i >= priPOSeeprom ; i--)
           {
              //Serial.print(i);
              //Serial.print(":");
              //Serial.println(F(EEPROM.read(i));
                if (EEPROM.read(i) != 0)
                    return (i+1);
           }
           return priPOSeeprom;  //EEPROM VACIA
        }
 
        int eSaveSound(int s[5]){
            int i=CalcularpriPOSVaciaeeprom();

            Serial.print(F("eeprom save :"));
            Serial.println(i);
            
              
            EEPROM.write(i, s[0]);
            EEPROM.write(i+1, s[1]);
            EEPROM.write(i+2, s[2]);
            EEPROM.write(i+3, s[3]);
            EEPROM.write(i+4, s[4]);
 
            if (ultPOSeeprom < (i+5)) //si grabo un sonido actualizo el puntero si es menor
                ultPOSeeprom=i+5;
            return i;
        }
 
        int eDeleteSound(int i){
            EEPROM.write(i, 0);
            EEPROM.write(i+1, 0);
            EEPROM.write(i+2, 0);
            EEPROM.write(i+3, 0);
            EEPROM.write(i+4, 0);
           
            if (ultPOSeeprom == i+5) //si borre el ultimo sonido actulizo el puntero a fin
                ultPOSeeprom=i;
            return i;
        }
 
        int eUpdateSound(int i, int s[5]){
            if (i<priPOSeeprom) return -1;
            EEPROM.write(i, s[0]);
            EEPROM.write(i+1, s[1]);
            EEPROM.write(i+2, s[2]);
            EEPROM.write(i+3, s[3]);
            EEPROM.write(i+4, s[4]);
            return i;
        }
 
        void cleanEEPROM()
        {           
            for (int i = 0/*priPOSeeprom*/; i < EEPROM.length(); i++)
                EEPROM.write(i, 0);           
            
            if (modo==0)
              EEPROM.write(0,1);
            else
              EEPROM.write(0,2);
              
            ultPOSeeprom=priPOSeeprom;
 
        }
        void initEEPROM(){
            if ( (EEPROM.read(0)!=1) && (EEPROM.read(0)!=2) ){ //nunca inicio el programa
                Serial.println(F("INICIANDO EEPROM - PRIMERA VEZ "));
                cleanEEPROM();
                if (modo==0)
                  EEPROM.write(0,1);
                else
                  EEPROM.write(0,2);  
            }
            else{
                ultPOSeeprom=CalcularultPOSeeprom();
                Serial.println(F("ultima pos eeprom "));
                Serial.println(ultPOSeeprom );
            }
        }
       
        //usos
            //en el setup : initEEPROM();
            //en operacion
                //eSaveSound([1,2,3,4]);
                //eDeleteSound(idsound);
                //eRenameSound(idsound, [1,2,3,4]);
                //cleanEEPROM();
 
        
//---- Funciones EEPROM ----


void grabar(int id){  
    //grabo picos no count de frecuencias, ya que en la lectura me quedo con los picos en aplitud de las 3 frecuencias mas altas en amp
    //por ende cambio el grabar, para que tenga en cuenta la cantidad de frecuencias picos, pero no solo con el filtro, sino siempre guardo la frecuencia de los 3 pcios mas altos.
    
  //PROCESO GRABACION
  int picosDeMuestrasGrabacion[FHT_N/2];
  //Inicializo
  for (int i = 0 ; i < FHT_N/2 ; i++) {
    picosDeMuestrasGrabacion[i]=0;
  }
  
  Serial.println(F("Comenzado a grabar el sonido en 2 segundos"));
  delay(200000);
  Serial.println(F("Comenzado a grabar YA"));

  /*
  bool comenzo=false;
  int valorInicial=200;

  cli();
  while(!comenzo){
    while(!(ADCSRA & 0x10)); // wait for adc to be ready
    ADCSRA = 0xf5; // restart adc
    byte m = ADCL; // fetch adc data
    byte j = ADCH;
    int k = (j << 8) | m; // form into an int
    k -= 0x0200; // form into a signed int
    k <<= 6; // form into a 16b signed int
    Serial.println(F(k);
    if (k > valorInicial){
      Serial.println(F("SIIIII");
      comenzo=true;
    }
    delayMicroseconds(DELAY_ANCHO_BANDA);      //
  } 
  */    
  
  //Uso los primeros segundos para grabar un sonido y usarlo después para los picos.
  int cantidadDeCiclos=400;
  cli();  // UDRE interrupt slows this way down on arduino1.0
  while(cantidadDeCiclos>=0) { // reduces jitter
    for (int i = 0 ; i < FHT_N ; i++) { // save 256 samples
      while(!(ADCSRA & 0x10)); // wait for adc to be ready
      ADCSRA = 0xf5; // restart adc
      byte m = ADCL; // fetch adc data
      byte j = ADCH;
      int k = (j << 8) | m; // form into an int
      k -= 0x0200; // form into a signed int
      k <<= 6; // form into a 16b signed int
      fht_input[i] = k; // put real data into bins
      delayMicroseconds(DELAY_ANCHO_BANDA);      //
    }
    fht_window(); // window the data for better frequency response
    fht_reorder(); // reorder the data before doing the fht
    fht_run(); // process the data in the fht
    fht_mag_log(); // take the output of the fht
    
    cantidadDeCiclos=cantidadDeCiclos-1;

    //Obtengo los picos y sus posiciones de esa tirada de audio
    int tresPrimerosPicos[4];
    tresPrimerosPicos[0]=0;
    tresPrimerosPicos[1]=0;
    tresPrimerosPicos[2]=0;
    tresPrimerosPicos[3]=0;
    int tresPrimerosPicosPos[4];
    tresPrimerosPicosPos[0]=0;
    tresPrimerosPicosPos[1]=0;
    tresPrimerosPicosPos[2]=0;
    tresPrimerosPicosPos[3]=0;
    
    
    for (int i = 0 ; i < FHT_N/2 ; i++) {
      //Obtengo los picos
      if(i!=0){
        if(fht_log_out[i]>fht_log_out[i-1] && fht_log_out[i]>fht_log_out[i+1] && fht_log_out[i]>60 && i>10){//AHI PUSE 40 DE REFERENCIA
          //Es pico, incremento su pos
           //Serial.print(i);
           //Serial.print(":");
           //Serial.println(F(fht_log_out[i]);

//------------------------------------------------------------------------ me quedo con los 3 mas altos
        if(fht_log_out[i]>tresPrimerosPicos[0]){
          tresPrimerosPicos[3]=tresPrimerosPicos[2];
          tresPrimerosPicosPos[3]=tresPrimerosPicosPos[2];  
          tresPrimerosPicos[2]=tresPrimerosPicos[1];
          tresPrimerosPicosPos[2]=tresPrimerosPicosPos[1];            
          tresPrimerosPicos[1]=tresPrimerosPicos[0]; 
          tresPrimerosPicosPos[1]=tresPrimerosPicosPos[0];                       
          tresPrimerosPicos[0]=fht_log_out[i];
          tresPrimerosPicosPos[0]=i;
         
        }else{
          if(fht_log_out[i]>tresPrimerosPicos[1]){
            tresPrimerosPicos[3]=tresPrimerosPicos[2];
            tresPrimerosPicosPos[3]=tresPrimerosPicosPos[2];  
            tresPrimerosPicos[2]=tresPrimerosPicos[1];
            tresPrimerosPicosPos[2]=tresPrimerosPicosPos[1];              
            tresPrimerosPicos[1]=fht_log_out[i];
            tresPrimerosPicosPos[1]=i;
            
          }else{
            if(fht_log_out[i]>tresPrimerosPicos[2]){
              tresPrimerosPicos[3]=tresPrimerosPicos[2];
              tresPrimerosPicosPos[3]=tresPrimerosPicosPos[2]; 
              tresPrimerosPicos[2]=fht_log_out[i];
              tresPrimerosPicosPos[2]=i;
              
            }else{
              if(fht_log_out[i]>tresPrimerosPicos[3]){
                tresPrimerosPicos[3]=fht_log_out[i];
                tresPrimerosPicosPos[3]=i;
                
              }
            }
          }
        }
        

//--------------------------------------------------------------------------------------------------
          /*error fuera del for va
          //contar la canidad de veces que un pico es encontrado
          if (tresPrimerosPicosPos[0]>10)  picosDeMuestrasGrabacion[tresPrimerosPicosPos[0]]++;
          if (tresPrimerosPicosPos[1]>10)  picosDeMuestrasGrabacion[tresPrimerosPicosPos[1]]++;
          if (tresPrimerosPicosPos[2]>10)  picosDeMuestrasGrabacion[tresPrimerosPicosPos[2]]++;
          if (tresPrimerosPicosPos[3]>10)  picosDeMuestrasGrabacion[tresPrimerosPicosPos[3]]++;
          */
        }
      }
    }//for de 1 muestra de 256 valores

    //contar la canidad de veces que un pico es encontrado
    if (tresPrimerosPicosPos[0]>10)  picosDeMuestrasGrabacion[tresPrimerosPicosPos[0]]++;
    if (tresPrimerosPicosPos[1]>10)  picosDeMuestrasGrabacion[tresPrimerosPicosPos[1]]++;
    if (tresPrimerosPicosPos[2]>10)  picosDeMuestrasGrabacion[tresPrimerosPicosPos[2]]++;
    if (tresPrimerosPicosPos[3]>10)  picosDeMuestrasGrabacion[tresPrimerosPicosPos[3]]++;
    
  } //del while de n muetras
  sei();

  /*Serial.println(F("-----Veo picos caracteristicos------");
for (int i = 0 ; i < FHT_N/2 ; i++) {
  
  Serial.println(F(picosDeMuestrasGrabacion[i]);
}

delay(200000);
  Serial.println(F("-----Veo picos caracteristicos------");


  Serial.println(F("Sonido finalizado de grabar, comenzando a escuchar en 2 segundos");
  delay(200000);
*/
  int picosConocidos[5];
  picosConocidos[0]=0;
  picosConocidos[1]=0;
  picosConocidos[2]=0;
  picosConocidos[3]=0;
  picosConocidos[4]=0;
  int picosConocidosValor[5];
  picosConocidosValor[0]=0;
  picosConocidosValor[1]=0;
  picosConocidosValor[2]=0;
  picosConocidosValor[3]=0;
  picosConocidosValor[4]=0;
  
  //Obtengo del vector aquellos 3 que tengan mas veces contabilizados y seran los 3 picos que voy a grabar
  for (int i = 0 ; i < FHT_N/2 ; i++) {
    if(picosDeMuestrasGrabacion[i]>0){
        //picosDeMuestrasGrabacion[i] cantidad de veces que aparecio la frecuencia i
      if(picosDeMuestrasGrabacion[i]>picosConocidosValor[0]){
        //Lo grabo en la primera y corro los otros    
        picosConocidosValor[4]=picosConocidosValor[3];
        picosConocidosValor[3]=picosConocidosValor[2];    
        picosConocidosValor[2]=picosConocidosValor[1];
        picosConocidosValor[1]=picosConocidosValor[0];
        picosConocidosValor[0]=picosDeMuestrasGrabacion[i];
        picosConocidos[4]=picosConocidos[3];
        picosConocidos[3]=picosConocidos[2];
        picosConocidos[2]=picosConocidos[1];
        picosConocidos[1]=picosConocidos[0];
        picosConocidos[0]=i;
      }else{
        if(picosDeMuestrasGrabacion[i]>picosConocidosValor[1]){
          //Lo grabo en la segunda y corro los otros dos  
          picosConocidosValor[4]=picosConocidosValor[3];
          picosConocidosValor[3]=picosConocidosValor[2];      
          picosConocidosValor[2]=picosConocidosValor[1];
          picosConocidosValor[1]=picosDeMuestrasGrabacion[i];
          picosConocidos[4]=picosConocidos[3];
          picosConocidos[3]=picosConocidos[2];
          picosConocidos[2]=picosConocidos[1];
          picosConocidos[1]=i;
        }else{
          if(picosDeMuestrasGrabacion[i]>picosConocidosValor[2]){
            //Lo grabo en la segunda y corro los otros dos  
            picosConocidosValor[4]=picosDeMuestrasGrabacion[3];
            picosConocidosValor[3]=picosDeMuestrasGrabacion[2];      
            picosConocidosValor[2]=picosDeMuestrasGrabacion[i];
            picosConocidos[4]=picosConocidos[3];
            picosConocidos[3]=picosConocidos[2];
            picosConocidos[2]=i;
          }else{
            if(picosDeMuestrasGrabacion[i]>picosConocidosValor[3]){
              picosConocidosValor[4]=picosDeMuestrasGrabacion[3];
              picosConocidosValor[3]=picosDeMuestrasGrabacion[i]; 
              picosConocidos[4]=picosConocidos[3];
              picosConocidos[3]=i;
            }else{
              if(picosDeMuestrasGrabacion[i]>picosConocidosValor[4]){
                picosConocidosValor[4]=picosDeMuestrasGrabacion[i]; 
                picosConocidos[4]=i;
              }
            }
          }
        }
      }
    }
  }

  //Serial.println(F("Picos grabados");
  //Finalizado esto ya tengo en picosconocidos las pos de los 3 picos mas caracterisiticos del sonido recien escuchado

  Serial.println(F("Grabo en la eeprom desde:"));
  int getPrimeraPosParaGrabarLas5=0;
  if (id!=-1){
    getPrimeraPosParaGrabarLas5=eUpdateSound(id, picosConocidos);
  }else
    getPrimeraPosParaGrabarLas5=eSaveSound(picosConocidos);
  
  Serial.println(F("valores:"));
  Serial.println(getPrimeraPosParaGrabarLas5);
  
  for (int i = 0 ; i < 5 ; i++) {
      Serial.println(EEPROM.read(getPrimeraPosParaGrabarLas5+i));
  }
  IDSoundGrab=getPrimeraPosParaGrabarLas5;
  
  //TERMINO PROCESO GRABACION
}

void modoPatron() {

int idsound=-1;
int cantidadCiclos=0;
int cantidadEncuentros=0;
int cantidadCiclosEncuentro=1;
int picosConocidosDeMemoria[5];

while(cantidadCiclos<cantidadCiclosEncuentro){

  //cli();  // UDRE interrupt slows this way down on arduino1.0
  for (int i = 0 ; i < FHT_N ; i++) { // save 256 samples
    while(!(ADCSRA & 0x10)); // wait for adc to be ready
    ADCSRA = 0xf5; // restart adc
    byte m = ADCL; // fetch adc data
    byte j = ADCH;
    int k = (j << 8) | m; // form into an int
    k -= 0x0200; // form into a signed int
    k <<= 6; // form into a 16b signed int
    fht_input[i] = k; // put real data into bins
    delayMicroseconds(DELAY_ANCHO_BANDA);      //
  }
  fht_window(); // window the data for better frequency response
  fht_reorder(); // reorder the data before doing the fht
  fht_run(); // process the data in the fht
  fht_mag_log(); // take the output of the fht
  //sei();
    
  //Serial.println(F("----Ya Tome las Lecturas-----");
  
  //int picos[128];
  int tresPrimerosPicos[4];
  tresPrimerosPicos[0]=0;
  tresPrimerosPicos[1]=0;
  tresPrimerosPicos[2]=0;
  tresPrimerosPicos[3]=0;
  int tresPrimerosPicosPos[4];
  tresPrimerosPicosPos[0]=0;
  tresPrimerosPicosPos[1]=0;
  tresPrimerosPicosPos[2]=0;
  tresPrimerosPicosPos[3]=0;
  
  for (int i = 0 ; i < FHT_N/2 ; i++) {
    //picos[i]=0;
    //Obtengo los picos
    if(i!=0){
      if(fht_log_out[i]>fht_log_out[i-1] && fht_log_out[i]>fht_log_out[i+1] && fht_log_out[i]>60 && i>10){//AHI PUSE 40 DE REFERENCIA
        if(fht_log_out[i]>tresPrimerosPicos[0]){
          tresPrimerosPicos[3]=tresPrimerosPicos[2];
          tresPrimerosPicosPos[3]=tresPrimerosPicosPos[2];  
          tresPrimerosPicos[2]=tresPrimerosPicos[1];
          tresPrimerosPicosPos[2]=tresPrimerosPicosPos[1];            
          tresPrimerosPicos[1]=tresPrimerosPicos[0]; 
          tresPrimerosPicosPos[1]=tresPrimerosPicosPos[0];                       
          tresPrimerosPicos[0]=fht_log_out[i];
          tresPrimerosPicosPos[0]=i;
        }else{
          if(fht_log_out[i]>tresPrimerosPicos[1]){
            tresPrimerosPicos[3]=tresPrimerosPicos[2];
            tresPrimerosPicosPos[3]=tresPrimerosPicosPos[2];  
            tresPrimerosPicos[2]=tresPrimerosPicos[1];
            tresPrimerosPicosPos[2]=tresPrimerosPicosPos[1];              
            tresPrimerosPicos[1]=fht_log_out[i];
            tresPrimerosPicosPos[1]=i;
          }else{
            if(fht_log_out[i]>tresPrimerosPicos[2]){
              tresPrimerosPicos[3]=tresPrimerosPicos[2];
              tresPrimerosPicosPos[3]=tresPrimerosPicosPos[2]; 
              tresPrimerosPicos[2]=fht_log_out[i];
              tresPrimerosPicosPos[2]=i;
            }else{
              if(fht_log_out[i]>tresPrimerosPicos[3]){
                tresPrimerosPicos[3]=fht_log_out[i];
                tresPrimerosPicosPos[3]=i;
              }
            }
          }
        }
        //picos[i]=fht_log_out[i];
      }
    }
  }


  /*Serial.println(F("-----Picos escuchados------");
  //delay(300000);
  for (int i = 0 ; i < 3 ; i++) {
   //Serial.println(F(tresPrimerosPicosPos[i]);
  }*/

  //si ya encontre uno, me fijo si este mismo aparece n veces durante la cantidad de tomas.
  if (cantidadEncuentros>=1){
    int picosusado[5];
    for (int i = 0 ; i <= 4 ; i++) picosusado[i]=0;  
    int con=0;
    
    for (int i = 0 ; i <= 3 ; i++) { //los picos
      for (int p=0; p<=4; p++){// la memo
        if( 
                ((picosConocidosDeMemoria[p]-delta)<= tresPrimerosPicosPos[i]) 
                && ((picosConocidosDeMemoria[p]+delta)>= tresPrimerosPicosPos[i]) 
                && (picosusado[p]==0) 
        ){
          con++;
          picosusado[p]=1;
          break;
        }
      }
    }
    if (con>=3){
      cantidadEncuentros++;
      Serial.println(F("Encontro una vez B"));
      //break; // ojo CORTO si ya encontre 2 veces!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }
    
  }else{
    
    int valorHastaDondeLeoDeLaEEPROM=ultPOSeeprom;
    //Serial.println(F("Hasta donde leo de la EEPROM");
    //Serial.println(F(valorHastaDondeLeoDeLaEEPROM);
    //bool CantCoicidencia[valorHastaDondeLeoDeLaEEPROM]; //vector que guardara la cantida de coincidencias por patron ? tiene sentido, o si le pega a 3 listo adentro?
    if (valorHastaDondeLeoDeLaEEPROM <= 0) return;
    
    //Comparo lo que escuché recién con algo conocido
    for (int i = priPOSeeprom ; i <= valorHastaDondeLeoDeLaEEPROM ; i=i+5) {
      if (EEPROM.read(i)==0) continue; //valor borrado
      
      idsound=i;
      //Me guardo de a 5
      picosConocidosDeMemoria[5];
      picosConocidosDeMemoria[0]=EEPROM.read(i);
      picosConocidosDeMemoria[1]=EEPROM.read(i+1);
      picosConocidosDeMemoria[2]=EEPROM.read(i+2);
      picosConocidosDeMemoria[3]=EEPROM.read(i+3);
      picosConocidosDeMemoria[4]=EEPROM.read(i+4);

      /*
          //Lo que leo de la EEPROM:
          Serial.println(F("VOy a comparar contra esto de la eeprom:");
          Serial.println(F(picosConocidosDeMemoria[0]);
          Serial.println(F(picosConocidosDeMemoria[1]);
          Serial.println(F(picosConocidosDeMemoria[2]);
          Serial.println(F(picosConocidosDeMemoria[3]);
          Serial.println(F(picosConocidosDeMemoria[4]);
          //delay(1000000);
          Serial.println(F("------------Vs picos:-------------");
          Serial.println(F(tresPrimerosPicosPos[0]);
          Serial.println(F(tresPrimerosPicosPos[1]);
          Serial.println(F(tresPrimerosPicosPos[2]);
          Serial.println(F(tresPrimerosPicosPos[3]);
      */    

      int picosusado[5];
      for (int i = 0 ; i <= 4 ; i++) picosusado[i]=0;  
      int con=0;
      
      /*int picosConocidosDeMemoriaUSADO[5];
      for (int i = 0 ; i < 5 ; i++) picosConocidosDeMemoriaUSADO[i]=0;*/
      /*
      tresPrimerosPicosPos[0]=1;
      tresPrimerosPicosPos[1]=51;
      tresPrimerosPicosPos[2]=1;
      tresPrimerosPicosPos[3]=1;
      */
      
      for (int i = 0 ; i <= 3 ; i++) { //los picos
        for (int p=0; p<=4; p++){// la memo
          if( 
                ((picosConocidosDeMemoria[p]-delta)<= tresPrimerosPicosPos[i]) 
                && ((picosConocidosDeMemoria[p]+delta)>= tresPrimerosPicosPos[i]) 
                && (picosusado[p]==0) 
          ){
            /*
            Serial.println(F(picosConocidosDeMemoria[p]-delta);
            Serial.println(F(tresPrimerosPicosPos[i]);
            Serial.println(F(picosConocidosDeMemoria[p]+delta);
            Serial.println(F(picosusado[i]);
            */
            con++;
            picosusado[p]=1;
            break;
          }
        }
      }

      if (con>=3){
        /*
        Serial.println(F("Lo que lei: "); 
        for (int i = 0 ; i <= 3 ; i++) Serial.println(F(tresPrimerosPicosPos[i]); 
        Serial.println(F("sonido encontrado: "); 
        for (int i = 0 ; i <= 4 ; i++) Serial.println(F(picosConocidosDeMemoria[i]); 
        Serial.println(F("sonido encontrado: ");  
        Serial.println(F(idsound);  
        for (int i = 0 ; i <= 4 ; i++) Serial.println(F(picosusado[i]);  
        */
        cantidadCiclosEncuentro=6; //incremento el contador y voy a procesar de nuevo el while
        cantidadEncuentros++;
        Serial.println(F("Encontro una vez A"));
        break;
      }
    }//for eeprom
  }//encontre>=1
  
  cantidadCiclos++;
}//While de encontrar


  if(cantidadEncuentros>2){

    Serial.println(F("Encontro mas de una"));
    Serial.println(cantidadEncuentros);
    
    //Prendo
     //Prendo led
      digitalWrite(ledPin1, HIGH);
      //Envio sms x blue

      sms="N";
      sms=sms+idsound+"|";
      Serial.println(sms);
      
      //BT1.print('N'+idsound+'|');
      //BT1.write(idsound+'|');
      /*BT1.write(idsound);
      BT1.write('|');
      BT1.flush();*/

      BT1.write('N');
      delay(300000);
      BT1.print(idsound);
      delay(300000);
      BT1.write('|');
      delay(300000);
      BT1.flush();
      delay(300000);
      digitalWrite(ledPin1, LOW);
  }
}
