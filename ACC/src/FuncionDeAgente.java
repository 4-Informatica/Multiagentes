import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class FuncionDeAgente implements Runnable {
    GestorMensajes gm;
    ArrayList<AccLocalizado> contenedor_directorio_ACCs;
    String ID_Propio;
    String IP_Propia;
    int puertoUDP, puertoTCP;

    int cromosAlbum = 20; // Numero de cromos a completar en el album
    int cromosIniciales = 30;
    int[] listaCromos = new int[cromosAlbum];
    int[] listaNecesitados = new int[cromosAlbum];

    // int numeroCoronas
         //ArrayList<Integer> albumCromos = new ArrayList<>(cromosAlbum);
    /**
     *
     *  Método crearAlbum()
     *  @authors David Ruiz, Miguel Picazo, Adrian Lozano, Juan Ramón Romero
     *  @fechaCreación 17/12/2022
     *  @ultima_versión 17/12/2022
     *  @version 1.0
     *  @return void
     *
     *
    **/
    public void crearAlbum(){
        for (int i=0;i<cromosIniciales;i++){
            int cromo = (int)(Math.random() * 20);
            listaCromos[cromo] ++;
        }
        System.out.println("\nÁlbum del agente "+ID_Propio);
        for (int i=0;i<cromosAlbum;i++) {
            System.out.println("Nº "+ i+ ", cromos totales: "+listaCromos[i]);
        }
    }

    /**
     *
     *  Método crearAlbumNecesitados()
     *  @authors David Ruiz, Miguel Picazo, Adrian Lozano, Juan Ramón Romero
     *  @fechaCreación 17/12/2022
     *  @ultima_versión 17/12/2022
     *  @version 1.0
     *  @return void
     *
     *
     **/
    public void crearAlbumNecesitados() {
        for (int i=0;i<cromosAlbum;i++) {
            if(listaCromos[i] == 0){
                System.out.println("Se necesita el cromo: "+i);
                listaNecesitados[i]++;
            }
        }
    }
    public void notificarCromosNecesitados(){
        for(AccLocalizado Acc : contenedor_directorio_ACCs){
            //Falta implementacion del body
            //HashMap<String, String> body = null;
            Mensaje m = new Mensaje(gm.generaCab(String.valueOf(this.puertoUDP), this.ID_Propio, this.IP_Propia, Acc.puerto, Acc.ID, Acc.IP, "SolicitudTransaccion", "TCP", "1"), null, null);
            generaMensajeAEnviar(m);
            System.out.println("Se ha notificado de los cromos necesitados");
        }
    }
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
        crearAlbum();
        crearAlbumNecesitados();
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
        if (m.tipoMensaje.equals("busqueda")) {
            // se debe comprobar si no es un mensaje enviado por el propio agente a si mismo
            // en un intento de localizar otros agentes
            if (!Objects.equals(m.getEmisorID(), ID_Propio)) {
                // se responde al agente
                this.generaMensajeAEnviar(new Mensaje(gm.generaCab(String.valueOf(this.puertoUDP), this.ID_Propio, this.IP_Propia, m.getPuertoEmisor(), m.getEmisorID(), m.getEmisorIP(), "respuesta_busqueda", "UDP", "1"), null, null));

                // si se quiere se añade el agente que intenta localizar al directorio
                // de momento se hace
                this.addAgenteLocalizado(m.getEmisorID(), m.getEmisorIP(), m.getPuertoEmisor(), m.getHoraGeneracion());
            }
        } else if (m.tipoMensaje.equals("respuesta_busqueda")) {
            // se añade el agente que responde a la lsita de agentes localizados
            this.addAgenteLocalizado(m.getEmisorID(), m.getEmisorIP(), m.getPuertoEmisor(), m.getHoraGeneracion());
        } else if (m.tipoMensaje.equals("SolicitudTransaccion")) {
            //int[] listaNecesitados = m.getListaCromos();
            int[] listaNecesitados = {1,0,1,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0,1}; // Los valores a 1 son los cromos que le faltan
            int[] listaEnviados = new int[cromosAlbum];
            for (int i=0;i<listaNecesitados.length;i++){
                if(listaCromos[i]>1 && listaNecesitados[i]==1){
                    listaCromos[i]--;
                    listaEnviados[i]++;
                }
            }
            //Falta implementacion del body
            //HashMap<String, String> body = null;
            Mensaje mensajeRespuesta = new Mensaje(gm.generaCab(String.valueOf(this.puertoUDP), this.ID_Propio, this.IP_Propia, m.getPuertoEmisor(), m.getEmisorID(), m.getEmisorIP(), "Respuesta_SolicitudTransaccion", "TCP", "1"), null, null);
            System.out.println("Se ha solicitado una transaccion");
            generaMensajeAEnviar(m);
        } else if (m.tipoMensaje.equals("Respuesta_SolicitudTransaccion")) {
            //int[] listaRecibidos = m.getListaCromos();
            int[] listaRecibidos = {1,0,1,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0,1}; // Los valores a 1 son los cromos que le faltan

            for (int i=0;i<listaRecibidos.length;i++){
                if (listaRecibidos[i]==1){
                    listaCromos[i]++;
                }
            }

        }
    }

    void generaMensajeAEnviar(Mensaje m) {
        //System.out.println("generaMensajeAEnviar");
        this.gm.AñadirMensajeContenedor(m);
    }

    public void addAgenteLocalizado(String ID, String IP, String puerto, String hora_generacion){
        AccLocalizado nuevoAgente = new AccLocalizado(ID, IP, puerto, hora_generacion);
        if(!contenedor_directorio_ACCs.contains(nuevoAgente)) {
            contenedor_directorio_ACCs.add(nuevoAgente);
            System.out.println("Lista de Agentes encontrados:");
            System.out.println(this.contenedor_directorio_ACCs);
            System.out.println("");
        }
    }
}
