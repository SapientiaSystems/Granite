//  ##########################################################################
//  # File Name: UserProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.Application.Service;
using Granite.Common.Access;
using Granite.Common.Value;
using Granite.Common.Verification;
using Granite.Domain.Role;
using Granite.Domain.Session;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using Granite.ProvidedService.Transfer;
using Granite.ProvidedService.User;
using Strata.Domain.NamedQuery;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;
using SystemEx.Injection;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class UserProcessor:
        AuthorizedService,
        IUserProcessor,
        IUserDomainEventObserver,
        ITenantDomainEventObserver
    {
        private IUserFactory               itsUserFactory;
        private ITenantRepository          itsTenantRepository;
        private IUserRepository            itsUserRepository;
        private IRoleRepository            itsRoleRepository;
        private ISessionRepository         itsSessionRepository;
        private IVerificationCodeGenerator itsGenerator;
        private IVerificationNotifier      itsNotifier;
        private IMapper                    itsMapper;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        UserProcessor(
            IUnitOfWorkProvider        provider,
            IUserFactory               userFactory,
            ITenantRepository          tenantRepository,
            IUserRepository            userRepository,
            IRoleRepository            roleRepository,
            ISessionRepository         sessionRepository,
            IVerificationCodeGenerator generator,
            IVerificationNotifier      notifier,
            ILogger                    logger,
            IMapper                    mapper):
            base(provider,logger,sessionRepository)
        {
            itsUserFactory       = userFactory;
            itsTenantRepository  = tenantRepository;
            itsUserRepository    = userRepository;
            itsRoleRepository    = roleRepository;
            itsSessionRepository = sessionRepository;
            itsGenerator         = generator;
            itsNotifier          = notifier;
            itsMapper            = mapper;

            itsUserRepository
                .Observers
                .Add(this);
            itsTenantRepository
                .Observers
                .Add(this);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreateUserReply 
        CreateUser(CreateUserRequest request)
        {
            ITenant tenant = null;
            IUser   user = null;

            try
            {
                ValidateRequest(request);

                tenant = DoGetOrCreateTenant(request);
                user = DoCreateUser(request,tenant);

                user.ActivateTenant();

                itsNotifier.Notify(
                    new VerificationNotice()
                    {
                        MessageType = MessageKind.CONFIRM_NEW_USER_EMAIL,
                        From = "verification@ezvaluator.com",
                        To = request.UserToCreate.Email.ToString(),
                        Parameters =
                            new Dictionary<string,string>()
                            {
                            {Constant.CORRELATION_ID_PROPERTY,request.CorrelationId.ToString()},
                            {Constant.USER_ID_PROPERTY,user.UserId.ToString()},
                            {Constant.USER_NAME_PROPERTY,user.UserName},
                            {Constant.VERIFICATION_CODE_PROPERTY,user.VerificationCode}
                            }
                    });

                return
                    new CreateUserReply()
                    {
                        IsSuccessful = true,
                        NewUser = itsMapper.Map(user,new UserData()),
                        SuccessMessage =
                             string.Format(
                                "Registration for user '{0}' has started. " +
                                "A confirmation email was sent to '{1}'. " +
                                "Please follow the instructions in this " +
                                "email to verify the user. Thank you.",
                                user.UserName,
                                user.Email.ToString())
                    };
            }
            finally
            {
                if (user != null)
                    user.DeactivateTenant();
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public VerifyUserReply
        VerifyUser(VerifyUserRequest request)
        {
            IUser user = 
                itsUserRepository.GetUniqueWithUserName(request.UserNameToVerify);

            if (user == null)
                throw 
                    new NoSuchEntityException(
                        typeof(IUser),
                        request.UserNameToVerify);

            user = VerifyUser(user,request.VerificationCode);
            user = itsUserRepository.Update(user);

            return
                new VerifyUserReply()
                {
                    UserIdVerified = user.UserId,
                    IsSuccessful = true,
                    SuccessMessage =
                        string.Format(
                            "NewUser '{0}' has been verified.",
                            user.UserName)
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public UpdateUserReply
        UpdateUser(UpdateUserRequest request)
        {
            IUser user = GetUserToUpdate(request);

            if (user == null)
                return
                    new UpdateUserReply()
                    {
                        IsSuccessful = false,
                        FailureMessage =
                            string.Format(
                                "User {0} does not exist.",
                                GetUserIdentifier(request))
                    };

            user = DoUpdateUser(user,request);

            if (request.UserToUpdate.Operation == UserUpdateKind.CHANGE_EMAIL)
            {
                itsNotifier.Notify(
                    new VerificationNotice()
                    {
                        MessageType = MessageKind.CONFIRM_UPDATED_USER_EMAIL,
                        From = "verification@ezvaluator.com",
                        To = request.UserToUpdate.NewEmail.ToString(),
                        Parameters =
                            new Dictionary<string,string>()
                            {
                            {Constant.CORRELATION_ID_PROPERTY,request.CorrelationId.ToString()},
                            {Constant.USER_ID_PROPERTY,user.UserId.ToString()},
                            {Constant.USER_NAME_PROPERTY,user.UserName},
                            {Constant.VERIFICATION_CODE_PROPERTY,user.VerificationCode}
                            }
                    });
            }

            return
                new UpdateUserReply()
                {
                    IsSuccessful = true,
                    SuccessMessage =
                            string.Format(
                                "User {0} has been updated.",
                                user.UserName),
                    UpdatedUser = itsMapper.Map(user,new UserData())
                };

        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeleteUserReply 
        DeleteUser(DeleteUserRequest request)
        {
            IUser   user   = GetUserToRemove(request);
            ITenant tenant = null;
            bool    deletedTenant = false;

            if (user == null)
                return
                    new DeleteUserReply()
                    {
                        IsSuccessful = true,
                        SuccessMessage = 
                            string.Format(
                                "User {0} does not exist or " +
                                "has already been deleted.",
                                user.UserId)
                    };

            tenant = user.Tenant;
            itsUserRepository.Remove(user);

            if (tenant.TenantName.Equals(user.UserName))
            {
                itsTenantRepository.Remove(tenant);
                deletedTenant = true;
            }

            return
                new DeleteUserReply()
                {
                    IsSuccessful  = true,
                    DeletedUserId = user.UserId,
                    HasAlsoDeletedTenant = deletedTenant,
                    DeletedTenantId = 
                        deletedTenant ? tenant.TenantId : Guid.Empty
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public GetUserReply
        GetUser(GetUserRequest request)
        {
            IUser user = null;

            CheckPermission(request);
            user = GetUserToReturn(request);

            if (user == null)
                return
                    new GetUserReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = GetFailureMessage(request)
                    };

            return
                new GetUserReply()
                {
                    IsSuccessful = true,
                    RequestedUser = itsMapper.Map(user,new UserData())
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void
        OnEvent(IUserDomainEvent evnt)
        {
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void
        OnEvent(ITenantDomainEvent evnt)
        {
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void
        OnException(Exception exception)
        {
            throw exception;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected void
        ValidateRequest(CreateUserRequest request)
        {
            InitialUserData userToCreate = request.UserToCreate;

            if (itsUserRepository.HasUniqueWithUserName(userToCreate.UserName))
                throw
                    new NotUniqueException(
                        string.Format(
                            "UserName '{0} has already been used. " +
                            "Please select another username. Thank you.",
                            userToCreate.UserName));
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual ITenant
        DoGetOrCreateTenant(CreateUserRequest request)
        {
            InitialUserData userToCreate = request.UserToCreate;

            if (userToCreate.UseExistingTenant)
            {
                if (itsTenantRepository.HasUnique(userToCreate.TenantId))
                    return 
                        itsTenantRepository.GetUnique(userToCreate.TenantId);

                throw 
                    new NoSuchEntityException(
                        typeof(ITenant),
                        userToCreate.TenantId);
            }

            if (
                itsTenantRepository.HasUniqueWithTenantName(
                    userToCreate.UserName))
                throw
                    new InvalidOperationException(
                        string.Format(
                            "Cannot create new tenant '{0}' " +
                            "because it already exists.",
                            userToCreate.UserName));

            return
                itsTenantRepository.Insert(
                    new StandardTenant(
                        userToCreate.UserName,
                        new HashedString(userToCreate.Password)));
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual IUser 
        DoCreateUser(CreateUserRequest request,ITenant tenant)
        {
            InitialUserData userToCreate = request.UserToCreate;

            if (userToCreate.HasSubscriptionType)
                return
                    itsUserRepository.Insert(
                        itsUserFactory.CreateUser(
                            tenant,
                            userToCreate.UserName,
                            new HashedString(userToCreate.Password),
                            userToCreate.Email,
                            userToCreate.SubscriptionType));

            return
                itsUserRepository.Insert(
                    itsUserFactory.CreateUser(
                            tenant,
                            userToCreate.UserName,
                            new HashedString(userToCreate.Password),
                            userToCreate.Email,
                            itsRoleRepository.GetRoles(
                                new string[] { "GraniteUser" })));
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IUser
        GetUserToUpdate(UpdateUserRequest request)
        {
            UpdateUserData target = request.UserToUpdate;

            if (target == null)
                return null;

            if (target.CurrentUser)
                return GetCurrentUser(request);

            if (target.UserIdOfUserToUpdate != null)
                return itsUserRepository.GetUnique(target.UserIdOfUserToUpdate);

            if (!string.IsNullOrEmpty(target.UserNameOfUserToUpdate))
                return
                    itsUserRepository.GetUniqueWithUserName(
                        target.UserNameOfUserToUpdate);

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected string
        GetUserIdentifier(UpdateUserRequest request)
        {
            UpdateUserData target = request.UserToUpdate;

            if (target == null)
                return "<no user>";

            if (target.UserIdOfUserToUpdate != null)
                return target.UserIdOfUserToUpdate.ToString();
            else if (!string.IsNullOrEmpty(target.UserNameOfUserToUpdate))
                return target.UserNameOfUserToUpdate;

            return "<no user>";
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual IUser
        DoUpdateUser(IUser user,UpdateUserRequest request)
        {
            UpdateUserData userToUpdate = request.UserToUpdate;

            if (userToUpdate.Operation == UserUpdateKind.CHANGE_PASSWORD)
            {
                if (!user.Password.Matches(userToUpdate.CurrentPassword))
                    throw 
                        new AuthenticationException(
                            "current password is incorrect.");

                user.Password = new HashedString(userToUpdate.NewPassword);
            }           
            else if (userToUpdate.Operation == UserUpdateKind.CHANGE_EMAIL)
            { 
                user.Email = userToUpdate.NewEmail;

            }

            return itsUserRepository.Update(user);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IUser
        GetUserToRemove(DeleteUserRequest request)
        {
            if (request.UserIdOfUserToDelete != null)
                return itsUserRepository.GetUnique(request.UserIdOfUserToDelete);
            else if (!string.IsNullOrEmpty(request.UserNameOfUserToDelete))
                return 
                    itsUserRepository.GetUniqueWithUserName(
                        request.UserNameOfUserToDelete);

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IUser 
        VerifyUser(IUser user,string verificationCode)
        {
            if (!user.Verify(verificationCode))
                throw
                    new VerificationFailedException(
                        user.UserId,
                        user.VerificationCode,
                        verificationCode);

            return user;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected void
        CheckPermission(GetUserRequest request)
        {
            IUser user = GetUserFrom(request);

            if (user == null)
                throw
                    new AuthorizationException(
                        "Invalid user: could not find user from request");

            if (request.GetCurrentUser)
            {
                if (!user.IsInRole("GraniteGuest"))
                    throw
                        new AuthorizationException(
                            "User is not in permissable role: GraniteGuest");

                return;
            }
            
            if (!user.IsInRole("GraniteAdministrator"))
                throw
                    new AuthorizationException(
                        "User is not in permissable role: " +
                        "GraniteAdministrator");


        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IUser
        GetUserToReturn(GetUserRequest request)
        {
            if (request.GetCurrentUser)
                return GetCurrentUser(request);

            if (request.UserIdOfUserToGet != Guid.Empty)
                return itsUserRepository.GetUnique(request.UserIdOfUserToGet);
            else if (!string.IsNullOrEmpty(request.UserNameOfUserToGet))
                return
                    itsUserRepository.GetUniqueWithUserName(
                        request.UserNameOfUserToGet);

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IUser
        GetCurrentUser(GraniteRequest request)
        {
            ISession session =
                itsSessionRepository.GetUniqueWithAccessToken(
                    request.AccessToken);

            return session.User;
        }


        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected string
        GetFailureMessage(GetUserRequest request)
        {
            if (request.GetCurrentUser)
            {
                IUser user = GetCurrentUser(request);

                return
                    string.Format(
                        "User '{0}' not found.",
                        request.UserNameOfUserToGet);
            }

            if (request.UserIdOfUserToGet != Guid.Empty)
                return
                    string.Format(
                        "User '{0}' not found.",
                        request.UserIdOfUserToGet);

            if (!string.IsNullOrEmpty(request.UserNameOfUserToGet))
                return
                    string.Format(
                        "User '{0}' not found.",
                        request.UserNameOfUserToGet);

            return "User not found";
        }
    }
}

//  ##########################################################################
