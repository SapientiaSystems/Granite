//  ##########################################################################
//  # File Name: BootstrapperTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.User;
using Granite.Common.Verification;
using Granite.Domain.User;
using Granite.ProvidedService.User;
using Granite.RequiredService.User;
using Granite.ServerPlatform.ActiveDirectory;
using NUnit.Framework;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;
using Strata.Domain.UnitOfWork;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.Payment;
using Granite.Domain.Tenant;
using Granite.Domain.Session;
using Granite.Domain.Role;
using Granite.Domain.Group;
using Granite.Domain.Constraint;
using Granite.Domain.Payment;
using Granite.Domain.Profile;
using Granite.ProvidedService.Registration;

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
            //Assert.NotNull(itsContainer.GetInstance<ActiveDirectoryGraphClient>());
            //Assert.NotNull(itsContainer.GetInstance<IExternalUserService>());
        }

        [Test]
        public void
        TestGetInstanceForDomain()
        {
            Assert.NotNull(itsContainer.GetInstance<ITenantRepository>());
            Assert.NotNull(itsContainer.GetInstance<ISessionRepository>());
            Assert.NotNull(itsContainer.GetInstance<IRoleRepository>());
            Assert.NotNull(itsContainer.GetInstance<IGroupRepository>());
            Assert.NotNull(itsContainer.GetInstance<IConstraintRepository>());
            Assert.NotNull(itsContainer.GetInstance<IUserRepository>());
            Assert.NotNull(itsContainer.GetInstance<IPlanRepository>());
            Assert.NotNull(itsContainer.GetInstance<IAccountRepository>());
            Assert.NotNull(itsContainer.GetInstance<ISubscriptionRepository>());
            Assert.NotNull(itsContainer.GetInstance<IProfileRepository>());
        }

        [Test]
        public void
        TestGetInstanceForApplication()
        {
            Assert.NotNull(itsContainer.GetInstance<IVerificationMessageGenerator>());
            Assert.NotNull(itsContainer.GetInstance<IVerificationCodeGenerator>());
            Assert.NotNull(itsContainer.GetInstance<IVerificationNotifier>());
            Assert.NotNull(itsContainer.GetInstance<ISessionService>());
            Assert.NotNull(itsContainer.GetInstance<IUserService>());
            Assert.NotNull(itsContainer.GetInstance<IPaymentService>());
            Assert.NotNull(itsContainer.GetInstance<IRegistrationService>());
        }
    }
}

//  ##########################################################################
