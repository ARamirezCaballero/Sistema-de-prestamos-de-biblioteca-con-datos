package biblioteca.ui.componentes;

import biblioteca.entities.prestamos.Devolucion;

/**
 * Simula la visualización y emisión de comprobantes al registrar una devolución.
 * Pertenece a la capa de Frontera/UI y cumple con RFS10 (Registro de Devolución).
 */
public class VisorComprobante {

    private Devolucion devolucionActual;

    public VisorComprobante() {
        // Constructor simple, no necesita control de negocio
    }

    /** Muestra la devolución en consola */
    public void mostrarComprobante(Devolucion devolucion) {
        if (devolucion == null) {
            System.out.println("No hay comprobante para mostrar.");
            return;
        }
        this.devolucionActual = devolucion;
        System.out.println("=== VISUALIZANDO COMPROBANTE DE DEVOLUCIÓN ===");
        System.out.println(devolucion.formatearParaUI());
    }

    /** Simula la impresión del comprobante */
    public void imprimirComprobante() {
        if (devolucionActual == null) {
            System.out.println("No hay comprobante seleccionado para imprimir.");
            return;
        }
        System.out.println("=== IMPRESIÓN DE COMPROBANTE ===");
        System.out.println(devolucionActual.formatearParaUI());
    }

    /** Cierra el visor del comprobante */
    public void cerrar() {
        if (devolucionActual != null) {
            System.out.println("Cerrando visor del comprobante de devolución #" + devolucionActual.getId());
            devolucionActual = null;
        } else {
            System.out.println("No hay comprobante abierto para cerrar.");
        }
    }
}