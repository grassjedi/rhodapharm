package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLException;
import java.util.HashMap;
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
            @RequestAttribute UserSession userSession,
            Long offset,
            Long limit)
    throws SQLException {
        if(!userSession.hasRole(UserRole.RAW_MATERIAL_MAINTENANCE)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform raw-material administration");
        }
        if(offset == null || offset <= -1) {
            offset = 0L;
        }
        if(limit == null || limit <= 0) {
            limit = 50L;
        }
        Map<String, Object> model = new HashMap<>();
        model.put("allUnits", Unit.values());
        List<RawMaterial> rawMaterials = rawMaterialRepository.findRawMaterial(offset, limit);
        log.debug("found {} raw-materials", rawMaterials.size());
        model.put("rawMaterials", rawMaterials);
        model.put("offset", offset);
        model.put("limit", limit);
        model.put("count", rawMaterials.size());
        model.put("nextOffset", offset + limit);
        model.put("hasMore", rawMaterials.size() == limit);
        model.put("hasPrev", offset > 0);
        model.put("prevOffset", Math.max(0L, offset - limit));
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
        RawMaterial rawMaterial = rawMaterialRepository.retrieve(rawMaterialId);
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
            Long offset,
            Long limit)
    throws SQLException{
        if(!userSession.hasRole(UserRole.RAW_MATERIAL_MAINTENANCE)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform raw-material administration");
        }
        if(limit == null) {
            limit = 50L;
        }
        if(offset == null) {
            offset = 0L;
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
            rawMaterialRepository.create(name, unit);
        }
        else if("update".equalsIgnoreCase(operation)) {
            if(rawMaterialId == null || name == null || name.isEmpty() || unit == null) {
                throw new XUnsupportedOperation(
                        String.format("can't update raw material (%s) with an empty name (%s) or without a unit (%s)",
                                rawMaterialId == null ? "null" : rawMaterialId.toString(),
                                name == null ? "null" : name,
                                unit == null ? "null" : unit.name()));
            }
            rawMaterialRepository.update(rawMaterialId, name, unit);
        }
        else {
            throw new XUnsupportedOperation("can't do that!");
        }
        return "redirect:/raw-material?offset=" + offset + "&limit=" + limit;
    }
}
