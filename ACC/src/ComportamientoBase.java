import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
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
                       double frecuencia_partos, double frecuencia_rastreo_puertos, GestorMensajes gm,String ipPropia, int puertoUDP, int puertoTCP, String ipInicial, String ipFin, int puertosBuscar, boolean puertos_aleatorios) {
        this.id = id;
        this.generaciones = generaciones;
        this.Puerto_Inicio = puerto_Inicio;
        this.Rango_Puertos = rango_Puertos;
        this.puertos_aleatorios = puertos_aleatorios;
        this.tiempoDeVida = tiempoDeVida;
        this.tiempo_espera_comportamiento_base = tiempo_espera_comportamiento_base;

        this.Frecuencia_partos = frecuencia_partos;
        this.Frecuencia_rastreo_puertos = frecuencia_rastreo_puertos;

        this.horaDeMuerte = System.currentTimeMillis() + (long) tiempoDeVida;
        this.direccionJar = "C:\\Users\\"+ System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\ACC.jar";

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

        com.sun.management.OperatingSystemMXBean mxbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Muestra la memoria ram maxima del pc
        System.out.println("Memoria maxima: "+mxbean.getTotalMemorySize()/(10241024) + " MegaBytes ");
        // Minima memoria libre permitida, si la maquina sobrepasa este limite de memoria el agente se muere
        double topeDeMemoriaMinima = mxbean.getTotalMemorySize()*0.5;
        // Muetra el limite
        System.out.println("Minima memoria libre: "+topeDeMemoriaMinima/(1024*1024) + " MegaBytes ");


        while (horaDeMuerte > System.currentTimeMillis()) {
            //------------------------------- COMPORTAMIENTO BASE ----------------------------
            // Aplica el porcentaje de generacion y comprueba que no es la ultima generacion
            if (this.Frecuencia_partos >= this.random.nextDouble() && mxbean.getFreeMemorySize() > topeDeMemoriaMinima) {
                GenerarNuevoAcc(id);
            }

            if(this.Frecuencia_rastreo_puertos >= this.random.nextDouble()) {
                GestorDeDirectorio();
            }

            try {
                Thread.sleep(tiempo_espera_comportamiento_base);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Fin del agente");
        gm.cerrarSockets();
        System.exit(0);     // Parar el agente
    }

    /**
     * Autores: Ignacio Gago Lopez, Pablo Domingo Fernandez, Alejandro Cebrian Sanchez, Daniel Cuenca Ortiz
     * Fecha de creacion: 12/10/2022
     * Genera un hijo nuevo como un proceso independiente de su padre
     * @param id
     */
    void GenerarNuevoAcc(String id) {
        System.out.println("Nuevo hijo");
        try {
            // Crea el proceso
            ProcessBuilder pb = new ProcessBuilder("C:/Program Files/Java/jdk-17.0.4.1/bin/java.exe", "-jar", direccionJar, "" + generaciones);
            // Ejecuta el proceso
            pb.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void GestorDeDirectorio() {
        String siguienteIP = this.ipInicial;
        if(this.puertos_aleatorios){
            do {
                for (int i = 0; i < this.puertosBuscar; i++) {
                    int puerto = this.Puerto_Inicio + this.random.nextInt(this.Rango_Puertos);
                    if (puerto % 2 != 0)    // comprobamos que sea impar (UDP)
                        puerto++;

                    // ponemos el mensaje de localización del host correspondiente en la lista de mensajes a enviar del gestor de mensajes
                    this.gm.AñadirMensajeContenedor(new Mensaje(gm.generaCab(String.valueOf(this.puertoUDP),this.id,this.ipPropia,String.valueOf(puerto),"ID_desconocida",siguienteIP,"busqueda","UDP","1"),null,null));
                }
                siguienteIP = siguienteIP(siguienteIP);
            }while (!siguienteIP.equals(this.ipFin));
        }else{
            do{
                for (int puerto = Puerto_Inicio; puerto <= Puerto_Inicio + Rango_Puertos; puerto += 2) {
                    // ponemos el mensaje de localización del host correspondiente en la lista de mensajes a enviar del gestor de mensajes
                    this.gm.AñadirMensajeContenedor(new Mensaje(gm.generaCab(String.valueOf(this.puertoUDP),this.id,this.ipPropia,String.valueOf(puerto),"ID_desconocida",siguienteIP,"busqueda","UDP","1"),null,null));
                }
                siguienteIP = siguienteIP(siguienteIP);
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
