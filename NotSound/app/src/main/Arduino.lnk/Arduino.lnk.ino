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
    
  //ojo delay en modo fast
  //delay(100.000); 1s y noarmal 1.000=1s
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
  setFast();
}



//Esta funcion toma 500 muestras y promedia el ruido para setear nuevo ruido promedio
void callback()
{
  if (modo!=0) return; // modo != OUT => salgo
  //if (normal==0) return; //modo fast me voy
  
  double arrayOfTops[50];
  int contArrayOfTops=0;
  //Serial.println("--------------Muestras-----------");
  for (int i=0; i<50; i++) {
    //Obtengo todos los valores y los aguardo para tomar lods 20 mas altos
    
    while(!(ADCSRA & 0x10)); // wait for adc to be ready
    ADCSRA = 0xf5; // restart adc
    byte m = ADCL; // fetch adc data
    byte j = ADCH;
    int k = (j << 8) | m; // form into an int
    //k -= 0x0200; // form into a signed int
    //k <<= 6; // form into a 16b signed int
    
    //if (lecturaMic <=1023) 
      //Serial.println(lecturaMic);
    
    arrayOfTops[contArrayOfTops]=k;
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
    //Serial.println("Antes modo 0");
    if (modo==0){ // modo OUT
      //Serial.println("modo 0");
      //Vamos leyendo lo que capta el mic
      //si supera valor promedio, prendemos led
      while(!(ADCSRA & 0x10)); // wait for adc to be ready
      ADCSRA = 0xf5; // restart adc
      byte m = ADCL; // fetch adc data
      byte j = ADCH;
      int k = (j << 8) | m; // form into an int
      //k -= 0x0200; // form into a signed int
      //k <<= 6; // form into a 16b signed int
      
      
      //if(lecturaMic>(GLOBAL_ruidoPromedio*procentajeSuperacionPromedio)){
      if(k>(GLOBAL_ruidoPromedio +  procentajeSuperacionPromedio)){
        //Prendo led
        digitalWrite(ledPin1, HIGH);
        //Envio sms x blue
        BT1.write("NA|");
        
        callback();
        delay(200000);
      }else{
        digitalWrite(ledPin1, LOW);
      }
    }else{ //modo IN
      //Serial.println("modo 1");
      modoPatron();
    }
  }
}

int primeraPosVaciaEEPROM(){
   for (int i = 0 ; i < EEPROM.length() ; i++) {
    if(EEPROM.read(i)==0){
      return i;
    }
   }
   //EEPROM LLENA
   return 0;
}


int ultimaPosLlenaEEPROM(){
  if(EEPROM.read(0)==0){
    return 0;
  }
    
   for (int i = 0 ; i < EEPROM.length() ; i++) {
    if(EEPROM.read(i+1)==0){
      return i;
    }
   }
  
   return EEPROM.length();
}

void cleanEEPROM(){
  for (int i = 0 ; i < EEPROM.length() ; i++) {
    EEPROM.write(i, 0);
  }
}

void grabar(){
  //PROCESO GRABACION
  int picosDeMuestrasGrabacion[FHT_N/2];
  //Inicializo
  for (int i = 0 ; i < FHT_N/2 ; i++) {
    picosDeMuestrasGrabacion[i]=0;
  }


  Serial.println("Comenzado a grabar el sonido en 2 segundos");
  delay(200000);
  Serial.println("Comenzado a grabar YA");
  
  //Uso los primeros segundos para grabar un sonido y usarlo después para los picos.
  int cantidadDeCiclos=100;
  while(cantidadDeCiclos>=0) { // reduces jitter
    cli();  // UDRE interrupt slows this way down on arduino1.0
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
    sei();
    cantidadDeCiclos=cantidadDeCiclos-1;

    //Obtengo los picos y sus posiciones de esa tirada de audio
    /*int tresPrimerosPicos[5];
    tresPrimerosPicos[0]=0;
    tresPrimerosPicos[1]=0;
    tresPrimerosPicos[2]=0;
    tresPrimerosPicos[3]=0;
    tresPrimerosPicos[4]=0;
    int tresPrimerosPicosPos[5];
    tresPrimerosPicosPos[0]=0;
    tresPrimerosPicosPos[1]=0;
    tresPrimerosPicosPos[2]=0;
    tresPrimerosPicosPos[3]=0;
    tresPrimerosPicosPos[4]=0;*/
    for (int i = 0 ; i < FHT_N/2 ; i++) {
      //Obtengo los picos
      if(i!=0){
        if(fht_log_out[i]>fht_log_out[i-1] && fht_log_out[i]>fht_log_out[i+1] && fht_log_out[i]>35 && i>10){//AHI PUSE 40 DE REFERENCIA
          //Es pico, incremento su pos
          picosDeMuestrasGrabacion[i]=picosDeMuestrasGrabacion[i]+1;
        }
      }
    }    
  }


  /*Serial.println("-----Veo picos caracteristicos------");
for (int i = 0 ; i < FHT_N/2 ; i++) {
  
  Serial.println(picosDeMuestrasGrabacion[i]);
}

delay(200000);
  Serial.println("-----Veo picos caracteristicos------");


  Serial.println("Sonido finalizado de grabar, comenzando a escuchar en 2 segundos");
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

  //Serial.println("Picos grabados");
  //Finalizado esto ya tengo en picosconocidos las pos de los 3 picos mas caracterisiticos del sonido recien escuchado

  int getPrimeraPosParaGrabarLas5=primeraPosVaciaEEPROM();
  Serial.println("Voy a grabar eeprom a partir de aca:");
  Serial.println(getPrimeraPosParaGrabarLas5);


  Serial.println("Grabo en la eeprom:");
  for (int i = 0 ; i < 5 ; i++) {
    /*if(EEPROM.length()==0){
      Serial.println("eprom vacia");
      EEPROM.write(EEPROM.length()+i, picosConocidos[i]);
      Serial.println(EEPROM.read(EEPROM.length()+i));
    }else{*/
      
      EEPROM.write(getPrimeraPosParaGrabarLas5+i, picosConocidos[i]);
      Serial.println(EEPROM.read(getPrimeraPosParaGrabarLas5+i));
   /* }*/
  }

  IDSoundGrab=getPrimeraPosParaGrabarLas5;
  
  //TERMINO PROCESO GRABACION
}

