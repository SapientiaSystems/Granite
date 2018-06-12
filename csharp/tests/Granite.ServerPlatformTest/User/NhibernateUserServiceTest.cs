//  ##########################################################################
//  # File Name: NhibernateUserServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.User;
using Granite.ServerPlatform.Bootstrap;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;

namespace Granite.ServerPlatform.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class NhibernateUserServiceTest:
        UserServiceTest
    {
        protected override IContainer 
        CreateContainer()
        {
            IBootstrapper bootstrapper = new Bootstrapper();
                    
            bootstrapper.Run(new MockApplicationFactory());
            return bootstrapper.Container;
        }
    }
}

//  ##########################################################################
