package rhodapharmacy.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "product_value")
public class ProductValue implements Comparable<ProductValue>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.ALL)
    private Product product;

    @ManyToOne(cascade = CascadeType.ALL)
    private User user;

    private Date effectiveDate;
    private Long value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Product getProduct() {
        return product;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public int compareTo(ProductValue o) {
        if(o == null) {
            return 1;
        }
        else if(o.effectiveDate != null && this.effectiveDate != null) {
            return this.effectiveDate.compareTo(o.effectiveDate);
        }
        else if(o.effectiveDate == null && this.effectiveDate != null) {
            return 1;
        }
        else if(o.effectiveDate != null) {
            return -1;
        }
        return 0;
    }
}
