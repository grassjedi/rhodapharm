package rhodapharmacy;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;

@Service
public class UserRepository {

    private DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User retrieveAndUpdateUser(String email, String token, String tokenInfo, Date tokenExpiry)
    throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, email, roles, created, disabled, token, token_info, token_expiry FROM usr WHERE email = ?");
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            int count = 0;
            User user = null;
            while(resultSet.next()) {
                if(count++ > 1) {
                    throw new XNoUniqueRecord(email);
                }
                user = new User();
                user.setId(resultSet.getLong("id"));
                user.setEmail(resultSet.getString("email"));
                user.setRoles(resultSet.getString("roles"));
                user.setCreated(resultSet.getTimestamp("created"));
                user.setDisabled(resultSet.getTimestamp("disabled"));
                user.setToken(resultSet.getString("token"));
                user.setTokenInfo(resultSet.getString("token_info"));
                user.setTokenExpiry(resultSet.getTimestamp("token_expiry"));
            }
            if(count < 1) {
                throw new XNoSuchRecord();
            }
            if(!token.equals(user.getToken())) {
                PreparedStatement update = connection.prepareStatement("UPDATE usr SET token = ?, token_info = ?, token_expiry = ? WHERE id = ?");
                update.setString(1, token);
                update.setString(2, tokenInfo);
                update.setTimestamp(3, new Timestamp(tokenExpiry.getTime()));
                update.setLong(4, user.getId());
                update.executeUpdate();
                user.setToken(token);
            }
            connection.commit();
            return user;
        }
        catch (SQLException | XNoSuchRecord e) {
            if(connection != null) {
                connection.rollback();
            }
            throw e;
        }
        finally {
            if(connection != null) {
                connection.close();
            }
        }
    }

    public User addUser(String email)
    throws SQLException {
        Connection connection = null;
        User user = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO usr (email) VALUES (?) RETURNING id");
            statement.setString(1, email);
            boolean result = statement.execute();
            if(result) {
                ResultSet resultSet = statement.getResultSet();
                if(resultSet.next()) {
                    user = new User();
                    user.setId(resultSet.getLong(1));
                    user.setEmail(email);
                }
            }
            if(user == null) {
                throw new SQLException(String.format("failed to insert user with email %s", email));
            }
            connection.commit();
            return user;
        }
        catch (SQLException e) {
            if(connection != null) {
                connection.rollback();
            }
            throw e;
        }
        finally {
            if(connection != null) {
                connection.close();
            }
        }
    }
}
