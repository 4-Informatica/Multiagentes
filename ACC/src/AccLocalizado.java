import java.util.Date;

/**
 * Clase que se encarga de guardar la información de los nuevos agentes Localizados
 * @author Daniel Espinosa Perez
 * @author Jose Antonio Garcia Castro
 * @author Miguel Paños Gonzalez
 * @author Jose Angel Serrano Pardo
 */
public class AccLocalizado {
    String ID;
    String IP;
    String puerto;
    String fecha_encontrado;

    /**
     * Constructor de la clase
     * @param ID del agente localido
     * @param IP del agente localido
     * @param puerto del agente localido
     */
    AccLocalizado(String ID, String IP, String puerto, String hora_generacion){
        this.ID = ID;
        this.IP = IP;
        this.puerto = puerto;
        if(hora_generacion== null)
            this.fecha_encontrado = new Date().toString();
        else
            this.fecha_encontrado = hora_generacion;
    }

    @Override
    public String toString() {
        return "Datos Agente: \t ID: " + this.ID + "\tIP: " + this.IP + "\tPuerto:" + this.puerto + "\t Fecha localización: " +this.fecha_encontrado + "\n" ;
        //return super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof AccLocalizado))
            return false;

        return this.ID.equals(((AccLocalizado) obj).ID) && this.IP.equals(((AccLocalizado) obj).IP) && this.puerto.equals(((AccLocalizado) obj).puerto);
    }
}
