package rhodapharmacy.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "product_manufacture_output")
public class ProductManufactureOutput {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    private Product product;

    private Date dateCaptured;
    private Long quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Date getDateCaptured() {
        return dateCaptured;
    }

    public void setDateCaptured(Date dateCaptured) {
        this.dateCaptured = dateCaptured;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quatity) {
        this.quantity = quatity;
    }

    @Transient
    public Long getValue() {
        ProductValue productValue = product.findProductValueAt(dateCaptured);
        if(productValue == null) return null;
        return productValue.getValue() * quantity;
    }
}
