package it.paolinucz.lazy.orm.config;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;

public class LazyJPAConfig {

    public static EntityManagerFactory entityManagerFactory() {

        Config config = ConfigFactory.load();

        HashMap<Object, Object> jpaProperties = new HashMap<>();

        jpaProperties.put("jakarta.persistence.jdbc.url", config.getString("lazy.db.url"));
        jpaProperties.put("jakarta.persistence.jdbc.user", config.getString("lazy.db.username"));
        jpaProperties.put("jakarta.persistence.jdbc.password", config.getString("lazy.db.password"));
        jpaProperties.put("jakarta.persistence.jdbc.driver", config.getString("lazy.db.driver"));
        jpaProperties.put("jakarta.persistence.provider", config.getString("lazy.db.provider"));
        jpaProperties.put("hibernate.dialect", config.getString("hibernate.dialect"));
        jpaProperties.put("hibernate.hbm2ddl.auto", config.getString("hibernate.hbm2ddl.auto"));
        jpaProperties.put("hibernate.show_sql", config.getBoolean("hibernate.show_sql"));

        return Persistence.createEntityManagerFactory("myJpaUnit", jpaProperties);
    }


}
