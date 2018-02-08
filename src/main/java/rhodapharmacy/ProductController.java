package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import rhodapharmacy.domain.Formulation;
import rhodapharmacy.domain.Product;
import rhodapharmacy.domain.RawMaterial;
import rhodapharmacy.domain.UserSession;
import rhodapharmacy.repo.FormulationRepository;
import rhodapharmacy.repo.ProductRepository;
import rhodapharmacy.repo.RawMaterialRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/product")
public class ProductController {

    private static Logger log = LoggerFactory.getLogger(ProductController.class);

    private ProductRepository productRepository;
    private RawMaterialRepository rawMaterialRepository;
    private FormulationRepository formulationRepository;

    public ProductController(ProductRepository productRepository, RawMaterialRepository rawMaterialRepository, FormulationRepository formulationRepository) {
        this.productRepository = productRepository;
        this.rawMaterialRepository = rawMaterialRepository;
        this.formulationRepository = formulationRepository;
    }

    @GetMapping
    public ModelAndView home(
            @RequestAttribute UserSession userSession)
    throws SQLException {
        if(!userSession.hasRole(UserRole.PRODUCT_MAINTENANCE)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform product administration");
        }
        Map<String, Object> model = new HashMap<>();
        List<RawMaterial> allRawMaterial = rawMaterialRepository.findAllEnabledRawMaterial();
        Long firstRawMaterialId = allRawMaterial == null || allRawMaterial.isEmpty() ? null : allRawMaterial.get(0).getId();
        model.put("firstRawMaterialId", firstRawMaterialId);
        model.put("rawMaterials", allRawMaterial);
        model.put("products", productRepository.findAll());
        return new ModelAndView("product", model);
    }

    @PostMapping
    public String createOrUpdate(
            @RequestAttribute UserSession userSession,
            String operation,
            String productName,
            Long[] rawMaterialId,
            Float[] quantity,
            Long productId,
            Boolean disabled)
    throws SQLException {
        if(!userSession.hasRole(UserRole.PRODUCT_MAINTENANCE)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform product administration");
        }
        if("create".equals(operation)) {
            if(rawMaterialId.length != quantity.length) {
                throw new XUnsupportedOperation(String.format("%d rawMaterials referenced and %d quantities supplied counts must match", rawMaterialId.length, quantity.length));
            }
            Product product = new Product();
            product.setName(productName);
            product.setDisabled(Boolean.FALSE);
            product.setFormulation(new LinkedList<>());
            product = productRepository.save(product);
            for(int i = 0; i < quantity.length; i++) {
                Formulation formulation = new Formulation();
                RawMaterial rawMaterial = rawMaterialRepository.findOne(rawMaterialId[i]);
                formulation.setRawMaterial(rawMaterial);
                formulation.setQuantity(quantity[i]);
                product.addFormulation(formulation);
                formulation.setProduct(product);
                formulationRepository.save(formulation);
            }
        }
        else if("disable".equals(operation)) {
            Product product = productRepository.findOne(productId);
            product.setDisabled(disabled == null ? product.getDisabled() : disabled);
            productRepository.save(product);
        }
        else {
            throw new XUnsupportedOperation("can't do that");
        }
        return "redirect:/product";
    }

    @GetMapping("/{productId}")
    public ModelAndView show(
            @RequestAttribute UserSession userSession,
            Long offset,
            Long limit,
            @PathVariable(name = "productId") Long productId)
    throws SQLException {
        if(!userSession.hasRole(UserRole.PRODUCT_MAINTENANCE)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform product administration");
        }
        if(offset == null) {
            offset = 0L;
        }
        if(limit == null) {
            limit = 50L;
        }
        Map<String, Object> model = new HashMap<>();
        model.put("offset", offset);
        model.put("limit", limit);
        model.put("product", productRepository.findOne(productId));
        return new ModelAndView("show_product", model);
    }
}
