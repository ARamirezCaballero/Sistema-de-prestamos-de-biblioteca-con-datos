package biblioteca.services;

import biblioteca.data.dao.SocioDAO;
import biblioteca.data.dao.PoliticaPrestamoDAO;
import biblioteca.data.dao.DAOException;
import biblioteca.entities.prestamos.PoliticaPrestamo;
import biblioteca.entities.usuarios.Socio;

import java.time.LocalDate;

/**
 * Controlador de servicios para la gestión de políticas de préstamo del sistema.
 * Coordina la obtención de políticas según la categoría del socio y cálculos relacionados con préstamos.
 */
public class ControlPoliticas {

    private PoliticaPrestamo politicaActual;

    private final SocioDAO socioDAO;
    private final PoliticaPrestamoDAO politicaDAO;

    public ControlPoliticas(SocioDAO socioDAO, PoliticaPrestamoDAO politicaDAO) {
        this.socioDAO = socioDAO;
        this.politicaDAO = politicaDAO;
    }

    //   POLÍTICA DE PRÉSTAMO
    /**
     * Obtiene la política de préstamo basada en la categoría del socio.
     * Mapea las categorías del socio a las categorías de las políticas:
     * - 'Estándar' -> 'GENERAL'
     * - 'Estudiante' -> 'ESTUDIANTE'
     * - 'Docente' -> 'DOCENTE'
     */
    public PoliticaPrestamo obtenerPoliticaPrestamo(Socio socio) throws DAOException {
        if (politicaDAO == null)
            throw new DAOException("PoliticaPrestamoDAO no inicializado.");

        String categoriaSocio = socio.getCategoria();
        if (categoriaSocio == null || categoriaSocio.isBlank()) {
            categoriaSocio = "Estándar";
        }

        String categoriaPolitica = mapearCategoriaSocioAPolitica(categoriaSocio);

        PoliticaPrestamo p = politicaDAO.buscarPorCategoria(categoriaPolitica);

        if (p == null) {
            p = politicaDAO.buscarPorCategoria("GENERAL");
            if (p == null) {
                throw new DAOException("No existe política vigente en la base de datos (categoría 'GENERAL').");
            }
        }

        return p;
    }

    @Deprecated
    public PoliticaPrestamo obtenerPoliticaPrestamo() throws DAOException {
        if (politicaActual != null) return politicaActual;

        if (politicaDAO == null)
            throw new DAOException("PoliticaPrestamoDAO no inicializado.");

        PoliticaPrestamo p = politicaDAO.buscarPorCategoria("GENERAL");

        if (p == null)
            throw new DAOException("No existe política vigente en la base de datos (categoría 'GENERAL').");

        politicaActual = p;
        return p;
    }

    /**
     * Mapea la categoría del socio a la categoría de la política de préstamo.
     */
    private String mapearCategoriaSocioAPolitica(String categoriaSocio) {
        if (categoriaSocio == null) return "GENERAL";

        String categoria = categoriaSocio.trim();
        // Mapear: Estándar -> GENERAL, Estudiante -> ESTUDIANTE, Docente -> DOCENTE
        if (categoria.equalsIgnoreCase("Estándar") || categoria.equalsIgnoreCase("Estandar")) {
            return "GENERAL";
        } else if (categoria.equalsIgnoreCase("Estudiante")) {
            return "ESTUDIANTE";
        } else if (categoria.equalsIgnoreCase("Docente")) {
            return "DOCENTE";
        }
        // Por defecto, usar GENERAL
        return "GENERAL";
    }

    public int obtenerDiasPrestamo(Socio socio) throws DAOException {
        return obtenerPoliticaPrestamo(socio).getDiasPrestamo();
    }

    public LocalDate calcularFechaDevolucion(Socio socio, LocalDate fechaPrestamo) throws DAOException {
        if (fechaPrestamo == null) fechaPrestamo = LocalDate.now();
        return fechaPrestamo.plusDays(obtenerDiasPrestamo(socio));
    }

    //   VERIFICACIÓN DE LÍMITES
    public boolean verificarLimitePrestamos(Socio socio) throws DAOException {
        if (socio == null) return false;

        // actualizar socio con datos desde BD
        Socio socioBD = socioDAO.buscarPorDni(socio.getDni());
        if (socioBD != null) socio = socioBD;

        if (!"ACTIVO".equalsIgnoreCase(socio.getEstado())) return false;
        if (socio.isTieneSanciones()) return false;
        if (socio.isTieneAtrasos()) return false;

        int prestamosActivos = socio.obtenerPrestamosActivos().size();

        // Obtener política basada en la categoría del socio
        PoliticaPrestamo politica = obtenerPoliticaPrestamo(socio);

        return politica.verificarLimitePrestamos(prestamosActivos);
    }

    //   CÁLCULO DE VENCIMIENTOS
    public LocalDate calcularFechaVencimiento(Socio socio) throws DAOException {
        return LocalDate.now().plusDays(obtenerDiasPrestamo(socio));
    }

    //   ACTUALIZACIÓN MANUAL DE POLÍTICA
    public void establecerNuevaPolitica(PoliticaPrestamo nuevaPolitica) {
        this.politicaActual = nuevaPolitica;
    }
}