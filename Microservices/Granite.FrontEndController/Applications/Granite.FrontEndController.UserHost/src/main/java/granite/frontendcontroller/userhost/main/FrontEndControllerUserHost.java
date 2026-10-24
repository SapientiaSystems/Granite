//////////////////////////////////////////////////////////////////////////////
// FrontEndControllerUserHost.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.userhost.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import strata.foundation.core.utility.DefaultEnvironment;
import strata.foundation.core.utility.IEnvironment;

import java.util.Map;

@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        KafkaAutoConfiguration.class})
public
class FrontEndControllerUserHost
{
    public static void
    main(String[] args)
    {
        SpringApplication application =
            new SpringApplication(FrontEndControllerUserHost.class);
        IEnvironment environment =
            DefaultEnvironment.of(
                Map.of("DEPLOY_ENV","local"));

        application.setAdditionalProfiles(environment.get("DEPLOY_ENV"));
        application.run(args);
    }
}

//////////////////////////////////////////////////////////////////////////////
