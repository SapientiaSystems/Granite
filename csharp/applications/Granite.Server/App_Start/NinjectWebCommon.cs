//  ##########################################################################
//  # File Name: NinjectWebCommon.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

[assembly: WebActivatorEx.PreApplicationStartMethod(
    typeof(Granite.Server.App_Start.NinjectWebCommon),"Start")]
[assembly: WebActivatorEx.ApplicationShutdownMethod(
    typeof(Granite.Server.App_Start.NinjectWebCommon),"Stop")]

namespace Granite.Server.App_Start
{
    using Granite.ServerPlatform.Bootstrap;
    using Microsoft.Web.Infrastructure.DynamicModuleHelper;
    using Ninject;
    using Ninject.Web.Common;
    using Ninject.Web.Common.WebHost;
    using Strata.Ninject.Injection;
    using System;
    using System.Web;
    using System.Web.Http;
    using WebApiContrib.IoC.Ninject;
    using Bootstrapper = Strata.Foundation.Bootstrap.Bootstrapper;
    using IBootstrapper = Strata.Foundation.Bootstrap.IBootstrapper;
    using NinjectBootstrapper = Ninject.Web.Common.Bootstrapper;

    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public static 
    class NinjectWebCommon 
    {
        private static readonly NinjectBootstrapper bootstrapper = 
            new NinjectBootstrapper();

        private static readonly IBootstrapper bootstrap =
            new Bootstrapper();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Starts the application
        /// </summary>
        /// 
        public static void 
        Start() 
        {
            DynamicModuleUtility.RegisterModule(typeof(OnePerRequestHttpModule));
            DynamicModuleUtility.RegisterModule(typeof(NinjectHttpModule));
     
            bootstrapper.Initialize(CreateKernel);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Stops the application
        /// </summary>
        /// 
        public static void 
        Stop()
        {
            bootstrapper.ShutDown();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Creates the kernel that will manage your application.
        /// </summary>
        /// <returns>The created kernel.</returns>
        /// 
        private static IKernel 
        CreateKernel()
        {
            bootstrap.Run(new GraniteApplicationFactory());

            IKernel kernel = ((NinjectContainer)bootstrap.Container).Kernel;

            try
            {
                kernel
                    .Bind<Func<IKernel>>()
                    .ToMethod(ctx => () => new NinjectBootstrapper().Kernel);
                kernel
                    .Bind<IHttpModule>()
                    .To<HttpApplicationInitializationHttpModule>();

                GlobalConfiguration.Configuration.DependencyResolver = 
                    new NinjectResolver(kernel);

                RegisterServices(kernel);
                return kernel;
            }
            catch
            {
                kernel.Dispose();
                throw;
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Load your modules or register your services here!
        /// </summary>
        /// <param name="kernel">The kernel.</param>
        /// 
        private static void 
        RegisterServices(IKernel kernel)
        {
        }        
    }
}
