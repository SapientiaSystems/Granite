package identitymanager.domain.useridentity;

import identitymanager.domain.inmemorypersistence.InMemoryPersistenceModule;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;

public 
class InMemoryUserIdentityRepositoryTest
    extends UserIdentityRepositoryTest
{

    public 
    InMemoryUserIdentityRepositoryTest() {}

    @Override
    protected IUserIdentityRepository 
    createTarget()
    {
        IContainer container = new Container();
        
        new InMemoryPersistenceModule().initialize( container );
        new UserIdentityDomainModule().initialize( container );
        
        return container.getInstance( IUserIdentityRepository.class );
    }

}

// ##########################################################################
