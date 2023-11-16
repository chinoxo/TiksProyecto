package twin.developers.projectmqtt;

public class Datos {
    private String Mensaje;

    public Datos() {
    }

    public Datos(String mensaje) {
        Mensaje = mensaje;
    }

    public String getMensaje() {
        return Mensaje;
    }

    public void setMensaje(String mensaje) {
        Mensaje = mensaje;
    }

}
