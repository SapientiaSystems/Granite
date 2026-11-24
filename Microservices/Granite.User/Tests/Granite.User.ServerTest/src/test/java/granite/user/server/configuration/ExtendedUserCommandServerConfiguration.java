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
import org.springframework.core.env.Environment;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;
import strata.foundation.spring.inject.RequestScoped;
import strata.foundation.spring.inject.SingletonScoped;
import strata.outbox.core.repository.IOutboxEventRepository;

@Configuration
@PropertySource({"classpath:application.properties","classpath:application-local.properties"})
public
class ExtendedUserCommandServerConfiguration
    extends GraniteUserCommandServerConfiguration
{
    @Override
    @Bean
    @SingletonScoped
    public IConfiguration
    configuration(Environment environment)
    {
        return new ApplicationConfigurationProvider("application-local").get();
    }

    @Override
    @Bean
    @RequestScoped
    public IUserEventSender
    userEventSender(IOutboxEventRepository repository)
    {
        return new MockUserEventSender();
    }

    @Bean
    @SingletonScoped
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
