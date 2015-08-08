package identitymanager.domain.useridentity;

import identitymanager.serverplatform.hibernatepersistence.HibernatePersistenceModule;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;

public class HibernateUserIdentityEventRepositoryTest
    extends UserIdentityEventRepositoryTest
{

    public 
    HibernateUserIdentityEventRepositoryTest() {}

    @Override
    protected IUserIdentityEventRepository 
    createTarget()
    {
        IContainer container = new Container();
        
        new HibernatePersistenceModule().initialize( container );
        new UserIdentityDomainModule().initialize( container );
        
        return container.getInstance( IUserIdentityEventRepository.class );
    }

}

// ##########################################################################
