//  ##########################################################################
//  # File Name: GraniteDomainModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Constraint;
using Granite.Domain.Group;
using Granite.Domain.Payment;
using Granite.Domain.Profile;
using Granite.Domain.Role;
using Granite.Domain.Session;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using Strata.Ninject.Injection;
using System.Collections.Generic;
using SystemEx.Injection;

namespace Granite.Domain.Configuration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class GraniteDomainModule<R,C,F>:
        AbstractModule
        where R: IProvider<IList<IRole>>
        where C: IProvider<IList<IConstraint>>
        where F: IUserFactory
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        GraniteDomainModule() {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void 
        Load()
        {
            Bind<IProvider<IList<IRole>>>()
                .To<R>()
                .InSingletonScope();

            Bind<IProvider<IList<IConstraint>>>()
                .To<C>()
                .InSingletonScope();

            Bind<IRoleRepository>()
                .To<RoleRepository>()
                .InSingletonScope();

            Bind<IConstraintRepository>()
                .To<ConstraintRepository>()
                .InSingletonScope();

            Bind<ITenantRepository>()
                .To<TenantRepository>()
                .InDefaultScope();

            Bind<IGroupRepository>()
                .To<GroupRepository>()
                .InDefaultScope();

            Bind<IUserFactory>()
                .To<F>()
                .InDefaultScope();

            Bind<IUserRepository>()
                .To<UserRepository>()
                .InDefaultScope();

            Bind<IProfileRepository>()
                .To<ProfileRepository>()
                .InDefaultScope();

            Bind<ISessionRepository>()
                .To<SessionRepository>()
                .InDefaultScope();

            Bind<IPlanRepository>()
                .To<PlanRepository>()
                .InDefaultScope();

            Bind<IAccountRepository>()
                .To<AccountRepository>()
                .InDefaultScope();

            Bind<ISubscriptionRepository>()
                .To<SubscriptionRepository>()
                .InDefaultScope();
        }
    }
}

//  ##########################################################################
