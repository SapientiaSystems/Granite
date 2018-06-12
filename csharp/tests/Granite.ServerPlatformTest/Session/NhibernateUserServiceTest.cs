//  ##########################################################################
//  # File Name: NhibernateSessionServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Session;
using Granite.ServerPlatform.Bootstrap;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;

namespace Granite.ServerPlatform.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class NhibernateSessionServiceTest:
        SessionServiceTest
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
