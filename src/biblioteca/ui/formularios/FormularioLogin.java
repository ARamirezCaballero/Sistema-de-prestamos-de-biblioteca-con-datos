package biblioteca.ui.formularios;

import biblioteca.entities.usuarios.Usuario;
import biblioteca.services.ControlUsuarios;

import java.util.Scanner;

/**
 * Formulario de inicio de sesión para usuarios del sistema.
 * Permite autenticar bibliotecarios y socios mediante username y password.
 */
public class FormularioLogin {

    private final ControlUsuarios controlUsuarios;
    private final Scanner scanner;

    public FormularioLogin(ControlUsuarios controlUsuarios) {
        this.controlUsuarios = controlUsuarios;
        this.scanner = new Scanner(System.in);
    }


    public Usuario mostrarFormulario() {
        System.out.println("\n=== LOGIN DE USUARIO ===");

        while (true) {
            try {
                System.out.print("Nombre de usuario: ");
                String username = scanner.nextLine().trim();

                System.out.print("Contraseña: ");
                String password = scanner.nextLine().trim();

                Usuario usuario = controlUsuarios.login(username, password);

                if (usuario != null) {
                    System.out.println("Acceso permitido para: " + usuario.getNombreCompleto());
                    System.out.println("Login exitoso. Bienvenido/a al sistema.");
                    return usuario;
                } else {
                    System.out.println("Credenciales incorrectas. Intente nuevamente.");
                }

                System.out.print("¿Desea intentar nuevamente? (S/N): ");
                String resp = scanner.nextLine().trim();
                if (!resp.equalsIgnoreCase("S")) {
                    System.out.println("Login cancelado por el usuario.");
                    return null;
                }

            } catch (Exception e) {
                System.out.println("ERROR: Ocurrió un problema durante el inicio de sesión: " + e.getMessage());
                System.out.print("¿Desea intentar nuevamente? (S/N): ");
                String resp = scanner.nextLine().trim();
                if (!resp.equalsIgnoreCase("S")) {
                    System.out.println("Login cancelado por el usuario.");
                    return null;
                }
            }
        }
    }
}
