package rhodapharmacy.signin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import rhodapharmacy.*;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@Component
public class GoogleAuthFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(GoogleAuthFilter.class);

    private static final String STATE_TOKEN = "__STATE__";
    private String googleAuthUrl;
    private String authCallbackUrl;
    private AuthorisationService authorisationService;

    public GoogleAuthFilter(
            Environment environment,
            SecuredConfig securedConfig,
            AuthorisationService authorisationService) {
        this.authorisationService = authorisationService;
        String clientId      = securedConfig.getProperty("google.clientId");
        String redirectUri    = environment.getProperty("google.redirectUrl");
        String scope          = environment.getProperty("google.scope");
        String accessType     = environment.getProperty("google.accessType");
        if(clientId == null || clientId.isEmpty()) {
            throw new IllegalStateException("could not resolve google client credentials");
        }
        log.error("scopes are: " + scope);
        this.googleAuthUrl =
                "https://accounts.google.com/o/oauth2/v2/auth?" +
                        "client_id=" + clientId + "" +
                        "&redirect_uri=" + redirectUri + "" +
                        "&scope=" + scope + "" +
                        "&access_type=" + accessType + "" +
                        "&response_type=code" +
                        "&state=" + STATE_TOKEN + "";
        this.authCallbackUrl = environment.getProperty("google.callbackUrl");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        final String requestUri = req.getRequestURI();
        //always try and get the cookie value
        if(this.authCallbackUrl.equals(requestUri)) {
            log.debug("google oauth callback");
            final String queryString = req.getQueryString();
            if (queryString == null) {
                res.sendError(400, "authorization state not provided");
                return;
            }
            Map<String, String> queryParams = parseQueryString(queryString);
            if (!queryParams.containsKey("state") || queryParams.get("state") == null) {
                res.sendError(403);
                return;
            }
            if (!queryParams.containsKey("code") || queryParams.get("code") == null) {
                res.sendError(400, "no auth code from google");
                return;
            }
            String callbackState = queryParams.get("state");
            log.debug("looking up session: {}", callbackState);
            UserSession userSession = authorisationService.validateSession(callbackState);
            if(userSession == null) {
                String googleAuthUrl = this.googleAuthUrl.replace(STATE_TOKEN, authorisationService.createSession().getSessionKey());
                res.sendRedirect(googleAuthUrl);
                log.debug("failed to find session: {} performing redirect to: {}", callbackState, googleAuthUrl);
                return;
            }
            try {
                authorisationService.authoriseSession(userSession, queryParams.get("code"));
                res.addCookie(new Cookie("kaart", userSession.getSessionKey()));
                res.sendRedirect("/");
                return;
            }
            catch (XAuthorisationFailure xAuthorisationFailure) {
                res.sendError(401, "authorisation failure");
                return;
            }
        }
        else {
            String kaartToken = getKaartTokenCookieValue(req);
            UserSession userSession = null;
            if(kaartToken != null) {
                userSession = authorisationService.validateSession(kaartToken);
            }
            if(userSession == null) {
                log.debug("no session found for request");
                res.sendRedirect(googleAuthUrl.replace(STATE_TOKEN, authorisationService.createSession().getSessionKey()));
                return;
            }
            req.setAttribute("userSession", userSession);
            return;
        }
    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        String[] params = queryString.split("&");
        for (String param : params) {
            String[] assignment = param.split("=");
            String key = assignment[0];
            String value = null;
            try {
                if (assignment.length > 1) {
                    value = URLDecoder.decode(assignment[1], "UTF-8");
                    value = value.replaceAll(" ", "+");
                }
                queryParams.put(key, value);
            }
            catch(UnsupportedEncodingException ignore) {}
        }
        return queryParams;
    }

    private String getKaartTokenCookieValue(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        String kaartToken = null;
        for (Cookie cookie : cookies) {
            if ("kaart".equals(cookie.getName())) {
                kaartToken = cookie.getValue();
            }
        }
        return kaartToken;
    }

    @Override
    public void destroy() {
    }
}
