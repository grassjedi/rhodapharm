package rhodapharmacy.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import rhodapharmacy.domain.UserSession;

import java.util.Date;

public interface UserSessionRepository extends CrudRepository<UserSession, Long> {

    UserSession findBySessionKey(String sessionKey);

    @Modifying
    @Query("delete from UserSession u where u.expiry <= ?1")
    void deleteExpiredSessions(Date expiry);
}
