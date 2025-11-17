package biblioteca.data.dao;

import biblioteca.data.db.ConexionBD;
import biblioteca.data.interfaces.DAO;
import biblioteca.entities.notificaciones.Notificacion;
import biblioteca.entities.prestamos.Prestamo;
import biblioteca.entities.inventario.Ejemplar;
import biblioteca.entities.inventario.Libro;
import biblioteca.entities.usuarios.Socio;
import biblioteca.entities.usuarios.Bibliotecario;
import biblioteca.entities.usuarios.TipoUsuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar la persistencia de notificaciones en la base de datos.
 * Maneja las operaciones CRUD sobre la tabla Notificacion, incluyendo JOINs completos con Prestamo y todas sus relaciones.
 */
public class NotificacionesDAO implements DAO<Notificacion> {

    @Override
    public void insertar(Notificacion notificacion) throws DAOException {
        String sql = """
                INSERT INTO Notificacion (fecha_envio, tipo, mensaje, leida, id_prestamo) 
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(notificacion.getFechaHora()));

            String tipo = inferirTipo(notificacion.getMensaje());
            ps.setString(2, tipo);
            ps.setString(3, notificacion.getMensaje());
            ps.setBoolean(4, notificacion.isLeida());

            if (notificacion.getPrestamo() == null) {
                throw new DAOException("La notificación debe estar asociada a un préstamo.");
            }
            ps.setInt(5, notificacion.getPrestamo().getId());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se pudo insertar la notificación (0 filas afectadas).");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se pudo obtener el ID generado para Notificacion.");
                }
                notificacion.setIdNotificacion(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al insertar notificación", e);
        }
    }

    @Override
    public Notificacion buscarPorId(int id) throws DAOException {
        String sql = """
                SELECT
                  n.id_notificacion, n.fecha_envio, n.tipo, n.mensaje, n.leida, n.id_prestamo,
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
                FROM Notificacion n
                INNER JOIN Prestamo p ON n.id_prestamo = p.id_prestamo
                INNER JOIN Socio s ON p.id_socio = s.id_socio
                INNER JOIN Usuario u_s ON s.id_usuario = u_s.id_usuario
                INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                INNER JOIN Libro l ON e.id_libro = l.id_libro
                INNER JOIN Autor a ON l.id_autor = a.id_autor
                INNER JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                WHERE n.id_notificacion = ?
                """;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearNotificacion(rs);
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar notificación por ID", e);
        }
    }

    @Override
    public List<Notificacion> listarTodos() throws DAOException {
        String sql = """
                SELECT
                  n.id_notificacion, n.fecha_envio, n.tipo, n.mensaje, n.leida, n.id_prestamo,
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
                FROM Notificacion n
                INNER JOIN Prestamo p ON n.id_prestamo = p.id_prestamo
                INNER JOIN Socio s ON p.id_socio = s.id_socio
                INNER JOIN Usuario u_s ON s.id_usuario = u_s.id_usuario
                INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                INNER JOIN Libro l ON e.id_libro = l.id_libro
                INNER JOIN Autor a ON l.id_autor = a.id_autor
                INNER JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                ORDER BY n.fecha_envio DESC
                """;
        List<Notificacion> lista = new ArrayList<>();

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearNotificacion(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al listar notificaciones", e);
        }
        return lista;
    }

    @Override
    public void actualizar(Notificacion notificacion) throws DAOException {
        String sql = """
                UPDATE Notificacion 
                SET leida = ?, mensaje = ?, tipo = ? 
                WHERE id_notificacion = ?
                """;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, notificacion.isLeida());
            ps.setString(2, notificacion.getMensaje());
            String tipo = inferirTipo(notificacion.getMensaje());
            ps.setString(3, tipo);
            ps.setInt(4, notificacion.getIdNotificacion());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró la notificación con ID " + notificacion.getIdNotificacion());
            }

        } catch (SQLException e) {
            throw new DAOException("Error al actualizar notificación", e);
        }
    }

    @Override
    public void eliminar(int id) throws DAOException {
        String sql = "DELETE FROM Notificacion WHERE id_notificacion = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró la notificación con ID " + id);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al eliminar notificación", e);
        }
    }


    public List<Notificacion> listarNoLeidas() throws DAOException {
        String sql = """
                SELECT
                  n.id_notificacion, n.fecha_envio, n.tipo, n.mensaje, n.leida, n.id_prestamo,
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
                FROM Notificacion n
                INNER JOIN Prestamo p ON n.id_prestamo = p.id_prestamo
                INNER JOIN Socio s ON p.id_socio = s.id_socio
                INNER JOIN Usuario u_s ON s.id_usuario = u_s.id_usuario
                INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                INNER JOIN Libro l ON e.id_libro = l.id_libro
                INNER JOIN Autor a ON l.id_autor = a.id_autor
                INNER JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                WHERE n.leida = FALSE
                ORDER BY n.fecha_envio DESC
                """;
        List<Notificacion> lista = new ArrayList<>();

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearNotificacion(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al listar notificaciones no leídas", e);
        }
        return lista;
    }

    public List<Notificacion> listarPorPrestamoId(int idPrestamo) throws DAOException {
        String sql = """
                SELECT
                  n.id_notificacion, n.fecha_envio, n.tipo, n.mensaje, n.leida, n.id_prestamo,
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
                FROM Notificacion n
                INNER JOIN Prestamo p ON n.id_prestamo = p.id_prestamo
                INNER JOIN Socio s ON p.id_socio = s.id_socio
                INNER JOIN Usuario u_s ON s.id_usuario = u_s.id_usuario
                INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                INNER JOIN Libro l ON e.id_libro = l.id_libro
                INNER JOIN Autor a ON l.id_autor = a.id_autor
                INNER JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                WHERE n.id_prestamo = ?
                ORDER BY n.fecha_envio DESC
                """;
        List<Notificacion> lista = new ArrayList<>();

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearNotificacion(rs));
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Error al listar notificaciones por préstamo", e);
        }
        return lista;
    }

    // === Métodos auxiliares ===

    private Notificacion mapearNotificacion(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_notificacion");

        // fecha_envio es DATETIME en la BD
        Timestamp timestamp = rs.getTimestamp("fecha_envio");
        LocalDateTime fechaHora = timestamp != null
                ? timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();

        String mensaje = rs.getString("mensaje");
        boolean leida = rs.getBoolean("leida");

        // Reconstruir Prestamo completo usando los mismos métodos de mapeo que PrestamoDAO
        Prestamo prestamo = mapearPrestamo(rs);

        return new Notificacion(id, mensaje, fechaHora, prestamo, leida);
    }

    /**
     * Mapea un registro SQL a un objeto Prestamo.
     * Reutiliza la lógica de PrestamoDAO para mantener consistencia.
     */
    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        int id = rs.getInt("p_id_prestamo");

        Timestamp timestamp = rs.getTimestamp("fecha_prestamo");
        java.time.LocalDate fechaPrestamo = timestamp != null
                ? timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : java.time.LocalDate.now();

        java.time.LocalDate fechaVencimiento = rs.getDate("fecha_vencimiento").toLocalDate();
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
        java.time.LocalDate fechaRegistro = rs.getDate("s_fecha_registro").toLocalDate();
        String username = rs.getString("s_username");
        String password = rs.getString("s_password");
        TipoUsuario tipo = TipoUsuario.valueOf(rs.getString("s_tipo").toUpperCase());

        String numeroSocio = rs.getString("numero_socio");
        java.time.LocalDate vencCarnet = rs.getDate("s_fecha_vencimiento_carnet") != null
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
        java.time.LocalDate fechaRegistro = rs.getDate("b_fecha_registro").toLocalDate();
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

    /**
     * Infiere el tipo de notificación según el contenido del mensaje.
     */
    private String inferirTipo(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return "Recordatorio";
        }
        String mensajeLower = mensaje.toLowerCase();
        if (mensajeLower.contains("vencido") || mensajeLower.contains("atraso")) {
            return "Alerta de atraso";
        }
        return "Recordatorio";
    }
}
