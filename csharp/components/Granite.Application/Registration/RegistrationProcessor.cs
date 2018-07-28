//  ##########################################################################
//  # File Name: RegistrationProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.Application.Payment;
using Granite.Application.Profile;
using Granite.Application.Service;
using Granite.Application.Session;
using Granite.Application.User;
using Granite.Domain.Session;
using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Registration;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.User;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using System;
using SystemEx.Injection;

namespace Granite.Application.Registration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class RegistrationProcessor:
        AuthorizedService,
        IRegistrationProcessor
    {
        private IUserProcessor    itsUserProcessor;
        private ISessionProcessor itsSessionProcessor;
        private IProfileProcessor itsProfileProcessor;
        private IPaymentProcessor itsPaymentProcessor;
        private IMapper           itsMapper;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        RegistrationProcessor(
            IUnitOfWorkProvider provider,
            ILogger             logger,
            ISessionRepository  sessions,
            IUserProcessor      userProcessor,
            ISessionProcessor   sessionProcessor,   
            IProfileProcessor   profileProcessor,
            IPaymentProcessor   paymentProcessor,
            IMapper             mapper) :
            base(provider,logger,sessions)
        {
            itsUserProcessor    = userProcessor;
            itsSessionProcessor = sessionProcessor;
            itsProfileProcessor = profileProcessor;
            itsPaymentProcessor = paymentProcessor;
            itsMapper           = mapper;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public RegisterReply
        Register(RegisterRequest request)
        {
            SessionData session = null;

            try
            {
                Tuple<RegisterReply,SessionData> result = null;

                ValidateRequest(request);

                switch (request.RegistrationType)
                {
                    case RegistrationKind.USER_PROFILE:
                        result = CreateUserAndProfile(request);
                        session = result.Item2;
                        return result.Item1;

                    case RegistrationKind.USER_PROFILE_ACCOUNT:
                        result = CreateUserProfileAndAccount(request);
                        session = result.Item2;
                        return result.Item1;

                    case RegistrationKind.USER_PROFILE_ACCOUNT_SUBSCRIPTION:
                        result = 
                            CreateUserProfileAccountAndSubscription(request);
                        session = result.Item2;
                        return result.Item1;
                }

                return
                    new RegisterReply()
                    {
                        RegistrationType = request.RegistrationType,
                        IsSuccessful = false,
                        FailureMessage = "unknown registration type"
                    };
            }
            finally
            {
                if (session != null)
                    itsSessionProcessor.Logout(
                        new LogoutRequest()
                        {
                            CorrelationId = request.CorrelationId,
                            SessionId = session.SessionId,
                            AccessToken = session.AccessToken
                        });
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private Tuple<RegisterReply,SessionData>
        CreateUserAndProfile(RegisterRequest request)
        {
            Tuple<CreateUserReply,SessionData> user = CreateUser(request);
            CreateUserReply                    userReply = user.Item1;
            SessionData                        session = user.Item2;
            CreateProfileRequest               profileRequest = null;
            CreateProfileReply                 profileReply = null;
            Tuple<RegisterReply,SessionData>   result = null;

            profileRequest = itsMapper.Map<CreateProfileRequest>(request);
            profileRequest.SessionId = session.SessionId;
            profileRequest.AccessToken = session.AccessToken;
            profileRequest.ProfileToCreate.UserId = userReply.NewUser.UserId;
            profileReply = itsProfileProcessor.CreateProfile(profileRequest);

            if (!profileReply.IsSuccessful)
                throw
                    new InvalidOperationException(
                        profileReply.FailureMessage);

            result =
                Tuple.Create(
                    itsMapper.Map(
                        userReply,
                        itsMapper.Map<RegisterReply>(profileReply)),
                    session);

            result.Item1.RegistrationType = request.RegistrationType;
            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private Tuple<RegisterReply,SessionData>
        CreateUserProfileAndAccount(RegisterRequest request)
        {
            Tuple<RegisterReply,SessionData> result = null;
            RegisterReply                    reply = null;
            SessionData                      session = null;
            CreateAccountRequest             accountRequest = null;
            CreateAccountReply               accountReply = null;

            result = CreateUserAndProfile(request);
            reply = result.Item1;
            session = result.Item2;

            accountRequest = itsMapper.Map<CreateAccountRequest>(request);
            accountRequest.SessionId = session.SessionId;
            accountRequest.AccessToken = session.AccessToken;
            accountRequest
                .AccountToCreate
                .UserIdOfAccountToCreate = 
                    reply
                        .NewUser
                        .UserId;

            accountReply = itsPaymentProcessor.CreateAccount(accountRequest);
            accountReply.SessionId = request.SessionId;

            if (!accountReply.IsSuccessful)
                throw
                    new InvalidOperationException(
                        accountReply.FailureMessage);

            return Tuple.Create(itsMapper.Map(accountReply,reply),session);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private Tuple<RegisterReply,SessionData>
        CreateUserProfileAccountAndSubscription(RegisterRequest request)
        {
            Tuple<RegisterReply,SessionData> result = null;
            RegisterReply                    reply = null;
            SessionData                      session = null;
            CreateSubscriptionRequest        subscriptionRequest = null;
            CreateSubscriptionReply          subscriptionReply = null;

            result = CreateUserProfileAndAccount(request);
            reply = result.Item1;
            session = result.Item2;

            subscriptionRequest =
                itsMapper.Map<CreateSubscriptionRequest>(request);
            subscriptionRequest.SessionId = session.SessionId;
            subscriptionRequest.AccessToken = session.AccessToken;
            subscriptionRequest
                .SubscriptionToCreate
                .AccountId =
                    reply
                        .NewAccount
                        .AccountId;
            subscriptionReply =
                itsPaymentProcessor.CreateSubscription(subscriptionRequest);
            subscriptionReply.SessionId = request.SessionId;

            if (!subscriptionReply.IsSuccessful)
                throw
                    new InvalidOperationException(
                        subscriptionReply.FailureMessage);

            return 
                Tuple.Create(itsMapper.Map(subscriptionReply,reply),session);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private void 
        ValidateRequest(RegisterRequest request)
        {
            if (request.UserToCreate == null)
                throw
                    new InvalidOperationException(
                        "RegisterRequest is missing user information");

            if (request.ProfileToCreate == null)
                throw
                    new InvalidOperationException(
                        "RegisterRequest is missing profile information");

            switch (request.RegistrationType)
            {
                case RegistrationKind.USER_PROFILE_ACCOUNT:
                    if (request.AccountToCreate == null)
                        throw
                            new InvalidOperationException(
                                "RegisterRequest is missing account information");
                    break;

                case RegistrationKind.USER_PROFILE_ACCOUNT_SUBSCRIPTION:
                    if (request.AccountToCreate == null)
                        throw
                            new InvalidOperationException(
                                "RegisterRequest is missing account information");

                    if (request.SubscriptionToCreate == null)
                        throw
                            new InvalidOperationException(
                                "RegisterRequest is missing subscription information");
                    break;
            }

        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private Tuple<CreateUserReply,SessionData>
        CreateUser(RegisterRequest request)
        {
            CreateUserReply userReply = null;
            LoginReply      loginReply = null;

            userReply =
                itsUserProcessor.CreateUser(
                    itsMapper.Map<CreateUserRequest>(request));

            if (!userReply.IsSuccessful)
                throw
                    new InvalidOperationException(userReply.FailureMessage);

            loginReply =
                itsSessionProcessor.Login(
                    new LoginRequest()
                    {
                        UserName = request.UserToCreate.UserName,
                        Password = request.UserToCreate.Password
                    });

            if (!loginReply.IsSuccessful)
                throw
                    new InvalidOperationException(
                        loginReply.FailureMessage);

            return Tuple.Create(userReply,loginReply.NewSession);
        }
    }
}

//  ##########################################################################
