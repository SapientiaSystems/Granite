//  ##########################################################################
//  # File Name: GraniteApplicationFactory.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Collections.Generic;
using Granite.Application.Payment;
using Granite.Application.Session;
using Granite.Application.User;
using Granite.ServerPlatform.InMemory;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;
using Strata.Ninject.Injection;

namespace Granite.Server.Bootstrap
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class GraniteApplicationFactory:
        AbstractApplicationFactory
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IList<IModule> 
        CreateModules()
        {
            IList<IModule> modules = new List<IModule>();

            modules.Add(new InMemoryModule());
            modules.Add(new UserApplicationModule());
            modules.Add(new SessionApplicationModule());
            modules.Add(new PaymentApplicationModule());

            return modules;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IContainer 
        CreateContainer(IList<IModule> modules)
        {
            return new NinjectContainer(modules);
        }
    }
}

//  ##########################################################################
