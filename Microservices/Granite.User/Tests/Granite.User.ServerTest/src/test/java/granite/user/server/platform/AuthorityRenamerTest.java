//////////////////////////////////////////////////////////////////////////////
// AuthorityRenamerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.user.server.application.TestConfiguration;
import granite.user.server.configuration.PoplarUserCommandServerConfiguration;
import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public
class AuthorityRenamerTest
{
    @Autowired
    private ApplicationContext context;

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
    renamesMixedCaseToUpper()
    {
        String userName = create("ROLE_Admin","ROLE_User");

        assertEquals(1,renamer(new GraniteApplicationRolesManagerSupplier().get()).rename());
        assertEquals(Set.of("ROLE_ADMIN","ROLE_USER"),authorities(userName));
    }

    @Test
    public void
    isIdempotent()
    {
        create("ROLE_Admin","ROLE_User");

        AuthorityRenamer renamer = renamer(new GraniteApplicationRolesManagerSupplier().get());

        assertEquals(1,renamer.rename());
        assertEquals(0,renamer.rename());
    }

    @Test
    public void
    expandsClosure()
    {
        String userName = create(PoplarUserCommandServerConfiguration.APPROVER);

        assertEquals(1,renamer(PoplarUserCommandServerConfiguration.poplarRoles()).rename());
        assertEquals(3,authorities(userName).size());
    }

    @Test
    public void
    leavesUnknownValues()
    {
        String userName = create("ROLE_Legacy");

        assertEquals(0,renamer(new GraniteApplicationRolesManagerSupplier().get()).rename());
        assertEquals(Set.of("ROLE_Legacy"),authorities(userName));
    }

    private AuthorityRenamer
    renamer(ApplicationRolesManager roles)
    {
        return
            new AuthorityRenamer(
                context.getBeanProvider(IUserRepository.class),
                context.getBeanProvider(ISpringUnitOfWorkManager.class),
                Map.of("ROLE_Admin","ROLE_ADMIN","ROLE_Auditor","ROLE_AUDITOR","ROLE_User","ROLE_USER"),
                roles);
    }

    private String
    create(String... authorities)
    {
        String userName = "renamer-" + UUID.randomUUID() + "@example.com";

        transaction.execute(
            status ->
                context
                    .getBean(IUserRepository.class)
                    .save(new User().setUserName(userName).setAuthorities(new LinkedHashSet<>(Set.of(authorities)))));
        return userName;
    }

    private Set<String>
    authorities(String userName)
    {
        return
            transaction.execute(
                status -> new LinkedHashSet<>(context.getBean(IUserRepository.class).findByUserName(userName).orElseThrow().getAuthorities()));
    }
}

//////////////////////////////////////////////////////////////////////////////
