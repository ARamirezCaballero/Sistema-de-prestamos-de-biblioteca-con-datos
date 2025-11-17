package biblioteca.ui;

import biblioteca.data.dao.*;
import biblioteca.entities.usuarios.Bibliotecario;
import biblioteca.entities.usuarios.Socio;
import biblioteca.entities.usuarios.Usuario;
import biblioteca.services.*;
import biblioteca.ui.formularios.*;
import biblioteca.ui.componentes.NotificadorEmail;
import biblioteca.ui.pantallas.*;

import java.util.Scanner;

/**
 * Clase principal que gestiona la interfaz de usuario del sistema de biblioteca.
 * Coordina la navegación entre menús, formularios y pantallas para bibliotecarios y socios.
 */
public class MenuPrincipal  {

    private final Scanner scanner;
    private final ControlUsuarios controlUsuarios;
    private final ControlPrestamos controlPrestamos;
    private final ControlDevoluciones controlDevoluciones;
    private final ControlPoliticas controlPoliticas;
    private final ControlValidaciones controlValidaciones;
    private final ControlComprobantes controlComprobantes;
    private final ControlConsultas controlConsultas;
    private final ControlHistorial controlHistorial;
    private final ControlLibros controlLibros;
    private final FormularioLogin formularioLogin;
    private final ControlNotificaciones controlNotificaciones;
    private NotificadorEmail notificadorEmail;

    // DAOs propagados a Formularios
    private final LibroDAO libroDAO;
    private final EjemplarDAO ejemplarDAO;
    private final PrestamoDAO prestamoDAO;

    public MenuPrincipal() throws DAOException {

        // DAOs
        this.libroDAO = new LibroDAO();
        this.prestamoDAO = new PrestamoDAO();
        DevolucionDAO devolucionDAO = new DevolucionDAO(prestamoDAO);
        this.ejemplarDAO = new EjemplarDAO();
        PoliticaPrestamoDAO politicaDAO = new PoliticaPrestamoDAO();
        SocioDAO socioDAO = new SocioDAO();
        ComprobanteDAO comprobanteDAO = new ComprobanteDAO(prestamoDAO);
        HistorialDAO historialDAO = new HistorialDAO(socioDAO);
        NotificacionesDAO notificacionesDAO = new NotificacionesDAO();
        BibliotecarioDAO bibliotecarioDAO = new BibliotecarioDAO();

        // CONTROLES
        this.scanner = new Scanner(System.in);
        this.controlUsuarios = new ControlUsuarios(bibliotecarioDAO, socioDAO);
        this.controlPoliticas = new ControlPoliticas(socioDAO, politicaDAO);
        this.controlValidaciones = new ControlValidaciones(socioDAO);
        this.controlHistorial = new ControlHistorial(historialDAO, prestamoDAO);
        this.controlComprobantes = new ControlComprobantes(comprobanteDAO);
        this.controlConsultas = new ControlConsultas(libroDAO, prestamoDAO);
        this.controlLibros = new ControlLibros(libroDAO, ejemplarDAO);

        // DEVOLUCIONES CON DAOs
        this.controlDevoluciones = new ControlDevoluciones(controlHistorial, prestamoDAO, devolucionDAO, ejemplarDAO);

        //NOTIFICADOR UI
        this.notificadorEmail = NotificadorEmail.defaultFake();

        // NOTIFICACIONES
        this.controlNotificaciones = new ControlNotificaciones(
                prestamoDAO,
                notificacionesDAO,
                notificadorEmail
        );

        // PRESTAMOS
        this.controlPrestamos = new ControlPrestamos(prestamoDAO, ejemplarDAO, socioDAO, controlPoliticas, controlValidaciones, controlHistorial);

        // LOGIN
        this.formularioLogin = new FormularioLogin(controlUsuarios);
    }

    public void iniciar() throws DAOException {
        System.out.println("===== SISTEMA DE GESTIÓN BIBLIOTECARIA =====");

        boolean salirSistema = false;

        while (!salirSistema) {
            System.out.println("""
                    
                    ===== SELECCIÓN DE ROL =====
                    1. Bibliotecario
                    2. Socio (Portal Web Simulado)
                    0. Salir
                    """);
            System.out.print("Seleccione una opción: ");
            int rol = leerOpcion();

            switch (rol) {
                case 1 -> iniciarBibliotecario();
                case 2 -> iniciarSocio();
                case 0 -> {
                    salirSistema = true;
                    System.out.println("Saliendo del sistema...");
                }
                default -> System.out.println("Opción inválida. Intente nuevamente.");
            }
        }
    }

    // MENÚ BIBLIOTECARIO
    private void iniciarBibliotecario() {
        System.out.println("\n--- Ingreso Bibliotecario ---");

        Usuario usuario = formularioLogin.mostrarFormulario();
        if (usuario == null) {
            System.out.println("No se pudo iniciar sesión. Volviendo al menú principal...");
            return;
        }

        // Verificar que el usuario sea un bibliotecario
        if (!(usuario instanceof Bibliotecario)) {
            System.out.println("Error: El usuario no es un bibliotecario. Volviendo al menú principal...");
            return;
        }

        Bibliotecario bibliotecarioLogueado = (Bibliotecario) usuario;

        boolean salir = false;
        while (!salir) {
            mostrarOpcionesBibliotecario();
            int opcion = leerOpcion();

            switch (opcion) {
                case 1 -> registrarLibro();
                case 2 -> registrarPrestamo(bibliotecarioLogueado);
                case 3 -> registrarDevolucion();
                case 4 -> consultarHistorial();
                case 5 -> generarReporte();
                //opción de notificaciones
                case 6 -> {
                    try {
                        controlNotificaciones.verificarVencimientos();
                        controlNotificaciones.enviarNotificacionesPendientes();
                    } catch (DAOException e) {
                        System.out.println("Error generando/enviando notificaciones: " + e.getMessage());
                    }
                }

                case 0 -> {
                    salir = true;
                    System.out.println("Cerrando sesión de bibliotecario...");
                }
                default -> System.out.println("Opción inválida. Intente nuevamente.");
            }
        }
    }

