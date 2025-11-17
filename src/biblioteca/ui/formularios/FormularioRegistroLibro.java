package biblioteca.ui.formularios;

import biblioteca.entities.inventario.Libro;
import biblioteca.services.ControlLibros;
import biblioteca.data.dao.DAOException;

import java.util.Scanner;

/**
 * Formulario para el registro de nuevos libros en el catálogo.
 * Permite ingresar datos del libro y crear ejemplares asociados.
 */
public class FormularioRegistroLibro {

    private final ControlLibros controlLibros;
    private final Scanner scanner;

    private Libro libroTemporal;
    private int cantidadEjemplares;

    public FormularioRegistroLibro(ControlLibros controlLibros) {
        this.controlLibros = controlLibros;
        this.scanner = new Scanner(System.in);
    }

    public void mostrarFormulario() {
        System.out.println("\n=== REGISTRO DE NUEVO LIBRO ===");
        try {
            ingresarDatos();
            ingresarCantidadEjemplares();
            confirmarRegistro();
        } catch (Exception e) {
            mostrarError("Error durante el registro: " + e.getMessage());
        }
    }

    /** Solicita los datos básicos del libro */
    private void ingresarDatos() {
        System.out.print("Título: ");
        String titulo = leerNoVacio("Título");

        System.out.print("Autor: ");
        String autor = leerNoVacio("Autor");

        System.out.print("ISBN: ");
        String isbn = leerNoVacio("ISBN");

        System.out.print("Categoría: ");
        String categoria = leerNoVacio("Categoría");

        System.out.print("Editorial: ");
        String editorial = leerNoVacio("Editorial");

        int anio = leerEntero("Año de publicación");

        // ID se asigna automáticamente por el DAO
        libroTemporal = new Libro(0, titulo, autor, isbn, categoria, editorial, anio);

        System.out.println("\nResumen del libro ingresado:");
        System.out.println(libroTemporal);
    }

    /** Solicita cantidad de ejemplares */
    private void ingresarCantidadEjemplares() {
        while (true) {
            cantidadEjemplares = leerEntero("Cantidad de ejemplares");
            if (cantidadEjemplares > 0) break;
            System.out.println("La cantidad de ejemplares debe ser mayor a 0. Intente nuevamente.");
        }
    }

    /** Confirmación final → registro + creación de ejemplares */
    private void confirmarRegistro() {
        System.out.print("¿Desea confirmar el registro de este libro? (S/N): ");
        String respuesta = scanner.nextLine().trim();

        if (!respuesta.equalsIgnoreCase("S")) {
            System.out.println("Registro cancelado por el usuario.");
            libroTemporal = null;
            return;
        }

        try {
            // 1. Registrar libro en la BD
            controlLibros.registrarLibro(libroTemporal);

            // 2. <<< Recargar el libro con su ID real >>>
            Libro libroBD = controlLibros.buscarLibroPorISBN(libroTemporal.getIsbn());
            if (libroBD == null) {
                mostrarError("El libro se registró, pero no se pudo recuperar desde la BD.");
                return;
            }

            // 3. Crear ejemplares usando el ID real
            controlLibros.crearEjemplares(
                    libroBD.getId(),
                    cantidadEjemplares,
                    "Disponible",
                    "Depósito"
            );

            System.out.println("Libro y ejemplares registrados exitosamente.");

        } catch (DAOException e) {
            mostrarError("No se pudo completar el registro: " + e.getMessage());
        }

        libroTemporal = null;
    }

    /** Valida que la entrada no esté vacía */
    private String leerNoVacio(String campo) {
        String valor;
        while (true) {
            valor = scanner.nextLine().trim();
            if (!valor.isBlank()) return valor;
            System.out.print(campo + " no puede estar vacío. Ingrese nuevamente: ");
        }
    }

    /** Lee un número entero con validación */
    private int leerEntero(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje + ": ");
                int valor = Integer.parseInt(scanner.nextLine().trim());
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Ingrese un número entero.");
            }
        }
    }

    /** Muestra mensaje de error */
    private void mostrarError(String mensaje) {
        System.out.println("ERROR: " + mensaje);
    }
}