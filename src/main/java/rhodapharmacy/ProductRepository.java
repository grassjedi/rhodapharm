package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Service
public class ProductRepository {

    private static Logger log = LoggerFactory.getLogger(ProductRepository.class);

    private DataSource dataSource;

    public ProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void create(final Product product)
    throws SQLException {
        new SqlTemplate<RawMaterial>(dataSource) {
            @Override
            public RawMaterial doAction(Connection connection)
            throws SQLException {
                PreparedStatement productStatement = connection.prepareStatement("" +
                        "INSERT INTO product (name) " +
                        "VALUES (?) RETURNING id");

                PreparedStatement formulationStatement = connection.prepareStatement("" +
                        "INSERT INTO formulation (product_id, raw_material_id, quantity) " +
                        "VALUES (?, ?, ?)");
                productStatement.setString(1, product.getName());
                productStatement.execute();
                ResultSet resultSet = productStatement.getResultSet();
                Long productId = null;
                if(resultSet.next()) {
                    productId = resultSet.getLong("id");
                }
                for (Formulation formulation : product.getFormulation()) {
                    formulationStatement.setLong(1, productId);
                    formulationStatement.setLong(2, formulation.getRawMaterial().getId());
                    formulationStatement.setLong(3, formulation.getQuantity());
                    formulationStatement.executeUpdate();
                }
                return null;
            }
        }.execute();
    }

    public void update(Product product)
    throws SQLException {
        final int maxBatchSize = 100;
        final List<Product> batchEntities = new LinkedList<>();
        new SqlTemplate<RawMaterial>(dataSource) {
            @Override
            public RawMaterial doAction(Connection connection)
                    throws SQLException {
                PreparedStatement productUpdateStatement = connection.prepareStatement("" +
                        "UPDATE product SET name = ? " +
                        "WHERE id = ?");
                PreparedStatement formulationDeleteStatement = connection.prepareStatement("" +
                        "DELETE FROM formulation " +
                        "WHERE product_id = ?");
                PreparedStatement formulationInsertStatement = connection.prepareStatement("" +
                        "INSERT INTO formulation (product_id, raw_material_id, quantity) " +
                        "VALUES (?, ?, ?)");
                productUpdateStatement.setString(1, product.getName());
                productUpdateStatement.setLong(2, product.getId());
                productUpdateStatement.executeUpdate();

                formulationDeleteStatement.setLong(1, product.getId());

                for(Formulation formulation : product.getFormulation()) {
                    formulationInsertStatement.setLong(1, product.getId());
                    formulationInsertStatement.setLong(2, formulation.getId());
                    formulationInsertStatement.setLong(3, formulation.getQuantity());
                    formulationInsertStatement.addBatch();
                }
                formulationInsertStatement.executeBatch();
                return null;
            }
        }.execute();
    }

    public Product retrieve(Long id)
    throws SQLException {
        return new SqlTemplate<Product>(dataSource) {
            @Override
            public Product doAction(Connection connection)
                    throws SQLException {
                PreparedStatement statement = connection.prepareStatement("" +
                        "SELECT " +
                        "   p.id AS product_id, " +
                        "   p.name AS product_name, " +
                        "   p.disabled AS product_disabled, " +
                        "   f.id AS formulation_id, " +
                        "   f.quantity AS formulation_quantity, " +
                        "   r.id AS raw_material_id, " +
                        "   r.name AS raw_material_name," +
                        "   r.units AS raw_material_units " +
                        "FROM product AS p " +
                        "LEFT JOIN formulation AS f ON p.id = f.product_id " +
                        "LEFT JOIN raw_material AS r ON f.raw_material_id = r.id " +
                        "WHERE p.id = ? " +
                        "ORDER BY product_id, formulation_id, raw_material_id");
                statement.setLong(1, id);
                ResultSet resultSet = statement.executeQuery();
                return mapProduct(resultSet).get(0);
            }
        }.execute();
    }

    public List<Product> mapProduct(ResultSet resultSet)
    throws SQLException {
        List<Product> products = new LinkedList<>();
        Long currentProductId = null;
        Long currentFormulationId = null;
        Product currentProduct = null;
        while(resultSet.next()) {
            Long resultSetProductId = resultSet.getLong("product_id");
            if(products.isEmpty() || !Objects.equals(resultSetProductId, currentProductId)) {
                currentProductId = resultSetProductId;
                currentProduct = new Product();
                currentProduct.setId(resultSetProductId);
                currentProduct.setFormulation(new LinkedList<>());
                currentProduct.setName(resultSet.getString("product_name"));
                currentProduct.setDisabled(resultSet.getBoolean("product_disabled"));
                products.add(currentProduct);
            }
            Long resultSetFormulationId = resultSet.getLong("formulation_id");
            RawMaterial rawMaterial = new RawMaterial();
            rawMaterial.setId(resultSet.getLong("raw_material_id"));
            rawMaterial.setName(resultSet.getString("raw_material_name"));
            rawMaterial.setUnits(Unit.valueOf(resultSet.getString("raw_material_units")));
            currentFormulationId = resultSetFormulationId;
            Formulation formulation = new Formulation();
            formulation.setId(currentFormulationId);
            formulation.setRawMaterial(rawMaterial);
            formulation.setQuantity(resultSet.getLong("formulation_quantity"));
            currentProduct.getFormulation().add(formulation);
        }
        return products;
    }

    public List<Product> listProducts()
    throws SQLException {
        return new SqlTemplate<List<Product>>(dataSource) {
            @Override
            public List<Product> doAction(Connection connection)
                    throws SQLException {
                PreparedStatement statement = connection.prepareStatement("" +
                        "SELECT " +
                        "   p.id AS product_id, " +
                        "   p.name AS product_name, " +
                        "   p.disabled AS product_disabled, " +
                        "   f.id AS formulation_id, " +
                        "   f.quantity AS formulation_quantity, " +
                        "   r.id AS raw_material_id, " +
                        "   r.name AS raw_material_name," +
                        "   r.units AS raw_material_units " +
                        "FROM product AS p " +
                        "LEFT JOIN formulation AS f ON p.id = f.product_id " +
                        "LEFT JOIN raw_material AS r ON f.raw_material_id = r.id " +
                        "ORDER BY product_id, formulation_id, raw_material_id ");
                ResultSet resultSet = statement.executeQuery();
                List<Product> products = mapProduct(resultSet);
                products.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
                return products;
            }
        }.execute();
    }

    public void setDisabled(Long productId, Boolean disabled)
            throws SQLException {
        new SqlTemplate<List<RawMaterial>>(dataSource) {
            @Override
            public List<RawMaterial> doAction(Connection connection)
                    throws SQLException {
                PreparedStatement statement = connection.prepareStatement("UPDATE product SET disabled = ? WHERE id = ?");
                statement.setBoolean(1, disabled);
                statement.setLong(2, productId);
                statement.executeUpdate();
                return null;
            }
        }.execute();
    }
}
