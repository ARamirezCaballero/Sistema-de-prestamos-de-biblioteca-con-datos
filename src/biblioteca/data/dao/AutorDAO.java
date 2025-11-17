package biblioteca.data.dao;

import biblioteca.data.db.ConexionBD;
import biblioteca.data.interfaces.DAO;
import biblioteca.entities.inventario.Autor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar la persistencia de autores en la base de datos.
 * Maneja las operaciones CRUD sobre la tabla Autor.
 */
public class AutorDAO implements DAO<Autor> {

    public AutorDAO() {
    }

    @Override
    public void insertar(Autor autor) throws DAOException {
        String sql = """
            INSERT INTO Autor (nombre_completo, nacionalidad, fecha_nacimiento)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, autor.getNombreCompleto());
            ps.setString(2, autor.getNacionalidad());
            if (autor.getFechaNacimiento() != null) {
                ps.setDate(3, Date.valueOf(autor.getFechaNacimiento()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se pudo obtener el ID generado para Autor.");
                }
                autor.setIdAutor(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al insertar autor", e);
        }
    }

    @Override
    public Autor buscarPorId(int id) throws DAOException {
        String sql = """
            SELECT id_autor, nombre_completo, nacionalidad, fecha_nacimiento
            FROM Autor
            WHERE id_autor = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAutor(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar autor por ID", e);
        }
        return null;
    }

    @Override
    public List<Autor> listarTodos() throws DAOException {
        List<Autor> autores = new ArrayList<>();
        String sql = """
            SELECT id_autor, nombre_completo, nacionalidad, fecha_nacimiento
            FROM Autor
            ORDER BY nombre_completo
        """;

        try (Connection conn = ConexionBD.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                autores.add(mapearAutor(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al listar autores", e);
        }

        return autores;
    }

    @Override
    public void actualizar(Autor autor) throws DAOException {
        String sql = """
            UPDATE Autor
            SET nombre_completo = ?, nacionalidad = ?, fecha_nacimiento = ?
            WHERE id_autor = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, autor.getNombreCompleto());
            ps.setString(2, autor.getNacionalidad());
            if (autor.getFechaNacimiento() != null) {
                ps.setDate(3, Date.valueOf(autor.getFechaNacimiento()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setInt(4, autor.getIdAutor());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Error al actualizar autor", e);
        }
    }

    @Override
    public void eliminar(int id) throws DAOException {
        String sql = "DELETE FROM Autor WHERE id_autor = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró el autor con ID " + id);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al eliminar autor", e);
        }
    }

    public Autor buscarPorNombre(String nombreCompleto) throws DAOException {
        String sql = """
            SELECT id_autor, nombre_completo, nacionalidad, fecha_nacimiento
            FROM Autor
            WHERE nombre_completo = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreCompleto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAutor(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar autor por nombre", e);
        }
        return null;
    }

    public int obtenerIdPorNombre(Connection conn, String nombreCompleto) throws DAOException {
        String sql = "SELECT id_autor FROM Autor WHERE nombre_completo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreCompleto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_autor");
            }
        } catch (SQLException e) {
            throw new DAOException("Error buscando autor", e);
        }
        return -1;
    }

    public int insertarAutor(Connection conn, String nombreCompleto) throws DAOException {
        String sql = "INSERT INTO Autor (nombre_completo) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreCompleto);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Error insertando autor", e);
        }
        throw new DAOException("No se pudo obtener ID de autor insertado", null);
    }

    private Autor mapearAutor(ResultSet rs) throws SQLException {
        Date fechaNac = rs.getDate("fecha_nacimiento");
        return new Autor(
                rs.getInt("id_autor"),
                rs.getString("nombre_completo"),
                rs.getString("nacionalidad"),
                fechaNac != null ? fechaNac.toLocalDate() : null
        );
    }
}
