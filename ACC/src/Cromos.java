public class Cromos {
    int id;
    int cantidad;
    int rareza;


    public Cromos(int id, int valor, int rareza){
        this.id=id;
        this.cantidad=valor;
        this.rareza=rareza;
    }

    public void setCantidad(int valor) {
        this.cantidad = valor;
    }

    public int getCantidad() {
        return cantidad;
    }
}