void modoPatron() {

  Serial.println("----Comienza la escucha-----");

  cli();  // UDRE interrupt slows this way down on arduino1.0
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
  sei();
    
  Serial.println("----Ya Tome las Lecturas-----");
  
  //int picos[128];
  int tresPrimerosPicos[3];
  tresPrimerosPicos[0]=0;
  tresPrimerosPicos[1]=0;
  tresPrimerosPicos[2]=0;
  int tresPrimerosPicosPos[3];
  tresPrimerosPicosPos[0]=0;
  tresPrimerosPicosPos[1]=0;
  tresPrimerosPicosPos[2]=0;
  
  for (int i = 0 ; i < FHT_N/2 ; i++) {
    //picos[i]=0;
    //Obtengo los picos
    if(i!=0){
      if(fht_log_out[i]>fht_log_out[i-1] && fht_log_out[i]>fht_log_out[i+1] && fht_log_out[i]>35){//AHI PUSE 40 DE REFERENCIA
        if(fht_log_out[i]>tresPrimerosPicos[0]){
          tresPrimerosPicos[2]=tresPrimerosPicos[1];
          tresPrimerosPicosPos[2]=tresPrimerosPicosPos[1];            
          tresPrimerosPicos[1]=tresPrimerosPicos[0]; 
          tresPrimerosPicosPos[1]=tresPrimerosPicosPos[0];                       
          tresPrimerosPicos[0]=fht_log_out[i];
          tresPrimerosPicosPos[0]=i;
        }else{
          if(fht_log_out[i]>tresPrimerosPicos[1]){
            tresPrimerosPicos[2]=tresPrimerosPicos[1];
            tresPrimerosPicosPos[2]=tresPrimerosPicosPos[1];              
            tresPrimerosPicos[1]=fht_log_out[i];
            tresPrimerosPicosPos[1]=i;
          }else{
            if(fht_log_out[i]>tresPrimerosPicos[2]){
              tresPrimerosPicos[2]=fht_log_out[i];
              tresPrimerosPicosPos[2]=i;
            }
          }
        }
        //picos[i]=fht_log_out[i];
      }
    }
  }


  /*Serial.println("-----Picos escuchados------");
  //delay(300000);
  for (int i = 0 ; i < 3 ; i++) {
   //Serial.println(tresPrimerosPicosPos[i]);
  }*/


  int valorHastaDondeLeoDeLaEEPROM=ultimaPosLlenaEEPROM();
  Serial.println("Hasta donde leo de la EEPROM");
  Serial.println(valorHastaDondeLeoDeLaEEPROM);

  //bool CantCoicidencia[valorHastaDondeLeoDeLaEEPROM]; //vector que guardara la cantida de coincidencias por patron ? tiene sentido, o si le pega a 3 listo adentro?
  
  //Comparo lo que escuché recién con algo conocido
  for (int i = 0 ; i <= valorHastaDondeLeoDeLaEEPROM ; i=i+5) {
    //Me guardo de a 5
    int picosConocidosDeMemoria[5];
    picosConocidosDeMemoria[0]=EEPROM.read(i);
    picosConocidosDeMemoria[1]=EEPROM.read(i+1);
    picosConocidosDeMemoria[2]=EEPROM.read(i+2);
    picosConocidosDeMemoria[3]=EEPROM.read(i+3);
    picosConocidosDeMemoria[4]=EEPROM.read(i+4);

    //Lo que leo de la EEPROM:
    Serial.println("VOy a comparar contra esto de la eeprom:");
    Serial.println(picosConocidosDeMemoria[0]);
    Serial.println(picosConocidosDeMemoria[1]);
    Serial.println(picosConocidosDeMemoria[2]);
    Serial.println(picosConocidosDeMemoria[3]);
    Serial.println(picosConocidosDeMemoria[4]);
    //delay(1000000);


    int con=0;
    for (int i = 0 ; i < 3 ; i++) {
      for (int p=0; p<=4; p++){
        if( ((picosConocidosDeMemoria[p]-2)<= tresPrimerosPicosPos[i]) && ((picosConocidosDeMemoria[p]+2)>= tresPrimerosPicosPos[i]) )
          con++;
      }
    }
    
    if (con>=3){
      //Prendo led
      digitalWrite(ledPin1, HIGH);
      //Envio sms x blue
      BT1.write('N');
      BT1.print(i);
      BT1.write('|');
      delay(500000);
      digitalWrite(ledPin1, LOW);
      break;
    }
  }
  
}
