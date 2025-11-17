package biblioteca.ui.formularios;

import biblioteca.data.dao.DAOException;
import biblioteca.entities.usuarios.Socio;
import biblioteca.services.ControlUsuarios;
import biblioteca.services.ControlValidaciones;

import java.util.Scanner;

/**
 * Formulario para el registro de nuevos socios en el sistema.
 * Permite ingresar datos personales y de categoría del socio.
 */
public class FormularioRegistroSocio {

    private final ControlUsuarios controlUsuarios;
    private final ControlValidaciones controlValidaciones;
    private final Scanner scanner;

    private Socio socioTemporal;

    public FormularioRegistroSocio(ControlUsuarios controlUsuarios, ControlValidaciones controlValidaciones) {
        this.controlUsuarios = controlUsuarios;
        this.controlValidaciones = controlValidaciones;
        this.scanner = new Scanner(System.in);
    }

    /** Flujo principal del formulario */
    public void mostrarFormulario() {
        System.out.println("\n=== REGISTRO DE NUEVO SOCIO ===");
        try {
            ingresarDatos();
            if (validarDatos()) {
                confirmarRegistro();
            } else {
                mostrarError("Los datos del socio no son válidos. Registro cancelado.");
            }
        } catch (Exception e) {
            mostrarError("Error durante el registro: " + e.getMessage());
        }
    }

    /** Solicita los datos personales y credenciales del nuevo socio */
    private void ingresarDatos() {
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine().trim();

        System.out.print("Apellido: ");
        String apellido = scanner.nextLine().trim();

        System.out.print("DNI: ");
        String dni = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Teléfono: ");
        String telefono = scanner.nextLine().trim();

        String categoria;
        while (true) {
            System.out.print("Categoría (Estándar / Estudiante / Docente): ");
            categoria = scanner.nextLine().trim();
            // Si está vacío, dejar que el DAO use el default "Estándar"
            // El setter de Socio también maneja el default
            if (categoria.isBlank()) categoria = "Estándar";
            if (categoria.equalsIgnoreCase("Estándar") || categoria.equalsIgnoreCase("Estandar") ||
                    categoria.equalsIgnoreCase("Estudiante") ||
                    categoria.equalsIgnoreCase("Docente")) break;
            System.out.println("Categoría inválida. Intente nuevamente.");
        }

        System.out.print("Nombre de usuario: ");
        String usuario = scanner.nextLine().trim();

        System.out.print("Contraseña: ");
        String contrasenia = scanner.nextLine().trim();

        socioTemporal = new Socio(
                nombre,
                apellido,
                dni,
                email,
                telefono,
                usuario,
                contrasenia,
                categoria
        );
    }

    /** Valida los datos del socio usando ControlValidaciones */
    private boolean validarDatos() {
        if (socioTemporal == null) {
            mostrarError("No se han ingresado datos del socio.");
            return false;
        }
        return controlValidaciones.validarDatosUsuario(socioTemporal);
    }

    /** Confirma y ejecuta el registro */
    private void confirmarRegistro() {
        if (socioTemporal == null) {
            mostrarError("No hay datos para registrar.");
            return;
        }

        System.out.print("¿Desea confirmar el registro de este socio? (S/N): ");
        String respuesta = scanner.nextLine().trim();

        if (!respuesta.equalsIgnoreCase("S")) {
            System.out.println("Registro cancelado por el usuario.");
            socioTemporal = null;
            return;
        }

        try {
            // Delegar registro a ControlUsuarios
            controlUsuarios.registrarSocio(socioTemporal);
            System.out.println("Socio registrado exitosamente: " + socioTemporal.getNombreCompleto());
        } catch (DAOException e) {
            mostrarError("No se pudo registrar el socio: " + e.getMessage());
        } finally {
            socioTemporal = null; // Limpiar datos temporales
        }
    }

    private void mostrarError(String mensaje) {
        System.out.println("ERROR: " + mensaje);
    }
}


