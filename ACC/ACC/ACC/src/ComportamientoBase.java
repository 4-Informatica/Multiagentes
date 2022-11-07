import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Random;

public class ComportamientoBase implements Runnable{
    String id, direccionJar;
    long horaDeMuerte;
    int generaciones, Puerto_Inicio, Rango_Puertos, tiempoDeVida, tiempo_espera_comportamiento_base;
    boolean puertos_aleatorios;
    double Frecuencia_partos, Frecuencia_rastreo_puertos;

    GestorMensajes gm;
    Random random = new Random();

    ComportamientoBase(String id, int generaciones, int puerto_Inicio, int rango_Puertos, int tiempoDeVida, int tiempo_espera_comportamiento_base,
                       double frecuencia_partos, double frecuencia_rastreo_puertos, GestorMensajes gm) {
        this.id = id;
        this.generaciones = generaciones;
        this.Puerto_Inicio = puerto_Inicio;
        this.Rango_Puertos = rango_Puertos;
        this.puertos_aleatorios = true;
        this.tiempoDeVida = tiempoDeVida;
        this.tiempo_espera_comportamiento_base = tiempo_espera_comportamiento_base;

        this.Frecuencia_partos = frecuencia_partos;
        this.Frecuencia_rastreo_puertos = frecuencia_rastreo_puertos;

        this.horaDeMuerte = System.currentTimeMillis() + (long) tiempoDeVida;
        this.direccionJar = "C:/ACC-Multiagentes/ACC.jar";

        this.gm = gm;

        new Thread(this, "comportamiento_base").start();
    }



    @Override
    public void run() {

        while (horaDeMuerte > System.currentTimeMillis()) {
            System.out.println("-------------------------------comportamiento base----------------------------");
            GenerarNuevoAcc(id);

            System.out.println(tiempo_espera_comportamiento_base);
            if(this.Frecuencia_rastreo_puertos >= this.random.nextDouble()) {
                try {
                    this.GestorDeDirectorio();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                Thread.sleep(tiempo_espera_comportamiento_base);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("fin");
        System.exit(0);     // Parar el agente
    }

    void GenerarNuevoAcc(String id) {
        try {
            if (Frecuencia_partos >= random.nextDouble() && generaciones > 0) {
                ProcessBuilder pb = new ProcessBuilder("C:/Program Files/Java/jdk-17.0.4.1/bin/java.exe", "-jar", direccionJar, "" + generaciones);
                pb.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void GestorDeDirectorio() throws InterruptedException {
        if(this.puertos_aleatorios){
            for (int i = 0; i < 254; i++) {
                String host = "172.19.154." + i;
                int puertos_a_buscar = 5;

                for(int j = 0; j < puertos_a_buscar ; j++) {
                    int puerto = this.Puerto_Inicio + this.random.nextInt(this.Rango_Puertos);
                    if (puerto % 2 != 0)    // comprobamos que ssea impar (UDP)
                        puerto++;
                    // ponemos el mensaje de localización del host correspondiente en la lista de mensajes a enviar del gestor de mensajes
                    System.out.println("envia mensaje");
                    try {
                        this.gm.EnviarMensaje();
                    } catch (ParserConfigurationException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    } catch (jdk.internal.org.xml.sax.SAXException e) {
                        throw new RuntimeException(e);
                    } catch (TransformerException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else{
            for (int i = 0; i < 254; i++) {
                String host = "172.19.154." + i;
                for (int puerto = Puerto_Inicio; puerto <= Puerto_Inicio + Rango_Puertos; puerto += 2) {
                    // ponemos el mensaje de localización del host correspondiente en la lista de mensajes a enviar del gestor de mensajes
                    try {
                        this.gm.EnviarMensaje();
                    } catch (ParserConfigurationException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    } catch (jdk.internal.org.xml.sax.SAXException e) {
                        throw new RuntimeException(e);
                    } catch (TransformerException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
