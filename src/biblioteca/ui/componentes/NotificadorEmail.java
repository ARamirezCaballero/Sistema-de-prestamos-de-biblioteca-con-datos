package biblioteca.ui.componentes;

import biblioteca.entities.notificaciones.Notificacion;

import java.util.List;
import java.util.regex.Pattern;

public class NotificadorEmail {

    private final String servidor;
    private final int puerto;
    private boolean conectado;

    public NotificadorEmail(String servidor, int puerto) {
        this.servidor = servidor;
        this.puerto = puerto;
        this.conectado = false;
    }

    public static NotificadorEmail defaultFake() {
        return new NotificadorEmail("smtp.local", 25);
    }

    public void conectar() {
        if (servidor == null || servidor.isBlank() || puerto <= 0) {
            System.out.println("Error: servidor o puerto inválido.");
            return;
        }
        conectado = true;
        System.out.println("Conectado al servidor de email " + servidor + ":" + puerto);
    }

    public void enviar(String destinatario, String asunto, String mensaje) {
        if (!conectado) {
            System.out.println("No conectado al servidor. Conecte primero.");
            return;
        }

        if (!validarEmail(destinatario)) {
            System.out.println("Email inválido: " + destinatario);
            return;
        }

        // Simulación de envío
        System.out.println("Enviando email a " + destinatario);
        System.out.println("Asunto: " + asunto);
        System.out.println("Mensaje: " + mensaje);
        System.out.println("Email enviado (simulado).");
    }

    public void desconectar() {
        if (conectado) {
            conectado = false;
            System.out.println("Desconectado del servidor de email.");
        }
    }

    public boolean validarEmail(String email) {
        if (email == null) return false;
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(regex, email);
    }

    /**
     * Envía un lote de notificaciones que el servicio de negocio ya preparó.
     *
     * Obtiene el email del destinatario a través de la relación:
     * Notificacion -> Prestamo -> Socio -> Email
     *
     * @param notificaciones Lista de notificaciones a enviar
     */
    public void enviarNotificaciones(List<Notificacion> notificaciones) {
        if (notificaciones == null || notificaciones.isEmpty()) {
            System.out.println("No hay notificaciones pendientes.");
            return;
        }

        conectar();

        for (Notificacion n : notificaciones) {
            // Obtener el email del socio a través del préstamo asociado
            // Estructura: Notificacion -> Prestamo -> Socio -> Email
            if (n.getPrestamo() == null || n.getPrestamo().getSocio() == null) {
                System.out.println("→ Error: La notificación no tiene préstamo o socio asociado. Se omite el envío.");
                continue;
            }

            String email = n.getPrestamo().getSocio().getEmail();

            if (email == null || email.isBlank()) {
                System.out.println("→ Error: El socio no tiene email registrado. Se omite el envío.");
                continue;
            }

            System.out.println("→ Enviando notificación a " + email
                    + " | Mensaje: " + n.getMensaje());

            if (!validarEmail(email)) {
                System.out.println("Email inválido. Se omite el envío.");
                continue;
            }

            enviar(email, "Notificación de Biblioteca", n.getMensaje());
        }

        desconectar();
        System.out.println("=== Envío de notificaciones finalizado ===");
    }
}