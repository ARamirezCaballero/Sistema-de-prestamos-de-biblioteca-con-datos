package biblioteca.entities.notificaciones;

import biblioteca.entities.prestamos.Prestamo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa una notificación del sistema.
 * Alineada con la tabla Notificacion de la base de datos, mantiene referencia al préstamo asociado y registra si ha sido leída.
 */
public class Notificacion {

    private int idNotificacion;
    private String mensaje;
    private LocalDateTime fechaHora;
    private Prestamo prestamo;
    private boolean leida;

    public Notificacion(int idNotificacion,
                        String mensaje,
                        LocalDateTime fechaHora,
                        Prestamo prestamo,
                        boolean leida) {

        if (idNotificacion < 0)
            throw new IllegalArgumentException("El ID de la notificación no puede ser negativo.");
        if (mensaje == null || mensaje.isBlank())
            throw new IllegalArgumentException("El mensaje de la notificación no puede estar vacío.");
        if (fechaHora == null)
            throw new IllegalArgumentException("La fecha/hora de la notificación no puede ser nula.");
        if (prestamo == null)
            throw new IllegalArgumentException("La notificación debe estar asociada a un préstamo.");

        this.idNotificacion = idNotificacion;
        this.mensaje = mensaje;
        this.fechaHora = fechaHora;
        this.prestamo = prestamo;
        this.leida = leida;
    }

    /**
     * Constructor simplificado para creación desde lógica de negocio:
     * notificación nueva, no leída, con fecha de envío = ahora.
     */
    public Notificacion(String mensaje, Prestamo prestamo) {
        this(0, mensaje, LocalDateTime.now(), prestamo, false);
    }

    // Getters y setters
    public int getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
        if (idNotificacion < 0)
            throw new IllegalArgumentException("El ID de la notificación no puede ser negativo.");
        this.idNotificacion = idNotificacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null)
            throw new IllegalArgumentException("La fecha/hora de la notificación no puede ser nula.");
        this.fechaHora = fechaHora;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        if (prestamo == null)
            throw new IllegalArgumentException("La notificación debe estar asociada a un préstamo.");
        this.prestamo = prestamo;
    }

    // ===== Estado de lectura =====
    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public void marcarComoLeida() {
        this.leida = true;
    }

    // === Representación ===
    @Override
    public String toString() {
        return "Notificacion{" +
                "mensaje='" + mensaje + '\'' +
                ", fechaHora=" + fechaHora +
                ", prestamo=" + (prestamo != null ? prestamo.getId() : "Sin préstamo") +
                ", leida=" + leida +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notificacion)) return false;
        Notificacion that = (Notificacion) o;
        return idNotificacion == that.idNotificacion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNotificacion);
    }
}