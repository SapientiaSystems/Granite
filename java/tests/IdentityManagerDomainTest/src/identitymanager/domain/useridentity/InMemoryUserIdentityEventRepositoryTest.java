package identitymanager.domain.useridentity;

import identitymanager.domain.inmemorypersistence.InMemoryPersistenceModule;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;

public 
class InMemoryUserIdentityEventRepositoryTest
    extends UserIdentityEventRepositoryTest
{

    public 
    InMemoryUserIdentityEventRepositoryTest() {}

    @Override
    protected IUserIdentityEventRepository 
    createTarget()
    {
        IContainer container = new Container();
        
        new InMemoryPersistenceModule().initialize( container );
        new UserIdentityDomainModule().initialize( container );
        
        return container.getInstance( IUserIdentityEventRepository.class );
    }

}

// ##########################################################################
