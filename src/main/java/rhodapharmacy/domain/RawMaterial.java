package rhodapharmacy.domain;

import rhodapharmacy.Unit;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
public class RawMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Unit units;
    private Boolean disabled;

    @OneToMany(cascade = CascadeType.ALL)
    private List<RawMaterialReceipt> receipts;

    @Transient
    private Float totalQuantity = 0.0f;

    @Transient
    private Long totalValue = 0L;

    @Transient
    private Float totalUsage = 0.0f;

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

    public Unit getUnits() {
        return units;
    }

    public void setUnits(Unit units) {
        this.units = units;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public List<RawMaterialReceipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<RawMaterialReceipt> receipts) {
        this.receipts = receipts;
    }

    public Float getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Float totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Long getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Long totalValue) {
        this.totalValue = totalValue;
    }

    public Float getTotalUsage() {
        return totalUsage;
    }

    public void setTotalUsage(Float totalUsage) {
        this.totalUsage = totalUsage;
    }

    public Float nettTotalQuantity() {
        return this.totalQuantity - totalUsage;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass())
                && (id != null && id.equals(((RawMaterial) obj).id) || ((RawMaterial) obj).id == null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
