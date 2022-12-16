/*
import java.util.ArrayList;
import java.util.HashMap;

public class pruebas {
    public static void main(String[] args) {

        GestorMensajes gm = new GestorMensajes(55000,56000);

        ArrayList<String> al = new ArrayList<>();
        al.add("1");
        al.add("5");
        al.add("6");
        HashMap<String, Object> body = gm.generaBody(al);
        Mensaje m = new Mensaje(gm.generaCab(String.valueOf(55000), "11", "192.168.0.13", String.valueOf(55000), "11", "192.168.0.13", "SolicitudTransaccion", "TCP", "1"), body, null);
        try {
            String t = gm.TransformarMensaje(m);
            Mensaje m2 = new Mensaje(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("fin");
    }
}
 */
