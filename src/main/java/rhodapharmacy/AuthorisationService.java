package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rhodapharmacy.signin.GoogleAccessToken;
import rhodapharmacy.signin.GoogleService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

@Service
public class AuthorisationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorisationService.class);

    private SessionRepository sessionRepository;
    private GoogleService googleService;
    private UserRepository userRepository;

    public AuthorisationService(GoogleService googleService,
                                UserRepository userRepository,
                                SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
        this.googleService = googleService;
        this.userRepository = userRepository;
    }

    public UserSession getSession(String sessionKey) {
        try {
            return sessionRepository.retrieveSession(sessionKey);
        }
        catch (SQLException e) {
            log.info("failed to retrieve session", e);
            return null;
        }
        catch (XSessionNotFound ignore) {
            log.debug("session invalid {}", sessionKey);
            return null;
        }
        catch (XNoSuchRecord ignore) {
            log.debug("session invalid {}", sessionKey);
            return null;
        }
    }

    public UserSession createSession() {
        try {
            return sessionRepository.newSession();
        }
        catch (SQLException e) {
            log.info("failed to create new session", e);
            return null;
        }
    }

    public UserSession authoriseSession(UserSession userSession, String code)
    throws XAuthorisationFailure {
        GoogleAccessToken accessToken = null;
        try {
            accessToken = googleService.getAccessToken(code);
        }
        catch (IOException e) {
            log.info("failed to retrieve access token from google", e);
            throw new XAuthorisationFailure();
        }
        User user = null;
        String email = accessToken.getGoogleAccessTokenInfo().getEmail();
        try {
            user = userRepository.retrieveAndUpdateUser(email, accessToken.getAccess_token(), accessToken.getId_token(), new Date(accessToken.getGoogleAccessTokenInfo().getExp() * 1000L));
            sessionRepository.authoriseSession(userSession.getSessionKey(), user.getId());
            userSession.setUser(user);
            return userSession;
        }
        catch (SQLException | XNoSuchRecord | XNoUniqueRecord e) {
            log.info("failed to retrieve user with email '{}'", email, e);
            throw new XAuthorisationFailure();
        }
    }
}
