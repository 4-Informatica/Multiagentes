import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class FuncionDeAgente implements Runnable {
    GestorMensajes gm;
    ArrayList<AccLocalizado> contenedor_directorio_ACCs;
    String ID_Propio;
    String IP_Propia;
    int puertoUDP, puertoTCP;

    Cromos [] cromos;
    Cromos [] cromos_sin_reserva;

    //Diccionario de ofertas
    HashMap<Integer,Oferta> ofertas_pendientes;

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
        //Primero, generamos la conf inicial del agente con los cromos:
        //Tomamos valor 40 como fijo de valor de coleccion y de numero de cromos a dar por agente
        dameCromos(40,40);

        //Ademas, sera necesario tener una lista de cromos que puede ofrecer en intercambios, de tal forma que no pueda ofrecer mas de los
        // que tiene, para ello, necesitamos otro array que llamamos cromos_sin_reserva

        this.cromos_sin_reserva = new Cromos[40];
        for(int i=0; i<40;i++){
            cromos_sin_reserva[i] = new Cromos(1,cromos[i].cantidad,cromos[i].rareza);
        }
        //Ademas de un conjunto donde almacenamos las peticiones que nos han ido llegando

        //Inicializamos el conjunto de ofertas como vacio
        ofertas_pendientes= new HashMap<Integer, Oferta>();

        //Iniciamos el bucle de la funcion agente

        while(true){
            if(!gm.ComprobarContenedorDeMensajes()) {
                Mensaje m = gm.CogerMensajeDelContenedor();
                procesaMensajeRecibido(m);
                //Usamos el metodo procesa mensaje para almacenar el agente y sus caracteristicas y, tras esto, seguimos el rpotocolo
                if(m.tipoMensaje=="Solicitud_transaccion"){
                    //Necesito el conjunto de los que tengo repes para ofrecer
                    //por lo tanto, primero cojo los repes:
                    HashSet<Cromos> repes = dameRepes();

                    //los quito para evitar fallos de concurrencia del array de cromos a dar


                    //Ahora necesitamos saber si el receptor quiere alguno de los que el emisor ofrece
                    //Aun no poedmos extraer los queridos del body, por falta de metodos, pero podemos obtener los que
                    //si querria, que en este caso definimos al agente con poca inteligencia y que coja cualquier cromo indepen
                    //de su valor, definimos un nuevo metodo que de los que tenga un total de 0
                    HashSet<Cromos> queridos= dameQueridos();

                    //Necesitamos hayar la interseccion de estos con los que ofrece el emisor
                    //Si el conjutno es vacio, no hace nada
                    //Si el conjunto tiene al menos ub elemento, ofrece un intercambio
                    //TODO: HALLAR LA INTERSECCION A PARTIR DE LOS CROMOS QUE OFRECEN CON LOS QUERIDOS

                    //Añadimos la oferta a la lista de ofertas
                    //TODO: añadir los cromos queridos y ofrecidos a una clase oferta

                }
                else if(m.tipoMensaje=="Respuesta_SolicitudTransaccion"){
                    //Comprobamos que los cromos que me ofertan son los que quiero:
                    HashSet<Cromos> queridos = dameQueridos();

                    //Saco del body los ofertados
                    //TODO sacar del body los ofertados

                    //SI me interesa, sigo adelante con el intercambio, quito de mi lista los que ofrezco
                    // y genero un objeto tipo Oferta donde la guardo toda la info
                    //DEbido a que faltan metodos del cuerpo, esto aun no se puede realizar


                }
                else if(m.tipoMensaje=="oferta"){
                    //Buscamos la oferta en nuestra lista
                    //Sino esta ignoramos
                    //Si esta entonces pasamos a iniciar la transiccion
                    //Comprobamos que aun tenemos los cromos
                    //TODO : a partir de los que nos pide el otro agente, los quitamos de nuestra lista y le enviamos un mensaje de
                    // que queremos confirmacion

                    //Envio mensaje de tipo 4
                    //Aun no tenemos los metodos para hacerlo


                }
                else if(m.tipoMensaje=="OK_oferta"){
                    //Buscamos la oferta en nuestra lista
                    //Sino esta ignoramos
                    //Si esta entonces pasamos a iniciar la transiccion por parte del agente receptor
                    //Sacamos los cromos
                    //TODO : a partir de los que nos pide el otro agente, los sacamos definitivamente de la lista

                    //Faltan metodos en el body para acabarlo


                }
                else if(m.tipoMensaje=="KO_transaccion"){
                    //Buscamos la oferta en nuestra lista
                    //Sino esta ignoramos
                    //Si esta entonces devolvemos los cromos uqe no usamos

                    //TODO: extraer del body aquellos que iban a formar parte de la transaccion y devolverlos

                    //Faltan metodos en el body para acabarlo


                }
            }

            //Enviamos de tipo 1 para que el agente tambien tome la iniciativa
            //Necesiatmos un metodo para obtener aquellos que tengo repes
            //HashMap cabecera = gm.generaCab();

            HashSet<Cromos> repes = dameRepes();

            for(AccLocalizado agente:contenedor_directorio_ACCs){

                //Genero mensaje tipo 1 y lo envio
                //TODO usar: metodos para generar un mensaje de tipo 1 y guardarlo en la cola a enviar para cada agente localizado
                //POsible problema de sobrecarga de mensajes tipo 1
                //Posible solucion --> Dormir el hilo antes
                //Posible solucion 2 --> Ejecutar el envio de mensajes tipo 1 de forma probabilistica
            }

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

    /** Este metodo nos va a devolver la configuracion de cromos inicial del agente
     *
     * @param num -> cantidad de cromos que queremos
     * @param tam_coleccion
     */
    public void dameCromos(int num, int tam_coleccion){
        this.cromos=new Cromos[tam_coleccion];
        for (int i =0;i<tam_coleccion;i++){
            this.cromos[i]= new Cromos(i,0,5);
        }
        for(int i =0; i<num;i++){
            int n =(int) (Math.random()*tam_coleccion +1);
            this.cromos[n].setCantidad(this.cromos[n].getCantidad() +1);  ;
        }
    }


    /** Este metodo devolvera los cromos repes de un agente, es decir, los que su cantidad > 1
     *
      * @return
     */

    public HashSet<Cromos> dameRepes(){
        HashSet<Cromos> repes = new HashSet<Cromos>();
        for(int i =0;i<40;i++){
            if(cromos_sin_reserva[i].getCantidad()>1){
                repes.add(cromos[i]);
            }
        }
        return repes;
    }

    /**
     * Usado para tipo de mensaje EnviaOferta, que eliminara de cromos repes que enviamos para iniciar el protocolo
     */

    public void quitaLosOfrecidos(){
        for(int i =0; i< 40;i++){
            if(cromos_sin_reserva[i].cantidad>1){
                cromos_sin_reserva[i].cantidad=cromos_sin_reserva[i].cantidad-1;
            }

        }
    }

    /** Metodo usado para pedir los cromos que le faltan a un agente
     *
     * @return
     */
    public HashSet<Cromos> dameQueridos(){
        HashSet<Cromos> cromosQueridos= new HashSet<>();
        for(int i =0;i<40;i++){
            if(cromos[i].cantidad==0){
                cromosQueridos.add(cromos[i]);
            }
        }
        return cromosQueridos;
    }
}

