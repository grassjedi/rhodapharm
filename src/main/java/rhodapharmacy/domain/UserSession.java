package rhodapharmacy.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rhodapharmacy.UserRole;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "usr_session")
public class UserSession {
    private static final Logger log = LoggerFactory.getLogger(UserSession.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
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

    @Transient
    public boolean isSessionExpired() {
        return expiry.before(new Date());
    }

    @Transient
    public boolean isUserTokenExpired() {
        if(user == null) return false;
        Date tokenExpire = user.getTokenExpiry();
        return tokenExpire != null && tokenExpire.before(new Date());
    }

    @Transient
    public boolean isAuthorised() {
        return !this.isAnonymous() && !this.isSessionExpired() && !this.isUserTokenExpired();
    }

    @Transient
    public boolean isAnonymous() {
        return user == null;
    }

    public String getUserEmail() {
        if(isAnonymous()) return null;
        return getUser().getEmail();
    }

    @Transient
    public boolean hasRole(String role) {
        if(user == null || user.getRoles() == null) return false;
        final String roles = " " + user.getRoles() + " ";
        log.debug("user roles: {}", roles);
        final String rolePattern = " " + role + " ";
        log.debug("role pattern: {}", rolePattern);
        return roles.contains(rolePattern);
    }

    @Transient
    public boolean hasRole(UserRole role) {
        return hasRole(role.name());
    }

    @Transient
    public boolean getUserAdmin() {
        return hasRole(UserRole.USER_ADMIN);
    }

    @Transient
    public boolean getRawMaterialAdmin() {
        return hasRole(UserRole.RAW_MATERIAL_MAINTENANCE);
    }

    @Transient
    public boolean getProductAdmin() {
        return hasRole(UserRole.PRODUCT_MAINTENANCE);
    }
}
