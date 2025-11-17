package biblioteca.entities.prestamos;

import java.time.LocalDate;

/**
 * Entidad que representa una política de préstamo del sistema.
 * Alineada con la tabla PoliticaPrestamo de la base de datos, define las reglas de préstamo según la categoría del socio.
 */
public class PoliticaPrestamo {

    private int idPolitica;
    private String categoria;
    private int diasPrestamo;
    private int maxPrestamosSimultaneos;
    private double multaPorDia;

    public PoliticaPrestamo(int idPolitica, String categoria, int diasPrestamo,
                            int maxPrestamosSimultaneos, double multaPorDia) {

        if (idPolitica < 0) throw new IllegalArgumentException("El ID de la política no puede ser negativo.");
        if (categoria == null || categoria.isBlank())
            throw new IllegalArgumentException("La categoría de la política no puede estar vacía.");
        if (diasPrestamo <= 0)
            throw new IllegalArgumentException("Los días de préstamo deben ser mayores a cero.");
        if (maxPrestamosSimultaneos <= 0)
            throw new IllegalArgumentException("El máximo de préstamos simultáneos debe ser mayor a cero.");
        if (multaPorDia < 0)
            throw new IllegalArgumentException("La multa por día no puede ser negativa.");

        this.idPolitica = idPolitica;
        // Normalizamos categoría a mayúsculas para coincidir con los valores de la BD (GENERAL, ESTUDIANTE, DOCENTE)
        this.categoria = categoria.toUpperCase();
        this.diasPrestamo = diasPrestamo;
        this.maxPrestamosSimultaneos = maxPrestamosSimultaneos;
        this.multaPorDia = multaPorDia;
    }

    /**
     * Constructor pensado para creación desde UI o lógica de negocio, sin ID aún.
     */
    public PoliticaPrestamo(String categoria, int diasPrestamo,
                            int maxPrestamosSimultaneos, double multaPorDia) {
        this(0, categoria, diasPrestamo, maxPrestamosSimultaneos, multaPorDia);
    }

    public int obtenerDiasPrestamo() {
        return diasPrestamo;
    }

    public LocalDate calcularFechaDevolucion(LocalDate fechaPrestamo) {
        return fechaPrestamo.plusDays(diasPrestamo);
    }

    public boolean verificarLimitePrestamos(int prestamosActivos) {
        return prestamosActivos < maxPrestamosSimultaneos;
    }

    public int getIdPolitica() { return idPolitica; }
    public String getCategoria() { return categoria; }
    public int getDiasPrestamo() { return diasPrestamo; }
    public int getMaxPrestamosSimultaneos() { return maxPrestamosSimultaneos; }
    public double getMultaPorDia() { return multaPorDia; }

    /** Setter usado por el DAO para asignar el ID auto-incremental. */
    public void setId(int idPolitica) {
        if (idPolitica <= 0) throw new IllegalArgumentException("El ID de la política debe ser positivo.");
        this.idPolitica = idPolitica;
    }

    public void setDiasPrestamo(int diasPrestamo) {
        if (diasPrestamo <= 0)
            throw new IllegalArgumentException("Los días de préstamo deben ser mayores a cero.");
        this.diasPrestamo = diasPrestamo;
    }

    public void setMaxPrestamosSimultaneos(int maxPrestamosSimultaneos) {
        if (maxPrestamosSimultaneos <= 0)
            throw new IllegalArgumentException("El máximo de préstamos simultáneos debe ser mayor a cero.");
        this.maxPrestamosSimultaneos = maxPrestamosSimultaneos;
    }

    public void setMultaPorDia(double multaPorDia) {
        if (multaPorDia < 0)
            throw new IllegalArgumentException("La multa por día no puede ser negativa.");
        this.multaPorDia = multaPorDia;
    }

    @Override
    public String toString() {
        return "Política [" + categoria + "] - Días préstamo: " + diasPrestamo +
                " | Máx. simultáneos: " + maxPrestamosSimultaneos +
                " | Multa/día: $" + multaPorDia;
    }

}
