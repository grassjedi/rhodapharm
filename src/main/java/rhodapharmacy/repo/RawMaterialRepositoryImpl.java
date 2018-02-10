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
                "SELECT " +
                "   m.id AS id, " +
                "   m.name AS name, " +
                "   m.units AS units, " +
                "   m.disabled AS disabled, " +
                "   SUM(r.quantity) AS totalQuantity, " +
                "   SUM(r.value) AS totalValue " +
                "FROM raw_material AS m " +
                "LEFT JOIN raw_material_receipt AS r ON m.id = r.raw_material_id " +
                "WHERE m.disabled = false " +
                "GROUP BY m.id ");
        query.setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                RawMaterial material = new RawMaterial();
                material.setId(((BigInteger)tuple[0]).longValue());
                material.setName((String)tuple[1]);
                material.setUnits(Unit.valueOf((String)tuple[2]));
                material.setDisabled((Boolean) tuple[3]);
                if(tuple[4] != null) {
                    material.setTotalQuantity((Float) tuple[4]);
                }
                if(tuple[5] != null) {
                    material.setTotalValue(((BigInteger) tuple[5]).longValue());
                }
                return material;
            }
        });
        return (List<RawMaterial>) query.list();
    }
}
