//  ##########################################################################
//  # File Name: UserProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Utility;
using Granite.Common.Value;
using Granite.Domain.User;
using Granite.ProvidedService.User;
using Granite.RequiredService.User;
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
        IUserProcessor
    {
        private IExternalUserService       itsService;
        private IUserRepository            itsRepository;
        private IVerificationCodeGenerator itsGenerator;
        private IEmailSender               itsSender;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public 
        UserProcessor(
            IExternalUserService       service,
            IUserRepository            repository,
            IVerificationCodeGenerator generator,
            IEmailSender               sender)
        {
            itsService = service;
            itsRepository = repository;
            itsGenerator = generator;
            itsSender = sender;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public StartRegistrationReply 
        StartRegistration(StartRegistrationRequest request)
        {
            CreateExternalUserReply createdUser = null;
            UserIdentity            user = null;

            createdUser = 
                itsService.CreateExternalUser(
                    new CreateExternalUserRequest()
                    {
                        UserName      = request.UserName,
                        Password      = request.Password,
                        Name          = request.Name,
                        Email         = request.Email,
                        Phone         = request.Phone,
                        Address       = request.Address
                    } );

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
                    new UserIdentity()
                    {
                        ExternalUserId   = createdUser.ExternalUserId,
                        VerificationCode = itsGenerator.Generate(),
                        Status           = UserStatus.PENDING
                    } );

            itsSender.GenerateAndSend(
                "registration@ezvaluator.com",
                request.Email.ToString(),
                new Dictionary<string,string>()
                {
                    {Constant.USER_NAME_PROPERTY,request.UserName},
                    {Constant.VERIFICATION_CODE_PROPERTY,user.VerificationCode}
                } );

            return 
                new StartRegistrationReply()
                {
                    UserId = user.UserId
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public FinishRegistrationReply 
        FinishRegistration(FinishRegistrationRequest request)
        {
            FinishRegistrationReply reply = new FinishRegistrationReply();

            return reply;
        }
    }
}

//  ##########################################################################
