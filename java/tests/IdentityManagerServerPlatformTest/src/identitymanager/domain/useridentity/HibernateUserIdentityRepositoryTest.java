package identitymanager.domain.useridentity;

import identitymanager.serverplatform.hibernatepersistence.HibernatePersistenceModule;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;

public class HibernateUserIdentityRepositoryTest
    extends UserIdentityRepositoryTest
{

    public 
    HibernateUserIdentityRepositoryTest() {}

    @Override
    protected IUserIdentityRepository 
    createTarget()
    {
        IContainer container = new Container();
        
        new HibernatePersistenceModule().initialize( container );
        new UserIdentityDomainModule().initialize( container );
        
        return container.getInstance( IUserIdentityRepository.class );
    }

}

// ##########################################################################
