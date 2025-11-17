package biblioteca.entities.inventario;

import java.time.LocalDate;

/**
 * Entidad que representa un autor de libros en el catálogo.
 * Alineada con la tabla Autor de la base de datos.
 */
public class Autor {

    private int idAutor;
    private String nombreCompleto;
    private String nacionalidad;
    private LocalDate fechaNacimiento;

    public Autor(int idAutor, String nombreCompleto, String nacionalidad, LocalDate fechaNacimiento) {
        if (idAutor < 0) throw new IllegalArgumentException("El ID del autor no puede ser negativo.");
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre completo del autor no puede estar vacío.");
        }
        this.idAutor = idAutor;
        this.nombreCompleto = nombreCompleto;
        this.nacionalidad = nacionalidad;
        this.fechaNacimiento = fechaNacimiento;
    }

    public Autor(String nombreCompleto, String nacionalidad, LocalDate fechaNacimiento) {
        this(0, nombreCompleto, nacionalidad, fechaNacimiento);
    }

    // Getters
    public int getIdAutor() {
        return idAutor;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    // Setters
    /** Setter pensado para uso desde el DAO al asignar el ID auto-incremental. */
    public void setIdAutor(int idAutor) {
        if (idAutor <= 0) throw new IllegalArgumentException("El ID del autor debe ser positivo.");
        this.idAutor = idAutor;
    }

    public void setNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre completo del autor no puede estar vacío.");
        }
        this.nombreCompleto = nombreCompleto;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public String toString() {
        return "Autor: " + nombreCompleto +
                (nacionalidad != null ? " (" + nacionalidad + ")" : "") +
                (fechaNacimiento != null ? " | Nacido el: " + fechaNacimiento : "");
    }
}


