import java.io.*;
import java.net.Socket;
import java.util.*;

public class FuncionDeAgente implements Runnable {
    GestorMensajes gm;
    ArrayList<AccLocalizado> contenedor_directorio_ACCs;
    String ID_Propio;
    String IP_Propia;
    int puertoUDP, puertoTCP;

    /**
     * Autores: Ignacio Gago Lopez, Pablo Domingo Fernandez, Alejandro Cebrian Sanchez, Daniel Cuenca Ortiz
     * Fecha de creacion: 08/12/2022
     */
    int Nc = 10;                // Número de cromos
    int Nci = 20;               // Número de cromos inicial
    int[] Acp = new int[Nc];    // Albúm de cromos poseídos
    ArrayList<String> Cn = new ArrayList<>(0); // Cromos necesitados

    double Pr = 0.5;  // Porcentaje de repetición

    int Tv;     // Tiempo de vida
    int Hn;     // Hora de nacimiento
    int Ha;     // Hora actual

    Random rd = new Random();   // Random

    FuncionDeAgente(GestorMensajes gm, String ID_Propio, String IP_Propia, int puertoUDP, int puertoTCP) {
        this.gm = gm;
        this.contenedor_directorio_ACCs = new ArrayList<AccLocalizado>();
        this.ID_Propio = ID_Propio;
        this.IP_Propia = IP_Propia;
        this.puertoUDP = puertoUDP;
        this.puertoTCP = puertoTCP;
        new Thread(this, "funcion_del_agente").start();
    }

    /**
     * Autores: Ignacio Gago Lopez, Pablo Domingo Fernandez, Alejandro Cebrian Sanchez, Daniel Cuenca Ortiz
     * Fecha de creacion: 08/12/2022
     * Genera el álbum inicial del agente
     */
    public void generarAlbum() {
        int i = 0;
        int cromo;
        double prob;
        boolean rellenado;
        while (i < Nci) {
            rellenado = true;
            for (int j=0; j < Nc; j++) {                //Comprueba si todas las posiciones estan rellenadas
                if(Acp[j] == 0) {
                    rellenado = false;
                }
            }
            if(rellenado) {
                break;
            }
            cromo = rd.nextInt(0, Nc);            // Genera cromo
            while (Acp[cromo] != 0) {                   // Revisa que no lo haya generado ya
                cromo = rd.nextInt(Nc);                 // Genera cromo
            }
            Acp[cromo] ++;                              // Añade cromo
            i ++;
            prob = rd.nextDouble(0,1);
            while ((prob <= Pr) && (i < Nci)) {         // Revisa la probabilidad de que se repita
                Acp[cromo] ++;                          // Añade cromo
                i ++;
                prob = rd.nextDouble(0,1);
            }
        }

        // Imprime el album de cromos poseidos
        System.out.print("Album de cromos: ");
        for (int j=0; j < Nc; j++) {
            System.out.print(Acp[j] + ", ");
        }
        System.out.println();
    }

    /**
     * Autores: Ignacio Gago Lopez, Pablo Domingo Fernandez, Alejandro Cebrian Sanchez, Daniel Cuenca Ortiz
     * Fecha de creacion: 08/12/2022
     * Genera el álbum de necesitados del agente
     */
    public void generarAlbumNecesitados() {
        Cn = new ArrayList<>(0);
        for (int j=0; j < Nc; j++) {
            if (Acp[j] == 0) {
                Cn.add(String.valueOf(j));
            }
        }

        // Imprime el album de cromos necesitados
        System.out.print("Album de cromos necesitados: "+Cn);
        System.out.println();

        // Imprime el album de cromos poseidos
        System.out.print("Album de cromos: ");
        for (int j=0; j < Nc; j++) {
            System.out.print(Acp[j] + ", ");
        }
        System.out.println();

        if (Cn.size() > 0) {
            publicarListaNecesitados();
        }
    }

    /**
     * Autores: Ignacio Gago Lopez, Pablo Domingo Fernandez, Alejandro Cebrian Sanchez, Daniel Cuenca Ortiz
     * Fecha de creacion: 15/12/2022
     * Funcion del agente del grupo 1
     */
    @Override
    public void run() {
        generarAlbum();

        while(true){
            if(!gm.ComprobarContenedorDeMensajes()) {
                generarAlbumNecesitados();

                Mensaje m = gm.CogerMensajeDelContenedor();
                procesaMensajeRecibido(m);
            }
        }
    }

    /**
     * Autores: Ignacio Gago Lopez, Pablo Domingo Fernandez, Alejandro Cebrian Sanchez, Daniel Cuenca Ortiz
     * Fecha de creacion: 15/12/2022
     * Publica el álbum de necesitados
     */
    void publicarListaNecesitados() {
        int cont=0;
        for (AccLocalizado accL : contenedor_directorio_ACCs) {
            HashMap<String, Object> body = gm.generaBody(Cn);
            Mensaje m = new Mensaje(gm.generaCab(String.valueOf(puertoTCP), ID_Propio, IP_Propia, String.valueOf(Integer.parseInt(accL.puerto)+1), accL.ID, accL.IP, "SolicitudTransaccion", "TCP", String.valueOf(cont)), body, null);
            generaMensajeAEnviar(m);

            cont++;
        }
    }

    void procesaMensajeRecibido(Mensaje m) {

        // Primero se debe comprobar el tipo de los mensajes
        // si es de busqueda:
        if(m.tipoMensaje.equals("busqueda")) {
            // se debe comprobar si no es un mensaje enviado por el propio agente a si mismo
            // en un intento de localizar otros agentes
            if (!Objects.equals(m.getEmisorID(), ID_Propio)) {
                // se responde al agente
                this.generaMensajeAEnviar(new Mensaje(gm.generaCab(String.valueOf(this.puertoUDP), this.ID_Propio, this.IP_Propia, m.getPuertoEmisor(), m.getEmisorID(), m.getEmisorIP(), "respuesta_busqueda", "UDP", "1"), null, null));

                // si se quiere se añade el agente que intenta localizar al directorio
                // de momento se hace
                this.addAgenteLocalizado(m.getEmisorID(), m.getEmisorIP(), m.getPuertoEmisor(), m.getHoraGeneracion());
            }
        } else if(m.tipoMensaje.equals("respuesta_busqueda")){
            // se añade el agente que responde a la lsita de agentes localizados
            this.addAgenteLocalizado(m.getEmisorID(), m.getEmisorIP(), m.getPuertoEmisor(), m.getHoraGeneracion());
        } else if(m.tipoMensaje.equals("SolicitudTransaccion")) {

            ArrayList<String> CromosEnviar = new ArrayList<>();
            for (Integer i: m.listaCromos) {
                if (Acp[i] > 1) {
                    Acp[i] --;
                    CromosEnviar.add(String.valueOf(i));
                }
            }
            Mensaje respuesta = new Mensaje(gm.generaCab(String.valueOf(this.puertoTCP), this.ID_Propio, this.IP_Propia, m.getPuertoEmisor(), m.getEmisorID(), m.getEmisorIP(), "Respuesta_SolicitudTransaccion", "TCP", m.getIdMensaje()), gm.generaBody(CromosEnviar), null);
            System.out.println("puerto emisor: "+m.getPuertoEmisor());
            generaMensajeAEnviar(respuesta);

        } else if (m.tipoMensaje.equals("Respuesta_SolicitudTransaccion")) {

            for (Integer i: m.listaCromos) {
                Acp[i]++;
            }
            //generarAlbumNecesitados();
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
