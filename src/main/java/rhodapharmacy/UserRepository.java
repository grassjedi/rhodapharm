package rhodapharmacy;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
            PreparedStatement statement = connection.prepareStatement("SELECT id, email, roles, created, disabled, token, token_info, token_expiry FROM usr WHERE email = ?");
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            User user = mapSingleUser(resultSet, false);
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

    public User addUser(String email, String roles)
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

    public List<User> listUsers()
    throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT " +
                            "u.id AS id, " +
                            "u.email AS email, " +
                            "u.roles AS roles, " +
                            "u.created AS created, " +
                            "u.disabled AS disabled, " +
                            "u.token AS token, " +
                            "u.token_info AS token_info, " +
                            "u.token_expiry AS token_expiry, " +
                            "u.token_expiry > CURRENT_TIMESTAMP AS token_valid, " +
                            "s.expiry > CURRENT_TIMESTAMP AS session_active " +
                            "FROM usr u LEFT JOIN usr_session s ON u.id = s.usr_id");
            ResultSet resultSet = statement.executeQuery();
            List<User> users = mapUsers(resultSet, true);
            connection.commit();
            return users;
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

    private User mapSingleUser(ResultSet resultSet, boolean mapAuthStatus)
    throws SQLException {
        int count = 0;
        User user = null;
        String email = "UNKNOWN";
        while(resultSet.next()) {
            if(count++ > 1) {
                throw new XNoUniqueRecord(String.format("the user email, %s, does not represent a unique user", email));
            }
            user = new User();
            user.setId(resultSet.getLong("id"));
            email = resultSet.getString("email");
            user.setEmail(email);
            String roles = resultSet.getString("roles");
            String cleanedRoles = UserRole.clean(roles);
            user.setRoles(cleanedRoles);
            user.setCreated(resultSet.getTimestamp("created"));
            user.setDisabled(resultSet.getTimestamp("disabled"));
            user.setToken(resultSet.getString("token"));
            user.setTokenInfo(resultSet.getString("token_info"));
            user.setTokenExpiry(resultSet.getTimestamp("token_expiry"));
            if(mapAuthStatus) {
                user.setGoogleLoginActive(resultSet.getBoolean("token_valid"));
                user.setSessionActive(resultSet.getBoolean("session_active"));
            }
        }
        if(count < 1) {
            throw new XNoSuchRecord();
        }
        return user;
    }

    private List<User> mapUsers(ResultSet resultSet, boolean mapAuthStatus)
            throws SQLException {
        List<User> users = new LinkedList<>();
        while(resultSet.next()) {
            User user = new User();
            user.setId(resultSet.getLong("id"));
            user.setEmail(resultSet.getString("email"));
            String roles = resultSet.getString("roles");
            String cleanedRoles = UserRole.clean(roles);
            user.setRoles(cleanedRoles);
            user.setCreated(resultSet.getTimestamp("created"));
            user.setDisabled(resultSet.getTimestamp("disabled"));
            user.setToken(resultSet.getString("token"));
            user.setTokenInfo(resultSet.getString("token_info"));
            user.setTokenExpiry(resultSet.getTimestamp("token_expiry"));
            if(mapAuthStatus) {
                user.setGoogleLoginActive(resultSet.getBoolean("token_valid"));
                user.setSessionActive(resultSet.getBoolean("session_active"));
            }
            users.add(user);
        }
        return users;
    }

    public void updateUser(Long userId, String roles)
    throws SQLException, XInvalidRole {
        UserRole.validate(roles);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE usr SET roles = ? WHERE id = ?");
            statement.setString(1, roles);
            statement.setLong(2, userId);
            statement.executeUpdate();
            connection.commit();
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

    public void deleteUser(Long userId)
    throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM usr WHERE id = ?");
            statement.setLong(1, userId);
            statement.executeUpdate();
            connection.commit();
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

    public void disableUser(Long userId)
    throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE usr SET disabled = current_timestamp WHERE id = ?");
            statement.setLong(1, userId);
            statement.executeUpdate();
            connection.commit();
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

    public void enableUser(String email)
    throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE usr SET disabled = NULL WHERE email = ?");
            statement.setString(1, email);
            statement.executeUpdate();
            connection.commit();
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

    public void enableUser(Long userId)
    throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE usr SET disabled = NULL WHERE id = ?");
            statement.setLong(1, userId);
            statement.executeUpdate();
            connection.commit();
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

    public User retrieveUser(Long userId)
    throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT id, email, roles, created, disabled, token, token_info, token_expiry FROM usr WHERE id = ?");
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            User user = mapSingleUser(resultSet, false);
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
}
