//  ##########################################################################
//  # File Name: NhibernateProfileServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Profile;
using Granite.ServerPlatform.Bootstrap;
using Ninject;
using Strata.Ninject.Bootstrap;

namespace Granite.ServerPlatform.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class NhibernateProfileServiceTest:
        ProfileServiceTest
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
