package biblioteca.data.dao;

import biblioteca.data.db.ConexionBD;
import biblioteca.data.interfaces.DAO;
import biblioteca.entities.prestamos.Devolucion;
import biblioteca.entities.prestamos.Prestamo;
import biblioteca.entities.inventario.Ejemplar;
import biblioteca.entities.inventario.Libro;
import biblioteca.entities.usuarios.Socio;
import biblioteca.entities.usuarios.Bibliotecario;
import biblioteca.entities.usuarios.TipoUsuario;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar la persistencia de devoluciones en la base de datos.
 * Maneja las operaciones CRUD sobre la tabla Devolucion, incluyendo JOINs completos con Prestamo y todas sus relaciones.
 */
public class DevolucionDAO implements DAO<Devolucion> {

    public DevolucionDAO(PrestamoDAO prestamoDAO) {
    }

    @Override
    public void insertar(Devolucion d) throws DAOException {
        String sqlInsert = """
                INSERT INTO Devolucion (fecha_devolucion, estado_ejemplar, observaciones, multa, id_prestamo)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(d.getFechaDevolucion().atStartOfDay()));
            ps.setString(2, d.getEstadoEjemplar());
            ps.setString(3, d.getObservaciones());
            ps.setDouble(4, d.getMulta());
            ps.setInt(5, d.getPrestamo().getId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se pudo obtener el ID generado para Devolucion.");
                }
                d.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al insertar devolución", e);
        }
    }

    @Override
    public Devolucion buscarPorId(int id) throws DAOException {
        String sql = """
                SELECT
                  d.id_devolucion, d.fecha_devolucion, d.estado_ejemplar, d.observaciones, d.multa, d.id_prestamo,
                  p.id_prestamo AS p_id_prestamo, p.fecha_prestamo, p.fecha_vencimiento, p.estado AS p_estado, p.dias_prestamo,
                  s.id_socio, s.id_usuario AS s_id_usuario, s.numero_socio, s.fecha_vencimiento_carnet AS s_fecha_vencimiento_carnet,
                  s.estado AS s_estado, s.tiene_sanciones, s.tiene_atrasos, s.categoria AS s_categoria,
                  u_s.nombre AS s_nombre, u_s.apellido AS s_apellido, u_s.dni AS s_dni, u_s.email AS s_email, u_s.telefono AS s_telefono,
                  u_s.fecha_registro AS s_fecha_registro, u_s.username AS s_username, u_s.password AS s_password, u_s.tipo_usuario AS s_tipo,
                  e.id_ejemplar, e.codigo_ejemplar, e.estado AS e_estado, e.ubicacion,
                  l.id_libro, l.titulo, l.isbn, l.categoria AS l_categoria, l.anio_publicacion,
                  l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                  l.id_editorial, ed.nombre AS nombre_editorial, ed.pais,
                  b.id_bibliotecario, b.legajo, b.turno,
                  u_b.id_usuario AS b_id_usuario, u_b.nombre AS b_nombre, u_b.apellido AS b_apellido, 
                  u_b.dni AS b_dni, u_b.username AS b_username, u_b.password AS b_password, 
                  u_b.email AS b_email, u_b.telefono AS b_telefono, u_b.fecha_registro AS b_fecha_registro
                FROM Devolucion d
                INNER JOIN Prestamo p ON d.id_prestamo = p.id_prestamo
                INNER JOIN Socio s ON p.id_socio = s.id_socio
                INNER JOIN Usuario u_s ON s.id_usuario = u_s.id_usuario
                INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                INNER JOIN Libro l ON e.id_libro = l.id_libro
                INNER JOIN Autor a ON l.id_autor = a.id_autor
                INNER JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                WHERE d.id_devolucion = ?
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearDevolucion(rs);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar devolución con ID " + id, e);
        }

