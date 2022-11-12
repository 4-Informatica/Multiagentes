import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class FuncionDeAgente implements Runnable {
    GestorMensajes gm;
    ArrayList<AccLocalizado> contenedor_directorio_ACCs;
    String ID_Propio;
    String IP_Propia;
    int puertoUDP, puertoTCP;
    FuncionDeAgente(GestorMensajes gm, String ID_Propio, String IP_Propia, int puertoUDP, int puertoTCP) {
        this.gm = gm;
        this.contenedor_directorio_ACCs = new ArrayList<AccLocalizado>();
        this.ID_Propio = ID_Propio;
        this.IP_Propia = IP_Propia;
        this.puertoUDP = puertoUDP;
        this.puertoTCP = puertoTCP;
        new Thread(this, "funcion_del_agente").start();
    }

    @Override
    public void run() {
        //procesaMensajeRecibido();
        //generaMensajeAEnviar();
        while(true){
            if(!gm.ComprobarContenedorDeMensajes()) {
                Mensaje m = gm.CogerMensajeDelContenedor();
                procesaMensajeRecibido(m);
            }
        }
    }

    void procesaMensajeRecibido(Mensaje m) {

        // Primero se debe comprobar el tipo de los mensajes
        // si es de busqueda:
        if(m.tipoMensaje.equals("busqueda")) {
            // se debe comprobar si no es un mensaje enviado por el propio agente a si mismo
            // en un intento de localizar otros agentes
            if (!Objects.equals(m.puertoEmisor, String.valueOf(gm.Puerto_PropioUdp))) {
                // se responde al agente
                this.generaMensajeAEnviar(new Mensaje(gm.generaCab(String.valueOf(this.puertoUDP), this.ID_Propio, this.IP_Propia, m.getPuertoEmisor(), m.getEmisorID(), m.getEmisorIP(), "respuesta_busqueda", "UDP", "1"), null, null));

                // si se quiere se añade el agente que intenta localizar al directorio
                // de momento se hace

                this.addAgenteLocalizado(m.getEmisorID(), m.getEmisorIP(), m.getPuertoEmisor(), m.getHoraGeneracion());
                System.out.println("Lista de Agentes encontrados:");
                System.out.println(this.contenedor_directorio_ACCs);
                System.out.println("");
            }
        } else if(m.tipoMensaje.equals("respuesta_busqueda")){
            // se añade el agente que responde a la lsita de agentes localizados
            this.addAgenteLocalizado(m.getEmisorID(), m.getEmisorIP(), m.getPuertoEmisor(), m.getHoraGeneracion());
            System.out.println("Lista de Agentes encontrados:");
            System.out.println(this.contenedor_directorio_ACCs);
            System.out.println("");
        }

    }

    void generaMensajeAEnviar(Mensaje m) {
        //System.out.println("generaMensajeAEnviar");
        this.gm.AñadirMensajeContenedor(m);
    }

    public void addAgenteLocalizado(String ID, String IP, String puerto, String hora_generacion){
        AccLocalizado nuevoAgente = new AccLocalizado(ID, IP, puerto, hora_generacion);
        if(!contenedor_directorio_ACCs.contains(nuevoAgente))
            contenedor_directorio_ACCs.add(nuevoAgente);
    }
}
