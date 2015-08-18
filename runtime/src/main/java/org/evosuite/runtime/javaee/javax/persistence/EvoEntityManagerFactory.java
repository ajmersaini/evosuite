package org.evosuite.runtime.javaee.javax.persistence;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A wrapper over an actual EntityManagerFactory.
 * Needed to create and control wrapped instances of EntityManager
 *
 * Created by Andrea Arcuri on 16/06/15.
 */
public class EvoEntityManagerFactory implements EntityManagerFactory{

    private final EntityManagerFactory factory;

    private final List<EvoEntityManager> managers;

    public EvoEntityManagerFactory(){
        factory = createEMFWithSpring();
        managers = new ArrayList<>();
    }

    private EntityManagerFactory createEMFWithSpring(){

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:.");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("**.*"); //search everything on classpath
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties properties = new Properties();
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.setProperty("hibernate.connection.shutdown", "true");
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");

        properties.setProperty("hibernate.classloading.use_current_tccl_as_parent", "false");

        em.setJpaProperties(properties);

        em.afterPropertiesSet();

        return em.getObject();
    }

    public void clearAllEntityManagers(){
        for(EvoEntityManager em : managers){
            if(em!=null){
                if(em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                if(em.isOpen()) {
                    em.close();
                }
            }
        }
        managers.clear();
    }

    //----------  EntityManagerFactory ---------------


    @Override
    public EntityManager createEntityManager() {
        EntityManager em = factory.createEntityManager();
        EvoEntityManager evo = new EvoEntityManager(em,this);
        managers.add(evo);

        return evo;
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        EntityManager em = factory.createEntityManager(map);
        EvoEntityManager evo = new EvoEntityManager(em,this);
        managers.add(evo);

        return evo;
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        EntityManager em = factory.createEntityManager(synchronizationType);
        EvoEntityManager evo = new EvoEntityManager(em,this);
        managers.add(evo);

        return evo;
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        EntityManager em = factory.createEntityManager(synchronizationType,map);
        EvoEntityManager evo = new EvoEntityManager(em,this);
        managers.add(evo);

        return evo;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return factory.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return factory.getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return factory.isOpen();
    }

    @Override
    public void close() {
        factory.close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return factory.getProperties();
    }

    @Override
    public Cache getCache() {
        return factory.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return factory.getPersistenceUnitUtil();
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        factory.addNamedQuery(name,query);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return factory.unwrap(cls);
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        factory.addNamedEntityGraph(graphName,entityGraph);
    }
}
