package rhodapharmacy.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rhodapharmacy.domain.Product;
import rhodapharmacy.domain.ProductManufactureOutput;

import java.util.List;

public interface ProductManufactureOutputRepository extends JpaRepository<ProductManufactureOutput, Long> {

    @Query("select o from ProductManufactureOutput o where o.product = ?1")
    List<ProductManufactureOutput> findByProduct(Product product);
}
