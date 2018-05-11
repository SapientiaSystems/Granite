//  ##########################################################################
//  # File Name: UserProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Utility;
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
        private IUserRepository            itsRepository;
        private IVerificationCodeGenerator itsGenerator;
        private IEmailSender               itsSender;
        private ILogger                    itsLogger;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public 
        UserProcessor(
            IUnitOfWorkProvider        provider,
            IExternalUserService       service,
            IUserRepository            repository,
            IVerificationCodeGenerator generator,
            IEmailSender               sender,
            ILogger                    logger):
            base(provider)
        {
            itsService = service;
            itsRepository = repository;
            itsGenerator = generator;
            itsSender = sender;
            itsLogger = logger;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public StartRegistrationReply 
        StartRegistration(StartRegistrationRequest request)
        {
            CreateExternalUserReply createdUser = null;
            IUser                   user = null;

            createdUser = CreateExternalUser(request);

            if (!createdUser.IsSuccessful)
                return
                    new StartRegistrationReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = 
                            "CreateExternalUser() failed: " + 
                            createdUser.FailureMessage,
                        StackTrace = createdUser.StackTrace
                    };

            user =
                itsRepository.Insert(
                    new StandardUser()
                    {
                        ExternalUserId   = createdUser.ExternalUserId,
                        UserName         = createdUser.UserName,
                        Email            = createdUser.Email,
                        JobTitle         = createdUser.JobTitle,
                        VerificationCode = itsGenerator.Generate(),
                        Status           = UserStatus.PENDING
                    } );

            itsSender.GenerateAndSend(
                "registration@ezvaluator.com",
                request.Email.ToString(),
                new Dictionary<string,string>()
                {
                    {Constant.CORRELATION_ID_PROPERTY,request.CorrelationId.ToString()},
                    {Constant.USER_ID_PROPERTY,user.UserId.ToString()},
                    {Constant.USER_NAME_PROPERTY,user.UserName},
                    {Constant.VERIFICATION_CODE_PROPERTY,user.VerificationCode}
                } );

            return
                new StartRegistrationReply()
                {
                    UserId = user.UserId,
                    UserName = createdUser.UserName,
                    Email = createdUser.Email,
                    JobTitle = createdUser.JobTitle,
                    SuccessMessage =
                         string.Format(
                            "Registration for user '{0}' has started. " +
                            "A confirmation email was sent to '{1}'. " +
                            "Please following the instructions in this " +
                            "email to finish user registration. Thank you.",
                            createdUser.UserName,
                            createdUser.Email.ToString())
                        };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public FinishRegistrationReply
        FinishRegistration(FinishRegistrationRequest request)
        {
            IUser user = 
                itsRepository.GetUniqueWithUserName(request.UserNameToVerify);

            if (user == null)
                throw 
                    new NoSuchEntityException(
                        typeof(IUser),
                        request.UserNameToVerify);

            user = VerifyUser(user,request.VerificationCode);
            user = itsRepository.Update(user);

            return
                new FinishRegistrationReply()
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
        public RemoveReply 
        Remove(RemoveRequest request)
        {
            IUser                   user = GetUserToRemove(request);
            DeleteExternalUserReply deleted = null;

            if (user == null)
                return
                    new RemoveReply()
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
                    new RemoveReply()
                    {
                        UserId = request.UserId,
                        SessionId = request.SessionId,
                        IsSuccessful = false,
                        FailureMessage = deleted.FailureMessage,
                        StackTrace = deleted.StackTrace
                    };

            itsRepository.Remove(user);

            return
                new RemoveReply()
                {
                    UserId = request.UserId,
                    SessionId = request.SessionId,
                    IsSuccessful = true,
                    UserIdRemoved = user.UserId
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected CreateExternalUserReply
        CreateExternalUser(StartRegistrationRequest request)
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
        GetUserToRemove(RemoveRequest request)
        {
            if (request.UserIdToRemove != null)
                return itsRepository.GetUnique(request.UserIdToRemove);
            else if (!string.IsNullOrEmpty(request.UserNameToRemove))
                return 
                    itsRepository.GetUniqueWithUserName(
                        request.UserNameToRemove);

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

    }
}

//  ##########################################################################
