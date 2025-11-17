package biblioteca.services;

import biblioteca.data.dao.BibliotecarioDAO;
import biblioteca.data.dao.DAOException;
import biblioteca.data.dao.SocioDAO;
import biblioteca.entities.usuarios.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de servicios para la gestión de usuarios del sistema.
 * Coordina las operaciones de registro, autenticación y consulta de bibliotecarios y socios.
 */
public class ControlUsuarios {

    private final BibliotecarioDAO bibliotecarioDAO;
    private final SocioDAO socioDAO;

    public ControlUsuarios(BibliotecarioDAO bibliotecarioDAO, SocioDAO socioDAO) {
        if (bibliotecarioDAO == null) throw new IllegalArgumentException("BibliotecarioDAO no puede ser nulo.");
        if (socioDAO == null) throw new IllegalArgumentException("SocioDAO no puede ser nulo.");
        this.bibliotecarioDAO = bibliotecarioDAO;
        this.socioDAO = socioDAO;
    }

    public String registrarSocio(Socio socio) throws DAOException {
        if (socio == null) {
            throw new IllegalArgumentException("El socio no puede ser nulo.");
        }

        if (buscarPorDni(socio.getDni()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI " + socio.getDni());
        }

        LocalDate fechaRegistro = LocalDate.now();
        socio.setFechaRegistro(fechaRegistro);

        String numeroSocio = "SOC-" + System.currentTimeMillis() % 100000;
        socio.setNumeroSocio(numeroSocio);

        socio.setFechaVencimientoCarnet(fechaRegistro.plusYears(1));
        socio.setEstado("Activo");
        socio.setTieneSanciones(false);
        socio.setTieneAtrasos(false);

        if (socio.getTipo() == null) {
            socio.setTipo(TipoUsuario.SOCIO);
        }

        socioDAO.insertar(socio);

        return "Socio registrado exitosamente: " + socio.getNombreCompleto() +
                ". Número de socio asignado: " + numeroSocio;
    }

    public String registrarBibliotecario(Bibliotecario b) throws DAOException {
        if (b == null) {
            throw new IllegalArgumentException("El bibliotecario no puede ser nulo.");
        }

        if (buscarPorDni(b.getDni()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI " + b.getDni());
        }

        bibliotecarioDAO.insertar(b);
        return "Bibliotecario registrado exitosamente: " + b.getNombreCompleto();
    }

    public Bibliotecario buscarBibliotecarioPorId(int id) throws DAOException {
        return bibliotecarioDAO.buscarPorId(id);
    }

    public Usuario buscarPorDni(String dni) throws DAOException {
        Bibliotecario bibliotecario = bibliotecarioDAO.buscarPorDni(dni);
        if (bibliotecario != null) return bibliotecario;

        Socio socio = socioDAO.buscarPorDni(dni);
        if (socio != null) return socio;

        return null;
    }

    public Usuario login(String nombreUsuario, String contrasenia) throws DAOException {
        Bibliotecario biblio = bibliotecarioDAO.buscarPorUsername(nombreUsuario);
        if (biblio != null && biblio.getPassword().equals(contrasenia)) {
            return biblio;
        }

        Socio socio = socioDAO.buscarPorUsername(nombreUsuario);
        if (socio != null && socio.getPassword().equals(contrasenia)) {
            return socio;
        }

        return null;
    }

    public String actualizarDatos(int idUsuario, String nuevoEmail, String nuevoTelefono) throws DAOException {
        Socio socio = socioDAO.buscarPorId(idUsuario);
        if (socio == null) {
            throw new IllegalArgumentException("No se encontró socio con ID " + idUsuario);
        }

        socio.setEmail(nuevoEmail);
        socio.setTelefono(nuevoTelefono);
        socioDAO.actualizar(socio);

        return "Datos actualizados correctamente para socio " + socio.getNombreCompleto();
    }

    public List<Usuario> listarTodosLosUsuarios() throws DAOException {
        List<Usuario> usuarios = new java.util.ArrayList<>();

        List<Bibliotecario> bibliotecarios = bibliotecarioDAO.listarTodos();
        usuarios.addAll(bibliotecarios);

        List<Socio> socios = socioDAO.listarTodos();
        usuarios.addAll(socios);

        return usuarios;
    }
}

