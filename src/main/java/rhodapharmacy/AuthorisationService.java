package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rhodapharmacy.domain.User;
import rhodapharmacy.domain.UserSession;
import rhodapharmacy.repo.UserRepository;
import rhodapharmacy.repo.UserSessionRepository;
import rhodapharmacy.signin.GoogleAccessToken;
import rhodapharmacy.signin.GoogleService;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class AuthorisationService {
    public static final long TOKEN_TTL = TimeUnit.MILLISECONDS.convert(8L, TimeUnit.HOURS);
    private static final Logger log = LoggerFactory.getLogger(AuthorisationService.class);

    private UserSessionRepository userSessionRepository;
    private GoogleService googleService;
    private UserRepository userRepository;
    private final SecureRandom rnd;
    private final MessageDigest messageDigest;

    public AuthorisationService(GoogleService googleService,
                                UserSessionRepository userSessionRepository,
                                UserRepository userRepository)
    throws NoSuchAlgorithmException {
        this.googleService = googleService;
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.rnd = new SecureRandom();
        this.messageDigest = MessageDigest.getInstance("SHA-256");
    }

    public UserSession getSession(String sessionKey) {
        return userSessionRepository.findBySessionKey(sessionKey);
    }

    public synchronized UserSession createSession() {
        userSessionRepository.deleteExpiredSessions(new Date(System.currentTimeMillis() - (2 * TOKEN_TTL)));
        byte[] tokenBytes = new byte[1024];
        rnd.nextBytes(tokenBytes);
        tokenBytes = messageDigest.digest(tokenBytes);
        final String token = Base64.getEncoder().encodeToString(tokenBytes);
        UserSession userSession = new UserSession();
        userSession.setSessionKey(token);
        userSession.setExpiry(new Date(System.currentTimeMillis() + TOKEN_TTL));
        userSessionRepository.save(userSession);
        return userSession;
    }

    public synchronized void authoriseSession(UserSession userSession, String code)
    throws XAuthorisationFailure {
        GoogleAccessToken accessToken = null;
        try {
            accessToken = googleService.getAccessToken(code);
        }
        catch (IOException e) {
            log.info("failed to retrieve access token from google", e);
            throw new XAuthorisationFailure();
        }
        String email = accessToken.getGoogleAccessTokenInfo().getEmail();
        User user = userRepository.findByEmail(email);
        if(user != null) {
            user.setToken(accessToken.getAccess_token());
            user.setTokenExpiry(new Date(accessToken.getGoogleAccessTokenInfo().getExp() * 1000L));
            user.setTokenInfo(accessToken.getId_token());
            userSession.setUser(user);
            userSessionRepository.save(userSession);
        }
    }
}
