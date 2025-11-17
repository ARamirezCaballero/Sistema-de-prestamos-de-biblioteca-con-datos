package biblioteca.entities.reportes;

import biblioteca.entities.prestamos.Prestamo;
import biblioteca.entities.usuarios.Socio;
import biblioteca.entities.inventario.Ejemplar;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa un comprobante de préstamo.
 * Alineada con la tabla Comprobante de la base de datos, mantiene referencia al préstamo asociado y contiene el contenido formateado.
 */
public class Comprobante {

    private int id;
    private LocalDate fechaEmision;
    private final String tipo;
    private String contenido;
    private final Prestamo prestamo;

    /**
     * Constructor principal usado desde la lógica de negocio.
     * El id puede ser 0 cuando aún no se ha persistido.
     */
    public Comprobante(int id, String tipo, Prestamo prestamo) {
        if (id < 0) throw new IllegalArgumentException("El ID del comprobante no puede ser negativo.");
        if (tipo == null || tipo.isBlank()) {
            // Coincide con el DEFAULT 'DIGITAL' de la BD
            tipo = "DIGITAL";
        }
        this.id = id;
        this.tipo = tipo.toUpperCase();
        this.prestamo = prestamo;
        this.fechaEmision = LocalDate.now();
        this.contenido = "";
    }

    /**
     * Constructor alternativo pensado para reconstruir desde la BD.
     */
    public Comprobante(int id, LocalDate fechaEmision, String tipo, String contenido, Prestamo prestamo) {
        if (id < 0) throw new IllegalArgumentException("El ID del comprobante no puede ser negativo.");
        if (tipo == null || tipo.isBlank()) {
            tipo = "DIGITAL";
        }
        this.id = id;
        this.tipo = tipo.toUpperCase();
        this.prestamo = prestamo;
        this.fechaEmision = (fechaEmision != null) ? fechaEmision : LocalDate.now();
        this.contenido = (contenido != null) ? contenido : "";
    }

    public void generar() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== COMPROBANTE DE ").append(tipo.toUpperCase()).append(" =====\n");
        sb.append("ID: ").append(id).append("\n");
        sb.append("Fecha de emisión: ").append(fechaEmision).append("\n\n");

        if (prestamo != null) {
            sb.append("Préstamo N°: ").append(prestamo.getId()).append("\n");
            Socio socio = prestamo.getSocio();
            Ejemplar ejemplar = prestamo.getEjemplar();

            sb.append("Socio: ").append(socio != null ? socio.getNombreCompleto() + " | Email: " + socio.getEmail() : "No disponible").append("\n");
            sb.append("Ejemplar: ").append(ejemplar != null ? ejemplar.getCodigo() : "No asignado").append("\n");
            sb.append("Estado préstamo: ").append(prestamo.getEstado()).append("\n");
            sb.append("Fecha vencimiento: ").append(prestamo.getFechaVencimiento()).append("\n");
        } else {
            sb.append("No hay información de préstamo.\n");
        }

        sb.append("\nGracias por utilizar el sistema de Biblioteca.\n");
        this.contenido = sb.toString();
    }

    public void imprimir() {
        System.out.println(contenido.isBlank() ? "Comprobante no generado aún." : contenido);
    }

    public String prepararEmail() {
        if (prestamo == null || prestamo.getSocio() == null) {
            return "No se pudo enviar el comprobante: falta información del socio.";
        }
        return "Enviando a: " + prestamo.getSocio().getEmail() + "\nAsunto: Comprobante de " + tipo + "\nContenido:\n" + contenido;
    }

    // Getters
    public int getId() { return id; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public String getTipo() { return tipo; }
    public Prestamo getPrestamo() { return prestamo; }
    public String getContenido() { return contenido; }

    // === Setters controlados ===
    public void setId(int id) {
        this.id = id;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comprobante)) return false;
        Comprobante that = (Comprobante) o;
        return id == that.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Comprobante #" + id + " (" + tipo + ") - Emitido: " + fechaEmision;
    }
}




