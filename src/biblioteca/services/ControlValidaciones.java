package biblioteca.services;

import biblioteca.data.dao.SocioDAO;
import biblioteca.data.dao.DAOException;
import biblioteca.entities.inventario.Ejemplar;
import biblioteca.entities.inventario.Libro;
import biblioteca.entities.usuarios.Socio;
import biblioteca.entities.usuarios.Usuario;

/**
 * Controlador de servicios para validaciones de reglas de negocio del sistema.
 * Coordina la validación de datos de usuarios, socios, libros y ejemplares.
 */
public class ControlValidaciones {

    private final SocioDAO socioDAO;

    public ControlValidaciones(SocioDAO socioDAO) {
        if (socioDAO == null) throw new IllegalArgumentException("SocioDAO no puede ser nulo.");
        this.socioDAO = socioDAO;
    }

    public boolean validarDisponibilidadEjemplar(Ejemplar ejemplar) {
        return ejemplar != null && ejemplar.verificarDisponibilidad();
    }

    public boolean validarEstadoSocio(Socio socio) throws DAOException {
        if (socio == null || socio.getDni() == null) {
            return false;
        }

        Socio socioBD = socioDAO.buscarPorDni(socio.getDni());
        if (socioBD == null) {
            return false;
        }
        return !socioBD.getEstado().equalsIgnoreCase("Inhabilitado");
    }

    public boolean verificarSanciones(Socio socio) {
        if (socio == null) throw new IllegalArgumentException("Socio no válido.");
        return socio.isTieneSanciones();
    }

    public boolean verificarAtrasos(Socio socio) {
        if (socio == null) throw new IllegalArgumentException("Socio no válido.");
        return socio.isTieneAtrasos();
    }

    public boolean validarDatosLibro(Libro libro) {
        if (libro == null) {
            return false;
        }

        if (libro.getTitulo() == null || libro.getTitulo().isBlank()) {
            return false;
        }
        if (libro.getAutor() == null || libro.getAutor().isBlank()) {
            return false;
        }
        if (libro.getIsbn() == null || libro.getIsbn().isBlank()) {
            return false;
        }

        return true;
    }

    public boolean validarDatosUsuario(Usuario usuario) {
        if (usuario == null) {
            return false;
        }

        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            return false;
        }
        if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
            return false;
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return false;
        }

        return true;
    }
}