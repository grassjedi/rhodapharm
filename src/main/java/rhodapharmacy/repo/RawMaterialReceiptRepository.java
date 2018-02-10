package rhodapharmacy.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rhodapharmacy.domain.RawMaterial;
import rhodapharmacy.domain.RawMaterialReceipt;

import java.util.List;

public interface RawMaterialReceiptRepository extends JpaRepository<RawMaterialReceipt, Long> {

    @Query("select r from RawMaterialReceipt r where r.rawMaterial = ?1")
    List<RawMaterialReceipt> findByRawMaterial(RawMaterial rawMaterial);
}
