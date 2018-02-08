package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import rhodapharmacy.domain.RawMaterial;
import rhodapharmacy.domain.UserSession;
import rhodapharmacy.repo.RawMaterialRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/raw-material")
public class RawMaterialController {

    private static Logger log = LoggerFactory.getLogger(RawMaterialController.class);

    private RawMaterialRepository rawMaterialRepository;

    public RawMaterialController(RawMaterialRepository rawMaterialRepository) {
        this.rawMaterialRepository = rawMaterialRepository;
    }

    @GetMapping
    public ModelAndView home(
            @RequestAttribute UserSession userSession)
    throws SQLException {
        if(!userSession.hasRole(UserRole.RAW_MATERIAL_MAINTENANCE)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform raw-material administration");
        }
        Map<String, Object> model = new HashMap<>();
        model.put("allUnits", Unit.values());
        Iterable<RawMaterial> rawMaterialIterable = rawMaterialRepository.findAll();
        List<RawMaterial> rawMaterials = new LinkedList<>();
        rawMaterialIterable.forEach(rawMaterials::add);
        log.debug("found {} raw-materials", rawMaterials.size());
        model.put("rawMaterials", rawMaterials);
        model.put("count", rawMaterials.size());
        return new ModelAndView("raw-material", model);
    }

    @GetMapping(path = "{rawMaterialId}")
    public ModelAndView showRawMaterial(
            @RequestAttribute UserSession userSession,
            @PathVariable(name = "rawMaterialId") Long rawMaterialId,
            Long offset,
            Long limit)
    throws SQLException{
        if(!userSession.hasRole(UserRole.RAW_MATERIAL_MAINTENANCE)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform raw-material administration");
        }
        Map<String, Object> model = new HashMap<>();
        if(rawMaterialId == null) {
            throw new XNoSuchRecord();
        }
        RawMaterial rawMaterial = rawMaterialRepository.findOne(rawMaterialId);
        if(rawMaterial == null) {
            throw new XNoSuchRecord();
        }
        if(offset == null) offset = 0L;
        if(limit == null) limit = 50L;
        model.put("offset", offset);
        model.put("limit", limit);
        model.put("allUnits", Unit.values());
        model.put("rawMaterial", rawMaterial);
        return new ModelAndView("show_raw-material", model);
    }

    @PostMapping
    public String createOrUpdate(
            @RequestAttribute UserSession userSession,
            String operation,
            Long rawMaterialId,
            String name,
            String units,
            Boolean disabled)
    throws SQLException{
        if(!userSession.hasRole(UserRole.RAW_MATERIAL_MAINTENANCE)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform raw-material administration");
        }
        Unit unit = null;
        if(units != null) {
            units = units.trim().toUpperCase();
            unit = Unit.valueOf(units);
        }
        if("create".equalsIgnoreCase(operation)) {
            if(name == null || name.isEmpty() || unit == null) {
                throw new XUnsupportedOperation(
                        String.format("can't create raw material with an empty name (%s) or without a unit (%s)",
                                name == null ? "null" : name,
                                unit == null ? "null" : unit.name()));
            }
            RawMaterial rawMaterial = new RawMaterial();
            rawMaterial.setUnits(Unit.valueOf(units));
            rawMaterial.setName(name);
            rawMaterial.setDisabled(disabled == null ? Boolean.FALSE : disabled);
            rawMaterialRepository.save(rawMaterial);
        }
        else if("update".equalsIgnoreCase(operation)) {
            if(rawMaterialId == null || name == null || name.isEmpty() || unit == null) {
                throw new XUnsupportedOperation(
                        String.format("can't update raw material (%s) with an empty name (%s) or without a unit (%s)",
                                rawMaterialId == null ? "null" : rawMaterialId.toString(),
                                name == null ? "null" : name,
                                unit == null ? "null" : unit.name()));
            }
            RawMaterial rawMaterial = rawMaterialRepository.findOne(rawMaterialId);
            if(rawMaterial == null) {
                throw new XNoSuchRecord();
            }
            rawMaterial.setName(name);
            rawMaterial.setDisabled(disabled == null ? rawMaterial.getDisabled() : disabled);
            rawMaterial.setUnits(Unit.valueOf(units));
            rawMaterialRepository.save(rawMaterial);
        }
        else if("disable".equalsIgnoreCase(operation) || "enable".equalsIgnoreCase(operation)) {
            if(rawMaterialId == null || disabled == null) {
                throw new XUnsupportedOperation("cannot enable / disable raw-material without id or disabled value");
            }
            RawMaterial rawMaterial = rawMaterialRepository.findOne(rawMaterialId);
            if(rawMaterial == null) {
                throw new XNoSuchRecord();
            }
            log.debug("setting disabled to {} for material {}", disabled, rawMaterialId);
            rawMaterial.setDisabled(disabled);
            rawMaterialRepository.save(rawMaterial);
        }
        else {
            throw new XUnsupportedOperation("can't do that!");
        }
        return "redirect:/raw-material";
    }
}
