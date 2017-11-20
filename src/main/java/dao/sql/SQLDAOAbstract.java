package main.java.dao.sql;

import main.java.dao.DAOException;
import main.java.models.ModelAbstract;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SQLDAOAbstract<T extends ModelAbstract> {

    abstract public T getById(int id) throws DAOException;

    abstract T objectFromResultSet(ResultSet resultSet) throws SQLException;

    T recentlyUpdated(PreparedStatement statement) throws DAOException, SQLException {
        ResultSet generatedKeys = statement.getGeneratedKeys();

        if (generatedKeys.next()) {
            int id = generatedKeys.getInt(1);
            return getById(id);
        }

        return null;
    }
}
