package biblioteca.ui.formularios;

import biblioteca.data.dao.DAOException;
import biblioteca.entities.prestamos.Devolucion;
import biblioteca.entities.prestamos.Prestamo;
import biblioteca.services.ControlDevoluciones;

import java.util.Scanner;

/**
 * Formulario para el registro de devoluciones de préstamos.
 * Permite ingresar datos del préstamo, estado del ejemplar y observaciones.
 */
public class FormularioDevolucion {

    private final ControlDevoluciones controlDevoluciones;
    private final Scanner scanner;

    public FormularioDevolucion(ControlDevoluciones controlDevoluciones) {
        this.controlDevoluciones = controlDevoluciones;
        this.scanner = new Scanner(System.in);
    }

    public void mostrarFormulario() {
        System.out.println("\n=== REGISTRO DE DEVOLUCIÓN ===");

        try {
            Prestamo prestamo = solicitarPrestamo();

            String estadoEjemplar = solicitarEstado();
            String observaciones = solicitarObservaciones();

            System.out.print("¿Confirmar devolución? (S/N): ");
            String resp = scanner.nextLine().trim();
            if (!resp.equalsIgnoreCase("S")) {
                System.out.println("Devolución cancelada por el usuario.");
                return;
            }

            Devolucion devolucion = controlDevoluciones.registrarDevolucion(
                    prestamo.getId(),
                    estadoEjemplar,
                    observaciones
            );

            mostrarConfirmacion(devolucion);

        } catch (NumberFormatException e) {
            mostrarError("ID de préstamo inválido. Debe ser un número entero.");
        } catch (Exception e) {
            mostrarError("Error durante la devolución: " + e.getMessage());
        }
    }

    private Prestamo solicitarPrestamo() {
        while (true) {
            try {
                System.out.print("Ingrese ID del préstamo: ");
                int id = Integer.parseInt(scanner.nextLine().trim());

                Prestamo prestamo = controlDevoluciones.buscarYValidarPrestamoParaDevolucion(id);
                return prestamo;

            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Debe ingresar un número entero.");
            } catch (DAOException e) {
                System.out.println("Error: " + e.getMessage() + " Intente nuevamente.");
            }
        }
    }

    private String solicitarEstado() {
        System.out.println("Ingrese estado final del ejemplar (Disponible, Dañado, Extraviado):");
        while (true) {
            String estado = scanner.nextLine().trim();
            if (estado.isBlank()) estado = "Disponible";
            if (estado.equalsIgnoreCase("Disponible") ||
                    estado.equalsIgnoreCase("Dañado") ||
                    estado.equalsIgnoreCase("Extraviado")) {
                return estado;
            }
            System.out.println("Estado inválido. Debe ser Disponible, Dañado o Extraviado.");
        }
    }

    private String solicitarObservaciones() {
        System.out.print("Ingrese observaciones (opcional): ");
        return scanner.nextLine().trim();
    }

    private void mostrarConfirmacion(Devolucion devolucion) {
        System.out.println("Devolución registrada correctamente:");
        System.out.println(devolucion.formatearParaUI());
    }

    private void mostrarError(String mensaje) {
        System.out.println("ERROR: " + mensaje);
    }
}