package biblioteca.entities.usuarios;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Clase base abstracta que representa un usuario del sistema.
 * Alineada con la tabla Usuario de la base de datos, contiene los datos comunes de bibliotecarios y socios.
 */
public abstract class Usuario {

    private int idUsuario;
    private final String dni;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private LocalDate fechaRegistro;
    private final TipoUsuario tipoUsuario;
    private String username;
    private String password;

    public Usuario(int idUsuario, String nombre, String apellido, String dni, String email, String telefono,
                   LocalDate fechaRegistro, TipoUsuario tipoUsuario, String username, String password) {

        if (idUsuario < 0) throw new IllegalArgumentException("El ID del usuario no puede ser negativo.");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre no puede estar vacío.");
        if (apellido == null || apellido.isBlank()) throw new IllegalArgumentException("El apellido no puede estar vacío.");
        if (dni == null || dni.isBlank()) throw new IllegalArgumentException("El DNI no puede estar vacío.");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        if (tipoUsuario == null) throw new IllegalArgumentException("El tipo de usuario no puede ser nulo.");

        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.email = email;
        this.telefono = telefono;
        this.fechaRegistro = (fechaRegistro != null) ? fechaRegistro : LocalDate.now();
        this.tipoUsuario = tipoUsuario;
        this.username = username;
        this.password = password;
    }

    /**
     * Constructor reducido usado cuando solo se conoce id/dni/tipo.
     */
    public Usuario(int idUsuario, String dni, TipoUsuario tipoUsuario) {
        if (idUsuario < 0) throw new IllegalArgumentException("El ID del usuario no puede ser negativo.");
        if (dni == null || dni.isBlank()) throw new IllegalArgumentException("El DNI no puede estar vacío.");
        if (tipoUsuario == null) throw new IllegalArgumentException("El tipo de usuario no puede ser nulo.");

        this.idUsuario = idUsuario;
        this.dni = dni;
        this.tipoUsuario = tipoUsuario;
    }

    public boolean validarCredenciales(String usuarioIngresado, String contraseniaIngresada) {
        if (usuarioIngresado == null || contraseniaIngresada == null) return false;
        return Objects.equals(this.username, usuarioIngresado) &&
                Objects.equals(this.password, contraseniaIngresada);
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    // --- Getters alineados con la BD ---

    /** ID de la tabla Usuario (id_usuario). */
    public int getIdUsuario() { return idUsuario; }

    /** Alias legacy para evitar romper el código existente. */
    public int getId() { return idUsuario; }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }

    /** Fecha de registro en la tabla Usuario (fecha_registro). */
    public LocalDate getFechaRegistro() { return fechaRegistro; }

    /** Alias legacy para mantener compatibilidad con el código que usa fechaAlta. */
    public LocalDate getFechaAlta() { return fechaRegistro; }

    public TipoUsuario getTipoUsuario() { return tipoUsuario; }

    /** username en la tabla Usuario. */
    public String getUsername() { return username; }

    /** Alias legacy para el nombre de usuario. */
    public String getUsuario() { return username; }

    /** password en la tabla Usuario. */
    public String getPassword() { return password; }

    /** Alias legacy para la contraseña. */
    public String getContrasenia() { return password; }

    /**
     * Permite establecer el ID generado por la BD (AUTO_INCREMENT).
     */
    public void setId(int idUsuario) {
        if (idUsuario <= 0) throw new IllegalArgumentException("El ID del usuario debe ser positivo.");
        this.idUsuario = idUsuario;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        if (apellido == null || apellido.isBlank())
            throw new IllegalArgumentException("El apellido no puede estar vacío.");
        this.apellido = apellido;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setUsuario(String usuario) {
        if (usuario == null || usuario.isBlank())
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
        this.username = usuario;
    }

    public void setUsername(String username) {
        setUsuario(username);
    }

    public void setContrasenia(String contrasenia) {
        if (contrasenia == null || contrasenia.isBlank())
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        this.password = contrasenia;
    }

    public void setPassword(String password) {
        setContrasenia(password);
    }

    // --- Métodos abstractos ---
    public abstract String getTipo();

    @Override
    public String toString() {
        return tipoUsuario + ": " + getNombreCompleto() +
                " | DNI: " + dni +
                " | Email: " + email +
                " | Teléfono: " + telefono +
                " | Fecha registro: " + fechaRegistro;
    }

    public abstract void setTipo(TipoUsuario tipoUsuario);

    /** Setter protegido para la fecha de registro, usado por entidades hijas. */
    protected void setFechaAltaInterno(LocalDate fechaAlta) {
        if (fechaAlta != null) this.fechaRegistro = fechaAlta;
    }
}

