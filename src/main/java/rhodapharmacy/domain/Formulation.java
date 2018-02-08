package rhodapharmacy.domain;

import javax.persistence.*;

@Entity
@Table(name = "formulation")
public class Formulation {

    @EmbeddedId
    private FormulationPrimaryKey id;

    @ManyToOne
    @MapsId("productId")
    private Product product;

    @ManyToOne
    @MapsId("rawMaterialId")
    private RawMaterial rawMaterial;

    private Float quantity;

    public Formulation() {
        id = new FormulationPrimaryKey();
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
