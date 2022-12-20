//import jdk.internal.org.xml.sax.InputSource;
//import jdk.internal.org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.InetAddress;
import java.util.*;

public class Mensaje {
    Document mensaje;
    boolean correcto;

    String receptorID;
    String puertoReceptor;
    String receptorIP;
    String emisorIP;
    String emisorID;
    String puertoEmisor;

    String protocolo;


    //MENSAJE ATTRIBUTES
    String idMensaje;
    String horaGeneracion; //No esta en mensaje GRUPO 4
    String tipoMensaje;

    //MENSAJE CUERPO
    Element eElementBody;

    String id_transaccion;
    String id_oferta;
    String TTL;
    String info_error;

    List<String[]> ListaDeseadosACC1; //[id, interes, precio]
    List<String[]> ListaDeseadosACC2;

    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @descripcion Este constructor de la clase mensaje lo hemos implementado con el fin de que
     * gracias a él se puedan llamar a métodos sin tener que crear un mensaje, métodos como CrearBodySolicitudTransaccion.
     * Querremos usar estos métodos por ejemplo a la hora de crear mensajes de solicitud.
     *
     */

    public Mensaje(){

    }


    /**
     * @Descripcion Constructor de la case Mensaje para MENSAJES RECIBIDOS: crea una instancia de la clase Mensaje a partir de un string de entrada que
     * corresponde al mensaje XML que se ha recibido. Este string se convierte a un Documento XML, el cual se recorre guardando
     * cada atributo del mensaje XML en sus variables correspondientes de la clase Mensaje.
     *
     * @author Grupo_4
     * @version 29/11/2022
     * @Ultima_Modificacion 29/11/2022 13:00
     * @param xml (string que nos llega del socket)
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws org.xml.sax.SAXException
     */
    public Mensaje(String xml) throws ParserConfigurationException, IOException, SAXException, org.xml.sax.SAXException {

        //Creamos el documento XML desde el String que hemos recibido (por socket)
        // quitamos los espacios en blanco del final del fichero
        xml = xml.substring(0, xml.lastIndexOf('>')+1);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));//.getByteStream());

        //una vez tenemos el DOM:
        //Tratamos la cabeza del mensaje
        NodeList nListHead = doc.getElementsByTagName("head");
        Node nNodeHead = nListHead.item(0);
        Element eElementHead = (Element) nNodeHead;

        /**
         * Una vez "dentro" de la etiqueta del head del mensaje XML podemos obtener los valoress de las etiquetas
         * que queremos fácilmente con el siguiente código. Sabemos de estas etiquetas porque conocemos la estructura
         * de los mensajes XML.
         * */
        this.tipoMensaje = eElementHead.getElementsByTagName("tipo").item(0).getTextContent();
        this.idMensaje = eElementHead.getElementsByTagName("id_Mensaje").item(0).getTextContent();

        // Repetimos el proceso, esta vez para obtener los datos del emisor.
        NodeList nListEmisor = eElementHead.getElementsByTagName("emisor");
        Node nNodeEmisor = nListEmisor.item(0);
        Element eElementEmisor = (Element) nNodeEmisor;
        this.emisorID = eElementEmisor.getElementsByTagName("id").item(0).getTextContent();
        this.puertoEmisor = eElementEmisor.getElementsByTagName("puerto").item(0).getTextContent();
        this.emisorIP = eElementEmisor.getElementsByTagName("ip").item(0).getTextContent();

        // Repetimos el proceso, esta vez para obtener los datos del receptor.
        NodeList nListReceptor = eElementHead.getElementsByTagName("receptor");
        Node nNodeReceptor = nListReceptor.item(0);
        Element eElementReceptor = (Element) nNodeReceptor;
        this.receptorID = eElementReceptor.getElementsByTagName("id").item(0).getTextContent();
        this.puertoReceptor = eElementReceptor.getElementsByTagName("puerto").item(0).getTextContent();
        this.receptorIP = eElementReceptor.getElementsByTagName("ip").item(0).getTextContent();

        //TRATAR body: POR AHORA SE GUARDA COMO UNA INSTANCIA ELEMENT en eElementBody (Aun no se trata)
        NodeList nListBody = doc.getElementsByTagName("body");
        Node nNodeBody = nListBody.item(0);
        this.eElementBody = (Element) nNodeBody;

        //Dependiendo del tipo de mensaje crearemos el body de una forma u otra.
        switch (this.tipoMensaje){
            case "SolicitudTransaccion":
                bodySolicitudTransaccion(this.eElementBody);
            case "Respuesta_SolicitudTransaccion":
                bodyRespuesta_SolicitudTransaccion(this.eElementBody);
            case "oferta":
                bodyoferta(this.eElementBody);
            case "OK_oferta":
                bodyOK_oferta(this.eElementBody);
            case "KO_transaccion":
                bodyKO_transaccion(this.eElementBody);
            case "error_transaccion":
                bodyerror_transaccion(this.eElementBody);
        }

    }

    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param body
     * @descripcion en este método sacamos los datos del body de los mensajes tipo SolicitudTransaccion
     */
    private void bodySolicitudTransaccion(Element body){
        this.id_transaccion = body.getElementsByTagName("id_transaccion").item(0).getTextContent();

        NodeList LDeseados = body.getElementsByTagName("ListaDeseadosACC1");
        Node nNodeDeseo1 = LDeseados.item(0);
        Element eElementDeseados = (Element) nNodeDeseo1;
        NodeList nListDeseoCromos = eElementDeseados.getElementsByTagName("cromo");//aqui
        for(int i=0; i < nListDeseoCromos.getLength(); i++){
            Node nNodoCromo = nListDeseoCromos.item(i);// i
            Element eElementCromo = (Element) nNodoCromo;
            String id, interes, precio;
            id= eElementCromo.getElementsByTagName("id_Cromo").item(0).getTextContent();
            interes = eElementCromo.getElementsByTagName("interes").item(0).getTextContent();
            precio = eElementCromo.getElementsByTagName("precio").item(0).getTextContent();
            String aux[] = {id, precio, interes};
            this.ListaDeseadosACC1.add(aux);
        }
        //this.ListaDeseadosACC2 = null;
    }

    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param body
     * @descripcion en este método sacamos los datos del body de los mensajes tipo Respuesta_SolicitudTransaccion
     */
    private void bodyRespuesta_SolicitudTransaccion(Element body){
        this.id_transaccion = body.getElementsByTagName("id_transaccion").item(0).getTextContent();

        NodeList LDeseados1 = body.getElementsByTagName("ListaDeseadosACC1");
        Node nNodeDeseo1 = LDeseados1.item(0);
        Element eElementDeseados1 = (Element) nNodeDeseo1;
        NodeList nListDeseoCromos1 = eElementDeseados1.getElementsByTagName("cromo");
        for(int i=0; i < nListDeseoCromos1.getLength(); i++){
            Node nNodoCromo = nListDeseoCromos1.item(i);// i
            Element eElementCromo = (Element) nNodoCromo;
            String id, interes, precio;
            id= eElementCromo.getElementsByTagName("id_Cromo").item(0).getTextContent();
            interes = eElementCromo.getElementsByTagName("interes").item(0).getTextContent();
            precio = eElementCromo.getElementsByTagName("precio").item(0).getTextContent();
            String aux[] = {id, precio, interes};
            this.ListaDeseadosACC1.add(aux);
        }

        NodeList LDeseados2 = body.getElementsByTagName("ListaDeseadosACC2");
        Node nNodeDeseo2 = LDeseados2.item(0);
        Element eElementDeseados2 = (Element) nNodeDeseo2;
        NodeList nListDeseoCromos2 = eElementDeseados2.getElementsByTagName("cromo");
        for(int i=0; i < nListDeseoCromos2.getLength(); i++){
            Node nNodoCromo = nListDeseoCromos2.item(i);// i
            Element eElementCromo = (Element) nNodoCromo;
            String id, interes, precio;
            id= eElementCromo.getElementsByTagName("id_Cromo").item(0).getTextContent();
            interes = eElementCromo.getElementsByTagName("interes").item(0).getTextContent();
            precio = eElementCromo.getElementsByTagName("precio").item(0).getTextContent();
            String aux[] = {id, precio, interes};
            this.ListaDeseadosACC2.add(aux);
        }
    }

    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param body
     * @descripcion en este método sacamos los datos del body de los mensajes tipo oferta
     */
    private void bodyoferta(Element body){
        this.id_transaccion = body.getElementsByTagName("id_transaccion").item(0).getTextContent();
        this.id_oferta = body.getElementsByTagName("id_oferta").item(0).getTextContent();
        this.TTL = body.getElementsByTagName("TTL").item(0).getTextContent();

        NodeList oferta = body.getElementsByTagName("Oferta");
        Node nodeoferta = oferta.item(0);
        Element eElementoferta = (Element) nodeoferta;

        NodeList LDeseados1 = eElementoferta.getElementsByTagName("ListaDeseadosACC1");
        Node nNodeDeseo1 = LDeseados1.item(0);
        Element eElementDeseados1 = (Element) nNodeDeseo1;
        NodeList nListDeseoCromos1 = eElementDeseados1.getElementsByTagName("cromo");
        for(int i=0; i < nListDeseoCromos1.getLength(); i++){
            Node nNodoCromo = nListDeseoCromos1.item(i);// i
            Element eElementCromo = (Element) nNodoCromo;
            String id, interes, precio;
            id= eElementCromo.getElementsByTagName("id_Cromo").item(0).getTextContent();
            interes = eElementCromo.getElementsByTagName("interes").item(0).getTextContent();
            precio = eElementCromo.getElementsByTagName("precio").item(0).getTextContent();
            String aux[] = {id, precio, interes};
            this.ListaDeseadosACC1.add(aux);
        }

        NodeList LDeseados2 = eElementoferta.getElementsByTagName("ListaDeseadosACC2");
        Node nNodeDeseo2 = LDeseados2.item(0);
        Element eElementDeseados2 = (Element) nNodeDeseo2;
        NodeList nListDeseoCromos2 = eElementDeseados2.getElementsByTagName("cromo");
        for(int i=0; i < nListDeseoCromos2.getLength(); i++){
            Node nNodoCromo = nListDeseoCromos2.item(i);// i
            Element eElementCromo = (Element) nNodoCromo;
            String id, interes, precio;
            id= eElementCromo.getElementsByTagName("id_Cromo").item(0).getTextContent();
            interes = eElementCromo.getElementsByTagName("interes").item(0).getTextContent();
            precio = eElementCromo.getElementsByTagName("precio").item(0).getTextContent();
            String aux[] = {id, precio, interes};
            this.ListaDeseadosACC2.add(aux);
        }
    }

    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param body
     * @descripcion en este método sacamos los datos del body de los mensajes tipo OK_oferta
     */
    private void bodyOK_oferta(Element body){
        this.id_transaccion = body.getElementsByTagName("id_transaccion").item(0).getTextContent();
        this.id_oferta = body.getElementsByTagName("id_oferta").item(0).getTextContent();
        this.TTL = body.getElementsByTagName("TTL").item(0).getTextContent();
    }

    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param body
     * @descripcion en este método sacamos los datos del body de los mensajes tipo KO_oferta
     */
    private void bodyKO_transaccion(Element body){
        this.id_transaccion = body.getElementsByTagName("id_transaccion").item(0).getTextContent();
        //this.id_oferta = body.getElementsByTagName("id_oferta").item(0).getTextContent();
        //this.TTL = body.getElementsByTagName("TTL").item(0).getTextContent();
    }

    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param body
     * @descripcion en este método sacamos los datos del body de los mensajes tipo error_transaccion
     */
    private void bodyerror_transaccion(Element body){
        this.id_transaccion = body.getElementsByTagName("id_transaccion").item(0).getTextContent();
        this.info_error = body.getElementsByTagName("info_error").item(0).getTextContent();

        //this.id_oferta = body.getElementsByTagName("id_oferta").item(0).getTextContent();
        //this.TTL = body.getElementsByTagName("TTL").item(0).getTextContent();
    }

    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param Deseados1 lista de cromos deseados que se va a introducir en el mensaje
     * @return se returnea un elemnto body compatible con mensajes tipo SolicitudTransaccion
     * @throws ParserConfigurationException
     * @descripcion esta funcion deberá llamarse para crear un body para un mensaje tipo SolicitudTransaccion.
     * Recibe la lista de cromos que se quieren solicitar en el mensaje y crea una id_transsacion que asignara al body del mensaje.
     */
    public  Element CrearBodySolicitud(List<String[]> Deseados1) throws ParserConfigurationException {
        UUID uuid = UUID.randomUUID();         //Genera un UUID que es un identificador unico y lo guarda en la variable uuid.
        String id_transaccion_valor = uuid.toString();      //Convierte el objeto uuid en un String de Java llamando al toString haciendo mas facil el tratameinto y el almacenamiento de este.

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element ElementBody = doc.createElement("body");

        Element Elementid_transaccion = doc.createElement("id_transaccion");
        Elementid_transaccion.appendChild(doc.createTextNode(id_transaccion_valor));

        ElementBody.appendChild(Elementid_transaccion);

        Element ElementListaDeseadosACC1 = doc.createElement("ListaDeseadosACC1");

        for(int i=0; i < Deseados1.size(); i++){

            Element ElementCromo = doc.createElement("cromo");

            Element Elementid_cromo = doc.createElement("id_Cromo");
            ElementCromo.appendChild(doc.createTextNode(Deseados1.get(i)[0]));

            Element Elementinteres = doc.createElement("interes");
            ElementCromo.appendChild(doc.createTextNode(Deseados1.get(i)[1]));

            Element Elementprecio = doc.createElement("precio");
            ElementCromo.appendChild(doc.createTextNode(Deseados1.get(i)[2]));

            ElementListaDeseadosACC1.appendChild(ElementCromo);
        }

        ElementBody.appendChild(ElementListaDeseadosACC1);

        return ElementBody;
    }


    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param id_transaccion
     * @param Deseados1
     * @param Deseados2
     * @return
     * @throws ParserConfigurationException
     * @descripcion Recibe el id de la transaccion que asignara al body y las listas de cromos deseados por ambos agentes.
     */
    public Element CrearBodyRespuesta_SolicitudTransaccion(String id_transaccion, List<String[]> Deseados1, List<String[]> Deseados2) throws ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element ElementBody = doc.createElement("body");

        Element Elementid_transaccion = doc.createElement("id_transaccion");
        Elementid_transaccion.appendChild(doc.createTextNode(id_transaccion));

        ElementBody.appendChild(Elementid_transaccion);

        Element ElementListaDeseadosACC1 = doc.createElement("ListaDeseadosACC1");

        for(int i=0; i < Deseados1.size(); i++){

            Element ElementCromo = doc.createElement("cromo");

            Element Elementid_cromo = doc.createElement("id_Cromo");
            ElementCromo.appendChild(doc.createTextNode(Deseados1.get(i)[0]));

            Element Elementinteres = doc.createElement("interes");
            ElementCromo.appendChild(doc.createTextNode(Deseados1.get(i)[1]));

            Element Elementprecio = doc.createElement("precio");
            ElementCromo.appendChild(doc.createTextNode(Deseados1.get(i)[2]));

            ElementListaDeseadosACC1.appendChild(ElementCromo);
        }

        ElementBody.appendChild(ElementListaDeseadosACC1);

        Element ElementListaDeseadosACC2 = doc.createElement("ListaDeseadosACC2");

        for(int i=0; i < Deseados2.size(); i++){

            Element ElementCromo = doc.createElement("cromo");

            Element Elementid_cromo = doc.createElement("id_Cromo");
            ElementCromo.appendChild(doc.createTextNode(Deseados2.get(i)[0]));

            Element Elementinteres = doc.createElement("interes");
            ElementCromo.appendChild(doc.createTextNode(Deseados2.get(i)[1]));

            Element Elementprecio = doc.createElement("precio");
            ElementCromo.appendChild(doc.createTextNode(Deseados2.get(i)[2]));

            ElementListaDeseadosACC2.appendChild(ElementCromo);
        }

        ElementBody.appendChild(ElementListaDeseadosACC2);

        return ElementBody;
    }


    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param id_transaccion
     * @param Deseados1
     * @param Deseados2
     * @return
     * @throws ParserConfigurationException
     * @descripcion Igual a CrearBodyRespuesta_SolicitudTransaccion pero tambien se crea un id de oferta.
     */
    public Element CrearBodyoferta(String id_transaccion,List<String []> Deseados1, List<String []> Deseados2 ) throws ParserConfigurationException {

        UUID uuid = UUID.randomUUID();         //Genera un UUID que es un identificador unico y lo guarda en la variable uuid.
        String id_oferta = uuid.toString();      //Convierte el objeto uuid en un String de Java llamando al toString haciendo mas facil el tratameinto y el almacenamiento de este.

        String TTL = "1000";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element ElementBody = doc.createElement("body");

        Element Elementid_transaccion = doc.createElement("id_transaccion");
        Elementid_transaccion.appendChild(doc.createTextNode(id_transaccion));

        Element Elementid_oferta = doc.createElement("id_oferta");
        Elementid_oferta.appendChild(doc.createTextNode(id_oferta));

        Element ElementTTL = doc.createElement("TTL");
        ElementTTL.appendChild(doc.createTextNode(TTL));

        ElementBody.appendChild(Elementid_transaccion);
        ElementBody.appendChild(Elementid_oferta);
        ElementBody.appendChild(ElementTTL);

        Element oferta = doc.createElement("Oferta");


        Element ElementListaDeseadosACC1 = doc.createElement("ListaDeseadosACC1");

        for(int i=0; i < Deseados1.size(); i++){

            Element ElementCromo = doc.createElement("cromo");

            Element Elementid_cromo = doc.createElement("id_Cromo");
            ElementCromo.appendChild(doc.createTextNode(Deseados1.get(i)[0]));

            Element Elementinteres = doc.createElement("interes");
            ElementCromo.appendChild(doc.createTextNode(Deseados1.get(i)[1]));

            Element Elementprecio = doc.createElement("precio");
            ElementCromo.appendChild(doc.createTextNode(Deseados1.get(i)[2]));

            ElementListaDeseadosACC1.appendChild(ElementCromo);
        }

        oferta.appendChild(ElementListaDeseadosACC1);

        Element ElementListaDeseadosACC2 = doc.createElement("ListaDeseadosACC2");

        for(int i=0; i < Deseados2.size(); i++){

            Element ElementCromo = doc.createElement("cromo");

            Element Elementid_cromo = doc.createElement("id_Cromo");
            ElementCromo.appendChild(doc.createTextNode(Deseados2.get(i)[0]));

            Element Elementinteres = doc.createElement("interes");
            ElementCromo.appendChild(doc.createTextNode(Deseados2.get(i)[1]));

            Element Elementprecio = doc.createElement("precio");
            ElementCromo.appendChild(doc.createTextNode(Deseados2.get(i)[2]));

            ElementListaDeseadosACC2.appendChild(ElementCromo);
        }

        oferta.appendChild(ElementListaDeseadosACC2);

        ElementBody.appendChild(oferta);

        return ElementBody;
    }


    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param id_transaccion
     * @param id_oferta
     * @return
     * @throws ParserConfigurationException
     * @descripcion se crea el mesaje con los id pasados como parámetros.
     */
    public Element CrearBodyOK_oferta(String id_transaccion, String id_oferta) throws ParserConfigurationException {


        String TTL = "1000";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element ElementBody = doc.createElement("body");

        Element Elementid_transaccion = doc.createElement("id_transaccion");
        Elementid_transaccion.appendChild(doc.createTextNode(id_transaccion));

        Element Elementid_oferta = doc.createElement("id_oferta");
        Elementid_oferta.appendChild(doc.createTextNode(id_oferta));

        Element ElementTTL = doc.createElement("TTL");
        ElementTTL.appendChild(doc.createTextNode(TTL));

        ElementBody.appendChild(Elementid_transaccion);
        ElementBody.appendChild(Elementid_oferta);
        ElementBody.appendChild(ElementTTL);

        return ElementBody;
    }

    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param id_transaccion
     * @return
     * @throws ParserConfigurationException
     */
    public Element CrearBodyKO_oferta(String id_transaccion/*, String id_oferta*/) throws ParserConfigurationException {


        //String TTL = "1000";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element ElementBody = doc.createElement("body");

        Element Elementid_transaccion = doc.createElement("id_transaccion");
        Elementid_transaccion.appendChild(doc.createTextNode(id_transaccion));

       /* Element Elementid_oferta = doc.createElement("id_oferta");
        Elementid_oferta.appendChild(doc.createTextNode(id_oferta));

        Element ElementTTL = doc.createElement("TTL");
        ElementTTL.appendChild(doc.createTextNode(TTL));

        ElementBody.appendChild(Elementid_oferta);
        ElementBody.appendChild(ElementTTL);*/
        ElementBody.appendChild(Elementid_transaccion);


        return ElementBody;
    }

    /**
     * @autor: Grupo 4
     * @fecha 17/12/2022
     * @ultimamodificacion 18/12/2022, Javier Pérez, Alejandro Martínez y José Jesús Gonzalez
     * @param id_transaccion
     * @param error
     * @return
     * @throws ParserConfigurationException
     */
    public Element CrearBodyerror_transaccion(String id_transaccion, String error/*, String id_oferta*/) throws ParserConfigurationException {


        //String TTL = "1000";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element ElementBody = doc.createElement("body");

        Element Elementid_transaccion = doc.createElement("id_transaccion");
        Elementid_transaccion.appendChild(doc.createTextNode(id_transaccion));

        Element Elementinfo_error = doc.createElement("info_error");
        Elementid_transaccion.appendChild(doc.createTextNode(error));

       /* Element Elementid_oferta = doc.createElement("id_oferta");
        Elementid_oferta.appendChild(doc.createTextNode(id_oferta));

        Element ElementTTL = doc.createElement("TTL");
        ElementTTL.appendChild(doc.createTextNode(TTL));

        ElementBody.appendChild(Elementid_oferta);
        ElementBody.appendChild(ElementTTL);*/
        ElementBody.appendChild(Elementid_transaccion);
        ElementBody.appendChild(Elementinfo_error);


        return ElementBody;
    }


    /**
     *  @Descripcion Constructor de la case Mensaje para MENSAJES A ENVIAR: Crea una instancia de la Clase Mensaje para
     *  construir un Mensaje a Enviar. Crea un documento XML, cuyo contenido corresponderá a los elementos de entrada del constructor.
     *  Una vez construido el mensaje XML, se validará con el documento XSD, también recibido como parámetro de entrada.
     *  creando un objeto document donde se almacena el XML formado por la cabecera y el cuerpo parados por parametros
     *  @author Grupo_4
     *  @version 29/11/2022
     *  @Ultima_Modificacion 29/11/2022 13:00
     *  @param cabe= HashMap con la informacion de la cabecera del mensaje XML a construir (nombre_campo, valor).
     *  @param body= cuerpo del mensaje, actualmente un atributo Element vacío (a implementar).
     *  @param dom= XSD para validar el mensaje.
     */
    public Mensaje(HashMap<String,Object> cabe, Element body, File dom){

        //Sacamos todos los atributos del hashmap pasado por parametros y los almacenamos
        this.receptorID=(String)((HashMap)cabe.get("receptor")).get("id");
        this.emisorID=(String)((HashMap)cabe.get("emisor")).get("id");
        this.puertoEmisor=(String) ((HashMap)cabe.get("emisor")).get("puerto");
        this.puertoReceptor=(String)((HashMap)cabe.get("receptor")).get("puerto");
        this.emisorIP= (String) ((HashMap)cabe.get("emisor")).get("ip");
        this.receptorIP=(String) ((HashMap)cabe.get("receptor")).get("ip");
        this.idMensaje=(String)cabe.get("id_Mensaje");
        this.tipoMensaje= (String) cabe.get("tipo");
        this.horaGeneracion=(String) cabe.get("tiempoEnvio");
        this.protocolo=(String)cabe.get("protocolo");

        //LLamada al metodo que genera un DOM y lo guarda en la clase a partir del head y body pasados
        try{
        this.mensaje=creaXML(cabe, body);
        this.correcto=false;
        if(this.mensaje!=null) {
            if (this.validaXML(this.mensaje)) {
                //Sera cierto si es posible validar el XML con el XSD pasado por parametro
                this.correcto = true;
            }

        }
        }catch (Exception e){
            System.out.println(e);
        }

    }
    /**Obtiene el documento a partir de los parametros
     *
     * @param cab cabeza del documento a generar
     * @param body cuerpo del documento
     * @return documento XML
     */


    private Document creaXML(HashMap<String,Object> cab,Element body){
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();


            //Se crea el elemento raiz
            Document document =  db.newDocument();


            Element root = document.createElement("root");

            //Se crean los elementos cabeza y cuerpo del que cuelgan el resto de nodos
            Element cabeza = document.createElement("head");
            Element cuerpo = document.createElement("body");


            //Se obtienen a partir de cab todos los nodos, que se recorren en profundidad para obtener el documento
            Set<String> conj =  cab.keySet();
            Iterator<String> it = conj.iterator();
            while(it.hasNext()){
                String etiqueta = it.next();
                Element e = document.createElement(etiqueta);
                Object o = cab.get(etiqueta);
                //Si hashmap en un elemento guarda un string significa que ese elemento no tiene mas hijos
                if(o instanceof String){
                    //Se obtiene los de profundidad 1
                    e.appendChild(document.createTextNode(String.valueOf(o)));
                    cabeza.appendChild(e);
                }
                else{
                    //Llamada para obener aquellos de profundidad +2
                    //Ya que un hashmap, cuando almacena un Hashmap para una key en lugar de un String, significa
                    // que tiene mas hijos
                    addRamasElem((HashMap<String,Object>)o,e,document);
                    cabeza.appendChild(e);
                }
            }
            /* Codigo que se usará cuando tengamos que procesar el cuerpo, que funciona igual que con head pero para body
            //Idem pero con el cuerpo
            conj =  body.keySet();
            it = conj.iterator();
            while(it.hasNext()){
                String etiqueta = it.next();
                Element e = document.createElement(etiqueta);
                Object o = cab.get(etiqueta);
                //Si hashmap en un elemento guarda un string significa que ese elemento no tiene mas hijos
                if(o instanceof String){
                    //Se obtiene los de profundidad 1
                    e.appendChild(document.createTextNode(String.valueOf(o)));
                    cuerpo.appendChild(e);
                }
                else{
                    //Llamada para obener aquellos de profundidad +2
                    addRamasElem((HashMap<String,Object>)o,e,document);
                    cuerpo.appendChild(e);
                }

            }

            //Se añaden los respectivos subarboles a la cabeza y cuerpo del documento
            */

            root.appendChild(cabeza);
            root.appendChild(cuerpo);
            document.appendChild(root);
            //System.out.println("Documento generado");
            return document;

        }
        catch(Exception e){
            System.out.println("Fallo en la creacion del documento");
        }
        return null;


    }


    //Metodo para obtener los arboles de profundidad mayor a dos, para generar el DOM
    private void addRamasElem(HashMap<String,Object> lista, Element e,Document document){
        Set<String> setKey = lista.keySet();
        Iterator<String> it = setKey.iterator();
        while(it.hasNext()){
            String key = it.next();
            Object o = lista.get(key);
            Element el = document.createElement(key);
            if( o instanceof String){

                el.appendChild(document.createTextNode((String) o) );
                e.appendChild(el);
            }
            else{
                //Si el hashmap no contiene para una key un String, es que tiene mas hijos, por lo tanto, los procesamos en profundidad
                addRamasElem((HashMap<String, Object>) o,el,document);
            }
        }
    }

    //Valida un XML a partir de un XSD

    /**
     *
     * @param dom
     * @return
     */
    private boolean validaXML(Document dom){
        try {

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Schema schema;
            switch (this.tipoMensaje){
                case "SolicitudTransaccion":
                    schema = factory.newSchema(new File("C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\ACC\\src\\xsdTipo1.xsd"));
                    break;
                case "Respuesta_SolicitudTransaccion":
                    schema = factory.newSchema(new File("C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\ACC\\src\\xsdTipo2.xsd"));
                    break;
                case "oferta":
                    schema = factory.newSchema(new File("C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\ACC\\src\\xsdTipo3.xsd"));
                    break;
                case "OK_oferta":
                    schema = factory.newSchema(new File("C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\ACC\\src\\xsdTipo4.xsd"));
                    break;
                case "KO_transaccion":
                    schema = factory.newSchema(new File("C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\ACC\\src\\xsdTipo5.xsd"));
                    break;
                case "error_transaccion":
                    schema = factory.newSchema(new File("C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\ACC\\src\\xsdTipo6.xsd"));
                    break;

            }
            schema = factory.newSchema(new File("C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\estructuraXML.xsd"));

            Validator validator = schema.newValidator();

            //EN path poner el doc XSD
            guardaConFormato(dom,"C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\ACC\\src\\xml_doc.xml");
            validator.validate(new StreamSource(new File("C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\ACC\\src\\xml_doc.xml")));

        } catch (Exception e) {
            System.out.println("Exception: "+e.getMessage());
            return false;
        }
        return true;
    }


    // Volcamos XML al fichero
    public static void guardaConFormato(Document document, String URI){
        try {
            TransformerFactory transFact = TransformerFactory.newInstance();

            //Formateamos el fichero. Añadimos sangrado y la cabecera de XML
            transFact.setAttribute("indent-number", new Integer(3));
            Transformer trans = transFact.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

            //Hacemos la transformación
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            DOMSource domSource = new DOMSource(document);
            trans.transform(domSource, sr);

            //Mostrar información a guardar por consola (opcional)
            //Result console= new StreamResult(System.out);
            //trans.transform(domSource, console);
            try {
                //Creamos fichero para escribir en modo texto
                PrintWriter writer = new PrintWriter(new FileWriter(URI));

                //Escribimos el árbol en el fichero
                writer.println(sw.toString());

                //Cerramos el fichero
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }



    //GETTERS


    public Document getMensaje() {
        return mensaje;
    }

    public boolean isCorrecto() {
        return correcto;
    }

    public String getReceptorID() {
        return receptorID;
    }

    public String getPuertoReceptor() {
        return puertoReceptor;
    }

    public String getReceptorIP() {
        return receptorIP;
    }

    public String getEmisorIP() {
        return emisorIP;
    }

    public String getEmisorID() {
        return emisorID;
    }

    public String getPuertoEmisor() {
        return puertoEmisor;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public String getHoraGeneracion() {
        return horaGeneracion;
    }

    public String getTipoMensaje() {
        return tipoMensaje;
    }

    public Element geteElementBody() {
        return eElementBody;
    }

    public String getId_transaccion() {
        return id_transaccion;
    }

    public String getId_oferta() {
        return id_oferta;
    }

    public String getTTL() {
        return TTL;
    }

    public String getInfo_error() {
        return info_error;
    }

    public List<String[]> getListaDeseadosACC1() {
        return ListaDeseadosACC1;
    }

    public List<String[]> getListaDeseadosACC2() {
        return ListaDeseadosACC2;
    }
}
