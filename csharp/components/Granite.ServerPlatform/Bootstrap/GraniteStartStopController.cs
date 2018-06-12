//  ##########################################################################
//  # File Name: GraniteStartStopController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Injection;
using Granite.Domain.Constraint;
using Granite.Domain.Group;
using Granite.Domain.Role;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;
using Strata.Foundation.Value;
using System;
using SystemEx.Injection;

namespace Granite.ServerPlatform.Bootstrap
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class GraniteStartStopController:
        IStartStopController
    {
        protected IContainer Container { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public 
        GraniteStartStopController(IContainer container)
        {
            Container = container;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void 
        StartApplication()
        {
            Global.Container = Container;
            CreateGuestUserIfNeeded();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void 
        StopApplication()
        {
        }

        protected virtual void
        CreateGuestUserIfNeeded()
        {
            ITenantRepository tenants =
                Container.GetInstance<ITenantRepository>();
            IUserRepository users =
                Container.GetInstance<IUserRepository>();
            IGroupRepository groups =
                Container.GetInstance<IGroupRepository>();
            IRoleRepository roles =
                Container.GetInstance<IRoleRepository>();
            IConstraintRepository constraints =
                Container.GetInstance<IConstraintRepository>();
            IUnitOfWorkProvider provider =
                Container.GetInstance<IUnitOfWorkProvider>();
            IUnitOfWork unitOfWork = null; ;
            ITenant tenant = null;

            unitOfWork = provider.GetUnitOfWork();

            if (!tenants.HasUniqueWithTenantName("guest"))
                tenant = tenants.Insert(
                    new StandardTenant("guest",new HashedString("guest")));
            else
                tenant = tenants.GetUniqueWithTenantName("guest");
            
            if (!users.HasUniqueWithUserName("guest"))
            {
                IUser guest =
                    new StandardUser(tenant,tenants,groups,roles,constraints)
                    {
                        TenantId = tenant.TenantId,
                        UserName = "guest",
                        Password = new HashedString("guest"),
                        Status = UserStatus.VERIFIED,
                        Created = DateTime.Now,
                        LastModified = DateTime.Now
                    };
                guest.AttachRole(
                    roles.GetRole("GraniteGuest"));

                users.Insert(guest);
            }

            unitOfWork.Commit();
        }
    }
}

//  ##########################################################################
