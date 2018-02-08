package rhodapharmacy.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
public class RawMaterialReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne private RawMaterial rawMaterial;
    @ManyToOne private User user;
    private Date dateCaptured;
    private Date invoiceDate;
    private String supplier;
    private Long quantity;
    private Long value;

    public RawMaterial getRawMaterial() {
        return rawMaterial;
    }

    public void setRawMaterial(RawMaterial rawMaterial) {
        this.rawMaterial = rawMaterial;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDateCaptured() {
        return dateCaptured;
    }

    public void setDateCaptured(Date dateCaptured) {
        this.dateCaptured = dateCaptured;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
