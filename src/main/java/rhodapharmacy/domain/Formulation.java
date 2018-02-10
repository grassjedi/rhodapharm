package rhodapharmacy.domain;

import javax.persistence.*;

@Entity
@Table(name = "formulation")
public class Formulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    private Product product;

    @ManyToOne(cascade = CascadeType.ALL)
    private RawMaterial rawMaterial;

    private Float quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RawMaterial getRawMaterial() {
        return rawMaterial;
    }

    public void setRawMaterial(RawMaterial rawMaterial) {
        this.rawMaterial = rawMaterial;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    @Transient
    public String getQuantityDescription() {
        return String.format("%1$,.2f", quantity) + " " + this.rawMaterial.getUnits().getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Formulation
                && (id != null && id.equals(((Formulation) obj).id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
