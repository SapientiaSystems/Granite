//  ##########################################################################
//  # File Name: MockApplicationFactory.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Configuration;
using Granite.Application.Decoration;
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
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Decoration;
using Strata.Foundation.Injection;
using Strata.Foundation.Logging;
using Strata.Ninject.Injection;
using System.Collections.Generic;

namespace Granite.ServerPlatform.Bootstrap
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class MockApplicationFactory:
        AbstractApplicationFactory
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
        protected override IList<IModule> 
        CreateModules()
        {
            IList<IModule> modules = new List<IModule>();

            modules.Add(new GraniteServerPlatformModule());
            modules.Add(new FakeUtilityModule());
            modules.Add(
                new GraniteDomainModule<
                    GraniteRolesProvider,
                    GraniteConstraintsProvider,
                    GraniteUserFactory>());
            modules.Add(new GraniteApplicationModule());
            modules.Add(new TestGeneratorModule());

            return modules;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override IBinder<IStartStopController> 
        CreateControllerBinder(ISourceBindingBuilder<IStartStopController> builder)
        {
            return 
                new TargetBasedBinder<
                    IStartStopController,
                    GraniteTestStartStopController>(
                        builder,
                        AbstractModule.DefaultScope);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override IBinder<ILogger>
        CreateLoggerBinder(ISourceBindingBuilder<ILogger> builder)
        {
            return
                new TargetBasedBinder<ILogger,ConsoleLogger>(
                    builder,
                    AbstractModule.DefaultScope);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IContainer 
        CreateContainer(IList<IModule> modules)
        {
            IDecoratorFactory factory = 
                new AuthorizedServiceDecoratorFactory(3);

            return
                new DecoratedContainer(
                    new NinjectContainer(modules),
                    new DecoratorMap()
                        .InsertDecorator<ISessionService>(factory)
                        .InsertDecorator<IUserService>(factory)
                        .InsertDecorator<IProfileService>(factory)
                        .InsertDecorator<IPaymentService>(factory)
                        .InsertDecorator<IRegistrationService>(factory));
        }
    }
}

//  ##########################################################################
