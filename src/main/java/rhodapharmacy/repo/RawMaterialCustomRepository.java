package rhodapharmacy.repo;


import rhodapharmacy.domain.RawMaterial;

import java.util.List;

public interface RawMaterialCustomRepository {

    List<RawMaterial> findAllEnabledRawMaterialAndLevels();
}
