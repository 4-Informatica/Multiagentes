public class FuncionDeAgente implements Runnable {
    FuncionDeAgente() {
        new Thread(this, "funcion_del_agente").start();
    }

    @Override
    public void run() {
        procesaMensajeRecibido();
        generaMensajeAEnviar();
    }

    void procesaMensajeRecibido() {
        System.out.println("procesaMensajeRecibido");
    }

    void generaMensajeAEnviar() {
        System.out.println("generaMensajeAEnviar");
    }
}
