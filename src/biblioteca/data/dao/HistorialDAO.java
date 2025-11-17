package biblioteca.data.dao;

import biblioteca.data.db.ConexionBD;
import biblioteca.data.interfaces.DAO;
import biblioteca.entities.reportes.Historial;
import biblioteca.entities.usuarios.Socio;
import biblioteca.entities.prestamos.Prestamo;
import biblioteca.entities.inventario.Ejemplar;
import biblioteca.entities.inventario.Libro;
import biblioteca.entities.usuarios.Bibliotecario;
import biblioteca.entities.usuarios.TipoUsuario;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar la persistencia del historial de operaciones en la base de datos.
 * Maneja las operaciones CRUD sobre la tabla Historial, incluyendo JOINs completos con Usuario, Socio, Prestamo, Libro y todas sus relaciones.
 */
public class HistorialDAO implements DAO<Historial> {

    public HistorialDAO(SocioDAO socioDAO) {
    }

    @Override
    public void insertar(Historial historial) throws DAOException {
        String sql = """
                INSERT INTO Historial (fecha, tipo_operacion, detalles, id_usuario, id_libro, id_prestamo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(historial.getFecha()));
            ps.setString(2, historial.getTipoOperacion());
            ps.setString(3, historial.getDetalles());
            ps.setInt(4, historial.getSocio().getIdUsuario());
            Integer idLibro = null;
            if (historial.getPrestamo() != null) {
                if (historial.getPrestamo().getEjemplar() != null &&
                        historial.getPrestamo().getEjemplar().getLibro() != null) {
                    idLibro = historial.getPrestamo().getEjemplar().getLibro().getId();
                }
                ps.setInt(6, historial.getPrestamo().getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            if (idLibro != null) {
                ps.setInt(5, idLibro);
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            int filas = ps.executeUpdate();
            if (filas == 0) throw new DAOException("No se pudo insertar historial (0 filas afectadas)");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se pudo obtener el ID generado para Historial.");
                }
                historial.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al insertar historial", e);
        }
    }

    @Override
    public Historial buscarPorId(int id) throws DAOException {
        String sql = """
                SELECT
                  h.id_historial, h.fecha, h.tipo_operacion, h.detalles, h.id_usuario, h.id_libro, h.id_prestamo,
                  s.id_socio, s.id_usuario AS s_id_usuario, s.numero_socio, s.fecha_vencimiento_carnet AS s_fecha_vencimiento_carnet,
                  s.estado AS s_estado, s.tiene_sanciones, s.tiene_atrasos, s.categoria AS s_categoria,
                  u_s.nombre AS s_nombre, u_s.apellido AS s_apellido, u_s.dni AS s_dni, u_s.email AS s_email, u_s.telefono AS s_telefono,
                  u_s.fecha_registro AS s_fecha_registro, u_s.username AS s_username, u_s.password AS s_password, u_s.tipo_usuario AS s_tipo,
                  p.id_prestamo AS p_id_prestamo, p.fecha_prestamo, p.fecha_vencimiento, p.estado AS p_estado, p.dias_prestamo,
                  e.id_ejemplar, e.codigo_ejemplar, e.estado AS e_estado, e.ubicacion,
                  l.id_libro AS l_id_libro, l.titulo, l.isbn, l.categoria AS l_categoria, l.anio_publicacion,
                  l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                  l.id_editorial, ed.nombre AS nombre_editorial, ed.pais,
                  b.id_bibliotecario, b.legajo, b.turno,
                  u_b.id_usuario AS b_id_usuario, u_b.nombre AS b_nombre, u_b.apellido AS b_apellido, 
                  u_b.dni AS b_dni, u_b.username AS b_username, u_b.password AS b_password, 
                  u_b.email AS b_email, u_b.telefono AS b_telefono, u_b.fecha_registro AS b_fecha_registro
                FROM Historial h
                INNER JOIN Usuario u_s ON h.id_usuario = u_s.id_usuario
                INNER JOIN Socio s ON u_s.id_usuario = s.id_usuario
                LEFT JOIN Prestamo p ON h.id_prestamo = p.id_prestamo
                LEFT JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                LEFT JOIN Libro l ON (h.id_libro = l.id_libro OR (e.id_libro = l.id_libro))
                LEFT JOIN Autor a ON l.id_autor = a.id_autor
                LEFT JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                WHERE h.id_historial = ?
                """;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearHistorial(rs);
                else return null;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar historial por ID", e);
        }
    }

    @Override
    public List<Historial> listarTodos() throws DAOException {
        String sql = """
                SELECT
                  h.id_historial, h.fecha, h.tipo_operacion, h.detalles, h.id_usuario, h.id_libro, h.id_prestamo,
                  s.id_socio, s.id_usuario AS s_id_usuario, s.numero_socio, s.fecha_vencimiento_carnet AS s_fecha_vencimiento_carnet,
                  s.estado AS s_estado, s.tiene_sanciones, s.tiene_atrasos, s.categoria AS s_categoria,
                  u_s.nombre AS s_nombre, u_s.apellido AS s_apellido, u_s.dni AS s_dni, u_s.email AS s_email, u_s.telefono AS s_telefono,
                  u_s.fecha_registro AS s_fecha_registro, u_s.username AS s_username, u_s.password AS s_password, u_s.tipo_usuario AS s_tipo,
                  p.id_prestamo AS p_id_prestamo, p.fecha_prestamo, p.fecha_vencimiento, p.estado AS p_estado, p.dias_prestamo,
                  e.id_ejemplar, e.codigo_ejemplar, e.estado AS e_estado, e.ubicacion,
                  l.id_libro AS l_id_libro, l.titulo, l.isbn, l.categoria AS l_categoria, l.anio_publicacion,
                  l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                  l.id_editorial, ed.nombre AS nombre_editorial, ed.pais,
                  b.id_bibliotecario, b.legajo, b.turno,
                  u_b.id_usuario AS b_id_usuario, u_b.nombre AS b_nombre, u_b.apellido AS b_apellido, 
                  u_b.dni AS b_dni, u_b.username AS b_username, u_b.password AS b_password, 
                  u_b.email AS b_email, u_b.telefono AS b_telefono, u_b.fecha_registro AS b_fecha_registro
                FROM Historial h
                INNER JOIN Usuario u_s ON h.id_usuario = u_s.id_usuario
                INNER JOIN Socio s ON u_s.id_usuario = s.id_usuario
                LEFT JOIN Prestamo p ON h.id_prestamo = p.id_prestamo
                LEFT JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                LEFT JOIN Libro l ON (h.id_libro = l.id_libro OR (e.id_libro = l.id_libro))
                LEFT JOIN Autor a ON l.id_autor = a.id_autor
                LEFT JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                ORDER BY h.fecha DESC
                """;
        List<Historial> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearHistorial(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error al listar historiales", e);
        }
    }

    @Override
    public void actualizar(Historial historial) throws DAOException {
        String sql = """
                UPDATE Historial 
                SET fecha = ?, tipo_operacion = ?, detalles = ?, id_usuario = ?, id_libro = ?, id_prestamo = ? 
                WHERE id_historial = ?
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(historial.getFecha()));
            ps.setString(2, historial.getTipoOperacion());
            ps.setString(3, historial.getDetalles());
            ps.setInt(4, historial.getSocio().getIdUsuario());

