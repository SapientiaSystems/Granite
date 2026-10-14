//////////////////////////////////////////////////////////////////////////////
// SessionQueryHost.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.queryhost.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.Map;

@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class})
@EnableTransactionManagement
public
class SessionQueryHost
{
    public static final String DEFAULT_DEPLOY_ENV = "production";

    public static void
    main(String[] args)
    {
        SpringApplication application = new SpringApplication(SessionQueryHost.class);
        String            deployEnv   = deployEnvironment();
        ArrayList<String> profiles    = new ArrayList<>();

        profiles.add(deployEnv);

        if (deployEnv.equals("development"))
            profiles.add("dev");

        application.setDefaultProperties(Map.of("DEPLOY_ENV",deployEnv));
        application.setAdditionalProfiles(profiles.toArray(new String[0]));
        application.run(args);
    }

    private static String
    deployEnvironment()
    {
        String value = System.getenv("DEPLOY_ENV");

        return value == null || value.isBlank() ? DEFAULT_DEPLOY_ENV : value;
    }
}

//////////////////////////////////////////////////////////////////////////////
