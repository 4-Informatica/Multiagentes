protocolo:

-Agente1 manda un mesaje tipo SolicitudTransaccion. En este mensaje declara los cromos que él quiere.

-Agente2 (receptor del mensaje SolicitudTransaccion) responde con un mensaje tipo Respuesta_SolicitudTransaccion.
Este mensaje indica los cromos deseados del Agente1 Y QUE YO(Agente2) ESTOY DISPESTO A INTERCAMBIAR en primer lugar y, en ListaDeseadosACC2, indicara los cromos que él(Agente2) desea. 

-Agente1 (receptor del mensaje Respuesta_SolicitudTransaccion) responde con un mensaje tipo oferta.
En este mensaje indicará en el campo ListaDeseadosACC1 los cromos que quiere intercambiar y en ListaDeseadosACC2 los cromos que desea agente2 y agente1 está dispuesto a intercambiarle.

-Agente2 responderá con un mensaje OK_oferta si quiere completar la transsaccion
-En caso de querer rechazar la oferta responderá con un tipo error_transaccion.

-Por último Agente 1 responderá con un mensaje tipo KO_transaccion para cerrar la comunicación.