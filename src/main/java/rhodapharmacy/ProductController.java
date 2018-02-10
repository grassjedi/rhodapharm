package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import rhodapharmacy.domain.*;
import rhodapharmacy.repo.*;

import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping(path = "/product")
public class ProductController {

    private static Logger log = LoggerFactory.getLogger(ProductController.class);

    private ProductRepository productRepository;
    private ProductManufactureOutputRepository productManufactureOutputRepository;
    private RawMaterialRepository rawMaterialRepository;
    private FormulationRepository formulationRepository;
    private UserRepository userRepository;
    private ProductValueRepository productValueRepository;

    public ProductController(
            ProductRepository productRepository,
            ProductManufactureOutputRepository productManufactureOutputRepository,
            RawMaterialRepository rawMaterialRepository,
            FormulationRepository formulationRepository,
            UserRepository userRepository,
            ProductValueRepository productValueRepository) {
        this.productRepository = productRepository;
        this.rawMaterialRepository = rawMaterialRepository;
        this.formulationRepository = formulationRepository;
        this.productManufactureOutputRepository = productManufactureOutputRepository;
        this.userRepository = userRepository;
        this.productValueRepository = productValueRepository;
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

    @PostMapping("/{productId}")
    @Transactional
    public String show(
            @RequestAttribute UserSession userSession,
            @PathVariable(name = "productId") Long productId,
            String name,
            Float value,
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date valueEffectiveDate)
    throws SQLException {
        if(!userSession.hasRole(UserRole.PRODUCT_MAINTENANCE)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform product administration");
        }
        User attachedUser = userRepository.findOne(userSession.getUser().getId());
        Product product = productRepository.findOne(productId);
        if(valueEffectiveDate != null && value != null) {
            log.debug("Saving new value " + value);
            ProductValue productValue = new ProductValue();
            productValue.setValue(((Number)(value * 100L)).longValue());
            productValue.setProduct(product);
            productValue.setUser(attachedUser);
            productValue.setEffectiveDate(valueEffectiveDate);
            productValueRepository.save(productValue);
        }
        if(name != null) {
            String trimmedName = name.trim();
            if(!trimmedName.isEmpty() && !trimmedName.equals(product.getName())) {
                product.setName(trimmedName);
                productRepository.save(product);
            }
        }
        return "redirect:/product/" + product.getId();
    }

    @GetMapping("{productId}/stock")
    public ModelAndView viewProductManufactureOutput(
            @RequestAttribute(name = "userSession") UserSession userSession,
            @PathVariable(name = "productId") Long productId
    ) {
        if(!userSession.hasRole(UserRole.PRODUCT_MANUFACTURE_ADMIN)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform raw-material administration");
        }
        Product product = productRepository.findOne(productId);
        List<ProductManufactureOutput> outputs = productManufactureOutputRepository.findByProduct(product);
        Map<String, Object> model = new HashMap<>();
        model.put("outputs", outputs);
        model.put("product", product);
        return new ModelAndView("product-manufacture-output", model);
    }

    @PostMapping(path = "{productId}/stock")
    @Transactional
    String captureStockReceipt(
            @RequestAttribute(name = "userSession") UserSession userSession,
            @PathVariable(name = "productId") Long productId,
            Long quantity
    ) {
        if(!userSession.hasRole(UserRole.PRODUCT_MANUFACTURE_ADMIN)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform raw-material administration");
        }
        Product product = productRepository.findOne(productId);
        ProductManufactureOutput output = new ProductManufactureOutput();
        User outputUser = userRepository.findOne(userSession.getUser().getId());
        output.setUser(outputUser);
        output.setProduct(product);
        output.setDateCaptured(new Date());
        output.setQuantity(quantity);
        productManufactureOutputRepository.save(output);
        return "redirect:/product/" + productId + "/stock";
    }
}
