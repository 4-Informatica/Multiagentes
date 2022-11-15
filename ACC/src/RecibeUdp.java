import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;

/**
 * Esta clase se ha creado con el motivo de separar la lógica de recepción de mensajes UDP
 */
public class RecibeUdp extends Thread {
    private GestorMensajes gestor; // Referencia del gestor de mensajes private int puertoTCP;
    DatagramSocket servidor;

    RecibeUdp(GestorMensajes gestor) {
        super();
        this.gestor = gestor;
        new Thread(this, "recibe_udp").start();
    }

    /**
     * Método que se va a llamar al iniciar el hilo de esta clase
     */
    public void run() {
        while (true) {
            try {
                servidor = new DatagramSocket(gestor.Puerto_PropioUdp);
                try {
                    // Se crea el socket vinculado al puerto, para esperar peticiones del cliente

                    //Indica que esta a la espera de la llegada de un mensaje
                    //System.out.println("Esperando petición UDP...");

                    // El servidor espera a que el cliente se conecte y devuelve un socket nuevo
                    // Obtiene el flujo de entrada y lee el objeto del stream
                    DatagramPacket mensaje = new DatagramPacket(new byte[1024], 1024);

                    // Recibimos el DatagramPacket
                    servidor.receive(mensaje);

                    /*System.out.println("Ha llegado una peticion \n");
                    System.out.println("Procedente de :" + mensaje.getAddress());
                    System.out.println("En el puerto :" + mensaje.getPort());
                    System.out.println("Sirviendo la petición");*/

                    // El mensaje se procesa y se añade a la cola
                    gestor.ProcesaMensaje(new String(mensaje.getData()));

                }finally {
                    servidor.close();
                }
            } catch (Exception e) {
                //Si llegamos a un error, imprimimos la exception correspondiente
                System.out.println("Problema al recibir UDP " + e.getMessage());
                System.out.println(this.gestor.Puerto_PropioUdp);
                e.printStackTrace();
            }
        }
    }
}

