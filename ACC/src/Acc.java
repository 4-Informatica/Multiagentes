import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class Acc {

    enum Estado_del_ACC
    {
        VIVO, MUERTO
    }

    static String Ip_Monitor, Inicio_rango_IPs, Ip_Propia, ID_propio;

    static int Puerto_Monitor, Rango_IPs, puertosBuscar, Tiempo_de_vida, Numero_de_generaciones, Puerto_Propio, Estado_Actual, Puerto_Inicio, Rango_Puertos, UDPport, TCPport,
            tiempo_espera_comportamiento_base;

    static boolean puertos_aleatorios;
    static double Frecuencia_partos, Frecuencia_rastreo_puertos;

    static Socket monitorComunicacion;
    static ServerSocket socketTCP;
    static DatagramSocket socketUDP;
    static DataOutputStream out;

    static String ipInicial, ipFin;

    static GestorMensajes gm;

    public static void main(String[] args) {
        Puerto_Inicio = 50000;
        Rango_Puertos = 100;
        puertos_aleatorios = true;
        // si puertos_aleatorios, cambiar los peurtos a buscar:
        //puertosBuscar=5;
        puertosBuscar = Rango_Puertos/10;

        buscaNido(); // Busca los valores para UDPport y TCPport, ademas de socketTCP y socketUDP

        generaConfiguracionInicial(args);

        gm = new GestorMensajes(TCPport, UDPport, socketTCP, socketUDP);

        ComportamientoBase cb = new ComportamientoBase(ID_propio, Numero_de_generaciones, Puerto_Inicio, Rango_Puertos, Tiempo_de_vida*1000,
                tiempo_espera_comportamiento_base*1000, Frecuencia_partos, Frecuencia_rastreo_puertos, gm,Ip_Propia,UDPport,TCPport, ipInicial, ipFin,puertosBuscar, puertos_aleatorios);

        FuncionDeAgente fa = new FuncionDeAgente(gm, ID_propio, Ip_Propia, UDPport, TCPport);

        notificaNacimiento();
        Estado_Actual = Estado_del_ACC.VIVO.ordinal();
        System.out.println("Agente iniciado");
    }

    /**
     *  Funcion buscaNido()
     *  @authors David Ruiz, Miguel Picazo, Adrian Lozano, Juan Ramón Romero
     *  @fechaCreación 08/11/2022
     *  @ultima_versión 22/11/2022
     *  @version 1.1
     *  @return void
     *
     *  La función buscaNido busca puertos libres para crear el DatagramSocket y el ServerSocket,
     *  a su vez asigna a las variables globales tanto el puerto TCP como el puerto UDP.
     *
     */
    static void buscaNido() {
        Random r = new Random(); // Creamos el generador de numeros aleatorios
        int Puerto_Fin = Puerto_Inicio + Rango_Puertos; // Establecemos el puerto máximo
        TCPport = Puerto_Inicio + r.nextInt((Puerto_Fin - Puerto_Inicio) + 1); // Obtenemos el TCPport con un valor random

        // Mientras no se pueda fijar el valor del socket estaremos realizaremos toda la ejecución
        while (true) {
            // Mientras que el puerto no sea par no lo aceptaremos
            while (TCPport % 2 != 0) {
                TCPport = Puerto_Inicio + r.nextInt((Puerto_Fin - Puerto_Inicio) + 1); // Generamos un nuevo TCPport random
            }
            try {
                socketTCP= new ServerSocket(TCPport); // Abrimos el socket para TCP
                ServerSocket socket = new ServerSocket(UDPport); // Probamos a abrir el socket para comprobar que este vacio
                socket.close(); // Una vez comprobado que esta vacío lo cerramos
                UDPport = TCPport + 1; // Asignamos el valor
                socketUDP = new DatagramSocket(UDPport); // Abrimos el socket para UDP

                socketTCP.close(); // QUITAR ESTO CUANDO SE UTILICE EN GM
                socketUDP.close(); // QUITAR ESTO CUANDO SE UTILICE EN GM

                break; // Al llegar a este punto podemos acabar la ejecución

            } catch (IOException e) {// si salta excepcion al abrir puerto, puerto ocupado
                continue; // continuamos buscando
            }
        }
    }

    static void generaConfiguracionInicial(String[] args) {
        try {
            Ip_Propia = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        ID_propio = Ip_Propia + "-" + UDPport + "-" + TCPport;
        Numero_de_generaciones = Integer.parseInt(args[0]) - 1;
        Tiempo_de_vida = 40;                    // Segundos
        tiempo_espera_comportamiento_base = 3;  // Segundos

        // TODO Poner la IP del monitor
        Ip_Monitor = "IP";
        Puerto_Monitor = 40000;

        // Rango de IPs pertenecientes a la subred del laboratorio
        ipInicial="172.24.196.1";
        ipFin="172.24.197.254";

        // Para pruebas fuera del laboratorio descomentar las siguientes lineas y modificarlas si es necesario
        ipInicial="192.168.0.1";
        ipFin="192.168.0.254";

        Frecuencia_partos = 0.5;
        Frecuencia_partos = 1;
        Frecuencia_rastreo_puertos = 0.7;
        Frecuencia_rastreo_puertos = 1;

        try {
            String path = "C:\\Users\\"+ System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\" + ID_propio;
            File file = new File(path + ".txt");
            int nuevo = 2;
            String ID_aux = ID_propio;
            // se comprueba si ya existe el fichero para que no escriban varios agentes en el mismo fichero si un
            // agente lo ha creado y ha terminado su ejecución
            while(file.exists()){
                file = new File(path +"-"+nuevo+".txt");
                ID_propio = ID_aux + "-" + nuevo;
                nuevo++;
            }
            System.setOut(new PrintStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        //System.out.println("generaConfiguracionInicial");
    }

    static void notificaNacimiento() {
        // se envia el mensaje usando el gestor de mensajes
        Mensaje m = new Mensaje(gm.generaCab(String.valueOf(TCPport), ID_propio, Ip_Propia, String.valueOf(Puerto_Monitor),"Monitor", Ip_Monitor,"nacimiento","TCP","1"),null,null);
        // TODO Descomentar la siguiente linea cuando exista un monitor
        //gm.AñadirMensajeContenedor(m);

        /*try {
            monitorComunicacion = new Socket(Ip_Propia, Puerto_Monitor);
            out = new DataOutputStream(monitorComunicacion.getOutputStream());
            out.writeUTF(
                    "El agente con id: " + ID_propio + "\nIp " + Ip_Propia + "\nPuerto UDP " + UDPport
                            + "\nPuerto TCP " + TCPport + "\nha creado un nuevo agente");
            System.out.println("Se ha enviado la notificación de nacimiento");
        } catch (IOException e) {
            System.out.println("No se ha podido enviar la notificación de nacimiento");
        }*/
    }
}