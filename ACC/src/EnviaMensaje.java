import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

    /**
     * EnviarMensaje() envia todos los mensajes de la cola. Si la cola está vacía se duerme y luego vuelve a mirar.
     *
     * @throws InterruptedException
     */

public class EnviaMensaje extends Thread {
    private static GestorMensajes gestor; // Referencia del gestor de mensajes

    EnviaMensaje(GestorMensajes gestor){
        super();
        this.gestor = gestor;
        new Thread(this, "envia-mensaje").start();
    }

        /**
     * Método que se va a llamar al iniciar el hilo de esta clase
     */

        @Override
    public void run()
    {
        System.out.println("Entrando en envía mensaje");

        while (true) {
            // Si el agente no tiene mensajes para enviar, se para 1s antes de mirar otra vez
            try {
                //System.out.println("Entrando en envía mensaje");

                // Si el agente no tiene mensajes para enviar, se para 1s antes de mirar otra vez
                if (!this.gestor.ComprobarContenedorDeMensajesAEnviar()) {
                    //System.out.println("Probando envia mensaje");

                    // Obtenemos un mensaje del contenedor de mensajes a enviar
                    Mensaje msg = this.gestor.CogerMensajeDelContenedorAEnviar();

                    // Obtenemos el mensaje en formato xml para enviarlo
                    String xml = this.gestor.TransformarMensaje(msg);
                    // Dependiendo del protocolo, enviamos el mensaje de una forma u otra
                    if (msg.getProtocolo().equals("TCP"))
                        this.gestor.EnviaTcp(xml, msg.getReceptorIP(), msg.getPuertoReceptor());
                    else {
                        this.gestor.EnviaUdp(xml, msg.getReceptorIP(), msg.getPuertoReceptor());
                    }
                    //System.out.println("Mensaje enviado");
                }
                else{
                    sleep(1000);
                }
            } catch (Exception e){
                System.out.println(e);
                System.out.println("No se ha podido enviar el mensaje");
            }
        }
    }

}


