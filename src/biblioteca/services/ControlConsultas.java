package biblioteca.services;

import biblioteca.data.dao.LibroDAO;
import biblioteca.data.dao.PrestamoDAO;
import biblioteca.data.dao.DAOException;
import biblioteca.entities.inventario.Libro;
import biblioteca.entities.prestamos.Prestamo;
import biblioteca.entities.reportes.Historial;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de servicios para consultas y reportes del sistema.
 * Coordina consultas de libros, préstamos e historial de operaciones.
 */
public class ControlConsultas {

    private final LibroDAO libroDAO;
    private final PrestamoDAO prestamoDAO;

    private final List<Historial> historiales;

    public ControlConsultas(LibroDAO libroDAO, PrestamoDAO prestamoDAO) {
        this.libroDAO = libroDAO;
        this.prestamoDAO = prestamoDAO;
        this.historiales = new ArrayList<>();
    }

    public void agregarHistorial(Historial historial) {
        if (historial != null) historiales.add(historial);
    }

    public List<Historial> consultarHistorialUsuario(int idUsuario) {
        List<Historial> resultado = new ArrayList<>();
        for (Historial h : historiales) {
            if (h.getSocio() != null && h.getSocio().getIdUsuario() == idUsuario) {
                resultado.add(h);
            }
        }
        return resultado;
    }

    public List<Historial> consultarHistorialLibro(String isbn) throws DAOException {
        List<Historial> resultado = new ArrayList<>();
        Libro libro = libroDAO.obtenerPorISBN(isbn);
        if (libro == null) return resultado;

        int idLibro = libro.getId();

        for (Historial h : historiales) {
            for (Prestamo p : h.obtenerPrestamos()) {
                if (p.getEjemplar() == null || p.getEjemplar().getLibro() == null) continue;

                if (p.getEjemplar().getLibro().getId() == idLibro) {
                    resultado.add(h);
                    break;
                }
            }
        }

        return resultado;
    }

    public List<Prestamo> consultarHistorialPorSocio(String dniSocio) throws DAOException {
        return prestamoDAO.obtenerPorDniSocio(dniSocio);
    }

    public List<Historial> getHistoriales() {
        return historiales;
    }

    public String generarReporte() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE DE PRÉSTAMOS ACTIVOS/VENCIDOS ===\n");

        try {
            List<Prestamo> todosPrestamos = prestamoDAO.listarTodos();

            if (todosPrestamos.isEmpty()) {
                sb.append("No hay préstamos registrados.\n");
            } else {
                sb.append("Total de préstamos: ").append(todosPrestamos.size()).append("\n");
                for (Prestamo p : todosPrestamos) {
                    sb.append(String.format(
                            "Préstamo N°%d | Socio: %s | Ejemplar: %s | Estado: %s | Fecha préstamo: %s | Vence: %s%n",
                            p.getId(),
                            p.getSocio() != null ? p.getSocio().getNombreCompleto() : "N/A",
                            p.getEjemplar() != null ? p.getEjemplar().getCodigo() : "N/A",
                            p.getEstado(),
                            p.getFechaPrestamo(),
                            p.getFechaVencimiento()
                    ));
                }
            }
        } catch (DAOException e) {
            sb.append("Error al generar el reporte: ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }
}


