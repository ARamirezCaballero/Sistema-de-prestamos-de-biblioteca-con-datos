package biblioteca.entities.prestamos;

import biblioteca.data.dao.DAOException;
import biblioteca.entities.inventario.Ejemplar;
import biblioteca.data.dao.PrestamoDAO;
import biblioteca.entities.usuarios.Socio;
import biblioteca.entities.usuarios.Bibliotecario;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Entidad que representa un préstamo de un ejemplar a un socio.
 * Alineada con la tabla Prestamo de la base de datos, mantiene referencias a Socio, Ejemplar, Bibliotecario y PoliticaPrestamo.
 */
public class Prestamo {

    private int id;
    private LocalDate fechaPrestamo;
    private LocalDate fechaVencimiento;
    private String estado;
    private int diasPrestamo;

    private Socio socio;
    private Ejemplar ejemplar;
    private Bibliotecario bibliotecario;
    private PoliticaPrestamo politica;

    // Constructores
    public Prestamo(int id, LocalDate fechaPrestamo, LocalDate fechaVencimiento,
                    Socio socio, Ejemplar ejemplar, Bibliotecario bibliotecario,
                    PoliticaPrestamo politica) {

        if (fechaPrestamo == null || fechaVencimiento == null)
            throw new IllegalArgumentException("Fechas no pueden ser nulas.");
        if (fechaVencimiento.isBefore(fechaPrestamo))
            throw new IllegalArgumentException("Vencimiento no puede ser anterior al préstamo.");
        if (socio == null || ejemplar == null || bibliotecario == null)
            throw new IllegalArgumentException("Socio, ejemplar o bibliotecario no pueden ser nulos.");

        this.id = id;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.socio = socio;
        this.ejemplar = ejemplar;
        this.bibliotecario = bibliotecario;
        this.politica = politica;
        this.diasPrestamo = (int) ChronoUnit.DAYS.between(fechaPrestamo, fechaVencimiento);
        // Normalizamos el estado inicial a mayúsculas, alineado con la BD (DEFAULT 'ACTIVO')
        this.estado = "ACTIVO";
    }

    // Constructor simplificado (sin id, típico antes del guardado)
    public Prestamo(Socio socio, Ejemplar ejemplar, Bibliotecario bibliotecario,
                    PoliticaPrestamo politica, LocalDate fechaPrestamo) {
        this(0, fechaPrestamo, fechaPrestamo.plusDays(politica.getDiasPrestamo()),
                socio, ejemplar, bibliotecario, politica);
    }

    // Constructor alternativo (uso exclusivo desde DAO para reconstruir desde BD)
    public Prestamo(int id, LocalDate fechaPrestamo, LocalDate fechaVencimiento,
                    String estado, int diasPrestamo,
                    Socio socio, Ejemplar ejemplar,
                    PoliticaPrestamo politica) {

        this.id = id;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = (estado != null && !estado.isBlank()) ? estado.toUpperCase() : "ACTIVO";
        this.diasPrestamo = diasPrestamo;
        this.socio = socio;
        this.ejemplar = ejemplar;
        this.bibliotecario = bibliotecario;
        this.politica = politica;
    }

    // Flujo de creación

    // Paso 18 - Crear préstamo con datos de dominio
    public static Prestamo crearPrestamo(
            int id,
            LocalDate fechaPrestamo,
            Socio socio,
            Ejemplar ejemplar,
            Bibliotecario bibliotecario,
            PoliticaPrestamo politica
    ) {
        if (fechaPrestamo == null)
            fechaPrestamo = LocalDate.now();

        // Calcular vencimiento usando la política
        LocalDate fechaVencimiento = politica.calcularFechaDevolucion(fechaPrestamo);

        // Crear y retornar el préstamo completo
        return new Prestamo(
                id,
                fechaPrestamo,
                fechaVencimiento,
                socio,
                ejemplar,
                bibliotecario,
                politica
        );
    }

    // Paso 19 - Simular persistencia
    public void guardar() {
        System.out.println("[BD] Guardando préstamo en base de datos: " + this);
        try {
            PrestamoDAO prestamoDAO = new PrestamoDAO();
            prestamoDAO.insertar(this); // Llama directo al insert
        } catch (DAOException e) {
            System.err.println("Error al guardar el préstamo: " + e.getMessage());
        }
    }

    // Paso 20 - Confirmación del guardado
    public boolean confirmarCreacion() {
        System.out.println("[Sistema] Préstamo #" + id + " creado correctamente.");
        return true;
    }

    // Paso 24 - Confirmar actualización de estado del ejemplar
    public void confirmarEstadoActualizado() {
        System.out.println("[Sistema] Estado del ejemplar actualizado a 'Prestado'.");
    }

    //Lógica de negocio

    public boolean estaVencido() {
        return LocalDate.now().isAfter(fechaVencimiento) && !"Devuelto".equalsIgnoreCase(estado);
    }

    public void marcarComoDevuelto() {
        this.estado = "DEVUELTO";
    }

    public void actualizarEstado() {
        if ("DEVUELTO".equalsIgnoreCase(estado)) return;
        if (estaVencido()) estado = "VENCIDO";
        else estado = "ACTIVO";
    }

    //Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public String getEstado() { return estado; }
    public int getDiasPrestamo() { return diasPrestamo; }
    public void setDiasPrestamo(int diasPrestamo) {
        this.diasPrestamo = diasPrestamo;
    }
    public Socio getSocio() { return socio; }
    public Ejemplar getEjemplar() { return ejemplar; }
    public Bibliotecario getBibliotecario() { return bibliotecario; }
    public void setBibliotecario(Bibliotecario bibliotecario) {
        this.bibliotecario = bibliotecario;
    }
    public PoliticaPrestamo getPolitica() { return politica; }

    @Override
    public String toString() {
        return "Prestamo{" +
                "id=" + id +
                ", socio=" + socio.getNombreCompleto() +
                ", ejemplar=" + ejemplar.getCodigo() +
                ", bibliotecario=" + bibliotecario.getNombreCompleto() +
                ", desde=" + fechaPrestamo +
                ", hasta=" + fechaVencimiento +
                ", estado='" + estado + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Prestamo)) return false;
        Prestamo p = (Prestamo) obj;
        return Objects.equals(ejemplar.getCodigo(), p.ejemplar.getCodigo()) &&
                Objects.equals(socio.getDni(), p.socio.getDni());
    }

    @Override
    public int hashCode() {
        return Objects.hash(ejemplar.getCodigo(), socio.getDni());
    }
}



