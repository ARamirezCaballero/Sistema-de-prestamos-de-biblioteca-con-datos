package biblioteca.data.dao;

import biblioteca.data.db.ConexionBD;
import biblioteca.data.interfaces.DAO;
import biblioteca.entities.inventario.Editorial;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar la persistencia de editoriales en la base de datos.
 * Maneja las operaciones CRUD sobre la tabla Editorial.
 */
public class EditorialDAO implements DAO<Editorial> {

    public EditorialDAO() {
    }

    @Override
    public void insertar(Editorial editorial) throws DAOException {
        String sql = """
            INSERT INTO Editorial (nombre, pais)
            VALUES (?, ?)
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, editorial.getNombre());
            ps.setString(2, editorial.getPais());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se pudo obtener el ID generado para Editorial.");
                }
                editorial.setIdEditorial(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al insertar editorial", e);
        }
    }

    @Override
    public Editorial buscarPorId(int id) throws DAOException {
        String sql = """
            SELECT id_editorial, nombre, pais
            FROM Editorial
            WHERE id_editorial = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEditorial(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar editorial por ID", e);
        }
        return null;
    }

    @Override
    public List<Editorial> listarTodos() throws DAOException {
        List<Editorial> editoriales = new ArrayList<>();
        String sql = """
            SELECT id_editorial, nombre, pais
            FROM Editorial
            ORDER BY nombre
        """;

        try (Connection conn = ConexionBD.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                editoriales.add(mapearEditorial(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al listar editoriales", e);
        }

        return editoriales;
    }

    @Override
    public void actualizar(Editorial editorial) throws DAOException {
        String sql = """
            UPDATE Editorial
            SET nombre = ?, pais = ?
            WHERE id_editorial = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, editorial.getNombre());
            ps.setString(2, editorial.getPais());
            ps.setInt(3, editorial.getIdEditorial());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Error al actualizar editorial", e);
        }
    }

    @Override
    public void eliminar(int id) throws DAOException {
        String sql = "DELETE FROM Editorial WHERE id_editorial = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró la editorial con ID " + id);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al eliminar editorial", e);
        }
    }

    public Editorial buscarPorNombre(String nombre) throws DAOException {
        String sql = """
            SELECT id_editorial, nombre, pais
            FROM Editorial
            WHERE nombre = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEditorial(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar editorial por nombre", e);
        }
        return null;
    }

    public int obtenerIdPorNombre(Connection conn, String nombre) throws DAOException {
        String sql = "SELECT id_editorial FROM Editorial WHERE nombre = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_editorial");
            }
        } catch (SQLException e) {
            throw new DAOException("Error buscando editorial", e);
        }
        return -1;
    }

    public int insertarEditorial(Connection conn, String nombre) throws DAOException {
        String sql = "INSERT INTO Editorial (nombre) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Error insertando editorial", e);
        }
        throw new DAOException("No se pudo obtener ID de editorial insertado", null);
    }

    private Editorial mapearEditorial(ResultSet rs) throws SQLException {
        return new Editorial(
                rs.getInt("id_editorial"),
                rs.getString("nombre"),
                rs.getString("pais")
        );
    }
}
