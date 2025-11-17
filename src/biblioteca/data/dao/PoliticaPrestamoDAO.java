package biblioteca.data.dao;

import biblioteca.data.db.ConexionBD;
import biblioteca.data.interfaces.DAO;
import biblioteca.entities.prestamos.PoliticaPrestamo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar la persistencia de políticas de préstamo en la base de datos.
 * Maneja las operaciones CRUD sobre la tabla PoliticaPrestamo, que define las reglas de préstamo por categoría de socio.
 */
public class PoliticaPrestamoDAO implements DAO<PoliticaPrestamo> {

    @Override
    public void insertar(PoliticaPrestamo p) throws DAOException {
        String sql = """
                INSERT INTO PoliticaPrestamo (categoria, dias_prestamo, max_prestamos_simultaneos, multa_por_dia)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getCategoria());
            ps.setInt(2, p.getDiasPrestamo());
            ps.setInt(3, p.getMaxPrestamosSimultaneos());
            ps.setDouble(4, p.getMultaPorDia());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se insertó la política de préstamo.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se pudo obtener el ID generado para PoliticaPrestamo.");
                }
                p.setId(rs.getInt(1));
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new DAOException("Ya existe una política con la categoría especificada: " + p.getCategoria(), ex);
        } catch (SQLException e) {
            throw new DAOException("Error al insertar política de préstamo: " + e.getMessage(), e);
        }
    }

    @Override
    public PoliticaPrestamo buscarPorId(int id) throws DAOException {
        String sql = """
                SELECT id_politica, categoria, dias_prestamo, max_prestamos_simultaneos, multa_por_dia
                FROM PoliticaPrestamo
                WHERE id_politica = ?
                """;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearPolitica(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar política por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PoliticaPrestamo> listarTodos() throws DAOException {
        String sql = """
                SELECT id_politica, categoria, dias_prestamo, max_prestamos_simultaneos, multa_por_dia
                FROM PoliticaPrestamo
                ORDER BY categoria
                """;
        List<PoliticaPrestamo> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPolitica(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error al listar políticas de préstamo: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(PoliticaPrestamo p) throws DAOException {
        String sql = """
                UPDATE PoliticaPrestamo
                SET categoria = ?, dias_prestamo = ?, max_prestamos_simultaneos = ?, multa_por_dia = ?
                WHERE id_politica = ?
                """;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getCategoria());
            ps.setInt(2, p.getDiasPrestamo());
            ps.setInt(3, p.getMaxPrestamosSimultaneos());
            ps.setDouble(4, p.getMultaPorDia());
            ps.setInt(5, p.getIdPolitica());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró la política para actualizar (id=" + p.getIdPolitica() + ").");
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new DAOException("La categoría proporcionada ya existe y viola la restricción UNIQUE.", ex);
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar política: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) throws DAOException {
        String sql = "DELETE FROM PoliticaPrestamo WHERE id_politica = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró la política para eliminar (id=" + id + ").");
            }

        } catch (SQLException e) {
            throw new DAOException("Error al eliminar política: " + e.getMessage(), e);
        }
    }


    public PoliticaPrestamo buscarPorCategoria(String categoria) throws DAOException {
        String sql = """
                SELECT id_politica, categoria, dias_prestamo, max_prestamos_simultaneos, multa_por_dia
                FROM PoliticaPrestamo
                WHERE categoria = ?
                """;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoria);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearPolitica(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar política por categoría: " + e.getMessage(), e);
        }
    }


    private PoliticaPrestamo mapearPolitica(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_politica");
        String categoria = rs.getString("categoria");
        int dias = rs.getInt("dias_prestamo");
        int max = rs.getInt("max_prestamos_simultaneos");
        double multa = rs.getDouble("multa_por_dia");
        return new PoliticaPrestamo(id, categoria, dias, max, multa);
    }
}
