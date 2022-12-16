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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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
    HashMap<String, Object> eElementBody;

    /**
     * Autores: Ignacio Gago Lopez, Pablo Domingo Fernandez, Alejandro Cebrian Sanchez, Daniel Cuenca Ortiz
     * Fecha de creacion: 15/12/2022
     */
    ArrayList<Integer> listaCromos;

//CONSTRUCTOR DE MENSAJE RECIBIDO
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

        /*

        // NO NECESARIO
        /HashMap DicCabeza con todos los atributos de la cabeza del mensaje
        HashMap<String, String> DicCabeza = new HashMap<String, String>();
        DicCabeza.put("tipo", TipoMsg);
        DicCabeza.put("id_Mensaje", ConversacionID);
        DicCabeza.put("emisorID", EmisorID);
        DicCabeza.put("emisorPuerto", EmisorPuerto);
        DicCabeza.put("emisorIP", EmisorIP);
        DicCabeza.put("receptorID", ReceptorID);
        DicCabeza.put("receptorPuerto", ReceptorPuerto);
        DicCabeza.put("receptorIP", ReceptorIP);
        DicCabeza.put("protocolo", Protocolo);
        DicCabeza.put("tiempoEnvio", Fecha);*/

        //TRATAR body: POR AHORA SE GUARDA COMO UNA INSTANCIA ELEMENT en eElementBody (Aun no se trata)
        /**
         * Autores: Ignacio Gago Lopez, Pablo Domingo Fernandez, Alejandro Cebrian Sanchez, Daniel Cuenca Ortiz
         * Fecha de creacion: 15/12/2022
         */
/*
        NodeList nListHead = doc.getElementsByTagName("head");
        Node nNodeHead = nListHead.item(0);
        Element eElementHead = (Element) nNodeHead;
        this.tipoMensaje = eElementHead.getElementsByTagName("tipo").item(0).getTextContent();
 */
        NodeList nListBody = doc.getElementsByTagName("body");
        Node nNodeBody = nListBody.item(0);
        Element eElementBody = (Element) nNodeBody;
        if (eElementBody!=null) {
            NodeList listaNodoCromos = eElementBody.getElementsByTagName("ListaCromos");
            listaNodoCromos = listaNodoCromos.item(0).getChildNodes();
            this.listaCromos = new ArrayList<>();

            for (int i = 0; i < listaNodoCromos.getLength(); i++) {
                this.listaCromos.add(Integer.valueOf(listaNodoCromos.item(i).getTextContent()));
            }
        }

        //this.eElementBody = (HashMap<String, Object>) nNodeBody;

    }

    /** Instancia un mensaje, creando un objeto document donde se almacena el XML formado por la cabecera y el cuerpo parados por parametros
     *
     * @param cabe= cabecera del mensaje
     * @param body=cuerpo del mensaje, dejamos en Element vacio porque aun no se implementa la parte del cuerpo
     * @param dom= XLS para comprobar que es correcto el mensaje
     */
    public Mensaje(HashMap<String,Object> cabe, HashMap<String,Object> body, File dom){

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

        /**
         * Autores: Ignacio Gago Lopez, Pablo Domingo Fernandez, Alejandro Cebrian Sanchez, Daniel Cuenca Ortiz
         * Fecha de creacion: 16/12/2022
         * Guardamos el body en eElementBody
         */
        this.eElementBody = body;
        //LLamada al metodo que genera un DOM y lo guarda en la clase a partir del head y body pasados
        try{
        this.mensaje=creaXML(cabe, body);
        this.correcto=false;
        if(this.mensaje!=null) {
            if (this.validaXML(dom)) {
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


    private Document creaXML(HashMap<String,Object> cab,HashMap<String,Object> body){
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
            // Codigo que se usará cuando tengamos que procesar el cuerpo, que funciona igual que con head pero para body
            //Idem pero con el cuerpo
            if (body!=null) {
                conj = body.keySet();
                it = conj.iterator();
                while (it.hasNext()) {
                    String etiqueta = it.next();
                    Element e = document.createElement(etiqueta);
                    Object o = body.get(etiqueta);
                    //Si hashmap en un elemento guarda un string significa que ese elemento no tiene mas hijos
                    if (o instanceof String) {
                        //Se obtiene los de profundidad 1
                        e.appendChild(document.createTextNode(String.valueOf(o)));
                        cuerpo.appendChild(e);
                    } else {
                        //Llamada para obener aquellos de profundidad +2
                        addRamasElem((HashMap<String, Object>) o, e, document);
                        cuerpo.appendChild(e);
                    }

                }
            }

            //Se añaden los respectivos subarboles a la cabeza y cuerpo del documento

            root.appendChild(cabeza);
            root.appendChild(cuerpo);
            document.appendChild(root);
            //System.out.println("Documento generado");

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("C:\\Users\\Dani\\Desktop\\xml.xml"));
            // If you use
            // StreamResult result = new StreamResult(System.out);
            // the output will be pushed to the standard output ...
            // You can use that for debugging
            transformer.transform(domSource, streamResult);
            //System.out.println("Done creating XML File");

            return document;

        }
        catch(Exception e){
            System.out.println("Fallo en la creacion del documento "+e);
            e.printStackTrace();
        }
        return null;


    }


    //Metodo para obtener los arboles de profundidad mayor a dos, para generar el DOM
    // Antes era privado
    public void addRamasElem(HashMap<String,Object> lista, Element e,Document document){
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
    private boolean validaXML(File dom){
        try {

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            //En path ,poner el lugar donde se aloje el XSD
            Schema schema = factory.newSchema(new File("C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\estructuraXML.xsd"));

            Validator validator = schema.newValidator();

            //EN path poner el doc XSD
            // TODO arreglar para que se use File dom pasado por parametro
            //validator.validate(new StreamSource(new File("C:\\Users\\" + System.getProperty("user.name") +"\\Desktop\\Multiagentes-main\\ACC\\src/estructuraXML.xml")));

        } catch (Exception e) {
            System.out.println("Exception: "+e.getMessage());
            return false;
        }
        return true;
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

    public HashMap<String, Object> geteElementBody() {
        return eElementBody;
    }
}
