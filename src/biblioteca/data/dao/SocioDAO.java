package biblioteca.data.dao;

import biblioteca.data.db.ConexionBD;
import biblioteca.data.interfaces.DAO;
import biblioteca.entities.usuarios.Socio;
import biblioteca.entities.usuarios.TipoUsuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar la persistencia de socios en la base de datos.
 * Maneja las operaciones CRUD sobre las tablas Usuario y Socio mediante JOINs, incluyendo la categoría del socio.
 */
public class SocioDAO implements DAO<Socio> {

    public SocioDAO() throws DAOException {
    }

    @Override
    public void insertar(Socio socio) throws DAOException {
        String sqlUsuario = """
            INSERT INTO Usuario 
            (nombre, apellido, dni, email, telefono, fecha_registro, tipo_usuario, username, password)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String sqlSocio = """
            INSERT INTO Socio 
            (id_usuario, numero_socio, fecha_vencimiento_carnet, estado, tiene_sanciones, tiene_atrasos, categoria)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conexion = ConexionBD.getConexion()) {
            conexion.setAutoCommit(false);

            int idUsuario;
            int idSocio;

            try {
                try (PreparedStatement psUsuario =
                             conexion.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                    psUsuario.setString(1, socio.getNombre());
                    psUsuario.setString(2, socio.getApellido());
                    psUsuario.setString(3, socio.getDni());
                    psUsuario.setString(4, socio.getEmail());
                    psUsuario.setString(5, socio.getTelefono());
                    psUsuario.setDate(6, Date.valueOf(socio.getFechaRegistro() != null ? socio.getFechaRegistro() : java.time.LocalDate.now()));
                    psUsuario.setString(7, TipoUsuario.SOCIO.name());
                    psUsuario.setString(8, socio.getUsername());
                    psUsuario.setString(9, socio.getPassword());
                    psUsuario.executeUpdate();

                    try (ResultSet rs = psUsuario.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("No se pudo obtener el ID generado para Usuario.");
                        }
                        idUsuario = rs.getInt(1);
                    }
                }

                socio.setId(idUsuario);

                try (PreparedStatement psSocio = conexion.prepareStatement(sqlSocio, Statement.RETURN_GENERATED_KEYS)) {
                    psSocio.setInt(1, idUsuario);
                    psSocio.setString(2, socio.getNumeroSocio());
                    psSocio.setDate(3, Date.valueOf(socio.getFechaVencimientoCarnet()));
                    psSocio.setString(4, socio.getEstado());
                    psSocio.setBoolean(5, socio.isTieneSanciones());
                    psSocio.setBoolean(6, socio.isTieneAtrasos());
                    String categoria = socio.getCategoria();
                    if (categoria == null || categoria.isBlank()) {
                        categoria = "Estándar";
                        socio.setCategoria(categoria);
                    }
                    psSocio.setString(7, categoria);
                    psSocio.executeUpdate();

                    try (ResultSet rs = psSocio.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("No se pudo obtener el ID generado para Socio.");
                        }
                        idSocio = rs.getInt(1);
                    }
                }

