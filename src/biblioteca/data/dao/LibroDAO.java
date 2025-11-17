package biblioteca.data.dao;

import biblioteca.entities.inventario.Libro;
import biblioteca.entities.inventario.Ejemplar;
import biblioteca.data.db.ConexionBD;

import java.sql.*;
import java.util.*;

/**
 * DAO para gestionar la persistencia de libros en la base de datos.
 * Maneja las operaciones CRUD sobre la tabla Libro, integrando Autor y Editorial mediante JOINs.
 */
public class LibroDAO implements biblioteca.data.interfaces.DAO<Libro> {

    private final AutorDAO autorDAO = new AutorDAO();
    private final EditorialDAO editorialDAO = new EditorialDAO();

    @Override
    public void insertar(Libro libro) throws DAOException {
        if (libro == null) throw new IllegalArgumentException("Libro no puede ser null");

        try (Connection conn = ConexionBD.getConexion()) {
            conn.setAutoCommit(false);

            try {
                int idAutor;
                if (libro.getIdAutor() > 0) {
                    idAutor = libro.getIdAutor();
                } else {
                    idAutor = autorDAO.obtenerIdPorNombre(conn, libro.getAutor());
                    if (idAutor == -1) {
                        idAutor = autorDAO.insertarAutor(conn, libro.getAutor());
                    }
                    libro.setIdAutor(idAutor);
                }

                int idEditorial;
                if (libro.getIdEditorial() > 0) {
                    idEditorial = libro.getIdEditorial();
                } else {
                    idEditorial = editorialDAO.obtenerIdPorNombre(conn, libro.getEditorial());
                    if (idEditorial == -1) {
                        idEditorial = editorialDAO.insertarEditorial(conn, libro.getEditorial());
                    }
                    libro.setIdEditorial(idEditorial);
                }
                String sql = """
                    INSERT INTO Libro (titulo, id_autor, isbn, categoria, id_editorial, anio_publicacion)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, libro.getTitulo());
                    ps.setInt(2, idAutor);
                    ps.setString(3, libro.getIsbn());
                    ps.setString(4, libro.getCategoria());
                    ps.setInt(5, idEditorial);
                    ps.setInt(6, libro.getAnioPublicacion());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("No se pudo obtener el ID generado para Libro.");
                        }
                        libro.setId(rs.getInt(1));
                    }
                }

                conn.commit();

            } catch (SQLException | DAOException e) {
                conn.rollback();
                throw new DAOException("Error al insertar libro: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al insertar libro: " + e.getMessage(), e);
        }
    }

    @Override
    public Libro buscarPorId(int id) throws DAOException {
        String sql = """
            SELECT 
                l.id_libro, l.titulo, l.isbn, l.categoria, l.anio_publicacion,
                l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                l.id_editorial, e.nombre AS nombre_editorial, e.pais,
                ej.id_ejemplar, ej.codigo_ejemplar, ej.estado, ej.ubicacion
            FROM Libro l
            INNER JOIN Autor a ON l.id_autor = a.id_autor
            INNER JOIN Editorial e ON l.id_editorial = e.id_editorial
            LEFT JOIN Ejemplar ej ON l.id_libro = ej.id_libro
            WHERE l.id_libro = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                Libro libro = null;
                while (rs.next()) {
                    if (libro == null) {
                        libro = mapearLibro(rs);
                    }
                    Ejemplar ej = mapearEjemplar(rs);
                    if (ej != null) {
                        libro.agregarEjemplar(ej);
                    }
                }
                return libro;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al buscar libro por ID", e);
        }
    }

    @Override
    public List<Libro> listarTodos() throws DAOException {
        String sql = """
            SELECT 
                l.id_libro, l.titulo, l.isbn, l.categoria, l.anio_publicacion,
                l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                l.id_editorial, e.nombre AS nombre_editorial, e.pais,
                ej.id_ejemplar, ej.codigo_ejemplar, ej.estado, ej.ubicacion
            FROM Libro l
            INNER JOIN Autor a ON l.id_autor = a.id_autor
            INNER JOIN Editorial e ON l.id_editorial = e.id_editorial
            LEFT JOIN Ejemplar ej ON l.id_libro = ej.id_libro
            ORDER BY l.id_libro
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<Integer, Libro> map = new LinkedHashMap<>();

            while (rs.next()) {
                int id = rs.getInt("id_libro");
                Libro libro = map.get(id);

                if (libro == null) {
                    libro = mapearLibro(rs);
                    map.put(id, libro);
                }

                Ejemplar ej = mapearEjemplar(rs);
                if (ej != null) {
                    libro.agregarEjemplar(ej);
                }
            }

            return new ArrayList<>(map.values());

        } catch (SQLException e) {
            throw new DAOException("Error al listar todos los libros", e);
        }
    }

    @Override
    public void actualizar(Libro libro) throws DAOException {
        if (libro == null) throw new IllegalArgumentException("Libro no puede ser null");

        try (Connection conn = ConexionBD.getConexion()) {
            conn.setAutoCommit(false);

            try {
                // 1. Obtener o crear Autor si es necesario (usando métodos de compatibilidad que aceptan Connection)
                int idAutor = libro.getIdAutor();
                if (idAutor <= 0 && libro.getAutor() != null) {
                    idAutor = autorDAO.obtenerIdPorNombre(conn, libro.getAutor());
                    if (idAutor == -1) {
                        // Crear nuevo autor
                        idAutor = autorDAO.insertarAutor(conn, libro.getAutor());
                    }
                    libro.setIdAutor(idAutor);
                }

                // 2. Obtener o crear Editorial si es necesario (usando métodos de compatibilidad que aceptan Connection)
                int idEditorial = libro.getIdEditorial();
                if (idEditorial <= 0 && libro.getEditorial() != null) {
                    idEditorial = editorialDAO.obtenerIdPorNombre(conn, libro.getEditorial());
                    if (idEditorial == -1) {
                        // Crear nueva editorial
                        idEditorial = editorialDAO.insertarEditorial(conn, libro.getEditorial());
                    }
                    libro.setIdEditorial(idEditorial);
                }

                // 3. Actualizar Libro
                String sql = """
                    UPDATE Libro 
                    SET titulo = ?, id_autor = ?, categoria = ?, id_editorial = ?, anio_publicacion = ?
                    WHERE id_libro = ?
                """;

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, libro.getTitulo());
                    ps.setInt(2, idAutor);
                    ps.setString(3, libro.getCategoria());
                    ps.setInt(4, idEditorial);
                    ps.setInt(5, libro.getAnioPublicacion());
                    ps.setInt(6, libro.getId());
                    ps.executeUpdate();
                }

                conn.commit();

            } catch (SQLException | DAOException e) {
                conn.rollback();
                throw new DAOException("Error al actualizar libro", e);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al actualizar libro", e);
        }
    }

    @Override
    public void eliminar(int id) throws DAOException {
        String sql = "DELETE FROM Libro WHERE id_libro = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontró el libro con ID " + id);
            }

        } catch (SQLException e) {
            throw new DAOException("Error al eliminar libro", e);
        }
    }

    public Libro obtenerPorTitulo(String titulo) throws DAOException {
        String sql = """
            SELECT 
                l.id_libro, l.titulo, l.isbn, l.categoria, l.anio_publicacion,
                l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                l.id_editorial, e.nombre AS nombre_editorial, e.pais,
                ej.id_ejemplar, ej.codigo_ejemplar, ej.estado, ej.ubicacion
            FROM Libro l
            INNER JOIN Autor a ON l.id_autor = a.id_autor
            INNER JOIN Editorial e ON l.id_editorial = e.id_editorial
            LEFT JOIN Ejemplar ej ON l.id_libro = ej.id_libro
            WHERE l.titulo = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, titulo);

            try (ResultSet rs = ps.executeQuery()) {
                Libro libro = null;
                while (rs.next()) {
                    if (libro == null) {
                        libro = mapearLibro(rs);
                    }
                    Ejemplar ej = mapearEjemplar(rs);
                    if (ej != null) {
                        libro.agregarEjemplar(ej);
                    }
                }
                return libro;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al obtener libro por título", e);
        }
    }