            Integer idLibro = null;
            if (historial.getPrestamo() != null) {
                if (historial.getPrestamo().getEjemplar() != null &&
                        historial.getPrestamo().getEjemplar().getLibro() != null) {
                    idLibro = historial.getPrestamo().getEjemplar().getLibro().getId();
                }
                ps.setInt(6, historial.getPrestamo().getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            if (idLibro != null) {
                ps.setInt(5, idLibro);
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setInt(7, historial.getId());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró el historial con ID " + historial.getId());
            }

        } catch (SQLException e) {
            throw new DAOException("Error al actualizar historial", e);
        }
    }

    @Override
    public void eliminar(int id) throws DAOException {
        String sql = "DELETE FROM Historial WHERE id_historial = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró el historial con ID " + id);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al eliminar historial", e);
        }
    }

    public Historial buscarPorDni(String dni) throws DAOException {
        String sql = """
                SELECT
                  h.id_historial, h.fecha, h.tipo_operacion, h.detalles, h.id_usuario, h.id_libro, h.id_prestamo,
                  s.id_socio, s.id_usuario AS s_id_usuario, s.numero_socio, s.fecha_vencimiento_carnet AS s_fecha_vencimiento_carnet,
                  s.estado AS s_estado, s.tiene_sanciones, s.tiene_atrasos, s.categoria AS s_categoria,
                  u_s.nombre AS s_nombre, u_s.apellido AS s_apellido, u_s.dni AS s_dni, u_s.email AS s_email, u_s.telefono AS s_telefono,
                  u_s.fecha_registro AS s_fecha_registro, u_s.username AS s_username, u_s.password AS s_password, u_s.tipo_usuario AS s_tipo,
                  p.id_prestamo AS p_id_prestamo, p.fecha_prestamo, p.fecha_vencimiento, p.estado AS p_estado, p.dias_prestamo,
                  e.id_ejemplar, e.codigo_ejemplar, e.estado AS e_estado, e.ubicacion,
                  l.id_libro AS l_id_libro, l.titulo, l.isbn, l.categoria AS l_categoria, l.anio_publicacion,
                  l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                  l.id_editorial, ed.nombre AS nombre_editorial, ed.pais,
                  b.id_bibliotecario, b.legajo, b.turno,
                  u_b.id_usuario AS b_id_usuario, u_b.nombre AS b_nombre, u_b.apellido AS b_apellido, 
                  u_b.dni AS b_dni, u_b.username AS b_username, u_b.password AS b_password, 
                  u_b.email AS b_email, u_b.telefono AS b_telefono, u_b.fecha_registro AS b_fecha_registro
                FROM Historial h
                INNER JOIN Usuario u_s ON h.id_usuario = u_s.id_usuario
                INNER JOIN Socio s ON u_s.id_usuario = s.id_usuario
                LEFT JOIN Prestamo p ON h.id_prestamo = p.id_prestamo
                LEFT JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                LEFT JOIN Libro l ON (h.id_libro = l.id_libro OR (e.id_libro = l.id_libro))
                LEFT JOIN Autor a ON l.id_autor = a.id_autor
                LEFT JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                WHERE u_s.dni = ?
                ORDER BY h.fecha DESC
                LIMIT 1
                """;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearHistorial(rs);
                else return null;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar historial por DNI", e);
        }
    }

    public void registrarOperacion(int idUsuario, String tipo, String detalles, Integer idLibro, Integer idPrestamo) throws DAOException {
        String sql = """
                INSERT INTO Historial (fecha, tipo_operacion, detalles, id_usuario, id_libro, id_prestamo) 
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, tipo);
            ps.setString(3, detalles);
            ps.setInt(4, idUsuario);
            if (idLibro != null) ps.setInt(5, idLibro); else ps.setNull(5, Types.INTEGER);
            if (idPrestamo != null) ps.setInt(6, idPrestamo); else ps.setNull(6, Types.INTEGER);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error al registrar operación en historial", e);
        }
    }

    public List<Historial> listarPorTipo(String tipoOperacion) throws DAOException {
        String sql = """
                SELECT
                  h.id_historial, h.fecha, h.tipo_operacion, h.detalles, h.id_usuario, h.id_libro, h.id_prestamo,
                  s.id_socio, s.id_usuario AS s_id_usuario, s.numero_socio, s.fecha_vencimiento_carnet AS s_fecha_vencimiento_carnet,
                  s.estado AS s_estado, s.tiene_sanciones, s.tiene_atrasos, s.categoria AS s_categoria,
                  u_s.nombre AS s_nombre, u_s.apellido AS s_apellido, u_s.dni AS s_dni, u_s.email AS s_email, u_s.telefono AS s_telefono,
                  u_s.fecha_registro AS s_fecha_registro, u_s.username AS s_username, u_s.password AS s_password, u_s.tipo_usuario AS s_tipo,
                  p.id_prestamo AS p_id_prestamo, p.fecha_prestamo, p.fecha_vencimiento, p.estado AS p_estado, p.dias_prestamo,
                  e.id_ejemplar, e.codigo_ejemplar, e.estado AS e_estado, e.ubicacion,
                  l.id_libro AS l_id_libro, l.titulo, l.isbn, l.categoria AS l_categoria, l.anio_publicacion,
                  l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                  l.id_editorial, ed.nombre AS nombre_editorial, ed.pais,
                  b.id_bibliotecario, b.legajo, b.turno,
                  u_b.id_usuario AS b_id_usuario, u_b.nombre AS b_nombre, u_b.apellido AS b_apellido, 
                  u_b.dni AS b_dni, u_b.username AS b_username, u_b.password AS b_password, 
                  u_b.email AS b_email, u_b.telefono AS b_telefono, u_b.fecha_registro AS b_fecha_registro
                FROM Historial h
                INNER JOIN Usuario u_s ON h.id_usuario = u_s.id_usuario
                INNER JOIN Socio s ON u_s.id_usuario = s.id_usuario
                LEFT JOIN Prestamo p ON h.id_prestamo = p.id_prestamo
                LEFT JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                LEFT JOIN Libro l ON (h.id_libro = l.id_libro OR (e.id_libro = l.id_libro))
                LEFT JOIN Autor a ON l.id_autor = a.id_autor
                LEFT JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                WHERE h.tipo_operacion = ?
                ORDER BY h.fecha DESC
                """;
        List<Historial> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipoOperacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearHistorial(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error al listar historial por tipo de operación", e);
        }
    }

    /**
     * Lista el historial por id_usuario (de la tabla Usuario).
     * Para buscar por id_socio, usar listarPorIdSocio().
     */
    public List<Historial> listarPorSocioId(int idUsuario) throws DAOException {
        String sql = """
                SELECT
                  h.id_historial, h.fecha, h.tipo_operacion, h.detalles, h.id_usuario, h.id_libro, h.id_prestamo,
                  s.id_socio, s.id_usuario AS s_id_usuario, s.numero_socio, s.fecha_vencimiento_carnet AS s_fecha_vencimiento_carnet,
                  s.estado AS s_estado, s.tiene_sanciones, s.tiene_atrasos, s.categoria AS s_categoria,
                  u_s.nombre AS s_nombre, u_s.apellido AS s_apellido, u_s.dni AS s_dni, u_s.email AS s_email, u_s.telefono AS s_telefono,
                  u_s.fecha_registro AS s_fecha_registro, u_s.username AS s_username, u_s.password AS s_password, u_s.tipo_usuario AS s_tipo,
                  p.id_prestamo AS p_id_prestamo, p.fecha_prestamo, p.fecha_vencimiento, p.estado AS p_estado, p.dias_prestamo,
                  e.id_ejemplar, e.codigo_ejemplar, e.estado AS e_estado, e.ubicacion,
                  l.id_libro AS l_id_libro, l.titulo, l.isbn, l.categoria AS l_categoria, l.anio_publicacion,
                  l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                  l.id_editorial, ed.nombre AS nombre_editorial, ed.pais,
                  b.id_bibliotecario, b.legajo, b.turno,
                  u_b.id_usuario AS b_id_usuario, u_b.nombre AS b_nombre, u_b.apellido AS b_apellido, 
                  u_b.dni AS b_dni, u_b.username AS b_username, u_b.password AS b_password, 
                  u_b.email AS b_email, u_b.telefono AS b_telefono, u_b.fecha_registro AS b_fecha_registro
                FROM Historial h
                INNER JOIN Usuario u_s ON h.id_usuario = u_s.id_usuario
                INNER JOIN Socio s ON u_s.id_usuario = s.id_usuario
                LEFT JOIN Prestamo p ON h.id_prestamo = p.id_prestamo
                LEFT JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                LEFT JOIN Libro l ON (h.id_libro = l.id_libro OR (e.id_libro = l.id_libro))
                LEFT JOIN Autor a ON l.id_autor = a.id_autor
                LEFT JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                WHERE h.id_usuario = ?
                ORDER BY h.fecha DESC
                """;
        List<Historial> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearHistorial(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error al listar historial por socio", e);
        }
    }

    public List<Historial> listarPorLibroId(int idLibro) throws DAOException {
        String sql = """
                SELECT
                  h.id_historial, h.fecha, h.tipo_operacion, h.detalles, h.id_usuario, h.id_libro, h.id_prestamo,
                  s.id_socio, s.id_usuario AS s_id_usuario, s.numero_socio, s.fecha_vencimiento_carnet AS s_fecha_vencimiento_carnet,
                  s.estado AS s_estado, s.tiene_sanciones, s.tiene_atrasos, s.categoria AS s_categoria,
                  u_s.nombre AS s_nombre, u_s.apellido AS s_apellido, u_s.dni AS s_dni, u_s.email AS s_email, u_s.telefono AS s_telefono,
                  u_s.fecha_registro AS s_fecha_registro, u_s.username AS s_username, u_s.password AS s_password, u_s.tipo_usuario AS s_tipo,
                  p.id_prestamo AS p_id_prestamo, p.fecha_prestamo, p.fecha_vencimiento, p.estado AS p_estado, p.dias_prestamo,
                  e.id_ejemplar, e.codigo_ejemplar, e.estado AS e_estado, e.ubicacion,
                  l.id_libro AS l_id_libro, l.titulo, l.isbn, l.categoria AS l_categoria, l.anio_publicacion,
                  l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                  l.id_editorial, ed.nombre AS nombre_editorial, ed.pais,
                  b.id_bibliotecario, b.legajo, b.turno,
                  u_b.id_usuario AS b_id_usuario, u_b.nombre AS b_nombre, u_b.apellido AS b_apellido, 
                  u_b.dni AS b_dni, u_b.username AS b_username, u_b.password AS b_password, 
                  u_b.email AS b_email, u_b.telefono AS b_telefono, u_b.fecha_registro AS b_fecha_registro
                FROM Historial h
                INNER JOIN Usuario u_s ON h.id_usuario = u_s.id_usuario
                INNER JOIN Socio s ON u_s.id_usuario = s.id_usuario
                LEFT JOIN Prestamo p ON h.id_prestamo = p.id_prestamo
                LEFT JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                LEFT JOIN Libro l ON (h.id_libro = l.id_libro OR (e.id_libro = l.id_libro))
                LEFT JOIN Autor a ON l.id_autor = a.id_autor
                LEFT JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                WHERE h.id_libro = ?
                ORDER BY h.fecha DESC
                """;
        List<Historial> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearHistorial(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error al listar historial por libro", e);
        }
    }

    /**
     * Lista el historial por id_socio (PK de la tabla Socio).
     * Hace JOIN para convertir id_socio a id_usuario.
     */
    public List<Historial> listarPorIdSocio(int idSocio) throws DAOException {
        String sql = """
                SELECT
                  h.id_historial, h.fecha, h.tipo_operacion, h.detalles, h.id_usuario, h.id_libro, h.id_prestamo,
                  s.id_socio, s.id_usuario AS s_id_usuario, s.numero_socio, s.fecha_vencimiento_carnet AS s_fecha_vencimiento_carnet,
                  s.estado AS s_estado, s.tiene_sanciones, s.tiene_atrasos, s.categoria AS s_categoria,
                  u_s.nombre AS s_nombre, u_s.apellido AS s_apellido, u_s.dni AS s_dni, u_s.email AS s_email, u_s.telefono AS s_telefono,
                  u_s.fecha_registro AS s_fecha_registro, u_s.username AS s_username, u_s.password AS s_password, u_s.tipo_usuario AS s_tipo,
                  p.id_prestamo AS p_id_prestamo, p.fecha_prestamo, p.fecha_vencimiento, p.estado AS p_estado, p.dias_prestamo,
                  e.id_ejemplar, e.codigo_ejemplar, e.estado AS e_estado, e.ubicacion,
                  l.id_libro AS l_id_libro, l.titulo, l.isbn, l.categoria AS l_categoria, l.anio_publicacion,
                  l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                  l.id_editorial, ed.nombre AS nombre_editorial, ed.pais,
                  b.id_bibliotecario, b.legajo, b.turno,
                  u_b.id_usuario AS b_id_usuario, u_b.nombre AS b_nombre, u_b.apellido AS b_apellido, 
                  u_b.dni AS b_dni, u_b.username AS b_username, u_b.password AS b_password, 
                  u_b.email AS b_email, u_b.telefono AS b_telefono, u_b.fecha_registro AS b_fecha_registro
                FROM Historial h
                INNER JOIN Usuario u_s ON h.id_usuario = u_s.id_usuario
                INNER JOIN Socio s ON u_s.id_usuario = s.id_usuario
                LEFT JOIN Prestamo p ON h.id_prestamo = p.id_prestamo
                LEFT JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
                LEFT JOIN Libro l ON (h.id_libro = l.id_libro OR (e.id_libro = l.id_libro))
                LEFT JOIN Autor a ON l.id_autor = a.id_autor
                LEFT JOIN Editorial ed ON l.id_editorial = ed.id_editorial
                LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
                LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
                WHERE s.id_socio = ?
                ORDER BY h.fecha DESC
                """;
        List<Historial> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSocio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearHistorial(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error al listar historial por id_socio", e);
        }
    }

    // --- helper corregido (hace JOINs completos) ---
    private Historial mapearHistorial(ResultSet rs) throws SQLException {
        int idHistorial = rs.getInt("id_historial");

        // fecha es DATETIME en la BD
        Timestamp timestamp = rs.getTimestamp("fecha");
        LocalDateTime fecha = timestamp != null
                ? timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();

        String tipoOperacion = rs.getString("tipo_operacion");
        String detalles = rs.getString("detalles");

        // Reconstruir Socio completo
        Socio socio = mapearSocio(rs);

        // Reconstruir Prestamo si existe
        Prestamo prestamo = null;
        Integer idPrestamo = rs.getInt("id_prestamo");
        if (!rs.wasNull() && idPrestamo != null) {
            prestamo = mapearPrestamo(rs);
        }

        return new Historial(idHistorial, socio, prestamo, fecha, tipoOperacion, detalles);
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

    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        Integer idPrestamo = rs.getInt("p_id_prestamo");
        if (rs.wasNull() || idPrestamo == null) {
            return null;
        }

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

        Prestamo prestamo = new Prestamo(idPrestamo, fechaPrestamo, fechaVencimiento, estado, diasPrestamo, socio, ejemplar, null);
        prestamo.setBibliotecario(bibliotecario);
        return prestamo;
    }

    private Ejemplar mapearEjemplar(ResultSet rs) throws SQLException {
        Integer idEj = rs.getInt("id_ejemplar");
        if (rs.wasNull() || idEj == null) {
            return null;
        }

        String cod = rs.getString("codigo_ejemplar");
        String estado = rs.getString("e_estado");
        String ubicacion = rs.getString("ubicacion");
        Libro libro = mapearLibro(rs);
        return new Ejemplar(idEj, cod, estado, ubicacion, libro);
    }

    private Libro mapearLibro(ResultSet rs) throws SQLException {
        Integer idLibro = rs.getInt("l_id_libro");
        if (rs.wasNull() || idLibro == null) {
            return null;
        }

        Libro libro = new Libro(
                idLibro,
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
