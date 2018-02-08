package rhodapharmacy.repo;

import org.springframework.data.repository.CrudRepository;
import rhodapharmacy.domain.User;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByEmail(String email);

}
