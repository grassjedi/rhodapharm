package rhodapharmacy.domain;

import javax.persistence.*;
import java.util.*;

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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product")
    private List<ProductValue> values;

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

    public List<ProductValue> getValues() {
        return values;
    }

    public void setValues(List<ProductValue> values) {
        this.values = values;
    }

    @Transient
    public ProductValue getCurrentProductValue() {
        if(this.values == null || this.values.isEmpty()) return null;
        SortedSet<ProductValue> sortedValues = new TreeSet<>(this.values);
        return sortedValues.last();
    }

    @Transient
    public ProductValue findProductValueAt(Date date) {
        if(this.values == null || this.values.isEmpty()) return null;
        SortedSet<ProductValue> sortedValues = new TreeSet<>(this.values);
        ProductValue last = null;
        for(ProductValue value : sortedValues) {
            if(value.getEffectiveDate().equals(date) || value.getEffectiveDate().before(date)) {
                last = value;
            }
            else {
                break;
            }
        }
        return last;
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
