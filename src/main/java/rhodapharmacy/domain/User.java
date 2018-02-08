package rhodapharmacy.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "usr")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String roles;
    private Date created;
    private Date disabled;
    private String token;
    private String tokenInfo;
    private Date tokenExpiry;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getDisabled() {
        return disabled;
    }

    public void setDisabled(Date disabled) {
        this.disabled = disabled;
    }

    public String getTokenInfo() {
        return tokenInfo;
    }

    public void setTokenInfo(String tokenInfo) {
        this.tokenInfo = tokenInfo;
    }

    public Date getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(Date tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

}
