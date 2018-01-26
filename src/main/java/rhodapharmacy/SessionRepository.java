package rhodapharmacy;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SessionRepository {
    public static final long TOKEN_TTL = TimeUnit.MILLISECONDS.convert(8L, TimeUnit.HOURS);
    public static final long SESSION_CLEAN_TIMEOUT = TimeUnit.MILLISECONDS.convert(5L, TimeUnit.MINUTES);

    private DataSource dataSource;

    private volatile Long nextClean;
    private final SecureRandom rnd;
    private final MessageDigest messageDigest;

    public SessionRepository(
            DataSource dataSource) {
        this.dataSource = dataSource;
        this.rnd = new SecureRandom();
        try {
            this.messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("could not initialize SHA-256", e);
        }
    }

    public UserSession newSession()
            throws SQLException {
        byte[] tokenBytes = new byte[1024];
        rnd.nextBytes(tokenBytes);
        tokenBytes = messageDigest.digest(tokenBytes);
        final String token = Base64.getEncoder().encodeToString(tokenBytes);
        Connection connection = null;
        final Long expiry = System.currentTimeMillis() + TOKEN_TTL;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO usr_session (expiry, key) VALUES (?, ?)");
            statement.setTimestamp(1, new Timestamp(expiry));
            statement.setString(2, token);
            statement.executeUpdate();
            connection.commit();
            UserSession userSession = new UserSession();
            userSession.setExpiry(new Date(expiry));
            userSession.setSessionKey(token);
            return userSession;
        }
        catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public UserSession retrieveSession(String sessionKey)
            throws SQLException, XSessionNotFound {
        cleanSessions();
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT " +
                            "s.expiry AS session_expiry, " +
                            "u.id AS usr_id, " +
                            "u.email AS usr_email, " +
                            "u.roles AS usr_roles, " +
                            "u.token AS usr_token, " +
                            "u.created AS usr_created, " +
                            "u.disabled AS usr_disabled " +
                    "FROM usr_session s " +
                    "LEFT JOIN usr u ON s.usr_id = u.id " +
                    "WHERE KEY = ? AND expiry > ?");
            statement.setString(1, sessionKey);
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis() - TOKEN_TTL));
            ResultSet resultSet = statement.executeQuery();
            User user = null;
            UserSession session = null;
            boolean found = false;
            while (resultSet.next()) {
                if (found) {
                    user = null;
                    session = null;
                    throw new XNoUniqueRecord("duplicate sessions exist");
                }
                Long userId = (Long) resultSet.getObject("usr_id");
                session = new UserSession();
                session.setExpiry(resultSet.getTimestamp("session_expiry"));
                session.setSessionKey(sessionKey);
                if(userId != null) {
                    user = new User();
                    user.setId(userId);
                    user.setEmail(resultSet.getString("usr_email"));
                    user.setToken(resultSet.getString("usr_token"));
                    user.setRoles(resultSet.getString("usr_roles"));
                    user.setCreated(resultSet.getTimestamp("usr_created"));
                    user.setDisabled(resultSet.getTimestamp("usr_disabled"));
                }
                session.setUser(user);
                found = true;
            }
            if (session == null) {
                throw new XNoSuchRecord();
            }
            connection.commit();
            return session;
        }
        catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public void authoriseSession(String sessionKey, Long userId)
    throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE usr_session SET usr_id = ? WHERE key = ?");
            statement.setLong(1, userId);
            statement.setString(2, sessionKey);
            int recordCount = statement.executeUpdate();
            if (recordCount == 0) {
                throw new XNoSuchRecord();
            }
            if (recordCount > 1) {
                throw new XNoUniqueRecord("unique session not found");
            }
            connection.commit();
        }
        catch (SQLException | XNoSuchRecord | XNoUniqueRecord e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public synchronized void cleanSessions()
    throws SQLException {
        if (nextClean == null || System.currentTimeMillis() > nextClean) {
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM usr_session WHERE expiry < ?");
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
                connection.commit();
                nextClean = System.currentTimeMillis() + SESSION_CLEAN_TIMEOUT;
            } catch (SQLException | XNoSuchRecord | XNoUniqueRecord e) {
                if (connection != null) {
                    connection.rollback();
                }
                throw e;
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }
    }
}