    // MENÚ SOCIO
    private void iniciarSocio() throws DAOException {
        System.out.println("\n=== Portal Web Simulado para Socios ===");

        boolean salir = false;
        Socio socioActivo = null;

        while (!salir) {

            if (socioActivo == null) {
                System.out.println("""
                    
                    ===== MENÚ SOCIO =====
                    1. Registrarse
                    2. Iniciar sesión
                    0. Volver al menú principal
                    """);
                System.out.print("Seleccione una opción: ");
                int opcion = leerOpcion();

                switch (opcion) {
                    case 1 -> {
                        FormularioRegistroSocio registro =
                                new FormularioRegistroSocio(controlUsuarios, controlValidaciones);
                        registro.mostrarFormulario();
                    }
                    case 2 -> {
                        Usuario usuario = formularioLogin.mostrarFormulario();
                        if (usuario instanceof Socio socio) {
                            socioActivo = socio;
                            System.out.println("Login exitoso. Bienvenido/a, " + socioActivo.getNombreCompleto() + ".");
                        } else if (usuario == null) {
                            System.out.println("Credenciales incorrectas o usuario no encontrado.");
                        } else {
                            System.out.println("El usuario no corresponde a un socio.");
                        }
                    }
                    case 0 -> salir = true;
                    default -> System.out.println("Opción inválida. Intente nuevamente.");
                }

            } else {
                System.out.println("\n===== MENÚ SOCIO (" + socioActivo.getNombreCompleto() + ") =====");
                System.out.println("""
                    1. Ver mi historial de préstamos
                    2. Ver libros y ejemplares disponibles
                    0. Cerrar sesión
                    """);
                System.out.print("Seleccione una opción: ");
                int opcion = leerOpcion();

                switch (opcion) {
                    case 1 -> controlConsultas.consultarHistorialPorSocio(socioActivo.getDni());
                    case 2 -> mostrarLibrosDisponibles();
                    case 0 -> {
                        System.out.println("Cerrando sesión de socio...");
                        socioActivo = null;
                    }
                    default -> System.out.println("Opción inválida. Intente nuevamente.");
                }
            }
        }
    }

    // UTILITARIOS
    private void mostrarOpcionesBibliotecario() {
        System.out.println("""
                
                ===== MENÚ BIBLIOTECARIO =====
                1. Registrar libro o ejemplar
                2. Registrar préstamo
                3. Registrar devolución
                4. Consultar historial
                5. Generar reporte
                6. Ejecutar proceso de notificaciones automáticas
                0. Cerrar sesión
                """);
        System.out.print("Seleccione una opción: ");
    }

    private int leerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void registrarLibro() {
        System.out.println("\n--- Registrar Libro o Ejemplar ---");
        FormularioRegistroLibro formulario = new FormularioRegistroLibro(controlLibros);
        formulario.mostrarFormulario();
    }

    private void registrarPrestamo(Bibliotecario bibliotecarioLogueado) {
        FormularioPrestamos formulario = new FormularioPrestamos(
                controlPrestamos,
                controlComprobantes,
                controlUsuarios
        );
        // Establecer el bibliotecario logueado para usar en los préstamos
        formulario.setBibliotecarioLogueado(bibliotecarioLogueado);
        formulario.mostrarFormulario();
    }

    private void registrarDevolucion() {
        FormularioDevolucion formulario = new FormularioDevolucion(controlDevoluciones);
        formulario.mostrarFormulario();
    }

    private void mostrarLibrosDisponibles() {
        try {
            // Usar el método que filtra solo libros con ejemplares disponibles
            var listaLibros = controlLibros.listarLibrosDisponibles();
            System.out.println("\n=== LIBROS DISPONIBLES ===");

            if (listaLibros.isEmpty()) {
                System.out.println("No hay libros disponibles para préstamo en este momento.");
                return;
            }

            for (var detalle : listaLibros) {
                var libro = (biblioteca.entities.inventario.Libro) detalle.get("libro");
                System.out.println("Título: " + libro.getTitulo()
                        + " | Total: " + detalle.get("totalEjemplares")
                        + " | Disponibles: " + detalle.get("disponibles")
                        + " | Prestados: " + detalle.get("prestados"));
            }
        } catch (biblioteca.data.dao.DAOException e) {
            System.out.println("Error al listar libros: " + e.getMessage());
        }
    }

    private void consultarHistorial() {
        PantallaHistorial pantalla = new PantallaHistorial(controlConsultas, controlHistorial);
        pantalla.mostrarPantalla();
    }

    private void generarReporte() {
        System.out.println("\n=== REPORTE GENERAL DE PRÉSTAMOS ===");
        String reporte = controlConsultas.generarReporte();
        System.out.println(reporte);
        System.out.println("Reporte exportado exitosamente (simulado).");
    }
}