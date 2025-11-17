import biblioteca.data.dao.DAOException;
import biblioteca.ui.MenuPrincipal;

public class Main {
    public static void main(String[] args) {
        try {
            MenuPrincipal menu = new MenuPrincipal();
            menu.iniciar();
        } catch (DAOException e) {
            System.err.println("Error al inicializar el sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}