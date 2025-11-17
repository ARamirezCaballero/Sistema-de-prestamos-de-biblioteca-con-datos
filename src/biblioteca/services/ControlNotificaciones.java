package biblioteca.services;

import biblioteca.data.dao.DAOException;
import biblioteca.data.dao.NotificacionesDAO;
import biblioteca.data.dao.PrestamoDAO;
import biblioteca.entities.notificaciones.Notificacion;
import biblioteca.entities.prestamos.Prestamo;
import biblioteca.ui.componentes.NotificadorEmail;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Controlador de servicios para la gestión de notificaciones del sistema.
 * Coordina la generación de notificaciones por vencimientos y el envío de notificaciones a usuarios.
 */
public class ControlNotificaciones {

    private final PrestamoDAO prestamoDAO;
    private final NotificacionesDAO notificacionesDAO;
    private final NotificadorEmail notificadorEmail;

    public ControlNotificaciones(PrestamoDAO prestamoDAO,
                                 NotificacionesDAO notificacionesDAO,
                                 NotificadorEmail notificadorEmail) {
        this.prestamoDAO = prestamoDAO;
        this.notificacionesDAO = notificacionesDAO;
        this.notificadorEmail = notificadorEmail;
    }

    public void generarNotificacionesPendientes() throws DAOException {
        List<Prestamo> prestamos = prestamoDAO.listarTodos();

        for (Prestamo p : prestamos) {
            if (p == null) continue;

            if ("Activo".equalsIgnoreCase(p.getEstado())
                    && p.getFechaVencimiento().isBefore(LocalDate.now())) {
                generarNotificacion(p, "Aviso: su préstamo está vencido. Por favor devuelva el ejemplar.");
            }
        }
    }

    public void verificarVencimientos() throws DAOException {
        List<Prestamo> prestamos = prestamoDAO.listarTodos();
        LocalDate hoy = LocalDate.now();

        for (Prestamo p : prestamos) {
            if (p == null || p.getSocio() == null) continue;

            p.actualizarEstado();
            long diasRestantes = ChronoUnit.DAYS.between(hoy, p.getFechaVencimiento());

            if ("Activo".equalsIgnoreCase(p.getEstado()) && diasRestantes == 2) {
                generarNotificacion(p, "Recordatorio: su préstamo vence en 2 días.");
            } else if ("Vencido".equalsIgnoreCase(p.getEstado())) {
                generarNotificacion(p, "Aviso: su préstamo ha vencido.");
            }
        }
    }

    private void generarNotificacion(Prestamo prestamo, String mensaje) {
        try {
            for (Notificacion n : notificacionesDAO.listarPorPrestamoId(prestamo.getId())) {
                if (mensaje.equalsIgnoreCase(n.getMensaje())) return;
            }

            Notificacion nueva = new Notificacion(mensaje, prestamo);

            notificacionesDAO.insertar(nueva);

        } catch (DAOException e) {
            throw new RuntimeException("Error al generar notificación", e);
        }
    }

    public List<Notificacion> obtenerPendientes() {
        try {
            return notificacionesDAO.listarNoLeidas();
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    public void marcarComoEnviada(Notificacion n) {
        try {
            n.marcarComoLeida();
            notificacionesDAO.actualizar(n);
        } catch (DAOException e) {
            throw new RuntimeException("Error al marcar notificación como enviada", e);
        }
    }

    public List<Notificacion> listarTodas() {
        try {
            return notificacionesDAO.listarTodos();
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    public void enviarNotificacionesPendientes() {
        List<Notificacion> pendientes = obtenerPendientes();
        if (pendientes.isEmpty()) {
            System.out.println("No hay notificaciones pendientes.");
            return;
        }

        notificadorEmail.enviarNotificaciones(pendientes);

        for (Notificacion n : pendientes) {
            marcarComoEnviada(n);
        }
    }
}