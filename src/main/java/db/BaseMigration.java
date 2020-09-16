package db;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

public abstract class BaseMigration extends BaseJavaMigration
{
    private EntityManager entityManager = null;

    protected EntityManager buildEntityManager(Context context)
    {
        if (entityManager != null) {
            return entityManager;
        }

        LocalContainerEntityManagerFactoryBean b =
            new LocalContainerEntityManagerFactoryBean();

        b.setDataSource(context.getConfiguration().getDataSource());
        b.setPackagesToScan(
    		"eu.daiad.web.domain.admin",
    		"eu.daiad.web.domain.application"
		);
        b.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        b.setJpaProperties(new Properties());
        b.setPersistenceUnitName("default-migration");
        b.setPersistenceProviderClass(HibernatePersistenceProvider.class);

        b.afterPropertiesSet();
        EntityManagerFactory emf = b.getObject();

        entityManager = emf.createEntityManager();
        return entityManager;
    }

    @Override
    public void migrate(Context context)
    {
        EntityManager em = buildEntityManager(context);
        EntityTransaction t = em.getTransaction();
        t.begin();
        try {
            migrate(em);
        } catch (RuntimeException e) {
            t.rollback();
            throw e;
        }
        t.commit();
    }

    public abstract void migrate(EntityManager em) throws RuntimeException;
}
