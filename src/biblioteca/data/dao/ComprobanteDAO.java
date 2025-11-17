package biblioteca.data.dao;

import biblioteca.data.db.ConexionBD;
import biblioteca.data.interfaces.DAO;
import biblioteca.entities.prestamos.Prestamo;
import biblioteca.entities.reportes.Comprobante;
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
 * DAO para gestionar la persistencia de comprobantes en la base de datos.
 * Maneja las operaciones CRUD sobre la tabla Comprobante, incluyendo JOINs completos con Prestamo y todas sus relaciones.
 */
public class ComprobanteDAO implements DAO<Comprobante> {

    public ComprobanteDAO(PrestamoDAO prestamoDAO) {
    }

    @Override
    public void insertar(Comprobante comprobante) throws DAOException {
        String sql = """
            INSERT INTO Comprobante (fecha_emision, tipo, contenido, id_prestamo)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(comprobante.getFechaEmision().atStartOfDay()));
            ps.setString(2, comprobante.getTipo() != null ? comprobante.getTipo().toUpperCase() : "DIGITAL");
            ps.setString(3, comprobante.getContenido());
            ps.setInt(4, comprobante.getPrestamo().getId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se pudo obtener el ID generado para Comprobante.");
                }
                comprobante.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al insertar comprobante", e);
        }
    }

    @Override
    public Comprobante buscarPorId(int id) throws DAOException {
        String sql = """
            SELECT
              c.id_comprobante, c.fecha_emision, c.tipo, c.contenido, c.id_prestamo,
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
            FROM Comprobante c
            INNER JOIN Prestamo p ON c.id_prestamo = p.id_prestamo
            INNER JOIN Socio s ON p.id_socio = s.id_socio
            INNER JOIN Usuario u_s ON s.id_usuario = u_s.id_usuario
            INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
            INNER JOIN Libro l ON e.id_libro = l.id_libro
            INNER JOIN Autor a ON l.id_autor = a.id_autor
            INNER JOIN Editorial ed ON l.id_editorial = ed.id_editorial
            LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
            LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
            WHERE c.id_comprobante = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearComprobante(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar comprobante por ID", e);
        }
    }

    @Override
    public List<Comprobante> listarTodos() throws DAOException {
        String sql = """
            SELECT
              c.id_comprobante, c.fecha_emision, c.tipo, c.contenido, c.id_prestamo,
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
            FROM Comprobante c
            INNER JOIN Prestamo p ON c.id_prestamo = p.id_prestamo
            INNER JOIN Socio s ON p.id_socio = s.id_socio
            INNER JOIN Usuario u_s ON s.id_usuario = u_s.id_usuario
            INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
            INNER JOIN Libro l ON e.id_libro = l.id_libro
            INNER JOIN Autor a ON l.id_autor = a.id_autor
            INNER JOIN Editorial ed ON l.id_editorial = ed.id_editorial
            LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
            LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
            ORDER BY c.id_comprobante DESC
        """;
        List<Comprobante> comprobantes = new ArrayList<>();

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                comprobantes.add(mapearComprobante(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Error al listar comprobantes", e);
        }

        return comprobantes;
    }

    @Override
    public void actualizar(Comprobante comprobante) throws DAOException {
        String sql = """
            UPDATE Comprobante
            SET tipo = ?, contenido = ?, fecha_emision = ?
            WHERE id_comprobante = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, comprobante.getTipo() != null ? comprobante.getTipo().toUpperCase() : "DIGITAL");
            ps.setString(2, comprobante.getContenido());
            ps.setTimestamp(3, Timestamp.valueOf(comprobante.getFechaEmision().atStartOfDay()));
            ps.setInt(4, comprobante.getId());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró el comprobante con ID " + comprobante.getId());
            }

        } catch (SQLException e) {
            throw new DAOException("Error al actualizar comprobante", e);
        }
    }

    @Override
    public void eliminar(int id) throws DAOException {
        String sql = "DELETE FROM Comprobante WHERE id_comprobante = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró el comprobante con ID " + id);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al eliminar comprobante", e);
        }
    }

    public Comprobante buscarPorPrestamoId(int idPrestamo) throws DAOException {
        String sql = """
            SELECT
              c.id_comprobante, c.fecha_emision, c.tipo, c.contenido, c.id_prestamo,
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
            FROM Comprobante c
            INNER JOIN Prestamo p ON c.id_prestamo = p.id_prestamo
            INNER JOIN Socio s ON p.id_socio = s.id_socio
            INNER JOIN Usuario u_s ON s.id_usuario = u_s.id_usuario
            INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar
            INNER JOIN Libro l ON e.id_libro = l.id_libro
            INNER JOIN Autor a ON l.id_autor = a.id_autor
            INNER JOIN Editorial ed ON l.id_editorial = ed.id_editorial
            LEFT JOIN Bibliotecario b ON p.id_bibliotecario = b.id_bibliotecario
            LEFT JOIN Usuario u_b ON b.id_usuario = u_b.id_usuario
            WHERE c.id_prestamo = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearComprobante(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar comprobante por id_prestamo", e);
        }
    }

    private Comprobante mapearComprobante(ResultSet rs) throws SQLException {
        int idComprobante = rs.getInt("id_comprobante");

        Timestamp timestamp = rs.getTimestamp("fecha_emision");
        LocalDate fechaEmision = timestamp != null
                ? timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();

        String tipo = rs.getString("tipo");
        String contenido = rs.getString("contenido");

        Prestamo prestamo = mapearPrestamo(rs);

        return new Comprobante(idComprobante, fechaEmision, tipo, contenido, prestamo);
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
