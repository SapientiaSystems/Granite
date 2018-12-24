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
using Strata.Ninject.Bootstrap;
using Ninject;

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
        private INinjectBootstrapper itsTarget;
        private IKernel              itsContainer;

        [SetUp]
        public void
        SetUp()
        {
            itsTarget = new NinjectBootstrapper();
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
            Assert.NotNull(itsContainer.Get<IUnitOfWorkProvider>());
            //Assert.NotNull(itsContainer.GetInstance<ActiveDirectoryGraphClient>());
            //Assert.NotNull(itsContainer.GetInstance<IExternalUserService>());
        }

        [Test]
        public void
        TestGetInstanceForDomain()
        {
            Assert.NotNull(itsContainer.Get<ITenantRepository>());
            Assert.NotNull(itsContainer.Get<ISessionRepository>());
            Assert.NotNull(itsContainer.Get<IRoleRepository>());
            Assert.NotNull(itsContainer.Get<IGroupRepository>());
            Assert.NotNull(itsContainer.Get<IConstraintRepository>());
            Assert.NotNull(itsContainer.Get<IUserRepository>());
            Assert.NotNull(itsContainer.Get<IPlanRepository>());
            Assert.NotNull(itsContainer.Get<IAccountRepository>());
            Assert.NotNull(itsContainer.Get<ISubscriptionRepository>());
            Assert.NotNull(itsContainer.Get<IProfileRepository>());
        }

        [Test]
        public void
        TestGetInstanceForApplication()
        {
            Assert.NotNull(itsContainer.Get<IVerificationMessageGenerator>());
            Assert.NotNull(itsContainer.Get<IVerificationCodeGenerator>());
            Assert.NotNull(itsContainer.Get<IVerificationNotifier>());
            Assert.NotNull(itsContainer.Get<ISessionService>());
            Assert.NotNull(itsContainer.Get<IUserService>());
            Assert.NotNull(itsContainer.Get<IPaymentService>());
            Assert.NotNull(itsContainer.Get<IRegistrationService>());
        }
    }
}

//  ##########################################################################
