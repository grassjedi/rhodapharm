package rhodapharmacy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rhodapharmacy.domain.RawMaterial;
import rhodapharmacy.repo.RawMaterialRepository;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/rest/raw-material")
public class RawMaterialRestController {

    private RawMaterialRepository rawMaterialRepository;

    public RawMaterialRestController(RawMaterialRepository rawMaterialRepository) {
        this.rawMaterialRepository = rawMaterialRepository;
    }

    @GetMapping
    public List<RawMaterial> search(String q)
    throws SQLException {
        return rawMaterialRepository.findRawMaterial(q);
    }
}
