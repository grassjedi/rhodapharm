package rhodapharmacy.repo;

import org.springframework.data.repository.CrudRepository;
import rhodapharmacy.domain.Formulation;

public interface FormulationRepository extends CrudRepository<Formulation, Long> {
}
