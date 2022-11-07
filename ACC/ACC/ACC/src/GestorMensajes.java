import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * GestorMensajes es la clase que hemos visto necesaria crear para poder gestionar todos los mensajes, tanto recibidos
 * como enviados. Hemos visto necesario utilizar una clase aparte en vez de utilizar un método. Si no no podríamos hacer
 * lo que queremos hacer con esta clase. Esta clase es una forma de abstraer la lógica de comunicación y mensajería.
 */
public class GestorMensajes extends Thread
{
    // Necesitamos que ambos contenedores sean públicos para poder acceder desde el GestorDeMensajes

    public LinkedList<Mensaje> contenedor_de_mensajes_a_enviar = new LinkedList<>(); // Contenedor para almacenar cada uno de los mensajes enviados por un agente

    public LinkedList<Mensaje> contenedor_de_mensajes_recibidos = new LinkedList<>(); // Contenedor para almacenar cada uno de los mensajes recibidos por un agente

    private RecibeTcp recibeTcp; // Instancia de la clase que se va a encargar de recibir mensajes por TCP
    private RecibeUdp recibeUdp; // Instancia de la clase que se va a encargar de recibir mensajes por UDP
    private Object mutex = new Object(); // Mutex que vamos a utilizar para coordinar los hilos y asegurarnos de que no haya fallos

    public int Puerto_PropioTcp;
    public int Puerto_PropioUdp;

    /**
     * Constructor de la clase GestorMensajes
     */
    public GestorMensajes(int puerto_PropioTcp, int puerto_PropioUdp)
    {
        // Llamada al constructor de la clase Thread
        super();

        // Asignamos los puertos tcp y udp
        this.Puerto_PropioTcp = puerto_PropioTcp;
        this.Puerto_PropioUdp = puerto_PropioUdp;
    }

    /**
     * Método que se llamará al iniciar el hilo correspondiente del gestor de mensajes
     */
    @Override
    public void run()
    {
        // Creamos los hilos de recibir mensajes con ambos protocolos
        recibeTcp = new RecibeTcp();
        recibeUdp = new RecibeUdp();

        // Comenzamos ambos hilos para recibir mensajes con ambos protocolos
        recibeTcp.start();
        recibeUdp.start();
    }

    /**
     * EnviarMensaje() envia todos los mensajes de la cola. Si la cola está vacía se duerme y luego vuelve a mirar.
     *
     * @throws InterruptedException
     */
    public void EnviarMensaje() throws InterruptedException, ParserConfigurationException, IOException, SAXException, jdk.internal.org.xml.sax.SAXException, TransformerException {

        System.out.println("Entrando en envía mensaje");

        while (true) {
            // Si el agente no tiene mensajes para enviar, se para 1s antes de mirar otra vez
            if (ComprobarContenedorDeMensajes()) {
                System.out.println("Probando envia mensaje");
                sleep(1000);
                continue;
            }

            // Obtenemos un mensaje del contenedor de mensajes a enviar
            Mensaje msg = CogerMensajeDelContenedor();

            // Obtenemos el mensaje en formato xml para enviarlo
            String xml = TransformarMensaje(msg);

            // Dependiendo del protocolo, enviamos el mensaje de una forma u otra
            if (msg.getProtocolo().equals("tcp"))
                EnviaTcp(xml, msg.getEmisorIP(), msg.getPuertoEmisor());
            else
                EnviaUdp(xml, msg.getEmisorIP(), msg.getPuertoEmisor());
        }
    }

    /**
     * Método para añadir mensajes al contenedor.
     *
     * @param msg El mensaje a enviar
     */
    public void AñadirMensajeContenedor(Mensaje msg) {
        //De esta manera el contenedor de mensajes recibidos no es accedido por dos hilos al mismo tiempo y conseguimos mutual exclusion
        synchronized (mutex) {
            contenedor_de_mensajes_recibidos.add(msg);
        }
    }

    /**
     * Método para obtener un mensaje del contenedor. Se asume que al menos hay un mensaje.
     *
     * @return Mensaje a obtener
     */
    public Mensaje CogerMensajeDelContenedor() {
        Mensaje msg;

        synchronized (mutex) {
            msg = contenedor_de_mensajes_recibidos.pop();
        }

        return msg;
    }

    /**
     * Comprueba si el contenedor de mensajes recibidos está vacío o no
     *
     * @return Si está vacío o no
     */
    public boolean ComprobarContenedorDeMensajes() {
        boolean isEmpty;

        synchronized (mutex) {
            isEmpty = contenedor_de_mensajes_recibidos.isEmpty();
        }

        return isEmpty;
    }

