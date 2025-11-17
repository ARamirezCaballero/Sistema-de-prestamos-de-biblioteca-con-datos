package biblioteca.data.interfaces;

import biblioteca.data.dao.DAOException;

import java.util.List;

public interface DAO<T> {
    void insertar(T t) throws DAOException;
    T buscarPorId(int id) throws DAOException;
    List<T> listarTodos() throws DAOException;
    void actualizar(T t) throws DAOException;
    void eliminar(int id) throws DAOException;
}
