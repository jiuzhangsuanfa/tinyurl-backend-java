package com.jiuzhang.url.dbconfig;

import com.jiuzhang.url.enums.DBShard;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

import static com.jiuzhang.url.dbconfig.UserDataSourceConfig.BASE_PACKAGES;

@Configuration
@EnableSpringConfigured
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactoryUser",
        transactionManagerRef = "transactionManagerUser",
        basePackages = {BASE_PACKAGES})
public class UserDataSourceConfig {
    public static final String BASE_PACKAGES = "com.jiuzhang.url.db";

    @Bean
    public Shard1DBProperties shard1DBProperties() {
        return new Shard1DBProperties();
    }

    @Bean
    public Shard2DBProperties shard2DBProperties() {
        return new Shard2DBProperties();
    }

    @Bean
    public RoutingDataSource routingDataSource() {
        Map<DBShard, DBProperties> configurations = new HashMap<>();
        configurations.put(DBShard.url0, shard1DBProperties());
        configurations.put(DBShard.url1, shard2DBProperties());

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.initDataSources(configurations);
        return routingDataSource;
    }

    @Bean(initMethod = "migrate")
    public FlywayMigrationInitializer flywayMigrationInitializer() {
        return new FlywayMigrationInitializer();
    }

    @Bean(name = "entityManagerFactoryUser")
    @DependsOn(value = "flywayMigrationInitializer")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryUser(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(routingDataSource().getDefaultDataSource())
                .properties(getVendorProperties())
                .packages(BASE_PACKAGES)
                .persistenceUnit("userShardingPersistenceUnit")
                .build();
    }

    @Bean(name = "entityManagerUser")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryUser(builder).getObject().createEntityManager();
    }

    @Resource
    private JpaProperties jpaProperties;

    @Resource
    private HibernateProperties hibernateProperties;

    private Map<String, Object> getVendorProperties() {
        return hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());
    }

    @Bean(name = "transactionManagerUser")
    public PlatformTransactionManager transactionManagerUser(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryUser(builder).getObject());
    }


}
