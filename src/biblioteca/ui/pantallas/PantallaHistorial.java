package biblioteca.ui.pantallas;

import biblioteca.data.dao.DAOException;
import biblioteca.entities.prestamos.Prestamo;
import biblioteca.entities.reportes.Historial;
import biblioteca.services.ControlConsultas;
import biblioteca.services.ControlHistorial;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Interfaz de usuario para el caso de uso CU11 – Consultar Historial.
 * Permite visualizar historiales de socios o libros, aplicar filtros
 * y generar reportes de los movimientos registrados en el sistema.
 */
public class PantallaHistorial {

    private final ControlConsultas controlConsultas;
    private final ControlHistorial controlHistorial;
    private final Scanner scanner;

    public PantallaHistorial(ControlConsultas controlConsultas, ControlHistorial controlHistorial) {
        this.controlConsultas = controlConsultas;
        this.controlHistorial = controlHistorial;
        this.scanner = new Scanner(System.in);
    }

    /** Muestra el menú principal de la pantalla */
    public void mostrarPantalla() {
        int opcion = -1;
        do {
            System.out.println("\n===== CONSULTA DE HISTORIAL =====");
            System.out.println("1. Consultar historial por socio (DNI)");
            System.out.println("2. Consultar historial por libro (ISBN)");
            System.out.println("3. Ver historial completo del sistema");
            System.out.println("4. Exportar reporte de préstamos");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // limpia buffer
                seleccionarCriterio(opcion);
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Ingrese un número válido.");
                scanner.nextLine();
            }
        } while (opcion != 0);
    }

    /** Gestiona la selección del criterio de consulta */
    private void seleccionarCriterio(int opcion) {
        switch (opcion) {
            case 1 -> consultarHistorialSocio();
            case 2 -> consultarHistorialLibro();
            case 3 -> mostrarHistorialCompleto();
            case 4 -> exportarReporte();
            case 0 -> System.out.println("Regresando al menú principal...");
            default -> System.out.println("Opción no válida.");
        }
    }

    /** Consulta y muestra el historial de un socio por su DNI */
    private void consultarHistorialSocio() {
        System.out.print("Ingrese el DNI del socio: ");
        String dni = scanner.nextLine().trim();

        try {
            List<Prestamo> prestamos = controlHistorial.consultarHistorialUsuario(dni);

            if (prestamos == null || prestamos.isEmpty()) {
                System.out.println("No se encontraron registros de historial para el socio con DNI " + dni);
                return;
            }

            System.out.println("\n=== RESULTADOS DE LA CONSULTA ===");
            for (Prestamo p : prestamos) {
                System.out.printf("Préstamo N°%d | Libro: %s | Ejemplar: %s | Estado: %s | Fecha préstamo: %s | Vence: %s%n",
                        p.getId(),
                        p.getEjemplar().getLibro().getTitulo(),
                        p.getEjemplar().getCodigo(),
                        p.getEstado(),
                        p.getFechaPrestamo(),
                        p.getFechaVencimiento());
            }
        } catch (DAOException e) {
            System.out.println("Error consultando historial del socio: " + e.getMessage());
        }
    }

    /** Consulta y muestra el historial de un libro por su ISBN */
    private void consultarHistorialLibro() {
        System.out.print("Ingrese el ISBN del libro: ");
        String isbn = scanner.nextLine().trim();

        try {
            List<Prestamo> prestamos = controlHistorial.consultarHistorialLibro(isbn);

            if (prestamos == null || prestamos.isEmpty()) {
                System.out.println("No se encontraron registros de historial para el libro con ISBN " + isbn);
                return;
            }

            System.out.println("\n=== HISTORIAL DEL LIBRO ISBN " + isbn + " ===");
            for (Prestamo p : prestamos) {
                System.out.printf("Préstamo N°%d | Socio: %s %s | DNI: %s | Estado: %s | Fecha préstamo: %s | Vence: %s%n",
                        p.getId(),
                        p.getSocio().getNombre(),
                        p.getSocio().getApellido(),
                        p.getSocio().getDni(),
                        p.getEstado(),
                        p.getFechaPrestamo(),
                        p.getFechaVencimiento());
            }
        } catch (DAOException e) {
            System.out.println("Error consultando historial del libro: " + e.getMessage());
        }
    }

    /** Muestra todo el historial del sistema de forma segura */
    /** Muestra todo el historial del sistema de forma segura */
    private void mostrarHistorialCompleto() {
        try {
            // Usamos el método existente de ControlHistorial
            List<Historial> todos = controlHistorial.obtenerHistorialCompletoConDetalles();

            if (todos == null || todos.isEmpty()) {
                System.out.println("No hay registros en el historial del sistema.");
                return;
            }

            System.out.println("\n=== HISTORIAL COMPLETO DEL SISTEMA ===");
            for (Historial h : todos) {
                Prestamo p = h.getPrestamo();
                if (p != null) {
                    System.out.printf(
                            "Préstamo N°%d | Socio: %s %s | Libro: %s | Estado: %s | Fecha préstamo: %s | Vence: %s%n",
                            p.getId(),
                            p.getSocio().getNombre(),
                            p.getSocio().getApellido(),
                            p.getEjemplar().getLibro().getTitulo(),
                            p.getEstado(),
                            p.getFechaPrestamo(),
                            p.getFechaVencimiento()
                    );
                }
            }
        } catch (DAOException e) {
            System.out.println("Error consultando historial completo: " + e.getMessage());
        }
    }


    /** Exporta un reporte textual (simulado en consola) */
    private void exportarReporte() {
        System.out.println("\nGenerando reporte de préstamos...");
        try {
            String reporte = controlConsultas.generarReporte();
            System.out.println(reporte);
            System.out.println("Reporte exportado exitosamente (simulado).");
        } catch (Exception e) {
            System.out.println("Error generando reporte: " + e.getMessage());
        }
    }
}