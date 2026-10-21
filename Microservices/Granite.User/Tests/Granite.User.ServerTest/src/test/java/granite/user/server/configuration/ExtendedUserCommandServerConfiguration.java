//////////////////////////////////////////////////////////////////////////////
// ExtendedUserCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.user.server.platform.MockUserEventSender;
import granite.user.service.event.IUserEventSender;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.web.context.annotation.RequestScope;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;
import strata.outbox.core.repository.IOutboxEventRepository;

@Configuration
@PropertySource({"classpath:application.properties","classpath:application-local.properties"})
public
class ExtendedUserCommandServerConfiguration
    extends GraniteUserCommandServerConfiguration
{
    @Override
    @Bean
    @Scope("singleton")
    public IConfiguration
    configuration(Environment environment)
    {
        return new ApplicationConfigurationProvider("application-local").get();
    }

    @Override
    @Bean
    @RequestScope
    public IUserEventSender
    userEventSender(IOutboxEventRepository repository)
    {
        return new MockUserEventSender();
    }

    @Bean
    @Scope("singleton")
    public ObjectMapper
    objectMapper()
    {
        return new ObjectMapper();
    }

    @Override
    protected ModelMapper
    configureMapper(ModelMapper mapper)
    {
        super.configureMapper(mapper);
        mapper.getConfiguration().setSkipNullEnabled(true);
        return mapper;
    }
}

//////////////////////////////////////////////////////////////////////////////
