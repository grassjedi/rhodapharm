package rhodapharmacy.repo;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.BasicTransformerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rhodapharmacy.Unit;
import rhodapharmacy.domain.RawMaterial;

import java.math.BigInteger;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RawMaterialRepositoryImpl implements RawMaterialCustomRepository {

    private SessionFactory sessionFactory;

    public RawMaterialRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<RawMaterial> findAllEnabledRawMaterialAndLevels() {
        Session session = sessionFactory.getCurrentSession();
        SQLQuery query = session.createSQLQuery("" +
                "WITH usage_table AS (\n" +
                "   SELECT\n" +
                "      f.raw_material_id AS raw_material_id,\n" +
                "      SUM(o.quantity * f.quantity) AS used\n" +
                "   FROM product_manufacture_output AS o\n" +
                "   LEFT JOIN formulation AS f ON o.product_id = f.product_id\n" +
                "   GROUP BY f.raw_material_id\n" +
                ")\n" +
                "SELECT\n" +
                "   m.id AS id,\n" +
                "   m.name AS name,\n" +
                "   m.units AS units,\n" +
                "   m.disabled AS disabled,\n" +
                "   SUM(r.quantity) AS raw_material_qty,\n" +
                "   SUM(r.value) AS raw_material_value,\n" +
                "   (SELECT used FROM usage_table AS u WHERE u.raw_material_id = m.id)\n" +
                "FROM raw_material_receipt AS r\n" +
                "LEFT JOIN raw_material m ON r.raw_material_id = m.id\n" +
                "GROUP BY m.id");
        query.setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                RawMaterial material = new RawMaterial();
                material.setId(((BigInteger)tuple[0]).longValue());
                material.setName((String)tuple[1]);
                material.setUnits(Unit.valueOf((String)tuple[2]));
                material.setDisabled((Boolean) tuple[3]);
                if(tuple[4] != null) {
                    material.setTotalQuantity(((Number) tuple[4]).floatValue());
                }
                if(tuple[5] != null) {
                    material.setTotalValue(((Number) tuple[5]).longValue());
                }
                if(tuple[6] != null) {
                    material.setTotalUsage(((Number) tuple[6]).floatValue());
                }
                return material;
            }
        });
        return (List<RawMaterial>) query.list();
    }
}
