import jdk.internal.org.xml.sax.InputSource;
import jdk.internal.org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public class mensajeRecibido {
    Document mensaje;
    boolean correcto;

    //EMISOR ATTRIBUTES
    String ReceptorID;
    String ipEmisor;
    String idEmisor;
    String puertoEmisor;

    //RECEPTOR ATRIBUTOS
    String ReceptorPuerto;
    String ReceptorIP;
    String Protocolo;


    //MENSAJE ATTRIBUTES
    String idMensaje;
    String Fecha;
    String horaGeneracion; //No esta en mensaje GRUPO 4
    String tipoMensaje;

    //MENSAJE CUERPO
    Element eElementBody;


    public mensajeRecibido(String xml) throws ParserConfigurationException, IOException, SAXException, org.xml.sax.SAXException {


        //Creamos el documento XML desde el String que hemos recibido (por socket)
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(String.valueOf(new InputSource(new StringReader(xml))));//.getByteStream());

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
        String TipoMsg = eElementHead.getElementsByTagName("tipo").item(0).getTextContent();
        String ConversacionID = eElementHead.getElementsByTagName("id_Mensaje").item(0).getTextContent();

        // Repetimos el proceso, esta vez para obtener los datos del emisor.
        NodeList nListEmisor = eElementHead.getElementsByTagName("emisor");
        Node nNodeEmisor = nListEmisor.item(0);
        Element eElementEmisor = (Element) nNodeEmisor;
        idEmisor = eElementEmisor.getElementsByTagName("id").item(0).getTextContent();
        puertoEmisor = eElementEmisor.getElementsByTagName("puerto").item(0).getTextContent();
        ipEmisor = eElementEmisor.getElementsByTagName("ip").item(0).getTextContent();

        // Repetimos el proceso, esta vez para obtener los datos del receptor.
        NodeList nListReceptor = eElementHead.getElementsByTagName("receptor");
        Node nNodeReceptor = nListReceptor.item(0);
        Element eElementReceptor = (Element) nNodeReceptor;
        ReceptorID = eElementReceptor.getElementsByTagName("id").item(0).getTextContent();
        ReceptorIP = eElementReceptor.getElementsByTagName("ip").item(0).getTextContent();

        Protocolo = eElementHead.getElementsByTagName("protocolo").item(0).getTextContent();
        Fecha = eElementHead.getElementsByTagName("tiempoEnvio").item(0).getTextContent();

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
        NodeList nListBody = doc.getElementsByTagName("body");
        Node nNodeBody = nListBody.item(0);
        eElementBody = (Element) nNodeBody;

    }

    public boolean validaXML(Document xls, Document xml){
        try {


            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            //En path,poner el lugar donde se aloje el XML generado
            Schema schema = factory.newSchema(new File("prueba.xml"));

            Validator validator = schema.newValidator();
            //EN path poner el doc XSD
            validator.validate(new StreamSource(new File("prueba.xsd")));

        } catch (Exception e) {
            System.out.println("Exception: "+e.getMessage());
            return false;
        }
        return true;
    }

    /*NO NECESARIO public void dameCab() {
        NodeList nodos = this.mensaje.getChildNodes();
        for (int i = 0; i < nodos.getLength(); i++) {
            if (nodos.item(i).getNodeName() == "head") {
                NodeList head = nodos.item(i).getChildNodes();

                for(int j = 0; j < nodos.getLength(); j++){
                    if (head.item(j).getNodeName() == "tipo"){
                        setTipoMensaje(head.item(j).getTextContent());
                    } else if (head.item(j).getNodeName() == "id_Mensaje") {
                        setIdMensaje(head.item(j).getTextContent());
                    }else if (head.item(j).getNodeName() == "emisor") {
                        NodeList emisor = head.item(i).getChildNodes();

                        for(int k = 0; k < nodos.getLength(); k++){
                            if (emisor.item(k).getNodeName() == "id"){
                                setIdEmisor(emisor.item(k).getTextContent());
                            }else if (emisor.item(k).getNodeName() == "ip"){
                                setIpEmisor(emisor.item(k).getTextContent());
                            }else if (emisor.item(k).getNodeName() == "puerto"){
                                setPuertoEmisor(emisor.item(k).getTextContent());
                            }
                        }

                    }else if (head.item(j).getNodeName() == "tiempoEnvio") {
                        setHoraGeneracion(head.item(j).getTextContent());
                    }

                }
            }
        }
    }


/**
 public HashMap<String,String> dameCuerpo(){
 HashMap<String,String> b = new HashMap<String,String>();
 return b;
 }
 */




    /**
     *  GETTER AND SETTER DE LAS DISTINTAS VARIABLES DE LA CLASE:
     * @return
     */

    public String getReceptorID() {
        return ReceptorID;
    }


    public String getIpEmisor() {
        return ipEmisor;
    }

    public void setIpEmisor(String ipEmisor) {
        this.ipEmisor = ipEmisor;
    }

    public String getIdEmisor() {
        return idEmisor;
    }

    public void setIdEmisor(String idEmisor) {
        this.idEmisor = idEmisor;
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(String idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getHoraGeneracion() {
        return horaGeneracion;
    }

    public void setHoraGeneracion(String horaGeneracion) {
        this.horaGeneracion = horaGeneracion;
    }

    public String getPuertoEmisor() {
        return puertoEmisor;
    }

    public void setPuertoEmisor(String puertoEmisor) {
        this.puertoEmisor = puertoEmisor;
    }

    public String getTipoMensaje() {
        return tipoMensaje;
    }

    public void setTipoMensaje(String tipoMensaje) {
        this.tipoMensaje = tipoMensaje;
    }
    public String getReceptorPuerto() {
        return ReceptorPuerto;
    }

    public String getReceptorIP() {
        return ReceptorIP;
    }

    public String getProtocolo() {
        return Protocolo;
    }

    public String getFecha() {
        return Fecha;
    }

    public Element geteElementBody() {
        return eElementBody;
    }
}
