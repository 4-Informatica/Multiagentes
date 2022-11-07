import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

public class Acc {

    enum Estado_del_ACC
    {
        VIVO, MUERTO
    }
    static String Ip_Monitor, Inicio_rango_IPs, Ip_Propia, ID_propio;

    static int Puerto_Monitor, Rango_IPs, Tiempo_de_vida, Numero_de_generaciones, Puerto_Propio, Estado_Actual, Puerto_Inicio, Rango_Puertos, UDPport, TCPport,
            tiempo_espera_comportamiento_base;

    static double Frecuencia_partos, Frecuencia_rastreo_puertos;

    static Socket monitorComunicacion;
    static DataOutputStream out;

    static ArrayList<accLocalizado> contenedor_directorio_ACCs;

    public static void main(String[] args) {
        Puerto_Inicio = 50000;
        Rango_Puertos = 100;

        //Puerto_Propio = buscaNido();
        int ports[] = buscaNido();
        UDPport = ports[0]; // getAvailablePort para obtener un puerto libre y asignarselo a
        TCPport = ports[1]; // nuestro agente

        generaConfiguracionInicial(args);

        gestorMensajes gm = new gestorMensajes(TCPport, UDPport);

        comportamientoBase cb = new comportamientoBase(ID_propio, Numero_de_generaciones, Puerto_Inicio, Rango_Puertos, Tiempo_de_vida*1000,
                tiempo_espera_comportamiento_base*1000, Frecuencia_partos, Frecuencia_rastreo_puertos, gm);

        funcionDeAgente fa = new funcionDeAgente();

        notificaNacimiento();
        Estado_Actual = Estado_del_ACC.VIVO.ordinal();
    }
    static int[] buscaNido() {
        // CUIDADO CONCURRENCIA

        /* SECCION 1 */
        ServerSocket socket = null;
        ServerSocket socket2 = null;
        int[] ports = { 0, 0 };

        Random r = new Random();
        int Puerto_Fin = Puerto_Inicio + Rango_Puertos;
        int n = Puerto_Inicio + r.nextInt((Puerto_Fin - Puerto_Inicio) + 1);
        while (true) {

            /* SECCION 2 */
            while (n % 2 != 0) {
                n = Puerto_Inicio + r.nextInt((Puerto_Fin - Puerto_Inicio) + 1);
            }
            try {
                socket = new ServerSocket(n); // abrimos dos sockets, uno para UDP otro para TCP
                socket2 = new ServerSocket(n + 1);

                assert socket != null;
                try {
                    /* SECCION 3 */
                    socket.close(); // cerramos los sockets
                    socket2.close();
                } catch (IOException e) { // control de excepcion
                    e.printStackTrace();
                }
                // puerto par UPD, puerto impar TCP arr
                ports[0] = n; // array [UDP,TCP]
                ports[1] = n + 1;
            } catch (IOException e) {// si salta excepcion al abrir puerto, puerto ocupado
                continue; // continuamos buscando
            }
            /* SECCION 4 */
            return ports; // retornamos [0,0] si no hay puerto
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
        Tiempo_de_vida = 10;                    // Segundos
        tiempo_espera_comportamiento_base = 3;  // Segundos
        Puerto_Monitor = 40000;

        Frecuencia_partos = 0.5;
        Frecuencia_rastreo_puertos = 0.7;

        try {
            System.setOut(new PrintStream(new File("C:/ACC-Multiagentes/salida" + ID_propio + ".txt")));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println("generaConfiguracionInicial");
    }

    static void notificaNacimiento() {
        try {
            monitorComunicacion = new Socket(Ip_Propia, Puerto_Monitor);
            out = new DataOutputStream(monitorComunicacion.getOutputStream());
            out.writeUTF(
                    "El agente con id: " + ID_propio + "\nIp " + Ip_Propia + "\nPuerto UDP " + UDPport
                            + "\nPuerto TCP " + TCPport + "\nha creado un nuevo agente");
            System.out.println("Se ha enviado la notificación de nacimiento");
        } catch (IOException e) {
            System.out.println(" No se ha podido enviar la notificación de nacimiento");
        }
    }

    /*static void tratarNuevoMensaje(){
        // si el mensaje es de tipo respuesta a la localizacion, se debe llamar a la funcion addAgenteLocalizado(...)
        if (mensaje.tipo==respuestaLocalizacion){
            addAgenteLocalizado(ID, IP, puerto);
        }
    }*/

    /**
     * Funcion que añade un agente localizado al contenedor de agentes localizados, si no se estuviera ya hay.
     * @author Daniel Espinosa Perez
     * @author Jose Antonio Garcia Castro
     * @author Miguel Paños Gonzalez
     * @author Jose Angel Serrano Pardo
     * @param ID del agente localizado
     * @param IP del agente localizado
     * @param puerto del agente localizado
     */
    static public void addAgenteLocalizado(String ID, String IP, int puerto){
        accLocalizado nuevoAgente = new accLocalizado(ID, IP, puerto);
        if(!contenedor_directorio_ACCs.contains(nuevoAgente))
            contenedor_directorio_ACCs.add(nuevoAgente);
    }
}