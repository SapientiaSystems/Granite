//  ##########################################################################
//  # File Name: BootstrapperTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.User;
using Granite.Common.Utility;
using Granite.Domain.User;
using Granite.ProvidedService.User;
using Granite.RequiredService.User;
using Granite.ServerPlatform.ActiveDirectory;
using NUnit.Framework;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;
using Strata.Persistence.UnitOfWork;

namespace Granite.ServerPlatform.Bootstrap
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public
    class BootstrapperTest
    {
        private IBootstrapper itsTarget;
        private IContainer    itsContainer;

        [SetUp]
        public void
        SetUp()
        {
            itsTarget = new Bootstrapper();
            itsTarget.Run(new GraniteApplicationFactory());
            itsContainer = itsTarget.Container;
        }

        [TearDown]
        public void
        TearDown()
        {
            itsContainer = null;
            itsTarget = null;
        }

        [Test]
        public void
        TestGetInstanceForServerPlatform()
        {
            Assert.NotNull(itsContainer.GetInstance<IUnitOfWorkProvider>());
            Assert.NotNull(itsContainer.GetInstance<ActiveDirectoryGraphClient>());
            Assert.NotNull(itsContainer.GetInstance<IExternalUserService>());
        }

        [Test]
        public void
        TestGetInstanceForDomain()
        {
            Assert.NotNull(itsContainer.GetInstance<IUserRepository>());
        }

        [Test]
        public void
        TestGetInstanceForApplication()
        {
            Assert.NotNull(itsContainer.GetInstance<IEmailGenerator>());
            Assert.NotNull(itsContainer.GetInstance<IVerificationCodeGenerator>());
            Assert.NotNull(itsContainer.GetInstance<IEmailSender>());
            Assert.NotNull(itsContainer.GetInstance<IUserProcessor>());
            Assert.NotNull(itsContainer.GetInstance<IUserService>());

        }
    }
}

//  ##########################################################################
