package biblioteca.entities.reportes;

import biblioteca.entities.prestamos.Prestamo;
import biblioteca.entities.prestamos.Devolucion;
import biblioteca.entities.usuarios.Socio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa un registro del historial de operaciones del sistema.
 * Alineada con la tabla Historial de la base de datos, registra operaciones como préstamos y devoluciones asociadas a usuarios.
 */
public class Historial {

    private int id;
    private Socio socio;
    private List<Prestamo> prestamos = new ArrayList<>();
    private List<Devolucion> devoluciones = new ArrayList<>();

    private Prestamo prestamo;
    private LocalDateTime fecha;
    private String tipoOperacion;
    private String detalles;

    public Historial(int id, Socio socio) {
        if (id < 0) throw new IllegalArgumentException("El ID del historial no puede ser negativo.");
        if (socio == null) throw new IllegalArgumentException("El socio no puede ser nulo.");
        this.id = id;
        this.socio = socio;
    }

    // Constructor para DAO/operaciones individuales
    public Historial(int id, Socio socio, Prestamo prestamo, LocalDateTime fecha, String tipoOperacion, String detalles) {
        this(id, socio);
        if (fecha == null) throw new IllegalArgumentException("La fecha de la operación no puede ser nula.");
        if (tipoOperacion == null || tipoOperacion.isBlank())
            throw new IllegalArgumentException("El tipo de operación no puede estar vacío.");
        this.prestamo = prestamo;
        this.fecha = fecha;
        this.tipoOperacion = tipoOperacion;
        this.detalles = detalles;
    }

    public void agregarPrestamo(Prestamo prestamo) {
        if (prestamo != null && !prestamos.contains(prestamo)) prestamos.add(prestamo);
    }

    public void agregarDevolucion(Devolucion devolucion) {
        if (devolucion != null) devoluciones.add(devolucion);
    }

    public List<Prestamo> obtenerPrestamos() {
        return new ArrayList<>(prestamos);
    }

    public List<Devolucion> getDevoluciones() {
        return devoluciones;
    }

    // --- Getters y setters necesarios para DAO ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public Prestamo getPrestamo() { return prestamo; }
    public void setPrestamo(Prestamo prestamo) { this.prestamo = prestamo; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String tipoOperacion) { this.tipoOperacion = tipoOperacion; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }

    @Override
    public String toString() {
        return "Historial #" + id + " - Socio: " + socio.getNombreCompleto()
                + " | Prestamo: " + (prestamo != null ? prestamo.getId() : "N/A")
                + " | Fecha: " + fecha
                + " | Tipo: " + tipoOperacion
                + " | Detalles: " + detalles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Historial)) return false;
        Historial historial = (Historial) o;
        return id == historial.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Resumen extendido opcional
    public String generarResumen() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== HISTORIAL DEL SOCIO =====\n");
        sb.append("Socio: ").append(socio.getNombreCompleto())
                .append(" | Email: ").append(socio.getEmail()).append("\n");

        sb.append("Préstamos registrados: ").append(prestamos.size()).append("\n");
        sb.append("Devoluciones registradas: ").append(devoluciones.size()).append("\n\n");

        sb.append("DETALLE DE PRÉSTAMOS:\n");
        for (Prestamo p : prestamos) {
            sb.append("Préstamo #").append(p.getId())
                    .append(" | Ejemplar: ").append(p.getEjemplar() != null ? p.getEjemplar().getCodigo() : "N/A")
                    .append(" | Estado: ").append(p.getEstado())
                    .append(" | Fecha vencimiento: ").append(p.getFechaVencimiento()).append("\n");
        }

        sb.append("\nDETALLE DE DEVOLUCIONES:\n");
        for (Devolucion d : devoluciones) {
            sb.append("Devolución #").append(d.getId())
                    .append(" | Fecha: ").append(d.getFechaDevolucion())
                    .append(" | Estado ejemplar: ").append(d.getEstadoEjemplar())
                    .append(" | Observaciones: ").append(d.getObservaciones()).append("\n");
        }

        return sb.toString();
    }
}


