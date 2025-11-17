package biblioteca.entities.inventario;

/**
 * Entidad que representa una editorial de libros en el catálogo.
 * Alineada con la tabla Editorial de la base de datos.
 */
public class Editorial {

    private int idEditorial;
    private String nombre;
    private String pais;

    public Editorial(int idEditorial, String nombre, String pais) {
        if (idEditorial < 0) throw new IllegalArgumentException("El ID de la editorial no puede ser negativo.");
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la editorial no puede estar vacío.");
        }
        this.idEditorial = idEditorial;
        this.nombre = nombre;
        this.pais = pais;
    }

    public Editorial(String nombre, String pais) {
        this(0, nombre, pais);
    }

    // Getters
    public int getIdEditorial() {
        return idEditorial;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPais() {
        return pais;
    }

    // Setters
    /** Setter pensado para uso desde el DAO al asignar el ID auto-incremental. */
    public void setIdEditorial(int idEditorial) {
        if (idEditorial <= 0) throw new IllegalArgumentException("El ID de la editorial debe ser positivo.");
        this.idEditorial = idEditorial;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la editorial no puede estar vacío.");
        }
        this.nombre = nombre;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    @Override
    public String toString() {
        return "Editorial: " + nombre +
                (pais != null ? " (" + pais + ")" : "");
    }
}


