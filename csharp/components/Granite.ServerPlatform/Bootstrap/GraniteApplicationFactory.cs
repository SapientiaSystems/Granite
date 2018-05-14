//  ##########################################################################
//  # File Name: GraniteApplicationFactory.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Payment;
using Granite.Application.Session;
using Granite.Application.User;
using Granite.Common.Verification;
using Granite.Domain.User;
using Granite.ServerPlatform.ActiveDirectory;
using Granite.ServerPlatform.Nhibernate;
using NLog.Config;
using NLog.Targets;
using Strata.Foundation.Bootstrap;
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
        protected override IList<IModule> 
        CreateModules()
        {
            IList<IModule> modules = new List<IModule>();

            modules.Add(new NhibernateModule());
            modules.Add(new ActiveDirectoryModule());
            modules.Add(new GraniteVerificationModule());
            modules.Add(new UserDomainModule());
            modules.Add(new UserApplicationModule());
            modules.Add(new SessionApplicationModule());
            modules.Add(new PaymentApplicationModule());

            return modules;
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
        protected override IContainer 
        CreateContainer(IList<IModule> modules)
        {
            return new NinjectContainer(modules);
        }
    }
}

//  ##########################################################################
