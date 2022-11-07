/**
 * Clase que se encarga de guardar la información de los nuevos agentes Localizados
 * @author Daniel Espinosa Perez
 * @author Jose Antonio Garcia Castro
 * @author Miguel Paños Gonzalez
 * @author Jose Angel Serrano Pardo
 */
public class accLocalizado {
    String ID;
    String IP;
    int puerto;

    /**
     * Constructor de la clase
     * @param ID del agente localido
     * @param IP del agente localido
     * @param puerto del agente localido
     */
    accLocalizado(String ID, String IP, int puerto){
        this.ID = ID;
        this.IP = IP;
        this.puerto = puerto;
    }
}
