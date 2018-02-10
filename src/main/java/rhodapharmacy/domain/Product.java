package rhodapharmacy.domain;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "product")
    private List<Formulation> formulation;
    private Boolean disabled;
    @OneToMany(cascade = CascadeType.ALL)
    private List<ProductManufactureOutput> productManufactureOutput;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Formulation> getFormulation() {
        return formulation;
    }

    public void setFormulation(List<Formulation> formulation) {
        this.formulation = formulation;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public void addFormulation(Formulation formulation) {
        formulation.setProduct(this);
    }

    public List<ProductManufactureOutput> getProductManufactureOutput() {
        return productManufactureOutput;
    }

    public void setProductManufactureOutput(List<ProductManufactureOutput> productManufactureOutput) {
        this.productManufactureOutput = productManufactureOutput;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass())
                && (id != null && id.equals(((Product) obj).id) || ((Product) obj).id == null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
