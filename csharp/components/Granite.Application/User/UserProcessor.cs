//  ##########################################################################
//  # File Name: UserProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Verification;
using Granite.Common.Value;
using Granite.Domain.Shared;
using Granite.Domain.User;
using Granite.ProvidedService.User;
using Granite.RequiredService.User;
using Strata.Foundation.Logging;
using Strata.Persistence.UnitOfWork;
using Strata.Service.Core;
using System.Collections.Generic;
using SystemEx.Injection;
using System;
using Strata.Persistence.NamedQuery;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class UserProcessor:
        AbstractProcessor,
        IUserProcessor
    {
        private IExternalUserService       itsService;
        private IUserRepository            itsUserRepository;
        private IRoleRepository            itsRoleRepository;
        private IVerificationCodeGenerator itsGenerator;
        private IVerificationNotifier      itsNotifier;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public 
        UserProcessor(
            IUnitOfWorkProvider        provider,
            IExternalUserService       service,
            IUserRepository            userRepository,
            IRoleRepository            roleRepository,
            IVerificationCodeGenerator generator,
            IVerificationNotifier      notifier,
            ILogger                    logger):
            base(provider,logger)
        {
            itsService = service;
            itsUserRepository = userRepository;
            itsRoleRepository = roleRepository;
            itsGenerator = generator;
            itsNotifier = notifier;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreateUserReply 
        CreateUser(CreateUserRequest request)
        {
            CreateExternalUserReply createdUser = null;
            IUser                   user = null;

            ValidateRequest(request);

            createdUser = CreateExternalUser(request);

            if (!createdUser.IsSuccessful)
                return
                    new CreateUserReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = 
                            "CreateExternalUser() failed: " + 
                            createdUser.FailureMessage,
                        StackTrace = createdUser.StackTrace
                    };

            user =
                new StandardUser()
                {
                    ExternalUserId = createdUser.ExternalUserId,
                    UserName = createdUser.UserName,
                    Email = createdUser.Email,
                    JobTitle = createdUser.JobTitle,
                    VerificationCode = itsGenerator.Generate(),
                    Status = UserStatus.PENDING,
                    Created = DateTime.Now,
                    LastModified = DateTime.Now,
                };
            user = SetRoles(user,request.AuthorizedRoles);
            user = itsUserRepository.Insert(user);
                    
            itsNotifier.Notify(
                new VerificationNotice()
                {
                    MessageType = MessageKind.CONFIRM_NEW_USER_EMAIL,
                    From = "verification@ezvaluator.com",
                    To = request.Email.ToString(),
                    Parameters = 
                        new Dictionary<string,string>()
                        {
                            {Constant.CORRELATION_ID_PROPERTY,request.CorrelationId.ToString()},
                            {Constant.USER_ID_PROPERTY,user.UserId.ToString()},
                            {Constant.USER_NAME_PROPERTY,user.UserName},
                            {Constant.VERIFICATION_CODE_PROPERTY,user.VerificationCode}
                        }
                } );

            return
                new CreateUserReply()
                {
                    UserId = user.UserId,
                    UserName = createdUser.UserName,
                    Email = createdUser.Email,
                    JobTitle = createdUser.JobTitle,
                    SuccessMessage =
                         string.Format(
                            "Registration for user '{0}' has started. " +
                            "A confirmation email was sent to '{1}'. " +
                            "Please follow the instructions in this " +
                            "email to verify the user. Thank you.",
                            createdUser.UserName,
                            createdUser.Email.ToString())
                        };
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
                            "User '{0}' has been verified.",
                            user.UserName)
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeleteUserReply 
        DeleteUser(DeleteUserRequest request)
        {
            IUser                   user = GetUserToRemove(request);
            DeleteExternalUserReply deleted = null;

            if (user == null)
                return
                    new DeleteUserReply()
                    {
                        UserId = request.UserId,
                        SessionId = request.SessionId,
                        IsSuccessful = true
                    };

            deleted =
                itsService.DeleteExternalUser(
                    new DeleteExternalUserRequest()
                    {
                        UserId = request.UserId,
                        ExternalUserId = user.ExternalUserId
                    } );

            if (!deleted.IsSuccessful)
                return
                    new DeleteUserReply()
                    {
                        UserId = request.UserId,
                        SessionId = request.SessionId,
                        IsSuccessful = false,
                        FailureMessage = deleted.FailureMessage,
                        StackTrace = deleted.StackTrace
                    };

            itsUserRepository.Remove(user);

            return
                new DeleteUserReply()
                {
                    UserId = request.UserId,
                    SessionId = request.SessionId,
                    IsSuccessful = true,
                    UserIdDeleted = user.UserId
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected void
        ValidateRequest(CreateUserRequest request)
        {
            IRole       graniteUser = 
                            itsRoleRepository.GetRole("GraniteUser");
            bool        isGraniteUser = false;
            ISet<IRole> roles = 
                            itsRoleRepository.GetRoles(
                                request.AuthorizedRoles);
            IEnumerator<IRole> enumerator = roles.GetEnumerator();

            if (itsUserRepository.HasUniqueWithUserName(request.UserName))
                throw
                    new NotUniqueException(
                        string.Format(
                            "Username '{0} has already been used. " +
                            "Please select another username. Thank you.",
                            request.UserName));

            while (!isGraniteUser &&  enumerator.MoveNext())
            {
                IRole role = enumerator.Current;

                if (role.Includes(graniteUser))
                    isGraniteUser = true;
            }

            if (!isGraniteUser)
                throw 
                    new InvalidOperationException(
                        "Specified roles do not include GraniteUser");

        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected CreateExternalUserReply
        CreateExternalUser(CreateUserRequest request)
        {
            CreateExternalUserReply created = 
                itsService.CreateExternalUser(
                    new CreateExternalUserRequest()
                    {
                        UserName = request.UserName,
                        Password = request.Password,
                        Name = request.Name,
                        Email = request.Email,
                        JobTitle = request.JobTitle,
                        Phone = request.Phone,
                        Address = request.Address
                    });
            PushRollbackAction(
                () =>
                {
                    DeleteExternalUserReply deleted = 
                        itsService.DeleteExternalUser(
                            new DeleteExternalUserRequest()
                            {
                                ExternalUserId = created.ExternalUserId
                            });

                    if (!deleted.IsSuccessful)
                        throw
                            new RollbackFailedException(
                                deleted.FailureMessage);
                });

            return created;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IUser
        GetUserToRemove(DeleteUserRequest request)
        {
            if (request.UserIdToDelete != null)
                return itsUserRepository.GetUnique(request.UserIdToDelete);
            else if (!string.IsNullOrEmpty(request.UserNameToDelete))
                return 
                    itsUserRepository.GetUniqueWithUserName(
                        request.UserNameToDelete);

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IUser 
        VerifyUser(IUser user,string verificationCode)
        {
            if (user.VerificationCode.Equals(verificationCode))
                user.Status = UserStatus.VERIFIED;
            else
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
        protected IUser
        SetRoles(IUser user,string[] roleNames)
        {
            user.SetRoles(itsRoleRepository.GetRoles(roleNames));
            return user;
        }
    }
}

//  ##########################################################################
