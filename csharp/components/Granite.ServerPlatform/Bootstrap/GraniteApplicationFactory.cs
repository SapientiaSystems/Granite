//  ##########################################################################
//  # File Name: GraniteApplicationFactory.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Configuration;
using Granite.Application.Decoration;
using Granite.Application.Mapper;
using Granite.Common.Verification;
using Granite.Domain.Configuration;
using Granite.Domain.Constraint;
using Granite.Domain.Role;
using Granite.Domain.User;
using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Registration;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.User;
using Granite.ServerPlatform.ActiveDirectory;
using Granite.ServerPlatform.Nhibernate;
using NLog.Config;
using NLog.Targets;
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
    class GraniteApplicationFactory:
        AbstractApplicationFactory
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        GraniteApplicationFactory()
        {
            AbstractModule.DefaultScope = typeof(RequestScope);
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
                    GraniteStartStopController>(
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
                new ProviderBasedBinder<ILogger>(
                    builder,
                    new FuncBasedProvider<ILogger>(
                        () =>
                        {
                            LoggingConfiguration config = 
                                new LoggingConfiguration();
                            FileTarget logfile = 
                                new FileTarget()
                                {
                                    FileName = "GraniteServer.log",
                                    Name = "logfile"
                                };
                            ConsoleTarget logconsole = 
                                new ConsoleTarget()
                                {
                                    Name = "logconsole"
                                };

                            config
                                .LoggingRules
                                .Add(
                                    new LoggingRule(
                                        "*",
                                        NLog.LogLevel.Info,
                                        logconsole));
                            config
                                .LoggingRules
                                .Add(
                                    new LoggingRule(
                                        "*",
                                        NLog.LogLevel.Info,
                                        logfile));

                            NLog.LogManager.Configuration = config;

                            return 
                                new NlogLogger(
                                    NLog.LogManager.GetLogger("Logger"));
                        } ),
                    AbstractModule.DefaultScope);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IList<IModule>
        CreateModules()
        {
            IList<IModule> modules = new List<IModule>();

            modules.Add(
                new GraniteServerPlatformModule<GraniteInitializer>());
            modules.Add(
                new VerificationModule<HtmlVerificationMessageGenerator>());
            modules.Add(
                new GraniteDomainModule<
                        GraniteRolesProvider,
                        GraniteConstraintsProvider,
                        GraniteUserFactory>());
            modules.Add(new GraniteApplicationModule<GraniteMapperProvider>());

            return modules;
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
                new NinjectDecoratedContainer(
                    new DecoratorMap()
                        .InsertDecorator<ISessionService>(factory)
                        .InsertDecorator<IUserService>(factory)
                        .InsertDecorator<IProfileService>(factory)
                        .InsertDecorator<IPaymentService>(factory)
                        .InsertDecorator<IRegistrationService>(factory),
                    modules);
        }
    }
}

//  ##########################################################################
