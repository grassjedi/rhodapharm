package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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

    public ProductController(ProductRepository productRepository, RawMaterialRepository rawMaterialRepository) {
        this.productRepository = productRepository;
        this.rawMaterialRepository = rawMaterialRepository;
    }

    @GetMapping
    public ModelAndView home(
            @RequestAttribute UserSession userSession)
    throws SQLException {
        if(!userSession.hasRole(UserRole.PRODUCT_MAINTENANCE)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform product administration");
        }
        Map<String, Object> model = new HashMap<>();
        List<RawMaterial> allRawMaterial = rawMaterialRepository.findAllRawMaterial(false);
        Long firstRawMaterialId = allRawMaterial == null || allRawMaterial.isEmpty() ? null : allRawMaterial.get(0).getId();
        model.put("firstRawMaterialId", firstRawMaterialId);
        model.put("rawMaterials", allRawMaterial);
        model.put("products", productRepository.listProducts());
        return new ModelAndView("product", model);
    }

    @PostMapping
    public String createOrUpdate(
            @RequestAttribute UserSession userSession,
            String operation,
            String productName,
            Long[] rawMaterialId,
            Long[] quantity,
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
            product.setFormulation(new LinkedList<>());
            for(int i = 0; i < quantity.length; i++) {
                Formulation formulation = new Formulation();
                formulation.setRawMaterial(new RawMaterial());
                formulation.getRawMaterial().setId(rawMaterialId[i]);
                formulation.setQuantity(quantity[i]);
                product.getFormulation().add(formulation);
            }
            productRepository.create(product);
        }
        else if("disable".equals(operation)) {
            productRepository.setDisabled(productId, disabled);
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
        model.put("product", productRepository.retrieve(productId));
        return new ModelAndView("show_product", model);
    }
}
