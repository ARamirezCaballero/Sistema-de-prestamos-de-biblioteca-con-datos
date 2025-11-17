package biblioteca.entities.inventario;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa un ejemplar físico de un libro en la biblioteca.
 * Alineada con la tabla Ejemplar de la base de datos, mantiene referencia al libro al que pertenece.
 */
public class Ejemplar {

    private int idEjemplar;
    private String codigo;
    private String estado;
    private String ubicacion;
    private Libro libro;

    private static final List<String> ESTADOS_VALIDOS = Arrays.asList("Disponible", "Prestado", "Dañado", "Extraviado");

    private static String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return "Disponible";
        }
        String estadoNormalizado = estado.trim();
        for (String estadoValido : ESTADOS_VALIDOS) {
            if (estadoValido.equalsIgnoreCase(estadoNormalizado)) {
                return estadoValido;
            }
        }
        throw new IllegalArgumentException("Estado no válido: " + estado);
    }

    public Ejemplar(int idEjemplar, String codigo, String estado, String ubicacion, Libro libro) {
        this.idEjemplar = idEjemplar;
        this.codigo = codigo;
        setEstado(estado != null ? estado : "Disponible");
        this.ubicacion = ubicacion;
        this.libro = libro;
    }

    // Constructor simplificado
    public Ejemplar(int idEjemplar, String codigo, boolean disponible) {
        this.idEjemplar = idEjemplar;
        this.codigo = codigo;
        this.estado = disponible ? "Disponible" : "Prestado";
        this.ubicacion = "Sin asignar";
        this.libro = null;
    }

    // Constructor alternativo usado en BaseDeDatosSimulada
    public Ejemplar(String codigo, Libro libro, String estado) {
        this.idEjemplar = (int) (Math.random() * 1000);
        this.codigo = codigo;
        this.libro = libro;
        setEstado(estado);
        this.ubicacion = "Depósito";
    }

    // Paso 5 del flujo CU03
    public boolean verificarDisponibilidad() {
        return "Disponible".equalsIgnoreCase(consultarEstado());
    }

    // Paso 6 del flujo CU03
    public String consultarEstado() {
        return estado;
    }

    // Paso 22 del flujo CU03
    public void actualizarEstado(String nuevoEstado) {
        this.estado = normalizarEstado(nuevoEstado);
    }

    // Paso 23 del flujo CU03
    public void cambiarEstadoBD(String nuevoEstado) {
        // Simulación de actualización en la “BD”
        actualizarEstado(nuevoEstado);
        System.out.println("[BD] Estado del ejemplar " + codigo + " actualizado a: " + nuevoEstado);
    }

    public void marcarComoPrestado() {
        cambiarEstadoBD("Prestado");
    }

    public void marcarComoDisponible() {
        cambiarEstadoBD("Disponible");
    }

    public static boolean esEstadoValido(String estado) {
        if (estado == null || estado.isBlank()) {
            return false;
        }
        String estadoNormalizado = estado.trim();
        for (String estadoValido : ESTADOS_VALIDOS) {
            if (estadoValido.equalsIgnoreCase(estadoNormalizado)) {
                return true;
            }
        }
        return false;
    }

    // ======== Getters y Setters ========
    public int getIdEjemplar() { return idEjemplar; }
    public void setIdEjemplar(int idEjemplar) {
        if (idEjemplar <= 0) throw new IllegalArgumentException("El ID del ejemplar debe ser positivo.");
        this.idEjemplar = idEjemplar;
    }
    public String getCodigo() { return codigo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) {
        this.estado = normalizarEstado(estado);
    }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    @Override
    public String toString() {
        return "Ejemplar{" +
                "codigo='" + codigo + '\'' +
                ", estado='" + estado + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", libro=" + (libro != null ? libro.getTitulo() : "Sin asignar") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ejemplar)) return false;
        Ejemplar ejemplar = (Ejemplar) o;
        return Objects.equals(codigo, ejemplar.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
}