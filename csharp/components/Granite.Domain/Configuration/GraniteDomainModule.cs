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
using Strata.Foundation.Injection;
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
        GraniteDomainModule() :
            base("GraniteDomainModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void 
        Initialize()
        {
            Bind<IProvider<IList<IRole>>>()
                .ToType<R>()
                .WithScope<SingletonScope>();

            Bind<IProvider<IList<IConstraint>>>()
                .ToType<C>()
                .WithScope<SingletonScope>();

            Bind<IRoleRepository>()
                .ToType<RoleRepository>()
                .WithScope<SingletonScope>();

            Bind<IConstraintRepository>()
                .ToType<ConstraintRepository>()
                .WithScope<SingletonScope>();

            Bind<ITenantRepository>()
                .ToType<TenantRepository>()
                .WithScope(DefaultScope);

            Bind<IGroupRepository>()
                .ToType<GroupRepository>()
                .WithScope(DefaultScope);

            Bind<IUserFactory>()
                .ToType<F>()
                .WithScope(DefaultScope);

            Bind<IUserRepository>()
                .ToType<UserRepository>()
                .WithScope(DefaultScope);

            Bind<IProfileRepository>()
                .ToType<ProfileRepository>()
                .WithScope(DefaultScope);

            Bind<ISessionRepository>()
                .ToType<SessionRepository>()
                .WithScope(DefaultScope);

            Bind<IPlanRepository>()
                .ToType<PlanRepository>()
                .WithScope(DefaultScope);

            Bind<IAccountRepository>()
                .ToType<AccountRepository>()
                .WithScope(DefaultScope);

            Bind<ISubscriptionRepository>()
                .ToType<SubscriptionRepository>()
                .WithScope(DefaultScope);
        }
    }
}

//  ##########################################################################
