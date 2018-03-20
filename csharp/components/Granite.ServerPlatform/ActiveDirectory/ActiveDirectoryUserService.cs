//  ##########################################################################
//  # File Name: ActiveDirectoryUserService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.User;
using Microsoft.Azure.ActiveDirectory.GraphClient;
using System;
using SystemEx.Injection;

namespace Granite.ServerPlatform.ActiveDirectory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ActiveDirectoryUserService:
        IExternalUserService
    {
        private ActiveDirectoryClient itsClient;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        ActiveDirectoryUserService(ActiveDirectoryClient client)
        {
            itsClient = client;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreateExternalUserReply 
        CreateExternalUser(CreateExternalUserRequest request)
        {
            // Create a new user object.
            IUser newUser = new User()
            {
                // Required settings
                DisplayName = request.UserName,
                PasswordProfile = 
                    new PasswordProfile()
                    {
                        Password = request.Password,
                        ForceChangePasswordNextLogin = false
                    },
                MailNickname = request.EmailNickName,
                AccountEnabled = true,

            };

            itsClient
                .Users
                .AddUserAsync(newUser)
                .Wait();

            return
                new CreateExternalUserReply()
                {
                    UserId = newUser.ObjectId
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public UpdateExternalUserReply 
        UpdateExternalUser(UpdateExternalUserRequest request)
        {
            IUser user =
                itsClient
                    .Users
                    .GetByObjectId(request.UserId)
                    .ExecuteAsync()
                    .Result;

            user.Mail = request.Email;
            user
                .UpdateAsync()
                .Wait();

            return
                new UpdateExternalUserReply()
                {
                    UserId = user.ObjectId
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeleteExternalUserReply 
        DeleteExternalUser(DeleteExternalUserRequest request)
        {
            IUser user =
                itsClient
                    .Users
                    .GetByObjectId(request.UserId)
                    .ExecuteAsync()
                    .Result;

            user
                .DeleteAsync()
                .Wait();

            return
                new DeleteExternalUserReply()
                {
                    UserId = user.ObjectId
                };

        }
    }
}

//  ##########################################################################
