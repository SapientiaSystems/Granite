//  ##########################################################################
//  # File Name: MapperTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.Application.Configuration;
using Granite.Common.Value;
using Granite.Common.Verification;
using Granite.Domain.Configuration;
using Granite.Domain.Constraint;
using Granite.Domain.Group;
using Granite.Domain.Profile;
using Granite.Domain.Role;
using Granite.Domain.Session;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Registration;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.User;
using Granite.RequiredService.Payment;
using Granite.ServerPlatform.InMemory;
using NUnit.Framework;
using Strata.Foundation.Injection;
using Strata.Foundation.Value;
using Strata.Ninject.Injection;
using System;

namespace Granite.Application.Mapper
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public 
    class MapperTest
    {
        private IContainer         itsContainer;
        private IMapper            itsTarget;
        private IRoleRepository    itsRoles;
        private ITestUserGenerator itsGenerator;

        [SetUp]
        public void
        SetUp()
        {
            AbstractModule.DefaultScope = typeof(ThreadScope);
            itsContainer = 
                new NinjectContainer(
                    new InMemoryModule(),
                    new FakeUtilityModule(),
                    new GraniteDomainModule<
                        GraniteRolesProvider,
                        GraniteConstraintsProvider,
                        GraniteUserFactory>(),
                    new GraniteApplicationModule<GraniteMapperProvider>(),
                    new TestGeneratorModule());
            itsTarget = itsContainer.GetInstance<IMapper>();
            itsRoles = itsContainer.GetInstance<IRoleRepository>();
            itsGenerator = itsContainer.GetInstance<ITestUserGenerator>();
        }

        [Test]
        public void
        TestMapCreateOrUpdateProfileDataToProfile()
        {
            CreateOrUpdateProfileData source =
                new CreateOrUpdateProfileData()
                {
                    UserId = Guid.NewGuid(),
                    UserName = itsGenerator.GenerateUserName(),
                    Name = itsGenerator.GenerateName(),
                    Email = itsGenerator.GenerateEmail(),
                    Phone = itsGenerator.GeneratePhone(),
                    Address = itsGenerator.GenerateAddress(),
                    JobTitle = "Software Engineer"
                };
            IProfile destination = new StandardProfile();

            itsTarget.Map(source,destination);

            Assert.AreEqual(source.UserId,destination.UserId);
            Assert.AreEqual(source.UserName,destination.UserName);
            Assert.AreEqual(source.Name,destination.Name);
            Assert.AreEqual(source.Email,destination.Email);
            Assert.AreEqual(source.Phone,destination.Phone);
            Assert.AreEqual(source.Address,destination.Address);
            Assert.AreEqual(source.JobTitle,destination.JobTitle);

        }

        [Test]
        public void
        TestMapProfileToCreateOrUpdateProfileData()
        {
            IProfile source = 
                new StandardProfile()
                {
                    UserId = Guid.NewGuid(),
                    UserName = itsGenerator.GenerateUserName(),
                    Name = itsGenerator.GenerateName(),
                    Email = itsGenerator.GenerateEmail(),
                    Phone = itsGenerator.GeneratePhone(),
                    Address = itsGenerator.GenerateAddress(),
                    JobTitle = "Software Engineer"
                };
            CreateOrUpdateProfileData destination =
                new CreateOrUpdateProfileData();

            itsTarget.Map(source,destination);

            Assert.AreEqual(source.UserId,destination.UserId);
            Assert.AreEqual(source.UserName,destination.UserName);
            Assert.AreEqual(source.Name,destination.Name);
            Assert.AreEqual(source.Email,destination.Email);
            Assert.AreEqual(source.Phone,destination.Phone);
            Assert.AreEqual(source.Address,destination.Address);
            Assert.AreEqual(source.JobTitle,destination.JobTitle);
                
        }

        [Test]
        public void
        TestMapUserToProfile()
        {
            IUser source =
                new StandardUser()
                {
                    UserId = Guid.NewGuid(),
                    UserName = itsGenerator.GenerateUserName(),
                    Email = itsGenerator.GenerateEmail(),
                };
            IProfile destination = new StandardProfile();

            itsTarget.Map(source,destination);

            Assert.AreEqual(source.UserId,destination.UserId);
            Assert.AreEqual(source.UserName,destination.UserName);
            Assert.AreEqual(source.Email,destination.Email);

        }

        [Test]
        public void
        TestMapSessionToSessionData()
        {
            ISession source =
                new StandardSession()
                {
                    PrimaryId = Guid.NewGuid(),
                    UserId = Guid.NewGuid(),
                    AccessToken = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                };
            SessionData destination =
                new SessionData();

            itsTarget.Map(source,destination);

            Assert.AreEqual(source.PrimaryId,destination.SessionId);
            Assert.AreEqual(source.UserId,destination.UserId);
            Assert.AreEqual(source.AccessToken,destination.AccessToken);

        }

        [Test]
        public void
        TestMapCreateUserDataToUser()
        {
            CreateUserData source =
                new CreateUserData()
                {
                    TenantId = Guid.NewGuid(),
                    UseExistingTenant = true,
                    UserName = itsGenerator.GenerateUserName(),
                    Password = itsGenerator.GeneratePassword(),
                    Email = itsGenerator.GenerateEmail()
                };
            IUser destination = new StandardUser();

            itsTarget.Map(source,destination);

            Assert.AreEqual(source.TenantId,destination.TenantId);
            Assert.AreEqual(source.UserName,destination.UserName);
            Assert.AreEqual(
                new HashedString(source.Password,destination.Password.Salt),
                destination.Password);
            Assert.AreEqual(source.Email,destination.Email);

        }

        [Test]
        public void
        TestMapUserToUserData()
        {
            ITenantRepository tenants = itsContainer.GetInstance<ITenantRepository>();
            IGroupRepository groups = itsContainer.GetInstance<IGroupRepository>();
            IRoleRepository roles = itsContainer.GetInstance<IRoleRepository>();
            IConstraintRepository constraints = itsContainer.GetInstance<IConstraintRepository>();
            ITenant tenant = tenants.Insert(new StandardTenant("test",new HashedString("test")));
            IUser source =
                new StandardUser(tenant,tenants,groups,roles,constraints)
                {
                    UserId = Guid.NewGuid(),
                    UserName = itsGenerator.GenerateUserName(),
                    Email = itsGenerator.GenerateEmail(),
                };
            UserData destination = new UserData();

            try
            {
                tenant.Activate();
                source.AttachRole(itsRoles.GetRole("GraniteUser"));

                itsTarget.Map(source,destination);

                Assert.AreEqual(source.UserId,destination.UserId);
                Assert.AreEqual(source.UserName,destination.UserName);
                Assert.AreEqual(source.Email.ToString(),destination.Email);
                Assert.AreEqual(1,destination.Roles.Length);
                Assert.AreEqual("GraniteUser",destination.Roles[0].RoleName);
                Assert.AreEqual("NullConstraint",destination.Roles[0].ConstraintId);
            }
            finally
            {
                tenant.Deactivate();
            }
        }

        [Test]
        public void
        TestMapRegisterRequestToCreateUserRequest()
        {
            RegisterRequest source =
                new RegisterRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    RegistrationType = RegistrationKind.USER_PROFILE,
                    UserToCreate =
                        new CreateUserData()
                        {
                            TenantId = Guid.NewGuid(),
                            UseExistingTenant = true,
                            UserName = itsGenerator.GenerateUserName(),
                            Password = itsGenerator.GeneratePassword(),
                            Email = itsGenerator.GenerateEmail(),
                            HasSubscriptionType = true,
                            SubscriptionType = "GoldLevel"
                        },
                    ProfileToCreate = 
                        new CreateOrUpdateProfileData()
                        {
                            UserId = Guid.NewGuid(),
                            Name = itsGenerator.GenerateName(),
                            Email = itsGenerator.GenerateEmail(),
                            Phone = itsGenerator.GeneratePhone(),
                            Address = itsGenerator.GenerateAddress(),
                            JobTitle = "Software Engineer"
                        },
                    SubscriptionToCreate = null

                };
            CreateUserRequest destination = itsTarget.Map<CreateUserRequest>(source);

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(source.UserToCreate.TenantId,destination.UserToCreate.TenantId);
            Assert.AreEqual(source.UserToCreate.UseExistingTenant,destination.UserToCreate.UseExistingTenant);
            Assert.AreEqual(source.UserToCreate.HasSubscriptionType,destination.UserToCreate.HasSubscriptionType);
            Assert.AreEqual(source.UserToCreate.SubscriptionType,destination.UserToCreate.SubscriptionType);
            Assert.AreEqual(source.UserToCreate.UserName,destination.UserToCreate.UserName);
            Assert.AreEqual(source.UserToCreate.Password,destination.UserToCreate.Password);
            Assert.AreEqual(source.UserToCreate.Email,destination.UserToCreate.Email);

        }

        [Test]
        public void
        TestMapRegisterRequestToCreateProfileRequest()
        {
            RegisterRequest source =
                new RegisterRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    RegistrationType = RegistrationKind.USER_PROFILE,
                    UserToCreate =
                        new CreateUserData()
                        {
                            TenantId = Guid.NewGuid(),
                            UseExistingTenant = true,
                            UserName = itsGenerator.GenerateUserName(),
                            Password = itsGenerator.GeneratePassword(),
                            Email = itsGenerator.GenerateEmail(),
                            HasSubscriptionType = true,
                            SubscriptionType = "GoldLevel"
                        },
                    ProfileToCreate =
                        new CreateOrUpdateProfileData()
                        {
                            UserId = Guid.NewGuid(),
                            Name = itsGenerator.GenerateName(),
                            Email = itsGenerator.GenerateEmail(),
                            Phone = itsGenerator.GeneratePhone(),
                            Address = itsGenerator.GenerateAddress(),
                            JobTitle = "Software Engineer"
                        },
                    SubscriptionToCreate = null

                };
            CreateProfileRequest destination = itsTarget.Map<CreateProfileRequest>(source);

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(source.ProfileToCreate.UserId,destination.ProfileToCreate.UserId);
            Assert.AreEqual(source.ProfileToCreate.UserName,destination.ProfileToCreate.UserName);
            Assert.AreEqual(source.ProfileToCreate.Email,destination.ProfileToCreate.Email);
            Assert.AreEqual(source.ProfileToCreate.Name,destination.ProfileToCreate.Name);
            Assert.AreEqual(source.ProfileToCreate.Phone,destination.ProfileToCreate.Phone);
            Assert.AreEqual(source.ProfileToCreate.Address,destination.ProfileToCreate.Address);
            Assert.AreEqual(source.ProfileToCreate.JobTitle,destination.ProfileToCreate.JobTitle);
        }

        [Test]
        public void
        TestMapCreateUserReplyToRegisterReply()
        {
            CreateUserReply source =
                new CreateUserReply()
                {
                    CorrelationId = Guid.NewGuid(),
                    IsSuccessful = true,
                    SuccessMessage = "success",
                    NewUser =
                        new UserData()
                        {
                            TenantId = Guid.NewGuid(),
                            UserId = Guid.NewGuid(),
                            UserName = itsGenerator.GenerateUserName(),
                            Email = itsGenerator.GenerateEmail().ToString(),
                            Roles =
                                new UserRoleData[]
                                {
                                    new UserRoleData()
                                    {
                                        RoleName = "GraniteUser",
                                        ConstraintId = "NullConstraint"
                                    }
                                },
                            Groups =
                                new UserGroupData[]
                                {
                                    new UserGroupData()
                                    {
                                        TenantId = Guid.NewGuid(),
                                        GroupName = "ProjectReaders",
                                        Roles =
                                            new UserRoleData[]
                                            {
                                                new UserRoleData()
                                                {
                                                    RoleName = "ProjectReader",
                                                    ConstraintId = "NullConstraint"
                                                }
                                            }
                                    }
                                }
                        }
                };
            RegisterReply destination = itsTarget.Map<RegisterReply>(source);

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(source.IsSuccessful,destination.IsSuccessful);
            Assert.AreEqual(source.SuccessMessage,destination.SuccessMessage);
            Assert.AreEqual(source.NewUser.TenantId,destination.NewUser.TenantId);
            Assert.AreEqual(source.NewUser.UserId,destination.NewUser.UserId);
            Assert.AreEqual(source.NewUser.UserName,destination.NewUser.UserName);
            Assert.AreEqual(source.NewUser.Email,destination.NewUser.Email);
            Assert.AreEqual(source.NewUser.Roles.Length,destination.NewUser.Roles.Length);
            Assert.AreEqual(source.NewUser.Roles[0].RoleName,destination.NewUser.Roles[0].RoleName);
            Assert.AreEqual(source.NewUser.Roles[0].ConstraintId,destination.NewUser.Roles[0].ConstraintId);
            Assert.AreEqual(source.NewUser.TenantId,destination.NewUser.TenantId);
            Assert.AreEqual(source.NewUser.Groups.Length,destination.NewUser.Groups.Length);
            Assert.AreEqual(source.NewUser.Groups[0].GroupName,destination.NewUser.Groups[0].GroupName);
            Assert.AreEqual(source.NewUser.Groups[0].Roles.Length,destination.NewUser.Groups[0].Roles.Length);
            Assert.AreEqual(source.NewUser.Groups[0].Roles[0].RoleName,destination.NewUser.Groups[0].Roles[0].RoleName);
            Assert.AreEqual(source.NewUser.Groups[0].Roles[0].ConstraintId,destination.NewUser.Groups[0].Roles[0].ConstraintId);

        }

        [Test]
        public void
        TestMapCreateProfileReplyToRegisterReply()
        {
            CreateProfileReply source =
                new CreateProfileReply()
                {
                    CorrelationId = Guid.NewGuid(),
                    IsSuccessful = true,
                    SuccessMessage = "success",
                    NewProfile =
                        new ProfileData()
                        {
                            ProfileId = Guid.NewGuid(),
                            UserId = Guid.NewGuid()
                        }
                };
            RegisterReply destination = itsTarget.Map<RegisterReply>(source);

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(source.IsSuccessful,destination.IsSuccessful);
            Assert.AreEqual(source.SuccessMessage,destination.SuccessMessage);
            Assert.AreEqual(source.NewProfile.ProfileId,destination.NewProfile.ProfileId);
            Assert.AreEqual(source.NewProfile.UserId,destination.NewProfile.UserId);
        }

        [Test]
        public void
        TestMapCreateUserReplyAndCreateProfileReplyToRegisterReply()
        {
            Guid correlationId = Guid.NewGuid();
            CreateUserReply source1 =
                new CreateUserReply()
                {
                    CorrelationId = correlationId,
                    IsSuccessful = true,
                    SuccessMessage = "successfully created user",
                    NewUser =
                        new UserData()
                        {
                            TenantId = Guid.NewGuid(),
                            UserId = Guid.NewGuid(),
                            UserName = itsGenerator.GenerateUserName(),
                            Email = itsGenerator.GenerateEmail().ToString(),
                            Roles =
                                new UserRoleData[]
                                {
                                    new UserRoleData()
                                    {
                                        RoleName = "GraniteUser",
                                        ConstraintId = "NullConstraint"
                                    }
                                },
                            Groups =
                                new UserGroupData[]
                                {
                                    new UserGroupData()
                                    {
                                        TenantId = Guid.NewGuid(),
                                        GroupName = "ProjectReaders",
                                        Roles =
                                            new UserRoleData[]
                                            {
                                                new UserRoleData()
                                                {
                                                    RoleName = "ProjectReader",
                                                    ConstraintId = "NullConstraint"
                                                }
                                            }
                                    }
                                }
                        }
                };
            CreateProfileReply source2 =
                new CreateProfileReply()
                {
                    CorrelationId = correlationId,
                    IsSuccessful = true,
                    SuccessMessage = "successfully created profile",
                    NewProfile =
                        new ProfileData()
                        {
                            ProfileId = Guid.NewGuid(),
                            UserId = Guid.NewGuid()
                        }
                };
            RegisterReply destination = 
                itsTarget.Map(
                    source2,
                    itsTarget.Map<RegisterReply>(source1));

            Assert.AreEqual(source1.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(source1.IsSuccessful,destination.IsSuccessful);
            Assert.AreEqual(source1.SuccessMessage + "#" + source2.SuccessMessage,destination.SuccessMessage);
            Assert.AreEqual(source1.NewUser.TenantId,destination.NewUser.TenantId);
            Assert.AreEqual(source1.NewUser.UserId,destination.NewUser.UserId);
            Assert.AreEqual(source1.NewUser.UserName,destination.NewUser.UserName);
            Assert.AreEqual(source1.NewUser.Email,destination.NewUser.Email);
            Assert.AreEqual(source1.NewUser.Roles.Length,destination.NewUser.Roles.Length);
            Assert.AreEqual(source1.NewUser.Roles[0].RoleName,destination.NewUser.Roles[0].RoleName);
            Assert.AreEqual(source1.NewUser.Roles[0].ConstraintId,destination.NewUser.Roles[0].ConstraintId);
            Assert.AreEqual(source1.NewUser.TenantId,destination.NewUser.TenantId);
            Assert.AreEqual(source1.NewUser.Groups.Length,destination.NewUser.Groups.Length);
            Assert.AreEqual(source1.NewUser.Groups[0].GroupName,destination.NewUser.Groups[0].GroupName);
            Assert.AreEqual(source1.NewUser.Groups[0].Roles.Length,destination.NewUser.Groups[0].Roles.Length);
            Assert.AreEqual(source1.NewUser.Groups[0].Roles[0].RoleName,destination.NewUser.Groups[0].Roles[0].RoleName);
            Assert.AreEqual(source1.NewUser.Groups[0].Roles[0].ConstraintId,destination.NewUser.Groups[0].Roles[0].ConstraintId);

            Assert.AreEqual(source2.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(source2.IsSuccessful,destination.IsSuccessful);
            Assert.AreEqual(source2.NewProfile.ProfileId,destination.NewProfile.ProfileId);
            Assert.AreEqual(source2.NewProfile.UserId,destination.NewProfile.UserId);
        }

        [Test]
        public void
        TestMapCreatePlanRequestToCreateExternalPlanRequest()
        {
            CreatePlanRequest source =
                new CreatePlanRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    PlanToCreate = 
                        new CreatePlanData()
                        {
                            PlanName = "Enterprise License",
                            IntervalType = PaymentIntervalKind.MONTH,
                            IntervalCount = 1,
                            Currency = "USD",
                            Amount = 125.95m
                        }
                };
            CreateExternalPlanRequest destination = itsTarget.Map<CreateExternalPlanRequest>(source);

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(source.PlanToCreate.PlanName,destination.PlanToCreate.PlanName);
            Assert.AreEqual(source.PlanToCreate.IntervalType,destination.PlanToCreate.IntervalType);
            Assert.AreEqual(source.PlanToCreate.IntervalCount,destination.PlanToCreate.IntervalCount);
            Assert.AreEqual(source.PlanToCreate.Currency,destination.PlanToCreate.Currency);
            Assert.AreEqual(source.PlanToCreate.Amount,destination.PlanToCreate.Amount);
        }


        [Test]
        public void
        TestMapCreateExternalPlanReplyToCreatePlanReply()
        {
            CreateExternalPlanReply source =
                new CreateExternalPlanReply()
                {
                    CorrelationId = Guid.NewGuid(),
                    IsSuccessful = true,
                    SuccessMessage = "success blah blah blah",
                    ExternalPlanId = "xxxxxxxxxxxx",
                    NewPlan =
                        new PlanData()
                        {
                            PlanName = "Enterprise License",
                            IntervalType = PaymentIntervalKind.MONTH,
                            IntervalCount = 1,
                            Currency = "USD",
                            Amount = 125.95m
                        }
                };
            CreatePlanReply destination = itsTarget.Map<CreatePlanReply>(source);

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(source.NewPlan.PlanName,destination.NewPlan.PlanName);
            Assert.AreEqual(source.NewPlan.IntervalType,destination.NewPlan.IntervalType);
            Assert.AreEqual(source.NewPlan.IntervalCount,destination.NewPlan.IntervalCount);
            Assert.AreEqual(source.NewPlan.Currency,destination.NewPlan.Currency);
            Assert.AreEqual(source.NewPlan.Amount,destination.NewPlan.Amount);
        }

    }
}

//  ##########################################################################
