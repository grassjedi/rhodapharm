package rhodapharmacy.domain;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FormulationPrimaryKey implements Serializable {

    private Long productId;
    private Long rawMaterialId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getRawMaterialId() {
        return rawMaterialId;
    }

    public void setRawMaterialId(Long rawMaterialId) {
        this.rawMaterialId = rawMaterialId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass())
                && Objects.equals(productId, ((FormulationPrimaryKey) obj).productId)
                && Objects.equals(rawMaterialId, ((FormulationPrimaryKey) obj).rawMaterialId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, rawMaterialId);
    }
}
