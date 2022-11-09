import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ComportamientoBase implements Runnable{
    String id, direccionJar,ipPropia;
    long horaDeMuerte;
    int generaciones, Puerto_Inicio, Rango_Puertos, tiempoDeVida, tiempo_espera_comportamiento_base,puertoUDP,puertoTCP;
    boolean puertos_aleatorios;
    double Frecuencia_partos, Frecuencia_rastreo_puertos;
    String ipInicial,ipFin;
    int puertosBuscar;

    GestorMensajes gm;
    Random random = new Random();

    ComportamientoBase(String id, int generaciones, int puerto_Inicio, int rango_Puertos, int tiempoDeVida, int tiempo_espera_comportamiento_base,
                       double frecuencia_partos, double frecuencia_rastreo_puertos, GestorMensajes gm,String ipPropia, int puertoUDP, int puertoTCP, String ipInicial, String ipFin, int puertosBuscar) {
        this.id = id;
        this.generaciones = generaciones;
        this.Puerto_Inicio = puerto_Inicio;
        this.Rango_Puertos = rango_Puertos;
        this.puertos_aleatorios = true;
        this.tiempoDeVida = tiempoDeVida;
        this.tiempo_espera_comportamiento_base = tiempo_espera_comportamiento_base;

        this.Frecuencia_partos = frecuencia_partos;
        this.Frecuencia_rastreo_puertos = frecuencia_rastreo_puertos;

        this.horaDeMuerte = System.currentTimeMillis() + (long) tiempoDeVida;
        this.direccionJar = "C:\\Users\\domin\\Desktop\\ACC.jar";

        this.gm = gm;
        this.ipFin=ipFin;
        this.ipInicial=ipInicial;
        this.ipPropia=ipPropia;
        this.puertoTCP=puertoTCP;
        this.puertoUDP=puertoUDP;
        this.puertosBuscar=puertosBuscar;
        new Thread(this, "comportamiento_base").start();
    }



    @Override
    public void run() {

        while (horaDeMuerte > System.currentTimeMillis()) {
            System.out.println("-------------------------------comportamiento base----------------------------");
            GenerarNuevoAcc(id);

            System.out.println(tiempo_espera_comportamiento_base);
            if(this.Frecuencia_rastreo_puertos >= this.random.nextDouble()) {
                try {
                    this.GestorDeDirectorio();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                Thread.sleep(tiempo_espera_comportamiento_base);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("fin");
        System.exit(0);     // Parar el agente
    }

    void GenerarNuevoAcc(String id) {
        try {
            if (Frecuencia_partos >= random.nextDouble() && generaciones > 0) {
                ProcessBuilder pb = new ProcessBuilder("C:/Program Files/Java/.../bin/java.exe", "-jar", direccionJar, "" + generaciones);
                pb.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void GestorDeDirectorio() throws InterruptedException {
        if(this.puertos_aleatorios){
            String siguienteIP = this.ipInicial;
            do {
                for (int j = 0; j < this.puertosBuscar; j++) {
                    int puerto = this.Puerto_Inicio + this.random.nextInt(this.Rango_Puertos);
                    if (puerto % 2 != 0) {   // comprobamos que ssea impar (UDP)
                        puerto++;}
                    // ponemos el mensaje de localización del host correspondiente en la lista de mensajes a enviar del gestor de mensajes
                    System.out.println("envia mensaje");
                    this.gm.AñadirMensajeContenedor(new Mensaje(gm.generaCab(String.valueOf(this.puertoUDP),this.id,this.ipPropia,String.valueOf(puerto),"idR",siguienteIP,"busqueda","UDP","1"),null,null));
                    siguienteIP = siguienteIP(siguienteIP);
                    System.out.println(siguienteIP);
                }
            }while (!siguienteIP.equals(this.ipFin)) ;
        }else{
            String siguienteIP = this.ipInicial;
            do{
                for (int puerto = Puerto_Inicio; puerto <= Puerto_Inicio + Rango_Puertos; puerto += 2) {
                    // ponemos el mensaje de localización del host correspondiente en la lista de mensajes a enviar del gestor de mensajes
                    this.gm.AñadirMensajeContenedor(new Mensaje(gm.generaCab(String.valueOf(this.puertoUDP),this.id,this.ipPropia,String.valueOf(puerto),"idR",siguienteIP,"busqueda","UDP","1"),null,null));
                }
                siguienteIP = siguienteIP(siguienteIP);
                System.out.println(siguienteIP);
            }while(!siguienteIP.equals(this.ipFin));
        }
    }

    //Funcion para actualizar IPs
    public static String siguienteIP(String ip){
        List<String> IP_split = Arrays.asList(ip.split("\\."));
        int[] IP_int = new int[4];

        for(int i = 0; i<4;i++)
            IP_int[i] = Integer.parseInt(IP_split.get(i));

        if(IP_int[3] < 255) IP_int[3] += 1;
        else{
            IP_int[3] = 0;
            if(IP_int[2]<255) IP_int[2] += 1;
            else{
                IP_int[2] = 0;
                if(IP_int[1]<255) IP_int[1] += 1;
                else{
                    IP_int[1] = 0;
                    IP_int[0] += 1;
                }
            }
        }
        return IP_int[0] + "." + IP_int[1] + "." + IP_int[2] + "." + IP_int[3];
    }
}
