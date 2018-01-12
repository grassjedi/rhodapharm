package rhodapharmacy;

import java.util.Date;

public class UserSession {
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
        Date tokenExpire = user.getTokenExpiry();
        return tokenExpire != null && tokenExpire.before(new Date());
    }

    public boolean isAnonymous() {
        return user == null;
    }
}
