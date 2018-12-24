//  ##########################################################################
//  # File Name: NhibernateSessionServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Session;
using Granite.ServerPlatform.Bootstrap;
using Ninject;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;
using Strata.Ninject.Bootstrap;

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
        protected override IKernel 
        CreateContainer()
        {
            INinjectBootstrapper bootstrapper = new NinjectBootstrapper();
                    
            bootstrapper.Run(new MockApplicationFactory());
            return bootstrapper.Container;
        }
    }
}

//  ##########################################################################
