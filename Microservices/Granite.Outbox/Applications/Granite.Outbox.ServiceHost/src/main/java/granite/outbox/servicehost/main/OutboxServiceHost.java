//////////////////////////////////////////////////////////////////////////////
// OutboxServiceHost.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.servicehost.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.core.utility.DefaultEnvironment;
import strata.foundation.core.utility.IEnvironment;

import java.util.Map;

@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        KafkaAutoConfiguration.class})
@EnableTransactionManagement
public
class OutboxServiceHost
{
    public static void
    main(String[] args)
    {
        SpringApplication application =
            new SpringApplication(OutboxServiceHost.class);
        IEnvironment environment =
            DefaultEnvironment.of(
                Map.of("DEPLOY_ENV","development"));

        application.setAdditionalProfiles(environment.get("DEPLOY_ENV"));
        application.run(args);
    }
}

//////////////////////////////////////////////////////////////////////////////
