package biblioteca.entities.usuarios;

import biblioteca.entities.prestamos.Prestamo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un socio de la biblioteca.
 * Alineada con las tablas Usuario y Socio de la base de datos, extiende Usuario y agrega información específica del socio.
 */
public class Socio extends Usuario {
    private int idSocio;

    private String numeroSocio;
    private LocalDate fechaVencimientoCarnet;
    private String estado;
    private boolean tieneSanciones;
    private boolean tieneAtrasos;
    private List<Prestamo> prestamos;
    private String categoria;

    public Socio(int id, String nombre, String apellido, String dni, String email, String telefono,
                 LocalDate fechaAlta, TipoUsuario tipo, String usuario, String contrasenia,
                 String numeroSocio, LocalDate fechaVencimientoCarnet, String estado,
                 boolean tieneSanciones, boolean tieneAtrasos, String categoria) {
        super(id, nombre, apellido, dni, email, telefono, fechaAlta, tipo, usuario, contrasenia);
        this.numeroSocio = numeroSocio;
        this.fechaVencimientoCarnet = fechaVencimientoCarnet;
        this.estado = estado;
        this.tieneSanciones = tieneSanciones;
        this.tieneAtrasos = tieneAtrasos;
        this.prestamos = new ArrayList<>();
        setCategoria(categoria);
    }

    // Constructor simplificado (id corresponde actualmente al id_usuario)
    public Socio(int id, String nombre, String apellido, String dni, String email, String telefono,
                 LocalDate fechaAlta, TipoUsuario tipo, String usuario, String contrasenia,
                 String numeroSocio, LocalDate fechaVencimientoCarnet, String estado,
                 boolean tieneSanciones, boolean tieneAtrasos) {
        this(id, nombre, apellido, dni, email, telefono, fechaAlta, tipo, usuario, contrasenia,
                numeroSocio, fechaVencimientoCarnet, estado, tieneSanciones, tieneAtrasos, "Estándar");
    }

    // Constructor para UI (nuevo socio sin IDs aún)
    public Socio(String nombre, String apellido, String dni, String email, String telefono,
                 String usuario, String contrasenia, String categoria) {
        super(0, nombre, apellido, dni, email, telefono, LocalDate.now(), TipoUsuario.SOCIO, usuario, contrasenia);
        this.idSocio = 0;
        this.numeroSocio = null;
        this.fechaVencimientoCarnet = null;
        this.estado = null;
        this.tieneSanciones = false;
        this.tieneAtrasos = false;
        this.prestamos = new ArrayList<>();
        setCategoria(categoria);
    }

    // === Setters accesibles ===
    public void setIdSocio(int idSocio) { this.idSocio = idSocio; }

    public void setNumeroSocio(String numeroSocio) { this.numeroSocio = numeroSocio; }
    public void setFechaVencimientoCarnet(LocalDate fechaVencimientoCarnet) { this.fechaVencimientoCarnet = fechaVencimientoCarnet; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setTieneSanciones(boolean tieneSanciones) { this.tieneSanciones = tieneSanciones; }
    public void setTieneAtrasos(boolean tieneAtrasos) { this.tieneAtrasos = tieneAtrasos; }
    public void setCategoria(String categoria) {
        // Si es null o vacío, usar 'Estándar' como valor por defecto
        this.categoria = (categoria == null || categoria.isBlank()) ? "Estándar" : categoria;
    }
    public void setFechaAlta(LocalDate fechaAlta) {
        setFechaAltaInterno(fechaAlta);
    }

    /**
     * Setter alineado con la BD (fecha_registro).
     * Este es el método preferido para establecer la fecha de registro.
     */
    public void setFechaRegistro(LocalDate fechaRegistro) {
        setFechaAltaInterno(fechaRegistro);
    }

    // === Getters ===
    public int getIdSocio() { return idSocio; }
    public String getNumeroSocio() { return numeroSocio; }
    public LocalDate getFechaVencimientoCarnet() { return fechaVencimientoCarnet; }
    public String getEstado() { return estado; }
    public boolean isTieneSanciones() { return tieneSanciones; }
    public boolean isTieneAtrasos() { return tieneAtrasos; }
    public List<Prestamo> getPrestamos() { return prestamos; }
    public String getCategoria() { return categoria; }

    // === Otros métodos ===
    public void renovarCarnet(int mesesExtra) {
        if (mesesExtra <= 0) throw new IllegalArgumentException("El número de meses debe ser mayor a cero.");
        if (fechaVencimientoCarnet != null) fechaVencimientoCarnet = fechaVencimientoCarnet.plusMonths(mesesExtra);
    }

    public boolean verificarVigencia() {
        return fechaVencimientoCarnet != null && !LocalDate.now().isAfter(fechaVencimientoCarnet);
    }

    public List<Prestamo> obtenerPrestamosActivos() {
        List<Prestamo> activos = new ArrayList<>();
        if (prestamos == null) return activos;
        for (Prestamo p : prestamos) {
            if (p != null && p.getEstado() != null && !p.getEstado().equalsIgnoreCase("Devuelto")) activos.add(p);
        }
        return activos;
    }

    public boolean verificarHabilitacion() {
        return estado != null && verificarVigencia() && !tieneSanciones && !tieneAtrasos && estado.equalsIgnoreCase("Activo");
    }

    public void suspender() { this.estado = "Suspendido"; }
    public void activar() { this.estado = "Activo"; this.tieneSanciones = false; this.tieneAtrasos = false; }

    public void agregarPrestamo(Prestamo prestamo) {
        if (prestamo == null) throw new IllegalArgumentException("El préstamo no puede ser nulo.");
        for (Prestamo p : prestamos) {
            if (p.getEjemplar().getCodigo().equals(prestamo.getEjemplar().getCodigo()))
                throw new IllegalStateException("El ejemplar ya fue prestado a este socio.");
        }
        prestamos.add(prestamo);
    }

    @Override
    public String getTipo() { return "Socio"; }
    @Override
    public void setTipo(TipoUsuario tipoUsuario) {
        // No hace nada, el tipo siempre es SOCIO
    }


    @Override
    public String toString() {
        return super.toString() + " | N° Socio: " + numeroSocio + " | Estado: " + estado +
                " | Categoría: " + (categoria != null ? categoria : "Estándar") +
                " | Vence: " + fechaVencimientoCarnet;
    }
}
