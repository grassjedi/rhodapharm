package rhodapharmacy.repo;

import org.springframework.data.repository.CrudRepository;
import rhodapharmacy.domain.Product;

public interface ProductRepository extends CrudRepository<Product, Long> {
}
