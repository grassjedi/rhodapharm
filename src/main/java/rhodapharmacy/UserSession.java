package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class UserSession {
    private static final Logger log = LoggerFactory.getLogger(UserSession.class);

    private User user;
    private String sessionKey;
    private Date expiry;

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public Date getExpiry() {
        return expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isSessionExpired() {
        return expiry.before(new Date());
    }

    public boolean isUserTokenExpired() {
        if(user == null) return false;
        Date tokenExpire = user.getTokenExpiry();
        return tokenExpire != null && tokenExpire.before(new Date());
    }

    public boolean isAuthorised() {
        return !this.isAnonymous() && !this.isSessionExpired() && !this.isUserTokenExpired();
    }

    public boolean isAnonymous() {
        return user == null;
    }

    public String getUserEmail() {
        if(isAnonymous()) return null;
        return getUser().getEmail();
    }

    public boolean hasRole(String role) {
        if(user == null || user.getRoles() == null) return false;
        final String roles = " " + user.getRoles() + " ";
        log.debug("user roles: {}", roles);
        final String rolePattern = " " + role + " ";
        log.debug("role pattern: {}", rolePattern);
        return roles.contains(rolePattern);
    }

    public boolean hasRole(UserRole role) {
        return hasRole(role.name());
    }

    public boolean getUserAdmin() {
        return hasRole(UserRole.USER_ADMIN);
    }

    public boolean getRawMaterialAdmin() {
        return hasRole(UserRole.RAW_MATERIAL_MAINTENANCE);
    }

    public boolean getProductAdmin() {
        return hasRole(UserRole.PRODUCT_MAINTENANCE);
    }
}
