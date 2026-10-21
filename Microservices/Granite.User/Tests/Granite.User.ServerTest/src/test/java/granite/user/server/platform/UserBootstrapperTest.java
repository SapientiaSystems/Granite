//////////////////////////////////////////////////////////////////////////////
// UserBootstrapperTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.user.server.application.TestConfiguration;
import granite.user.server.configuration.GraniteUserCommandServerConfiguration;
import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public
class UserBootstrapperTest
{
    @Autowired
    private ApplicationContext context;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ISpringUnitOfWorkManager unitOfWorkManager;

    private TransactionTemplate transaction;

    @BeforeEach
    public void
    setUp()
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
        transaction = new TransactionTemplate(unitOfWorkManager);
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    createsUserWithHashedPasswordAndAuthorities()
    {
        String userName = uniqueUserName();

        assertTrue(bootstrapper(true,userName,"first-secret").bootstrap());

        User created = find(userName).orElseThrow();

        assertTrue(encoder.matches("first-secret",created.getPassword()));
        assertFalse(created.getPassword().contains("first-secret"));
        assertEquals(Set.of("ROLE_USER","ROLE_ADMIN"),created.getAuthorities());
    }

    @Test
    public void
    secondRunIsNoOpAndKeepsExistingPassword()
    {
        String userName = uniqueUserName();

        assertTrue(bootstrapper(true,userName,"first-secret").bootstrap());
        assertFalse(bootstrapper(true,userName,"second-secret").bootstrap());

        String stored = find(userName).orElseThrow().getPassword();

        assertTrue(encoder.matches("first-secret",stored));
        assertFalse(encoder.matches("second-secret",stored));
    }

    @Test
    public void
    blankPasswordSkipsWithoutCreatingUser()
    {
        String userName = uniqueUserName();

        assertFalse(bootstrapper(true,userName," ").bootstrap());
        assertFalse(find(userName).isPresent());
    }

    @Test
    public void
    disabledBootstrapperDoesNothing()
    {
        String userName = uniqueUserName();

        assertFalse(bootstrapper(false,userName,"secret").bootstrap());
        assertFalse(find(userName).isPresent());
    }

    @Test
    public void
    seamPermitsBootstrapOnlyInLocalAndDevelopment()
    {
        GraniteUserCommandServerConfiguration subject = new GraniteUserCommandServerConfiguration();

        for (String[] profiles : new String[][] {{"local"},{"development"}})
            assertTrue(subject.userBootstrapper(providers(),providers(),encoder,environment(true,profiles)).isEnabled());

        for (String[] profiles : new String[][] {{"production"},{"test"},{"local","test"},{"development","production"},{}})
            assertThrows(
                IllegalStateException.class,
                () -> subject.userBootstrapper(providers(),providers(),encoder,environment(true,profiles)));

        assertFalse(subject.userBootstrapper(providers(),providers(),encoder,environment(false,"production")).isEnabled());
    }

    @Test
    public void
    seamBindsConfiguredAuthorities()
    {
        MockEnvironment environment = environment(true,"local");

        environment.setProperty("granite.user.bootstrap.authorities","ROLE_AUDITOR");

        UserBootstrapper bootstrapper =
            new GraniteUserCommandServerConfiguration()
                .userBootstrapper(
                    context.getBeanProvider(IUserRepository.class),
                    context.getBeanProvider(ISpringUnitOfWorkManager.class),
                    encoder,
                    environment);

        assertTrue(bootstrapper.bootstrap());
        assertEquals(Set.of("ROLE_AUDITOR"),find(environment.getProperty("granite.user.bootstrap.user-name")).orElseThrow().getAuthorities());
    }

    private UserBootstrapper
    bootstrapper(boolean enabled,String userName,String password)
    {
        return
            new UserBootstrapper(
                context.getBeanProvider(IUserRepository.class),
                context.getBeanProvider(ISpringUnitOfWorkManager.class),
                encoder,
                enabled,
                userName,
                password,
                Set.of("ROLE_USER","ROLE_ADMIN"));
    }

    private Optional<User>
    find(String userName)
    {
        return transaction.execute(status -> context.getBean(IUserRepository.class).findByUserName(userName));
    }

    private static MockEnvironment
    environment(boolean enabled,String... profiles)
    {
        MockEnvironment environment = new MockEnvironment();

        environment.setActiveProfiles(profiles);
        environment.setProperty("granite.user.bootstrap.enabled",String.valueOf(enabled));
        environment.setProperty("granite.user.bootstrap.user-name",uniqueUserName());
        environment.setProperty("granite.user.bootstrap.password","secret");
        return environment;
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T>
    providers()
    {
        return Mockito.mock(ObjectProvider.class);
    }

    private static String
    uniqueUserName() { return "bootstrap-" + UUID.randomUUID() + "@example.com"; }
}

//////////////////////////////////////////////////////////////////////////////
