package com.skronawi.rsql.persistence;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "com.skronawi.rsql.persistence")
@EnableJpaRepositories
public class PersistenceConfig {

    @Value(value = "${persistence.db.driverClassName:org.hsqldb.jdbcDriver}")
    private String dbDriverClassName;
    @Value(value = "${persistence.db.url:jdbc:hsqldb:mem:rsql}")
    private String dbUrl;
    @Value(value = "${persistence.db.username:sa}")
    private String dbUsername;
    @Value(value = "${persistence.db.password:}")
    private String dbPassword;

    @Value(value = "${persistence.hibernate.hbm2ddl_auto:update}")
    private String dbHbm2ddl;
    @Value(value = "${persistence.hibernate.show_sql:true}")
    private String dbShowSql;
    @Value(value = "${persistence.hibernate.dialect:org.hibernate.dialect.HSQLDialect}")
    private String dbDialect;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public DataSource dataSource() throws PropertyVetoException {

        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(dbDriverClassName);
        dataSource.setJdbcUrl(dbUrl);
        dataSource.setUser(dbUsername);
        dataSource.setPassword(dbPassword);
        dataSource.setMaxPoolSize(10);
        dataSource.setMinPoolSize(10);
        dataSource.setMaxIdleTime(10);

        return dataSource;
    }

    @Bean
    public EntityManagerFactory entityManagerFactory(DataSource dataSource) {

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);

        LocalContainerEntityManagerFactoryBean factory =
                new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.skronawi.rsql.persistence");
        factory.setDataSource(dataSource);
        factory.setJpaProperties(additionalProperties());
        factory.afterPropertiesSet();

        return factory.getObject();
    }

    private Properties additionalProperties() {

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", dbHbm2ddl);
        properties.setProperty("hibernate.show_sql", dbShowSql);
        properties.setProperty("hibernate.dialect", dbDialect);

        return properties;
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory) {

        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);

        return txManager;
    }
}
