package rhodapharmacy.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import rhodapharmacy.domain.ProductValue;

public interface ProductValueRepository extends JpaRepository<ProductValue, Long> {
}
