package rhodapharmacy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class SqlTemplate<T> {

    private DataSource dataSource;

    public SqlTemplate(DataSource dataSource)
    throws SQLException {
        this.dataSource = dataSource;
    }

    public abstract T doAction(Connection connection) throws SQLException;

    public T execute()
    throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            T result = doAction(connection);
            connection.commit();
            return result;
        }
        catch (SQLException e) {
            if(connection != null) {
                connection.rollback();
            }
            throw e;
        }
        catch (Exception e) {
            if(connection != null) {
                connection.rollback();
            }
            throw new SQLException(e);
        }
        finally {
            if(connection != null) {
                connection.close();
            }
        }
    }

}