    /**
     * Método para enviar mensajes por TCP
     *
     * @param msg Mensaje a enviar
     * @param host Ip de la máquina a la que queremos enviar el mensaje
     * @param puerto Puerto de la máquina por la que enviar el mensaje
     */
    public void EnviaTcp(String msg, String host, String puerto) throws ParserConfigurationException, IOException, SAXException, jdk.internal.org.xml.sax.SAXException {
        try {

            // Creación socket para comunicarse con el servidor con el host y puerto asociados al servidor
            Socket skCliente = new Socket(host, Integer.parseInt(puerto));

            // Creación flujo de salida
            DataOutputStream obj = new DataOutputStream(skCliente.getOutputStream());

            // Envía objeto al servidor
            obj.writeUTF(msg);

            // Cierra flujo de salida
            obj.close();

            // Cierra socket
            skCliente.close();

            // Ok
            System.out.println("Everything went fine \n");
        } catch (Exception e) {
            // Failure
            System.out.println("Some problem occured: " + e + "\n");
            ProcesaMensaje("Error fallo al enviar mensaje TCP");
        }
    }

    /**
     * Método para enviar mensajes por UDP
     *
     * @param msg Mensaje a enviar
     * @param host Ip de la máquina a la que queremos enviar el mensaje
     * @param puerto Puerto de la máquina por la que enviar el mensaje
     */
    public void EnviaUdp(String msg, String host, String puerto)
    {
        byte[] buffer = new byte[1024];

        try {
            //Creamos el socket de UDP
            DatagramSocket socketUDP = new DatagramSocket();

            //Convertimos el mensaje a bytes
            buffer = msg.getBytes();

            //Creamos un datagrama
            DatagramPacket mensaje = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(host), Integer.parseInt(puerto));

            //Lo enviamos con send
            System.out.println("Envio el datagrama");
            socketUDP.send(mensaje);

            //Cerramos el socket
            socketUDP.close();
        }
        catch (Exception ex)
        {
            System.out.println(ex);
            try
            {
                ProcesaMensaje("Error: fallo al recibir mensaje UDP");
            }
            catch (ParserConfigurationException eo) {
                throw new RuntimeException(eo);
            } catch (IOException eo) {
                throw new RuntimeException(eo);
            } catch (SAXException eo) {
                throw new RuntimeException(eo);
            } catch (jdk.internal.org.xml.sax.SAXException eo) {
                throw new RuntimeException(eo);
            }
        }
    }

    /**
     *
     * @param cab, en la que se espera que se incluyan los siguiente parametros:
     *      1) ip_Receptor:InetAdress
     *      2) puerto_Receptor:int
     *      3) id_Receptor:String
     *      4) tipoMensaje:String
     *      5) protocolo:String
     *      6)id_mensaje:String
     * @return cabecera completa, con la Ip del emisor, puerto, id, hora de generacion y los parametros pasados, ademas dek orden correcto
     */
    public HashMap<String,Object> generaCab(HashMap<String, Object> cab){


        // Se añaden los primeros parametros, en la implementacion final habria que sustituirlo por la
        //ip de la maquina con this.getIP o el metodo que sea implementado
        HashMap<String,Object> h = new HashMap<String,Object>();
        HashMap<String,Object> emisor = new HashMap<String,Object>();
        try {

            emisor.put("ip_Emisor",InetAddress.getAllByName("192.0.0.1"));


        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        emisor.put("puerto_emisor",1);
        emisor.put("id_Emisor","id1");
        h.put("emisor",emisor);

        //Se añaden el resto de parametros
        HashMap<String,Object> receptor = new HashMap<String,Object>();
        receptor.put("ip_Receptor",cab.get("ip_Receptor"));
        receptor.put("puerto_Receptor",cab.get("puerto_Receptor"));
        receptor.put("id_Receptor",cab.get("id_Receptor"));
        h.put("receptor",receptor);

        h.put("tipoMensaje",cab.get("tipoMensaje"));
        h.put("protocolo",cab.get("protocolo"));
        h.put("id_Mensaje",cab.get("id_Mensaje"));
        h.put("tiempo_envio",new Date());


        return h;
    }

    //AUTOR: GRUPO 4

    // Funcion recibeMensaje() -> Recibe mensages UDP o TCP, procesa la parte genérica (Llamando a TratarCabeza) y
    //  la almacena en ContenedorMensajesRecibidos(ArrayList) para que la función del agente los recoja cuando quiera

    public void ProcesaMensaje(String xml) throws ParserConfigurationException, IOException, SAXException, jdk.internal.org.xml.sax.SAXException {

        //Código para recibir String

        /*
         * Una vez tenemos el String recibido debemos crear una instancia de mensajeRecibido Y meterlo a la cola
         *
         * */

        //Creamos el mensajeRecibido y lo almacenamos
        Mensaje mR = new Mensaje(xml); // HABRÁ QUE INICIALIZARLO CORRECTAMENTE CUANDO TENGAMOS LA ESTRUCTURA DE LA CLASE

        AñadirMensajeContenedor(mR);
    }


    //Funcion enviamMensaje() -> Envia mensages UDO o TCP, cuando tiene el encargo en "contenedor de mensajes a enviar"
    /**
     * ENTRADA: Instancia de mensajeAEnviar*
     *
     * FUNCION: Vamos a sacar los datos necesarios de mensajeAEnviar para crear el XML
     * y lo transformaremos a String para que nuestros compañeros puedan enviarlo
     *
     *
     **/
    public String TransformarMensaje(Mensaje mA) throws IOException, SAXException,ParserConfigurationException, TransformerException {


        // Debemos sacar del mensajeAEnviar los datos de la cabecera y el elementobody para crear el XML

        String tipoMensaje = mA.getTipoMensaje();
        String id_Mensaje = mA.getIdMensaje();
        String ip_Emisor = mA.getEmisorIP();
        String puerto_Emisor = mA.getPuertoEmisor();
        String id_Emisor =mA.getEmisorID();

        String ip_Receptor = mA.getReceptorIP();
        String puerto_Receptor = mA.getPuertoReceptor();
        String id_Receptor = mA.getReceptorID();

        String protocolo = mA.getProtocolo();
        String tiempoEnvio = mA.getHoraGeneracion();

        Element body = mA.geteElementBody();

        // Una vez tenemos todos estos datos ya podemos crear el XML

        //  Construimos el XML

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        // definimos el elemento raíz del documento, en nuestro caso "message"
        Element eRaiz = doc.createElement("message");
        doc.appendChild(eRaiz);

        // definimos el nodo que contendrá los elementos
        Element head = doc.createElement("head");
        eRaiz.appendChild(head);


        //Insertamos el body que hemos sacado de mensajeAEnviar
        eRaiz.appendChild(body);



        // definimos cada uno de los elementos y les asignamos los valores sacados de mensajeAEnviar

        Element tipo = doc.createElement("tipo");
        tipo.appendChild(doc.createTextNode(tipoMensaje));
        head.appendChild(tipo);

        Element conversacion_id = doc.createElement("id_Mensaje");
        conversacion_id.appendChild(doc.createTextNode(id_Mensaje));
        head.appendChild(conversacion_id);


        Element emisor = doc.createElement("emisor");

        Element idEmisor = doc.createElement("id");
        idEmisor.appendChild(doc.createTextNode(id_Emisor));
        emisor.appendChild(idEmisor);
        Element puertoEmisor = doc.createElement("puerto");
        puertoEmisor.appendChild(doc.createTextNode(puerto_Emisor));
        emisor.appendChild(puertoEmisor);
        Element ipEmisor = doc.createElement("ip");
        ipEmisor.appendChild(doc.createTextNode(ip_Emisor.toString()));
        emisor.appendChild(ipEmisor);

        head.appendChild(emisor);

        Element receptor = doc.createElement("receptor");

        Element idReceptor = doc.createElement("id");
        idReceptor.appendChild(doc.createTextNode(id_Receptor));
        receptor.appendChild(idReceptor);
        Element puertoReceptor = doc.createElement("puerto");
        puertoReceptor.appendChild(doc.createTextNode(puerto_Receptor));
        receptor.appendChild(puertoReceptor);
        Element ipReceptor = doc.createElement("ip");
        ipReceptor.appendChild(doc.createTextNode(ip_Receptor.toString()));
        receptor.appendChild(ipReceptor);

        head.appendChild(receptor);

        Element eProtocolo = doc.createElement("protocolo");
        eProtocolo.appendChild(doc.createTextNode(protocolo));
        head.appendChild(eProtocolo);

        Element eFecha = doc.createElement("fecha");
        eFecha.appendChild(doc.createTextNode(tiempoEnvio));
        head.appendChild(eFecha);


        //PASAR DE DOM A STRING
        // Una vez tenemos el documento XML lo pasamos a String para poder enviarlo
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  //Parseamos el fichero con las clases DocumentBuilderFactory
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();                //"" DocumentBuilder

        //Creamos el transformer
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        transformer = transformerFactory.newTransformer();

        //Creamos un string writer
        StringWriter writer = new StringWriter();

        //transformamos el Dom Source con un transformer, eliminando saltos de linea, \r y tabulaciones
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        //Creamso el mensaje string a enviar
        String xml_string = writer.getBuffer().toString().replaceAll("\n|\r|    ", "");

        return xml_string;
    }
}