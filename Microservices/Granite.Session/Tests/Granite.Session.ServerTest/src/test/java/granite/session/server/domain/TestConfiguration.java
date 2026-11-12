//////////////////////////////////////////////////////////////////////////////
// TestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.InitializingBean;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;
import strata.foundation.spring.inject.RequestScoped;
import strata.foundation.spring.inject.SingletonScoped;
import strata.server.core.unitofwork.IUnitOfWorkSynchronizationManager;
import strata.server.spring.repository.LocalContainerEntityManagerFactoryBeanProvider;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;
import strata.server.spring.unitofwork.JpaUnitOfWork;
import strata.server.spring.unitofwork.JpaUnitOfWorkManager;
import strata.server.spring.unitofwork.SpringUnitOfWorkSynchronizationManager;

@Configuration
@EnableTransactionManagement
@PropertySource({"classpath:application.properties","classpath:application-local.properties"})
@Import({DomainConfiguration.class})
public
class TestConfiguration
{
    @Bean
    public BeanFactoryPostProcessor
    beanFactoryPostProcessor()
    {
        return
            factory ->
                factory.registerScope(
                    "request",
                    new org.springframework.web.context.request.RequestScope());
    }

    @Bean
    @SingletonScoped
    public IConfiguration
    configuration()
    {
        return
            new ApplicationConfigurationProvider("application-local").get();
    }

    @Bean
    @SingletonScoped
    public LocalContainerEntityManagerFactoryBean
    localContainerEntityManagerFactoryBean(IConfiguration configuration)
    {
        return
            new LocalContainerEntityManagerFactoryBeanProvider(configuration)
                .get();
    }

    @Bean
    @SingletonScoped
    public InitializingBean
    schemaInitializer(EntityManagerFactory factory)
    {
        return
            () ->
            {
                String script;

                try (InputStream in = getClass().getResourceAsStream("/schema.sql"))
                {
                    script = new String(in.readAllBytes(),StandardCharsets.UTF_8);
                }

                EntityManager em = factory.createEntityManager();

                try
                {
                    em.unwrap(org.hibernate.Session.class).doWork(
                        connection ->
                        {
                            for (String sql : script.split(";"))
                            {
                                if (!sql.isBlank())
                                {
                                    try (Statement statement = connection.createStatement())
                                    {
                                        statement.execute(sql.trim());
                                    }
                                }
                            }
                        });
                }
                finally
                {
                    em.close();
                }
            };
    }

    @Bean
    @RequestScoped
    public ISpringUnitOfWorkManager
    unitOfWorkManager(JpaUnitOfWork unitOfWork)
    {
        return new JpaUnitOfWorkManager(unitOfWork);
    }

    @Bean
    @RequestScoped
    public JpaUnitOfWork
    unitOfWork(EntityManagerFactory factory)
    {
        return new JpaUnitOfWork(factory);
    }

    @Bean
    @RequestScoped
    public IUnitOfWorkSynchronizationManager
    synchronizer()
    {
        return new SpringUnitOfWorkSynchronizationManager();
    }
}

//////////////////////////////////////////////////////////////////////////////
