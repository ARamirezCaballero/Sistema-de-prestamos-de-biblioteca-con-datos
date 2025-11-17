package biblioteca.data.dao;

import biblioteca.entities.usuarios.Bibliotecario;
import biblioteca.entities.usuarios.TipoUsuario;
import biblioteca.data.interfaces.DAO;
import biblioteca.data.db.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar la persistencia de bibliotecarios en la base de datos.
 * Maneja las operaciones CRUD sobre las tablas Usuario y Bibliotecario mediante JOINs.
 */
public class BibliotecarioDAO implements DAO<Bibliotecario> {

    public BibliotecarioDAO() {
    }

    @Override
    public void insertar(Bibliotecario b) throws DAOException {
        String sqlUsuario = """
                INSERT INTO Usuario (nombre, apellido, dni, email, telefono, fecha_registro, username, password, tipo_usuario)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String sqlBibliotecario = """
                INSERT INTO Bibliotecario (id_usuario, legajo, turno)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = ConexionBD.getConexion()) {
            conn.setAutoCommit(false);

            int idUsuario;
            int idBibliotecario;

            try {
                try (PreparedStatement psUsuario = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    psUsuario.setString(1, b.getNombre());
                    psUsuario.setString(2, b.getApellido());
                    psUsuario.setString(3, b.getDni());
                    psUsuario.setString(4, b.getEmail());
                    psUsuario.setString(5, b.getTelefono());
                    psUsuario.setDate(6, Date.valueOf(b.getFechaRegistro() != null ? b.getFechaRegistro() : java.time.LocalDate.now()));
                    psUsuario.setString(7, b.getUsername());
                    psUsuario.setString(8, b.getPassword());
                    psUsuario.setString(9, b.getTipoUsuario().name());
                    psUsuario.executeUpdate();

                    try (ResultSet rs = psUsuario.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se pudo obtener el ID generado para Usuario.");
                        idUsuario = rs.getInt(1);
                    }
                }

                b.setId(idUsuario);

                try (PreparedStatement psBiblio = conn.prepareStatement(sqlBibliotecario, Statement.RETURN_GENERATED_KEYS)) {
                    psBiblio.setInt(1, idUsuario);
                    psBiblio.setString(2, b.getLegajo());
                    psBiblio.setString(3, b.getTurno());
                    psBiblio.executeUpdate();

                    try (ResultSet rs = psBiblio.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se pudo obtener el ID generado para Bibliotecario.");
                        idBibliotecario = rs.getInt(1);
                    }
                }

                b.setIdBibliotecario(idBibliotecario);

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al insertar bibliotecario", e);
        }
    }

    @Override
    public void actualizar(Bibliotecario b) throws DAOException {
        String sql = """
                UPDATE Usuario u
                INNER JOIN Bibliotecario biblio ON u.id_usuario = biblio.id_usuario
                SET u.nombre = ?, u.apellido = ?, u.email = ?, u.telefono = ?, 
                    biblio.turno = ?, biblio.legajo = ?
                WHERE biblio.id_bibliotecario = ?
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getNombre());
            ps.setString(2, b.getApellido());
            ps.setString(3, b.getEmail());
            ps.setString(4, b.getTelefono());
            ps.setString(5, b.getTurno());
            ps.setString(6, b.getLegajo());
            ps.setInt(7, b.getIdBibliotecario());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar bibliotecario", e);
        }
    }

    @Override
    public void eliminar(int idBibliotecario) throws DAOException {
        String sqlEliminarBiblio = "DELETE FROM Bibliotecario WHERE id_bibliotecario = ?";
        String sqlEliminarUsuario = "DELETE FROM Usuario WHERE id_usuario = (SELECT id_usuario FROM Bibliotecario WHERE id_bibliotecario = ?)";

        try (Connection conn = ConexionBD.getConexion()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement psBiblio = conn.prepareStatement(sqlEliminarBiblio)) {
                    psBiblio.setInt(1, idBibliotecario);
                    int filas = psBiblio.executeUpdate();
                    if (filas == 0) throw new DAOException("No se encontró el bibliotecario con ID " + idBibliotecario);
                }

                try (PreparedStatement psUsuario = conn.prepareStatement(sqlEliminarUsuario)) {
                    psUsuario.setInt(1, idBibliotecario);
                    psUsuario.executeUpdate();
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw new DAOException("Error al eliminar bibliotecario y usuario asociado", e);
            } catch (DAOException e) {
                conn.rollback();
                throw e;
            }

        } catch (DAOException e) {
            throw e;
        } catch (SQLException e) {
            throw new DAOException("Error al eliminar bibliotecario y usuario asociado", e);
        }
    }

    @Override
    public Bibliotecario buscarPorId(int idBibliotecario) throws DAOException {
        String sql = """
                SELECT u.*, biblio.id_bibliotecario, biblio.legajo, biblio.turno
                FROM Usuario u
                INNER JOIN Bibliotecario biblio ON u.id_usuario = biblio.id_usuario
                WHERE biblio.id_bibliotecario = ?
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBibliotecario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearBibliotecario(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar bibliotecario por ID", e);
        }
    }

    public Bibliotecario buscarPorUsername(String username) throws DAOException {
        String sql = """
                SELECT u.*, biblio.id_bibliotecario, biblio.legajo, biblio.turno
                FROM Usuario u
                INNER JOIN Bibliotecario biblio ON u.id_usuario = biblio.id_usuario
                WHERE u.username = ?
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearBibliotecario(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar bibliotecario por username", e);
        }
    }

    public Bibliotecario buscarPorDni(String dni) throws DAOException {
        String sql = """
            SELECT u.*, biblio.id_bibliotecario, biblio.legajo, biblio.turno
            FROM Usuario u
            INNER JOIN Bibliotecario biblio ON u.id_usuario = biblio.id_usuario
            WHERE u.dni = ?
            """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearBibliotecario(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar bibliotecario por DNI", e);
        }
    }

    @Override
    public List<Bibliotecario> listarTodos() throws DAOException {
        List<Bibliotecario> bibliotecarios = new ArrayList<>();
        String sql = """
                SELECT u.*, biblio.id_bibliotecario, biblio.legajo, biblio.turno
                FROM Usuario u
                INNER JOIN Bibliotecario biblio ON u.id_usuario = biblio.id_usuario
                """;

        try (Connection conn = ConexionBD.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) bibliotecarios.add(mapearBibliotecario(rs));
            return bibliotecarios;

        } catch (SQLException e) {
            throw new DAOException("Error al listar bibliotecarios", e);
        }
    }

    private Bibliotecario mapearBibliotecario(ResultSet rs) throws SQLException {
        int idUsuario = rs.getInt("id_usuario");
        int idBibliotecario = rs.getInt("id_bibliotecario");

        Bibliotecario bibliotecario = new Bibliotecario(
                idUsuario,
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("dni"),
                rs.getString("email"),
                rs.getString("telefono"),
                rs.getDate("fecha_registro").toLocalDate(),
                TipoUsuario.valueOf(rs.getString("tipo_usuario").toUpperCase()),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("legajo"),
                rs.getString("turno")
        );

        bibliotecario.setIdBibliotecario(idBibliotecario);
        return bibliotecario;
    }
}