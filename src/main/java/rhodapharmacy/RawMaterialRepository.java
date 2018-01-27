package rhodapharmacy;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Service
public class RawMaterialRepository {

    private DataSource dataSource;

    public RawMaterialRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void create(String name, Unit unit)
    throws SQLException {
        RawMaterial rawMaterial = new RawMaterial();
        rawMaterial.setName(name);
        rawMaterial.setUnits(unit);
        create(rawMaterial);
    }

    public void create(final List<RawMaterial> rawMaterial)
    throws SQLException {
        final int maxBatchSize = 100;
        final List<RawMaterial> batchEntities = new LinkedList<>();
        new SqlTemplate<RawMaterial>(dataSource) {
            @Override
            public RawMaterial doAction(Connection connection)
            throws SQLException {
                PreparedStatement statement = connection.prepareStatement("" +
                        "INSERT INTO raw_material (name, description, units) " +
                        "VALUES (?, ?, ?) RETURNING id");
                for(int i = 0; i < rawMaterial.size(); i++) {
                    RawMaterial material = rawMaterial.get(i);
                    statement.setString(1, material.getName());
                    statement.setObject(2, material.getDescripion(), Types.VARCHAR);
                    statement.setString(3, material.getUnits().name());
                    batchEntities.add(material);
                    statement.addBatch();
                    if(batchEntities.size() == maxBatchSize || (i == (rawMaterial.size() - 1))) {
                        statement.executeBatch();
                        batchEntities.clear();
                    }
                }
                return null;
            }
        }.execute();
    }

    public void create(final RawMaterial rawMaterial)
    throws SQLException {
        this.create(Collections.singletonList(rawMaterial));
    }

    public void delete(final List<RawMaterial> rawMaterial)
    throws SQLException {
        final Timestamp deleted = new Timestamp(System.currentTimeMillis());
        final int maxBatchSize = 100;
        final List<RawMaterial> batch = new LinkedList<>();
        new SqlTemplate<RawMaterial>(dataSource) {
            @Override
            public RawMaterial doAction(Connection connection)
                    throws SQLException {
                PreparedStatement statement = connection.prepareStatement("UPDATE raw_material SET deleted = ? WHERE id = ?");
                int batchSize = 0;
                for(int i = 0; i < rawMaterial.size(); i++) {
                    RawMaterial material = rawMaterial.get(i);
                    statement.setTimestamp(1, deleted);
                    statement.setLong(2, material.getId());
                    batch.add(material);
                    statement.addBatch();
                    if(batch.size() == maxBatchSize || (i == (rawMaterial.size() - 1))) {
                        statement.executeBatch();
                        batchSize = 0;
                    }
                }
                return null;
            }
        }.execute();
    }

    public void delete(RawMaterial rawMaterial)
    throws SQLException {
        delete(Collections.singletonList(rawMaterial));
    }

    public RawMaterial retrieve(final Long id)
    throws SQLException {
        return new SqlTemplate<RawMaterial>(dataSource) {
            @Override
            public RawMaterial doAction(Connection connection)
                    throws SQLException {
                PreparedStatement statement = connection.prepareStatement("SELECT id, name, description, units FROM raw_material WHERE id = ?");
                statement.setLong(1, id);
                ResultSet resultSet = statement.executeQuery();
                List<RawMaterial> rawMaterials = mapRawMaterials(resultSet, true);
                if(rawMaterials.isEmpty()) {
                    throw new SQLException("no such record for id #" + id);
                }
                return rawMaterials.get(0);
            }
        }.execute();
    }

    public List<RawMaterial> findRawMaterial(String search, Long offset, Long max)
    throws SQLException {
        if(search == null || search.trim().isEmpty() || search.trim().length() < 3) return Collections.emptyList();
        return new SqlTemplate<List<RawMaterial>>(dataSource) {
            @Override
            public List<RawMaterial> doAction(Connection connection)
                    throws SQLException {
                PreparedStatement statement = connection.prepareStatement("SELECT id, name, description, units FROM raw_material WHERE name ILIKE ? OR description ILIKE ? ORDER BY name OFFSET ? LIMIT ?");
                String param = "%" + search.trim() + "%";
                statement.setString(1, param);
                statement.setString(2, param);
                statement.setLong(3, offset);
                statement.setLong(4, max);
                ResultSet resultSet = statement.executeQuery();
                return mapRawMaterials(resultSet, false);
            }
        }.execute();
    }

    public List<RawMaterial> findRawMaterial(Long offset, Long max)
    throws SQLException {
        return new SqlTemplate<List<RawMaterial>>(dataSource) {
            @Override
            public List<RawMaterial> doAction(Connection connection)
                    throws SQLException {
                PreparedStatement statement = connection.prepareStatement("SELECT id, name, description, units FROM raw_material ORDER BY name OFFSET ? LIMIT ?");
                statement.setLong(1, offset);
                statement.setLong(2, max);
                ResultSet resultSet = statement.executeQuery();
                return mapRawMaterials(resultSet, false);
            }
        }.execute();
    }

    public void update(final Long rawMaterialId, final String name, final Unit unit)
    throws SQLException {
        new SqlTemplate<List<RawMaterial>>(dataSource) {
            @Override
            public List<RawMaterial> doAction(Connection connection)
                    throws SQLException {
                PreparedStatement statement = connection.prepareStatement("UPDATE raw_material SET name= ?, units = ? WHERE id = ?");
                statement.setString(1, name);
                statement.setString(2, unit.name());
                statement.setLong(3, rawMaterialId);
                statement.executeUpdate();
                return null;
            }
        }.execute();
    }

    public List<RawMaterial> mapRawMaterials(ResultSet resultSet, boolean unique)
    throws SQLException {
        List<RawMaterial> result = new LinkedList<>();
        while(resultSet.next()) {
            if(unique && result.size() == 1) {
                throw new SQLException("failed to find unique result");
            }
            RawMaterial rawMaterial = new RawMaterial();
            rawMaterial.setId(resultSet.getLong("id"));
            rawMaterial.setName(resultSet.getString("name"));
            rawMaterial.setDescripion(resultSet.getString("description"));
            rawMaterial.setUnits(Unit.valueOf(resultSet.getString("units")));
            result.add(rawMaterial);
        }
        return result;
    }
}
