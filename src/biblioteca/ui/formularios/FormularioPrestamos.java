package biblioteca.ui.formularios;

import biblioteca.data.dao.DAOException;
import biblioteca.entities.inventario.Ejemplar;
import biblioteca.entities.usuarios.Bibliotecario;
import biblioteca.entities.usuarios.Socio;
import biblioteca.entities.prestamos.Prestamo;
import biblioteca.entities.reportes.Comprobante;
import biblioteca.services.ControlComprobantes;
import biblioteca.services.ControlPrestamos;
import biblioteca.services.ControlUsuarios;

import java.util.Scanner;

/**
 * Formulario para el registro de préstamos de ejemplares a socios.
 * Permite ingresar datos del socio, ejemplar y generar el comprobante correspondiente.
 */
public class FormularioPrestamos {

    private final ControlPrestamos controlPrestamos;
    private final ControlComprobantes controlComprobantes;
    private final ControlUsuarios controlUsuarios;
    private final Scanner scanner;
    private Bibliotecario bibliotecarioLogueado;

    public FormularioPrestamos(
            ControlPrestamos controlPrestamos,
            ControlComprobantes controlComprobantes,
            ControlUsuarios controlUsuarios
    ) {
        this.controlPrestamos = controlPrestamos;
        this.controlComprobantes = controlComprobantes;
        this.controlUsuarios = controlUsuarios;
        this.scanner = new Scanner(System.in);
        this.bibliotecarioLogueado = null;
    }

    /**
     * Establece el bibliotecario logueado para usar en los préstamos.
     * Si no se establece, se buscará por ID 1 (comportamiento legacy).
     *
     * @param bibliotecario El bibliotecario logueado en el sistema
     */
    public void setBibliotecarioLogueado(Bibliotecario bibliotecario) {
        this.bibliotecarioLogueado = bibliotecario;
    }

    public void mostrarFormulario() {
        System.out.println("\n=== FORMULARIO DE PRÉSTAMOS ===");

        boolean exito = false;

        while (!exito) {
            try {
                // --- Ingreso de datos ---
                String dniSocio = ingresarDniSocio();
                String codigoEjemplar = ingresarCodigoEjemplar();

                // --- Validaciones y búsquedas ---
                Socio socio = buscarSocio(dniSocio);
                Ejemplar ejemplar = buscarEjemplar(codigoEjemplar);

                // Obtener bibliotecario: usar el logueado si está disponible, sino buscar por ID 1 (legacy)
                Bibliotecario bibliotecario = obtenerBibliotecario();

                // --- Registrar préstamo ---
                Prestamo prestamo = controlPrestamos.registrarPrestamo(socio, ejemplar, bibliotecario);

                // --- Confirmación y comprobante ---
                confirmarPrestamo(prestamo);
                exito = true;

            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                System.out.print("¿Desea intentar nuevamente? (S/N): ");
                String resp = scanner.nextLine().trim();
                if (!resp.equalsIgnoreCase("S")) {
                    System.out.println("Registro de préstamo cancelado.");
                    break;
                }
            }
        }
    }

    private String ingresarDniSocio() {
        System.out.print("Ingrese DNI del socio: ");
        String dni;
        while (true) {
            dni = scanner.nextLine().trim();
            if (!dni.isBlank()) return dni;
            System.out.print("DNI no puede estar vacío. Intente nuevamente: ");
        }
    }

    private String ingresarCodigoEjemplar() {
        System.out.print("Ingrese código del ejemplar: ");
        String codigo;
        while (true) {
            codigo = scanner.nextLine().trim();
            if (!codigo.isBlank()) return codigo;
            System.out.print("Código no puede estar vacío. Intente nuevamente: ");
        }
    }

    private Socio buscarSocio(String dni) throws DAOException {
        var u = controlUsuarios.buscarPorDni(dni);
        if (u == null) throw new IllegalArgumentException("No existe un socio con ese DNI.");
        if (!(u instanceof Socio)) throw new IllegalArgumentException("El usuario no es socio.");
        return (Socio) u;
    }

    private Ejemplar buscarEjemplar(String codigo) throws DAOException {
        Ejemplar e = controlPrestamos.buscarEjemplar(codigo);
        if (e == null) throw new IllegalArgumentException("No existe un ejemplar con ese código.");
        return e;
    }

    /**
     * Obtiene el bibliotecario a usar para el préstamo.
     * Prioriza el bibliotecario logueado, si no está disponible, busca por ID 1 (comportamiento legacy).
     *
     * @return Bibliotecario a usar para el préstamo
     * @throws DAOException Si hay error al buscar el bibliotecario
     */
    private Bibliotecario obtenerBibliotecario() throws DAOException {
        // Si hay un bibliotecario logueado, usarlo
        if (bibliotecarioLogueado != null) {
            return bibliotecarioLogueado;
        }

        // Comportamiento legacy: buscar por ID 1 si no hay bibliotecario logueado
        Bibliotecario b = controlUsuarios.buscarBibliotecarioPorId(1);
        if (b == null) {
            throw new IllegalArgumentException("Bibliotecario no encontrado. Por favor, inicie sesión como bibliotecario.");
        }
        return b;
    }

    private void confirmarPrestamo(Prestamo prestamo) {
        System.out.println("\n=== PRÉSTAMO REGISTRADO ===");
        System.out.println("Socio: " + prestamo.getSocio().getNombreCompleto());
        System.out.println("Ejemplar: " + prestamo.getEjemplar().getCodigo());
        System.out.println("Fecha de vencimiento: " + prestamo.getFechaVencimiento());

        Comprobante comprobante = controlComprobantes.generarComprobante("Préstamo", prestamo);
        controlComprobantes.emitirComprobante(comprobante);

        System.out.println("=== Préstamo finalizado ===");
    }
}


