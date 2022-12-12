import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;

/**
 * Esta clase se ha creado con el motivo de separar la lógica de recepción de mensajes TCP
 */
public class RecibeTcp extends Thread {
    //private static GestorMensajes gestor; // Referencia del gestor de mensajes
    private GestorMensajes gestor;
    ServerSocket servidor;

    RecibeTcp(GestorMensajes gestor){
        super();
        this.gestor = gestor;
        new Thread(this, "recibe_tcp").start();
    }

    /**
     * Método que se va a llamar al iniciar el hilo de esta clase
     */
    @Override
    public void run()
    {
        try {
            // Se crea el socket vinculado al puerto, para esperar peticiones del cliente
            servidor = new ServerSocket(gestor.Puerto_PropioTcp);

            while (true) {


                //System.out.println("Esperando petición TCP...");

                // El servidor espera a que el cliente se conecte y devuelve un socket nuevo
                Socket cliente = servidor.accept();

                // Obtiene el flujo de entrada y lee el objeto del stream
                DataInputStream obj = new DataInputStream(cliente.getInputStream());

                //System.out.println("Se ha recibido una petición");

                // Lee el objeto del stream
                String mensaje = obj.readUTF();

                // Decidimos que hacer con el mensaje
                gestor.ProcesaMensaje(mensaje);

                //System.out.println("Cerrando sockets...");

                // Cerramos los sockets
                obj.close();

                // Cerramos el cliente
                cliente.close();

            }
        }


                /*
                 * Si llegamos a un error, imprimimos la exception correspondiente
                 * seguidamente el agente lo pasa a mensaje y el gestor lo añade
                 *
                 */

        catch (SocketException error){
        System.out.println("ServerSocket TCP cerrado");
        }
        catch (Exception e) {

        //Si llegamos a un error, imprimimos la exception correspondiente
        System.out.println("Problema al recibir TCP " + e.getMessage());
        System.out.println(this.gestor.Puerto_PropioTcp);
        e.printStackTrace();
        }
        finally {
            try {
                servidor.close();
            } catch (IOException e) {
                System.out.println("No se pudo cerrar el ServerSocketTCP");
                e.printStackTrace();
            }
        }
                // TODO
                //Hacer que mande error del mensaje a la cola de mensaje
                /*
                try
                {
                    gestor.ProcesaMensaje("Error: mensaje no recibido");
                }
                catch (ParserConfigurationException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (SAXException ex) {
                    throw new RuntimeException(ex);
                }
                */

    }
}