    public Libro obtenerPorISBN(String isbn) throws DAOException {
        String sql = """
            SELECT 
                l.id_libro, l.titulo, l.isbn, l.categoria, l.anio_publicacion,
                l.id_autor, a.nombre_completo AS nombre_autor, a.nacionalidad, a.fecha_nacimiento,
                l.id_editorial, e.nombre AS nombre_editorial, e.pais,
                ej.id_ejemplar, ej.codigo_ejemplar, ej.estado, ej.ubicacion
            FROM Libro l
            INNER JOIN Autor a ON l.id_autor = a.id_autor
            INNER JOIN Editorial e ON l.id_editorial = e.id_editorial
            LEFT JOIN Ejemplar ej ON l.id_libro = ej.id_libro
            WHERE l.isbn = ?
        """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);

            try (ResultSet rs = ps.executeQuery()) {
                Libro libro = null;
                while (rs.next()) {
                    if (libro == null) {
                        libro = mapearLibro(rs);
                    }
                    Ejemplar ej = mapearEjemplar(rs);
                    if (ej != null) {
                        libro.agregarEjemplar(ej);
                    }
                }
                return libro;
            }

        } catch (SQLException e) {
            throw new DAOException("Error al obtener libro por ISBN", e);
        }
    }

    // -------------------------
    // MÉTODOS AUXILIARES
    // -------------------------

    /**
     * Mapea un registro SQL a un objeto Libro.
     * Incluye datos de Autor y Editorial mediante JOINs.
     */
    private Libro mapearLibro(ResultSet rs) throws SQLException {
        Libro libro = new Libro(
                rs.getInt("id_libro"),
                rs.getString("titulo"),
                rs.getString("nombre_autor"), // nombre del autor para compatibilidad
                rs.getString("isbn"),
                rs.getString("categoria"),
                rs.getString("nombre_editorial"), // nombre de la editorial para compatibilidad
                rs.getInt("anio_publicacion")
        );

        // Asignar IDs de Autor y Editorial
        libro.setIdAutor(rs.getInt("id_autor"));
        libro.setIdEditorial(rs.getInt("id_editorial"));

        return libro;
    }

    /**
     * Mapea un registro SQL a un objeto Ejemplar.
     * Retorna null si no hay ejemplar (LEFT JOIN).
     */
    private Ejemplar mapearEjemplar(ResultSet rs) throws SQLException {
        int idEj = rs.getInt("id_ejemplar");
        if (rs.wasNull()) return null;

        // Reconstruir el Libro básico para el Ejemplar
        Libro libro = new Libro(
                rs.getInt("id_libro"),
                rs.getString("titulo"),
                rs.getString("nombre_autor"),
                rs.getString("isbn"),
                rs.getString("categoria"),
                rs.getString("nombre_editorial"),
                rs.getInt("anio_publicacion")
        );
        libro.setIdAutor(rs.getInt("id_autor"));
        libro.setIdEditorial(rs.getInt("id_editorial"));

        return new Ejemplar(
                idEj,
                rs.getString("codigo_ejemplar"),
                rs.getString("estado"),
                rs.getString("ubicacion"),
                libro
        );
    }
}