                socio.setIdSocio(idSocio);

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al insertar socio", e);
        }
    }

    @Override
    public Socio buscarPorId(int id) throws DAOException {
        String sql = """
            SELECT 
                u.id_usuario, u.nombre, u.apellido, u.dni, u.email, u.telefono, 
                u.fecha_registro, u.tipo_usuario, u.username, u.password,
                s.id_socio, s.numero_socio, s.fecha_vencimiento_carnet, s.estado, 
                s.tiene_sanciones, s.tiene_atrasos, s.categoria
            FROM Usuario u 
            INNER JOIN Socio s ON u.id_usuario = s.id_usuario 
            WHERE u.id_usuario = ?
        """;

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearSocio(rs);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar socio por ID", e);
        }
        return null;
    }

    public Socio buscarPorIdSocio(int idSocio) throws DAOException {
        String sql = """
            SELECT 
                u.id_usuario, u.nombre, u.apellido, u.dni, u.email, u.telefono, 
                u.fecha_registro, u.tipo_usuario, u.username, u.password,
                s.id_socio, s.numero_socio, s.fecha_vencimiento_carnet, s.estado, 
                s.tiene_sanciones, s.tiene_atrasos, s.categoria
            FROM Usuario u 
            INNER JOIN Socio s ON u.id_usuario = s.id_usuario 
            WHERE s.id_socio = ?
        """;

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idSocio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearSocio(rs);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar socio por id_socio", e);
        }
        return null;
    }

    @Override
    public List<Socio> listarTodos() throws DAOException {
        List<Socio> socios = new ArrayList<>();

        String sql = """
            SELECT 
                u.id_usuario, u.nombre, u.apellido, u.dni, u.email, u.telefono, 
                u.fecha_registro, u.tipo_usuario, u.username, u.password,
                s.id_socio, s.numero_socio, s.fecha_vencimiento_carnet, s.estado, 
                s.tiene_sanciones, s.tiene_atrasos, s.categoria
            FROM Usuario u 
            INNER JOIN Socio s ON u.id_usuario = s.id_usuario
            ORDER BY s.id_socio
        """;

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) socios.add(mapearSocio(rs));

        } catch (SQLException e) {
            throw new DAOException("Error al listar socios", e);
        }

        return socios;
    }

    public Socio buscarPorDni(String dni) throws DAOException {
        String sql = """
            SELECT 
                u.id_usuario, u.nombre, u.apellido, u.dni, u.email, u.telefono,
                u.fecha_registro, u.tipo_usuario, u.username, u.password,
                s.id_socio, s.numero_socio, s.fecha_vencimiento_carnet, s.estado,
                s.tiene_sanciones, s.tiene_atrasos, s.categoria
            FROM Usuario u
            INNER JOIN Socio s ON u.id_usuario = s.id_usuario
            WHERE u.dni = ?
        """;

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, dni);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearSocio(rs);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar socio por DNI", e);
        }

        return null;
    }

    public Socio buscarPorUsername(String username) throws DAOException {
        String sql = """
        SELECT 
            u.id_usuario, u.nombre, u.apellido, u.dni, u.email, u.telefono,
            u.fecha_registro, u.tipo_usuario, u.username, u.password,
            s.id_socio, s.numero_socio, s.fecha_vencimiento_carnet, s.estado,
            s.tiene_sanciones, s.tiene_atrasos, s.categoria
        FROM Usuario u
        INNER JOIN Socio s ON u.id_usuario = s.id_usuario
        WHERE u.username = ?
    """;

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearSocio(rs);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar socio por username", e);
        }

        return null;
    }

    @Override
    public void actualizar(Socio socio) throws DAOException {
        String sql = """
            UPDATE Usuario u
            INNER JOIN Socio s ON u.id_usuario = s.id_usuario
            SET u.nombre = ?, u.apellido = ?, u.email = ?, u.telefono = ?,
                s.estado = ?, s.tiene_sanciones = ?, s.tiene_atrasos = ?, 
                s.categoria = ?, s.numero_socio = ?, s.fecha_vencimiento_carnet = ?
            WHERE s.id_socio = ?
        """;

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, socio.getNombre());
            ps.setString(2, socio.getApellido());
            ps.setString(3, socio.getEmail());
            ps.setString(4, socio.getTelefono());
            ps.setString(5, socio.getEstado());
            ps.setBoolean(6, socio.isTieneSanciones());
            ps.setBoolean(7, socio.isTieneAtrasos());
            String categoria = socio.getCategoria();
            if (categoria == null || categoria.isBlank()) {
                categoria = "Estándar";
            }
            ps.setString(8, categoria);
            ps.setString(9, socio.getNumeroSocio());
            ps.setDate(10, Date.valueOf(socio.getFechaVencimientoCarnet()));
            ps.setInt(11, socio.getIdSocio());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Error al actualizar socio", e);
        }
    }

    @Override
    public void eliminar(int id) throws DAOException {
        String sqlSocio = "DELETE FROM Socio WHERE id_usuario = ?";
        String sqlUsuario = "DELETE FROM Usuario WHERE id_usuario = ?";

        try (Connection conexion = ConexionBD.getConexion()) {
            conexion.setAutoCommit(false);

            try {
                try (PreparedStatement ps1 = conexion.prepareStatement(sqlSocio)) {
                    ps1.setInt(1, id);
                    int filas = ps1.executeUpdate();
                    if (filas == 0) {
                        throw new DAOException("No se encontró el socio con id_usuario " + id);
                    }
                }

                try (PreparedStatement ps2 = conexion.prepareStatement(sqlUsuario)) {
                    ps2.setInt(1, id);
                    ps2.executeUpdate();
                }

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw new DAOException("Error al eliminar socio", e);
            } catch (DAOException e) {
                conexion.rollback();
                throw e;
            }

        } catch (DAOException e) {
            throw e;
        } catch (SQLException e) {
            throw new DAOException("Error al eliminar socio", e);
        }
    }

    private Socio mapearSocio(ResultSet rs) throws SQLException {
        int idUsuario = rs.getInt("id_usuario");
        int idSocio = rs.getInt("id_socio");

        String categoria = rs.getString("categoria");
        if (categoria == null || categoria.isBlank()) {
            categoria = "Estándar";
        }

        Socio socio = new Socio(
                idUsuario,
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("dni"),
                rs.getString("email"),
                rs.getString("telefono"),
                rs.getDate("fecha_registro").toLocalDate(),
                TipoUsuario.SOCIO,
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("numero_socio"),
                rs.getDate("fecha_vencimiento_carnet").toLocalDate(),
                rs.getString("estado"),
                rs.getBoolean("tiene_sanciones"),
                rs.getBoolean("tiene_atrasos"),
                categoria
        );

        socio.setIdSocio(idSocio);
        return socio;
    }
}