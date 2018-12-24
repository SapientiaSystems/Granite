//  ##########################################################################
//  # File Name: MockApplicationFactory.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Configuration;
using Granite.Application.Decoration;
using Granite.Application.Mapper;
using Granite.Common.Verification;
using Granite.Domain.Configuration;
using Granite.Domain.Constraint;
using Granite.Domain.Role;
using Granite.Domain.Shared;
using Granite.Domain.User;
using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Registration;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.User;
using Granite.ServerPlatform.ActiveDirectory;
using Granite.ServerPlatform.Nhibernate;
using Ninject.Modules;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Decoration;
using Strata.Foundation.Injection;
using Strata.Foundation.Logging;
using Strata.Ninject.Bootstrap;
using Strata.Ninject.Injection;
using System.Collections.Generic;
using AbstractModule = Strata.Ninject.Injection.AbstractModule;

namespace Granite.ServerPlatform.Bootstrap
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class MockApplicationFactory:
        NinjectApplicationFactory
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        MockApplicationFactory()
        {
            AbstractModule.DefaultScope = typeof(ThreadScope);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void 
        BindLogger(INinjectModule module)
        {
            module
                .Kernel
                .Bind<ILogger>()
                .To<ConsoleLogger>()
                .InDefaultScope();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void 
        BindStartStopController(INinjectModule module)
        {
            module
                .Kernel
                .Bind<IStartStopController>()
                .To<GraniteTestStartStopController>()
                .InDefaultScope();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IList<INinjectModule> 
        CreateModules()
        {
            IList<INinjectModule> modules = new List<INinjectModule>();

            modules.Add(
                new GraniteServerPlatformModule<GraniteInitializer>());
            modules.Add(new FakeUtilityModule());
            modules.Add(
                new GraniteDomainModule<
                    GraniteRolesProvider,
                    GraniteConstraintsProvider,
                    GraniteUserFactory>());
            modules.Add(new GraniteApplicationModule<GraniteMapperProvider>());
            modules.Add(new TestGeneratorModule());

            return modules;
        }
    }
}

//  ##########################################################################
