//////////////////////////////////////////////////////////////////////////////
// SessionQueryHost.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.queryhost.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.core.utility.DefaultEnvironment;
import strata.foundation.core.utility.IEnvironment;

import java.util.Map;

@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class})
@EnableTransactionManagement
public
class SessionQueryHost
{
    public static void
    main(String[] args)
    {
        SpringApplication application =
            new SpringApplication(SessionQueryHost.class);
        IEnvironment environment =
            DefaultEnvironment.of(
                Map.of("DEPLOY_ENV","local"));

        application.setDefaultProperties(Map.of("DEPLOY_ENV",environment.get("DEPLOY_ENV")));
        application.setAdditionalProfiles(environment.get("DEPLOY_ENV"));
        application.run(args);
    }
}

//////////////////////////////////////////////////////////////////////////////
