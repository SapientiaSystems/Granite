//  ##########################################################################
//  # File Name: GraniteApplicationFactory.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Configuration;
using Granite.Application.Mapper;
using Granite.Common.Verification;
using Granite.Domain.Configuration;
using Granite.Domain.Constraint;
using Granite.Domain.Role;
using Granite.Domain.User;
using Granite.ServerPlatform.Nhibernate;
using Ninject.Activation;
using Ninject.Modules;
using NLog.Config;
using NLog.Targets;
using Strata.Foundation.Bootstrap;
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
    class GraniteApplicationFactory:
        NinjectApplicationFactory
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
        public override void 
        BindLogger(INinjectModule module)
        {
            module
                .Kernel
                .Bind<ILogger>()
                .ToMethod(
                        (IContext c) =>
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
                        })
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
                .To<GraniteStartStopController>()
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
    }
}

//  ##########################################################################
