package biblioteca.data.dao;

public class DAOException extends Exception {
  public DAOException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }

  public DAOException(String mensaje) {
    super(mensaje);
  }
}