        return null;
    }

    @Override
    public List<Devolucion> listarTodos() throws DAOException {
        List<Devolucion> lista = new ArrayList<>();
        String sql = """
                SELECT
                  d.id_devolucion, d.fecha_devolucion, d.estado_ejemplar, d.observaciones, d.multa, d.id_prestamo,
                  p.id_prestamo AS p_id_prestamo, p.fecha_prestamo, p.fecha_vencimiento, p.estado AS p_estado, p.dias_prestamo,
                  s.id_socio, s.id_usuario AS s_id_usuario, s.numero_socio, s.fecha_vencimiento_carnet AS s_fecha_vencimiento_carnet,
                  s.estado AS s_estado, s.tiene_sanciones, s.tiene_atrasos, s.categoria AS s_categoria,
                  u_s.nombre AS s_nombre, u_s.apellido AS s_apellido, u_s.dni AS s_dni, u_s.email AS s_email, u_s.telefono AS s_telefono,
                  u_s.fecha_registro AS s_fecha_registro, u_s.username AS s_username, u_s.password AS s_password, u_s.tipo_usuario AS s_tipo,
                  e.id_ejemplar, e.codigo_ejemplar, e.estado AS e_estado, e.ubicacion,
                  l.id_libro, l.titulo, l.isbn, l.categoria AS l_categoria, l.anio_publicacion,
                  l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                  l.id_editorial, ed.nombre AS nombre_editorial, ed.pais,
                  b.id_bibliotecario, b.legajo, b.turno,
                  u_b.id_usuario AS b_id_usuario, u_b.nombre AS b_nombre, u_b.apellido AS b_apellido, 
                  u_b.dni AS b_dni, u_b.username AS b_username, u_b.password AS b_password, 
                  u_b.email AS b_email, u_b.telefono AS b_telefono, u_b.fecha_registro AS b_fecha_registro
                FROM Devolucion d
                INNER JOIN Prestamo p ON d.id_prestamo = p.id_prestamo
                INNER JOIN Socio s ON p.id_socio = s.id_socio
                INNER JOIN Usuario u_s ON s.id_usuario = u_s.id_usuario
                INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                INNER JOIN Libro l ON e.id_libro = l.id_libro
                INNER JOIN Autor a ON l.id_autor = a.id_autor
                INNER JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                ORDER BY d.id_devolucion DESC
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearDevolucion(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al listar devoluciones.", e);
        }

        return lista;
    }

    // Actualizar devolución (solo los campos de devolución)
    @Override
    public void actualizar(Devolucion d) throws DAOException {
        String sql = """
                UPDATE Devolucion 
                SET fecha_devolucion = ?, estado_ejemplar = ?, observaciones = ?, multa = ? 
                WHERE id_devolucion = ?
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(d.getFechaDevolucion().atStartOfDay()));
            ps.setString(2, d.getEstadoEjemplar());
            ps.setString(3, d.getObservaciones());
            ps.setDouble(4, d.getMulta());
            ps.setInt(5, d.getId());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró la devolución con ID " + d.getId());
            }

        } catch (SQLException e) {
            throw new DAOException("Error al actualizar devolución con ID " + d.getId(), e);
        }
    }

    // Eliminar devolución
    @Override
    public void eliminar(int id) throws DAOException {
        String sql = "DELETE FROM Devolucion WHERE id_devolucion = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró la devolución con ID " + id);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al eliminar devolución con ID " + id, e);
        }
    }

    /**
     * Mapea un registro SQL a un objeto Devolucion con Prestamo completo.
     * Usa los mismos métodos de mapeo que PrestamoDAO para mantener consistencia.
     */
    private Devolucion mapearDevolucion(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_devolucion");

        // fecha_devolucion es DATETIME en la BD
        Timestamp timestamp = rs.getTimestamp("fecha_devolucion");
        LocalDate fechaDevolucion = timestamp != null
                ? timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();

        String estadoEjemplar = rs.getString("estado_ejemplar");
        String observaciones = rs.getString("observaciones");
        double multa = rs.getDouble("multa");

        // Reconstruir Prestamo completo usando los mismos métodos de mapeo
        Prestamo prestamo = mapearPrestamo(rs);

        return new Devolucion(id, fechaDevolucion, estadoEjemplar, observaciones, prestamo, multa);
    }

    /**
     * Mapea un registro SQL a un objeto Prestamo.
     * Reutiliza la lógica de PrestamoDAO para mantener consistencia.
     */
    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        int id = rs.getInt("p_id_prestamo");

        Timestamp timestamp = rs.getTimestamp("fecha_prestamo");
        LocalDate fechaPrestamo = timestamp != null
                ? timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();

        LocalDate fechaVencimiento = rs.getDate("fecha_vencimiento").toLocalDate();
        String estado = rs.getString("p_estado");
        int diasPrestamo = rs.getInt("dias_prestamo");

        Socio socio = mapearSocio(rs);
        Ejemplar ejemplar = mapearEjemplar(rs);
        Bibliotecario bibliotecario = mapearBibliotecario(rs);

        // El constructor alternativo no incluye bibliotecario, se asigna después
        Prestamo prestamo = new Prestamo(id, fechaPrestamo, fechaVencimiento, estado, diasPrestamo, socio, ejemplar, null);
        prestamo.setBibliotecario(bibliotecario);
        return prestamo;
    }

    private Socio mapearSocio(ResultSet rs) throws SQLException {
        int idSocio = rs.getInt("id_socio");
        int idUsuario = rs.getInt("s_id_usuario");
        String nombre = rs.getString("s_nombre");
        String apellido = rs.getString("s_apellido");
        String dni = rs.getString("s_dni");
        String email = rs.getString("s_email");
        String telefono = rs.getString("s_telefono");
        LocalDate fechaRegistro = rs.getDate("s_fecha_registro").toLocalDate();
        String username = rs.getString("s_username");
        String password = rs.getString("s_password");
        TipoUsuario tipo = TipoUsuario.valueOf(rs.getString("s_tipo").toUpperCase());

        String numeroSocio = rs.getString("numero_socio");
        LocalDate vencCarnet = rs.getDate("s_fecha_vencimiento_carnet") != null
                ? rs.getDate("s_fecha_vencimiento_carnet").toLocalDate() : null;
        String estado = rs.getString("s_estado");
        boolean sanciones = rs.getBoolean("tiene_sanciones");
        boolean atrasos = rs.getBoolean("tiene_atrasos");
        String categoria = rs.getString("s_categoria");
        if (categoria == null || categoria.isBlank()) {
            categoria = "Estándar";
        }

        Socio socio = new Socio(idUsuario, nombre, apellido, dni, email, telefono, fechaRegistro, tipo,
                username, password, numeroSocio, vencCarnet, estado, sanciones, atrasos, categoria);
        socio.setIdSocio(idSocio);
        return socio;
    }

    private Ejemplar mapearEjemplar(ResultSet rs) throws SQLException {
        int idEj = rs.getInt("id_ejemplar");
        String cod = rs.getString("codigo_ejemplar");
        String estado = rs.getString("e_estado");
        String ubicacion = rs.getString("ubicacion");
        Libro libro = mapearLibro(rs);
        return new Ejemplar(idEj, cod, estado, ubicacion, libro);
    }

    private Libro mapearLibro(ResultSet rs) throws SQLException {
        Libro libro = new Libro(
                rs.getInt("id_libro"),
                rs.getString("titulo"),
                rs.getString("nombre_autor"),
                rs.getString("isbn"),
                rs.getString("l_categoria"),
                rs.getString("nombre_editorial"),
                rs.getInt("anio_publicacion")
        );
        libro.setIdAutor(rs.getInt("id_autor"));
        libro.setIdEditorial(rs.getInt("id_editorial"));
        return libro;
    }

    private Bibliotecario mapearBibliotecario(ResultSet rs) throws SQLException {
        int idBibliotecario = rs.getInt("id_bibliotecario");
        if (rs.wasNull()) {
            return null;
        }

        int idUsuario = rs.getInt("b_id_usuario");
        String nombre = rs.getString("b_nombre");
        String apellido = rs.getString("b_apellido");
        String dni = rs.getString("b_dni");
        String email = rs.getString("b_email");
        String telefono = rs.getString("b_telefono");
        LocalDate fechaRegistro = rs.getDate("b_fecha_registro").toLocalDate();
        String username = rs.getString("b_username");
        String password = rs.getString("b_password");
        String legajo = rs.getString("legajo");
        String turno = rs.getString("turno");

        Bibliotecario bibliotecario = new Bibliotecario(
                idUsuario, nombre, apellido, dni, email, telefono, fechaRegistro,
                TipoUsuario.BIBLIOTECARIO, username, password, legajo, turno
        );
        bibliotecario.setIdBibliotecario(idBibliotecario);
        return bibliotecario;
    }
}
