//  ##########################################################################
//  # File Name: GraniteTestStartStopController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Constraint;
using Granite.Domain.Group;
using Granite.Domain.Role;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using Ninject;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Injection;
using Strata.Foundation.Value;
using System;

namespace Granite.ServerPlatform.Bootstrap
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class GraniteTestStartStopController:
        GraniteStartStopController
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary></summary>
        ///  
        [Inject]
        public
        GraniteTestStartStopController(IKernel container):
            base(container)
        {

        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void
        StartApplication()
        {
            base.StartApplication();
            CreateAdminUserIfNeeded();
        }

        protected virtual void
        CreateAdminUserIfNeeded()
        {
            ITenantRepository tenants =
                Container.Get<ITenantRepository>();
            IUserRepository users =
                Container.Get<IUserRepository>();
            IGroupRepository groups =
                Container.Get<IGroupRepository>();
            IRoleRepository roles =
                Container.Get<IRoleRepository>();
            IConstraintRepository constraints =
                Container.Get<IConstraintRepository>();
            IUnitOfWorkProvider provider =
                Container.Get<IUnitOfWorkProvider>();
            IUnitOfWork unitOfWork = provider.GetUnitOfWork();
            ITenant tenant = null;

            if (!tenants.HasUniqueWithTenantName("test"))
                tenant = tenants.Insert(
                    new StandardTenant("test",new HashedString("test")));
            else
                tenant = tenants.GetUniqueWithTenantName("test");

            if (!users.HasUniqueWithUserName("test"))
            {
                IUser test =
                    new StandardUser(tenant,tenants,groups,roles,constraints)
                    {
                        TenantId = tenant.TenantId,
                        UserName = "test",
                        Password = new HashedString("test"),
                        Status = UserStatus.VERIFIED,
                        Created = DateTime.Now,
                        LastModified = DateTime.Now
                    };
                test.AttachRole(
                    roles.GetRole("GraniteAdministrator"));

                users.Insert(test);
            }

            unitOfWork.Commit();
        }
    }
}

//  ##########################################################################
