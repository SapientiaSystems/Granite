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
using Granite.Domain.Payment;
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
using Ninject;
using NUnit.Framework;
using Strata.Foundation.Injection;
using Strata.Foundation.Value;
using Strata.Ninject.Injection;
using System;
using AbstractModule = Strata.Ninject.Injection.AbstractModule;

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
        private IKernel            itsContainer;
        private IMapper            itsTarget;
        private IRoleRepository    itsRoles;
        private ITestUserGenerator itsGenerator;

        [SetUp]
        public void
        SetUp()
        {
            AbstractModule.DefaultScope = typeof(ThreadScope);
            itsContainer = 
                new StandardKernel(
                    new InMemoryModule(),
                    new FakeUtilityModule(),
                    new GraniteDomainModule<
                        GraniteRolesProvider,
                        GraniteConstraintsProvider,
                        GraniteUserFactory>(),
                    new GraniteApplicationModule<GraniteMapperProvider>(),
                    new TestGeneratorModule());
            itsTarget = itsContainer.Get<IMapper>();
            itsRoles = itsContainer.Get<IRoleRepository>();
            itsGenerator = itsContainer.Get<ITestUserGenerator>();
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
            ITenantRepository tenants = itsContainer.Get<ITenantRepository>();
            IGroupRepository groups = itsContainer.Get<IGroupRepository>();
            IRoleRepository roles = itsContainer.Get<IRoleRepository>();
            IConstraintRepository constraints = itsContainer.Get<IConstraintRepository>();
            ITenant tenant = tenants.Insert(new StandardTenant("test",new HashedString("test"))).Activate();
            IUser user =
                new StandardUser(tenant,tenants,groups,roles,constraints)
                {
                    UserId = Guid.NewGuid(),
                    UserName = itsGenerator.GenerateUserName(),
                    Email = itsGenerator.GenerateEmail(),
                    RoleIds = new StringSet() { "GraniteUser" }
                };
            ISession source =
                new StandardSession(user)
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
            Assert.NotNull(destination.User);
        }

        [Test]
        public void
        TestMapCreateUserDataToUser()
        {
            InitialUserData source =
                new InitialUserData()
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
            ITenantRepository tenants = itsContainer.Get<ITenantRepository>();
            IGroupRepository groups = itsContainer.Get<IGroupRepository>();
            IRoleRepository roles = itsContainer.Get<IRoleRepository>();
            IConstraintRepository constraints = itsContainer.Get<IConstraintRepository>();
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
                        new InitialUserData()
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
                        new InitialUserData()
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
                        new InitialPlanData()
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

        [Test]
        public void
        TestMapCreateAccountRequestToCreateExternalAccountRequest()
        {
            CreateAccountRequest source =
                new CreateAccountRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    AccountToCreate =
                        new InitialAccountData()
                        {
                            UserIdOfAccountToCreate = Guid.NewGuid(),
                            Description = "EzValuator Payment Account",
                            Email = "user@xyz.com",
                            CreditCardToCreate =
                                new InitialCreditCardData()
                                {
                                    CardHolderName = "Adam Smith",
                                    BillingAddress =
                                        new PostalAddress()
                                        {
                                            StreetAddress = "123 Main St",
                                            City = "Los Angeles",
                                            State = "CA",
                                            PostalCode = "90017",
                                            CountryCode = "US"
                                        },
                                    CardNumber = "4242123412341234",
                                    ExpirationMonth = 7,
                                    ExpirationYear = 2021,
                                    SecurityCode = "123"
                                }
                        }
                };
            CreateExternalAccountRequest destination = itsTarget.Map<CreateExternalAccountRequest>(source);

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(
                source.AccountToCreate.UserIdOfAccountToCreate,
                destination.AccountToCreate.UserIdOfAccountToCreate);
            Assert.AreEqual(
                source.AccountToCreate.Description,
                destination.AccountToCreate.Description);
            Assert.AreEqual(
                source.AccountToCreate.Email,
                destination.AccountToCreate.Email);
            Assert.AreEqual(
                source.AccountToCreate.CreditCardToCreate.CardHolderName,
                destination.AccountToCreate.CreditCardToCreate.CardHolderName);
            Assert.AreEqual(
                source.AccountToCreate.CreditCardToCreate.BillingAddress,
                destination.AccountToCreate.CreditCardToCreate.BillingAddress);
            Assert.AreEqual(
                source.AccountToCreate.CreditCardToCreate.CardNumber,
                destination.AccountToCreate.CreditCardToCreate.CardNumber);
            Assert.AreEqual(
                source.AccountToCreate.CreditCardToCreate.ExpirationMonth,
                destination.AccountToCreate.CreditCardToCreate.ExpirationMonth);
            Assert.AreEqual(
                source.AccountToCreate.CreditCardToCreate.ExpirationYear,
                destination.AccountToCreate.CreditCardToCreate.ExpirationYear);
            Assert.AreEqual(
                source.AccountToCreate.CreditCardToCreate.SecurityCode,
                destination.AccountToCreate.CreditCardToCreate.SecurityCode);
        }

        [Test]
        public void
        TestMapCreateExternalAccountReplyToCreateAccountReply()
        {
            CreateExternalAccountReply source =
                new CreateExternalAccountReply()
                {
                    CorrelationId = Guid.NewGuid(),
                    IsSuccessful = true,
                    SuccessMessage = "success blah blah blah",
                    ExternalAccountId = "acct_123456789",
                    ExternalPaymentMethodId = "card_123456789",
                    Email = "user@emailaddress.com",
                    CreditCard =
                        new CreditCardData()
                        {
                            CardHolderName = "Adam Smith",
                            BillingAddress =
                                        new PostalAddress()
                                        {
                                            StreetAddress = "123 Main St",
                                            City = "Los Angeles",
                                            State = "CA",
                                            PostalCode = "90017",
                                            CountryCode = "US"
                                        },
                            ObfuscatedCardNumber = "************1234",
                            ExpirationMonth = 7,
                            ExpirationYear = 2021,
                            SecurityCode = null

                        }
                };
            IAccount account = itsTarget.Map<IAccount>(source);
            CreateAccountReply destination =
                itsTarget.Map(account,itsTarget.Map<CreateAccountReply>(source));

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);

            Assert.AreEqual(
                source.ExternalAccountId,
                account.ExternalAccountId);
            Assert.AreEqual(
                source.ExternalPaymentMethodId,
                account.ExternalPaymentMethodId);

            Assert.AreEqual(
                source.CreditCard,
                destination.NewAccount.CreditCard);
        }

        [Test]
        public void
        TestMapUpdateAccountRequestToUpdateExternalAccountRequest()
        {
            UpdateAccountRequest source =
                new UpdateAccountRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    AccountToUpdate =
                        new UpdateAccountData()
                        {
                            UserIdOfAccountToUpdate = Guid.NewGuid(),
                            AccountIdOfAccountToUpdate = Guid.NewGuid(),
                            Description = "EzValuator Payment Account",
                            Email = "user@xyz.com",
                            ReplaceCreditCard = true,
                            CreditCardToCreate =
                                new InitialCreditCardData()
                                {
                                    CardHolderName = "Adam Smith",
                                    BillingAddress =
                                        new PostalAddress()
                                        {
                                            StreetAddress = "123 Main St",
                                            City = "Los Angeles",
                                            State = "CA",
                                            PostalCode = "90017",
                                            CountryCode = "US"
                                        },
                                    CardNumber = "4242123412341234",
                                    ExpirationMonth = 7,
                                    ExpirationYear = 2021,
                                    SecurityCode = "123"
                                }
                        }
                };
            IAccount account =
                new StandardAccount()
                {
                    AccountId = Guid.NewGuid(),
                    UserId = Guid.NewGuid(),
                    ExternalAccountId = "acct_123456789",
                    ExternalPaymentMethodId = "card_987654321"
                };
            UpdateExternalAccountRequest destination = 
                itsTarget.Map(account,itsTarget.Map<UpdateExternalAccountRequest>(source));

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(
               source.AccountToUpdate.ReplaceCreditCard,
               destination.ReplaceCreditCard);
            Assert.AreEqual(
                source.AccountToUpdate.CreditCardToCreate,
                destination.CreditCardToCreate);
            Assert.AreEqual(
                source.AccountToUpdate.Description,
                destination.Description);
            Assert.AreEqual(
                source.AccountToUpdate.Email,
                destination.Email);
            Assert.AreEqual(
                source.AccountToUpdate.CreditCardToCreate.CardHolderName,
                destination.CreditCardToCreate.CardHolderName);
            Assert.AreEqual(
                source.AccountToUpdate.CreditCardToCreate.BillingAddress,
                destination.CreditCardToCreate.BillingAddress);
            Assert.AreEqual(
                source.AccountToUpdate.CreditCardToCreate.CardNumber,
                destination.CreditCardToCreate.CardNumber);
            Assert.AreEqual(
                source.AccountToUpdate.CreditCardToCreate.ExpirationMonth,
                destination.CreditCardToCreate.ExpirationMonth);
            Assert.AreEqual(
                source.AccountToUpdate.CreditCardToCreate.ExpirationYear,
                destination.CreditCardToCreate.ExpirationYear);
            Assert.AreEqual(
                source.AccountToUpdate.CreditCardToCreate.SecurityCode,
                destination.CreditCardToCreate.SecurityCode);
            Assert.AreEqual(
                account.ExternalAccountId,
                destination.ExternalAccountId);
            Assert.AreEqual(
                account.ExternalPaymentMethodId,
                destination.ExternalPaymentMethodId);
        }

        [Test]
        public void
        TestMapUpdateExternalAccountReplyToUpdateAccountReply()
        {
            UpdateExternalAccountReply source =
                new UpdateExternalAccountReply()
                {
                    CorrelationId = Guid.NewGuid(),
                    IsSuccessful = true,
                    SuccessMessage = "success blah blah blah",
                    ExternalAccountId = "acct_123456789",
                    ExternalPaymentMethodId = "card_123456789",
                    Email = "user@emailaddress.com",
                    CreditCard =
                        new CreditCardData()
                        {
                            CardHolderName = "Adam Smith",
                            BillingAddress =
                                        new PostalAddress()
                                        {
                                            StreetAddress = "123 Main St",
                                            City = "Los Angeles",
                                            State = "CA",
                                            PostalCode = "90017",
                                            CountryCode = "US"
                                        },
                            ObfuscatedCardNumber = "************1234",
                            ExpirationMonth = 7,
                            ExpirationYear = 2021,
                            SecurityCode = null

                        }
                };
            IAccount account = itsTarget.Map<IAccount>(source);
            UpdateAccountReply destination =
                itsTarget.Map(account,itsTarget.Map<UpdateAccountReply>(source));

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);

            Assert.AreEqual(
                source.ExternalAccountId,
                account.ExternalAccountId);
            Assert.AreEqual(
                source.ExternalPaymentMethodId,
                account.ExternalPaymentMethodId);

            Assert.AreEqual(
                source.CreditCard,
                destination.UpdatedAccount.CreditCard);
            Assert.AreEqual(
                source.CreditCard.CardHolderName,
                destination.UpdatedAccount.CreditCard.CardHolderName);
            Assert.AreEqual(
                source.CreditCard.ObfuscatedCardNumber,
                destination.UpdatedAccount.CreditCard.ObfuscatedCardNumber);
            Assert.AreEqual(
                source.CreditCard.ExpirationMonth,
                destination.UpdatedAccount.CreditCard.ExpirationMonth);
            Assert.AreEqual(
                source.CreditCard.ExpirationYear,
                destination.UpdatedAccount.CreditCard.ExpirationYear);
            Assert.AreEqual(
                source.CreditCard.BillingAddress.StreetAddress,
                destination.UpdatedAccount.CreditCard.BillingAddress.StreetAddress);
            Assert.AreEqual(
                source.CreditCard.BillingAddress.City,
                destination.UpdatedAccount.CreditCard.BillingAddress.City);
            Assert.AreEqual(
                source.CreditCard.BillingAddress.State,
                destination.UpdatedAccount.CreditCard.BillingAddress.State);
            Assert.AreEqual(
                source.CreditCard.BillingAddress.PostalCode,
                destination.UpdatedAccount.CreditCard.BillingAddress.PostalCode);
            Assert.AreEqual(
                source.CreditCard.BillingAddress.CountryCode,
                destination.UpdatedAccount.CreditCard.BillingAddress.CountryCode);
        }

        [Test]
        public void
        TestMapCreateSubscriptionRequestToCreateExternalSubscriptionRequest()
        {
            CreateSubscriptionRequest source =
                new CreateSubscriptionRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    SubscriptionToCreate =
                        new InitialSubscriptionData()
                        {
                            UserId = Guid.NewGuid(),
                            AccountId = Guid.NewGuid(),
                            SubscriptionType = "Elite"
                        }
                };
            IAccount account = 
                new StandardAccount()
                {
                    AccountId = Guid.NewGuid(),
                    ExternalAccountId = "acct_123456789",
                    ExternalPaymentMethodId = "card_123456789"
                };
            CreateExternalSubscriptionRequest destination = 
                itsTarget.Map(
                    account,
                    itsTarget.Map<CreateExternalSubscriptionRequest>(source));

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(
                source.SubscriptionToCreate,
                destination.SubscriptionToCreate);
            Assert.AreEqual(
                account.ExternalAccountId,
                destination.ExternalAccountId);
        }

        [Test]
        public void
        TestMapCreateExternalSubscriptionReplyToCreateSubcriptionReply()
        {
            CreateExternalSubscriptionReply source =
                new CreateExternalSubscriptionReply()
                {
                    CorrelationId = Guid.NewGuid(),
                    IsSuccessful = true,
                    SuccessMessage = "success blah blah blah",
                    ExternalSubscriptionId = "sub_123456789"
                };
            ISubscription subscription = itsTarget.Map<ISubscription>(source);
            CreateSubscriptionReply destination = 
                itsTarget.Map(subscription,itsTarget.Map<CreateSubscriptionReply>(source));

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);

            Assert.AreEqual(
                source.ExternalSubscriptionId,
                subscription.ExternalSubscriptionId);

            Assert.AreEqual(
                subscription.SubscriptionId,
                destination.NewSubscription.SubscriptionId);
        }

        [Test]
        public void
        TestMapUpdateSubscriptionRequestToUpdateExternalSubscriptionRequest()
        {
            UpdateSubscriptionRequest source =
                new UpdateSubscriptionRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    CurrentSubscription =
                        new SubscriptionData()
                        {
                            SubscriptionId = Guid.NewGuid(),
                            PlanId = Guid.NewGuid(),
                            PlanName = "Elite"
                        },
                    NewSubscription =
                        new InitialSubscriptionData()
                        {
                            UserId = Guid.NewGuid(),
                            AccountId = Guid.NewGuid(),
                            SubscriptionType = "Enterprise"
                        },

                };
            ISubscription subscription =
                new StandardSubscription()
                {
                    SubscriptionId = Guid.NewGuid(),
                    AccountId = Guid.NewGuid(),
                    ExternalSubscriptionId = "sub_123456789",
                    ExternalPlanId = "plan_123456789"
                };
            UpdateExternalSubscriptionRequest destination =
                itsTarget.Map(
                    subscription,
                    itsTarget.Map<UpdateExternalSubscriptionRequest>(source));

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);
            Assert.AreEqual(
                subscription.ExternalSubscriptionId,
                destination.ExternalSubscriptionId);
            Assert.AreEqual(
                subscription.ExternalPlanId,
                destination.ExternalPlanId);
        }

        [Test]
        public void
        TestMapUpdateExternalSubscriptionReplyToUpdateSubcriptionReply()
        {
            UpdateExternalSubscriptionReply source =
                new UpdateExternalSubscriptionReply()
                {
                    CorrelationId = Guid.NewGuid(),
                    IsSuccessful = true,
                    SuccessMessage = "success blah blah blah",
                    ExternalSubscriptionId = "sub_123456789",
                    ExternalPlanId = "plan_123456789"
                };
            ISubscription subscription = itsTarget.Map<ISubscription>(source);
            UpdateSubscriptionReply destination =
                itsTarget.Map(subscription,itsTarget.Map<UpdateSubscriptionReply>(source));

            Assert.AreEqual(source.CorrelationId,destination.CorrelationId);

            Assert.AreEqual(
                source.ExternalSubscriptionId,
                subscription.ExternalSubscriptionId);
            Assert.AreEqual(
                source.ExternalPlanId,
                subscription.ExternalPlanId);

            Assert.AreEqual(
                subscription.SubscriptionId,
                destination.UpdatedSubscription.SubscriptionId);
        }

    }
}

//  ##########################################################################
