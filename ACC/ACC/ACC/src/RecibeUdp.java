import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Esta clase se ha creado con el motivo de separar la lógica de recepción de mensajes UDP
 */
public class RecibeUdp extends Thread {
    private static gestorMensajes gestor; // Referencia del gestor de mensajes

    /**
     * Método que se va a llamar al iniciar el hilo de esta clase
     */
    public void run() {

        while (true){
            try {

                // Se crea el socket vinculado al puerto, para esperar peticiones del cliente
                DatagramSocket servidor = new DatagramSocket(gestor.Puerto_PropioUdp);

                while (true)
                {
                    //Indica que esta a la espera de la llegada de un mensaje
                    System.out.println("Esperando petición UDP...");

                    // El servidor espera a que el cliente se conecte y devuelve un socket nuevo
                    // Obtiene el flujo de entrada y lee el objeto del stream
                    DatagramPacket mensaje = new DatagramPacket(new byte[1024],1024);

                    // Recibimos el DatagramPacket
                    servidor.receive(mensaje);

                    System.out.println("Ha llegado una peticion \n");
                    System.out.println("Procedente de :" + mensaje.getAddress());
                    System.out.println("En el puerto :" + mensaje.getPort());
                    System.out.println("Sirviendo la petición");

                    // Decidimos que hacer con el mensaje
                    gestor.ProcesaMensaje(mensaje.toString());
                }
            }
            catch (Exception e) {

                /*
                 * Si llegamos a un error, imprimimos la exception correspondiente
                 * seguidamente el agente lo pasa a mensaje y el gestor lo añade al contenedor
                 *
                 */

                System.out.println(e.getMessage() + "Recibir UDP");
                mensaje msg = null;
                try
                {
                    gestor.ProcesaMensaje("Error: fallo al recibir mensaje UDP");
                }
                catch (ParserConfigurationException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (SAXException ex) {
                    throw new RuntimeException(ex);
                } catch (jdk.internal.org.xml.sax.SAXException ex) {
                    throw new RuntimeException(ex);
                }
                gestor.AñadirMensajeContenedor(msg);
            }
        }
    }
}

