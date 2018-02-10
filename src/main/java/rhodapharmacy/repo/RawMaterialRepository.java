package rhodapharmacy.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import rhodapharmacy.domain.RawMaterial;

import java.util.List;

public interface RawMaterialRepository extends CrudRepository<RawMaterial, Long>, RawMaterialCustomRepository {

    @Query("select r from RawMaterial r where r.name like ?1")
    List<RawMaterial> findRawMaterial(String query);

    @Query("select r from RawMaterial r where r.disabled = false")
    List<RawMaterial> findAllEnabledRawMaterial();
}
