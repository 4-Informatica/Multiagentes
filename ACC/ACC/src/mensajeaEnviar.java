/**
 * @class  mensajeaEnviar
 * @group Grupo 3
 * @version 2.0
 *
*/
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 *  En esta clase se realizará la gestion del contenedor de mensajes a enviar, encontraremos un main para ejecutarlo,
 *  un constructor al que se le pasa por parametro una cabecera, un cuerpo y un dom del mensaje.
 */
public class mensajeaEnviar {
    Document mensaje;
    boolean correcto;

    InetAddress ip_Emisor;
    InetAddress ip_Receptor;

    String puerto_Emisor;
    String puerto_Receptor;

    String id_Emisor;
    String id_Receptor;

    String id_Mensaje;
    String protocolo;
    String tipoMensaje;
    String tiempoEnvio;

    Element body;
    HashMap<String, Object> cab;

    /**
     * Metodos getter para cada uno de los atributos de la clase
     */
    public InetAddress getIp_Emisor() {
        return ip_Emisor;
    }

    public InetAddress getIp_Receptor() {
        return ip_Receptor;
    }

    public String getPuerto_Emisor() {
        return puerto_Emisor;
    }

    public String getPuerto_Receptor() {
        return puerto_Receptor;
    }

    public String getId_Emisor() {
        return id_Emisor;
    }

    public String getId_Receptor() {
        return id_Receptor;
    }

    public String getId_Mensaje() {
        return id_Mensaje;
    }

    public String getTipoMensaje() {
        return tipoMensaje;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public String getTiempoEnvio() {
        return tiempoEnvio;
    }

    public Element getBody() {
        return body;
    }

    /**
     * Instancia un mensaje, creando un objeto document donde se almacena el XML formado por la cabecera y el cuerpo parados por parametros
     *
     * @param cabe=       cabecera del mensaje
     * @param body=cuerpo del mensaje, dejamos en Element vacio porque aun no se implementa la parte del cuerpo
     * @param dom=        XLS para comprobar que es correcto el mensaje
     */
    public mensajeaEnviar(HashMap<String, Object> cabe, Element body, File dom) {
        this.cab = cabe;
        this.body = body;


        //Sacamos todos los atributos del hashmap pasado por parametros y los almacenamos
        this.id_Receptor = (String) ((HashMap) cabe.get("receptor")).get("id");
        this.id_Emisor = (String) ((HashMap) cabe.get("emisor")).get("id");
        this.puerto_Emisor = (String) ((HashMap) cabe.get("emisor")).get("port");
        this.puerto_Receptor = (String) ((HashMap) cabe.get("receptor")).get("port");
        this.ip_Emisor = (InetAddress) ((HashMap) cabe.get("emisor")).get("ip");
        this.ip_Receptor = (InetAddress) ((HashMap) cabe.get("receptor")).get("ip");
        this.id_Mensaje = (String) cabe.get("id_Mensaje");
        this.tipoMensaje = (String) cabe.get("tipoMensaje");
        this.tiempoEnvio = (String) cabe.get("tiempoEnvio");
        this.protocolo = (String) cabe.get("protocolo");

        //LLamada al metodo que genera un DOM y lo guarda en la clase a partir del head y body pasados
        this.mensaje = creaXML(cab, body);

        this.correcto = false;
        if (this.mensaje != null) {

            if (this.validaXML(dom)) {
                //Sera cierto si es posible validar el XML con el XSL pasado por parametro
                this.correcto = true;
            }

        }

    }


    /**
     * Obtiene el documento a partir de los parametros
     *
     * @param cab  cabeza del documento a generar
     * @param body cuerpo del documento
     * @return documento XML
     */


    private Document creaXML(HashMap<String, Object> cab, Element body) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();


            //Se crea el elemento raiz
            Document document = db.newDocument();
            Element root = document.createElement("root");

            //Se crean los elementos cabeza y cuerpo del que cuelgan el resto de nodos
            Element cabeza = document.createElement("head");
            Element cuerpo = body;

            //Se obtienen a partir de cab todos los nodos, que se recorren en profundidad para obtener el documento
            Set<String> conj = cab.keySet();
            Iterator<String> it = conj.iterator();
            while (it.hasNext()) {
                String etiqueta = it.next();
                Element e = document.createElement(etiqueta);
                Object o = cab.get(etiqueta);
                //Si hashmap en un elemento guarda un string significa que ese elemento no tiene mas hijos
                if (o instanceof String) {
                    //Se obtiene los de profundidad 1
                    e.appendChild(document.createTextNode(String.valueOf(o)));
                    cabeza.appendChild(e);
                } else {
                    //Llamada para obener aquellos de profundidad +2
                    //Ya que un hashmap, cuando almacena un Hashmap para una key en lugar de un String, significa
                    // que tiene mas hijos
                    addRamasElem((HashMap<String, Object>) o, e, document);
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
            System.out.println("Documento generado");
            return document;


        } catch (Exception e) {
            System.out.println("Fallo en la creacion del documento");
        }
        return null;


    }


    //Metodo para obtener los arboles de profundidad mayor a dos, para generar el DOM
    private void addRamasElem(HashMap<String, Object> lista, Element e, Document document) {
        Set<String> setKey = lista.keySet();
        Iterator<String> it = setKey.iterator();
        while (it.hasNext()) {
            String key = it.next();
            Object o = lista.get(key);
            Element el = document.createElement(key);
            if (o instanceof String) {

                el.appendChild(document.createTextNode((String) o));
                e.appendChild(el);
            } else {
                //Si el hashmap no contiene para una key un String, es que tiene mas hijos, por lo tanto, los procesamos en profundidad
                addRamasElem((HashMap<String, Object>) o, el, document);
            }
        }
    }

    //Valida un XML a partir de un XSD
    private boolean validaXML(File dom) {
        try {

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            //En path,poner el lugar donde se aloje el XML generado
            Schema schema = factory.newSchema(new File("src/file.xsd"));

            Validator validator = schema.newValidator();
            //EN path poner el doc XSD

            validator.validate(new StreamSource(new File("src/file.xml")));

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return false;
        }
        return true;
    }
}