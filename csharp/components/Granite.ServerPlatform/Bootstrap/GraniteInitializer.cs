//  ##########################################################################
//  # File Name: GraniteInitializer.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Payment;
using Granite.Common.Value;
using Granite.Domain.Constraint;
using Granite.Domain.Group;
using Granite.Domain.Payment;
using Granite.Domain.Role;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using Granite.ProvidedService.Payment;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;
using SystemEx.Injection;

namespace Granite.ServerPlatform.Bootstrap
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Initializes the Granite application.
    /// </summary>
    ///  
    public
    class GraniteInitializer:
        AbstractApplicationInitializer
    {
        protected ITenantRepository     Tenants { get; set; }
        protected IUserRepository       Users { get; set; }
        protected IGroupRepository      Groups { get; set; }
        protected IRoleRepository       Roles { get; set; }
        protected IConstraintRepository Constraints { get; set; }
        protected IPlanRepository       Plans { get; set; }
        protected IPaymentProcessor     Payments { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Creates a new instance of <c>GraniteInitializer</c> with 
        /// the specified <c>IUnitOfWorkProvider</c> and repositories.
        /// </summary>
        ///
        [Inject]
        public
        GraniteInitializer(
            IUnitOfWorkProvider   provider,
            ITenantRepository     tenants,
            IUserRepository       users,
            IGroupRepository      groups,
            IRoleRepository       roles,
            IConstraintRepository constraints,
            IPlanRepository       plans,
            IPaymentProcessor     payments):
            base(provider)
        {
            Tenants     = tenants;
            Users       = users;
            Groups      = groups;
            Roles       = roles;
            Constraints = constraints;
            Plans       = plans;
            Payments    = payments;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        protected override void 
        DoInitialize()
        {
            CreateGuestUserIfNeeded();
            CreatePlansIfNeeded();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Creates the guest user entity if needed.
        /// </summary>
        ///
        protected virtual void
        CreateGuestUserIfNeeded()
        {
            ITenant tenant = null;

            if (!Tenants.HasUniqueWithTenantName("guest"))
                tenant = Tenants.Insert(
                    new StandardTenant("guest",new HashedString("guest")));
            else
                tenant = Tenants.GetUniqueWithTenantName("guest");

            if (!Users.HasUniqueWithUserName("guest"))
            {
                IUser guest =
                    new StandardUser(tenant,Tenants,Groups,Roles,Constraints)
                    {
                        TenantId = tenant.TenantId,
                        UserName = "guest",
                        Password = new HashedString("guest"),
                        Status = UserStatus.VERIFIED,
                        Created = DateTime.Now,
                        LastModified = DateTime.Now
                    };
                guest.AttachRole(
                    Roles.GetRole("GraniteGuest"));

                Users.Insert(guest);
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Creates the guest user entity if needed.
        /// </summary>
        ///
        protected virtual void
        CreatePlansIfNeeded()
        {
            foreach (InitialPlanData plan in GetPlansToCreate())
            {
                if (!Plans.HasUniqueWithPlanName(plan.PlanName))
                {
                    CreatePlanReply reply =
                        Payments.CreatePlan(
                            new CreatePlanRequest()
                            {
                                PlanToCreate = plan
                            });

                    if (!reply.IsSuccessful)
                        throw new Exception(reply.FailureMessage);
                }
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Gets the plans to create.
        /// </summary>
        ///
        protected virtual ICollection<InitialPlanData>
        GetPlansToCreate()
        {
            return
                new List<InitialPlanData>()
                {
                    new InitialPlanData()
                    {
                        PlanName = "Test",
                        IntervalType = PaymentIntervalKind.MONTH,
                        IntervalCount = 1,
                        Currency = "USD",
                        Amount = 25.95m
                    }
                };
        }
    }
}

//  ##########################################################################
