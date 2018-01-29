package rhodapharmacy;

import java.util.List;

public class Product {
    private Long id;
    private String name;
    private List<Formulation> formulation;
    private Boolean disabled;

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
}
