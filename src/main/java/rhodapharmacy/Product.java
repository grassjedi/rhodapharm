package rhodapharmacy;

import java.util.List;

public class Product {
    private Long id;
    private String name;
    private String description;
    private List<Formulation> formulation;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Formulation> getFormulation() {
        return formulation;
    }

    public void setFormulation(List<Formulation> formulation) {
        this.formulation = formulation;
    }
}
