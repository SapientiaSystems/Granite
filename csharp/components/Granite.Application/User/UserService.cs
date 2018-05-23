//  ##########################################################################
//  # File Name: UserService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.User;
using Strata.Foundation.Logging;
using Strata.Persistence.UnitOfWork;
using Strata.Service.Core;
using System;
using SystemEx.Injection;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class UserService:
        AbstractService,
        IUserService
    {
        private IUserProcessor itsProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        UserService(
            IUnitOfWorkProvider    provider,
            ILogger                logger,
            IUserProcessor         processor):
            base(provider,logger)
        {
            itsProcessor = processor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateUserReply 
        CreateUser(CreateUserRequest request)
        {
            return
                Execute(
                    (CreateUserRequest r) =>
                        { return itsProcessor.CreateUser(r); },
                    request );
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public VerifyUserReply 
        VerifyUser(VerifyUserRequest request)
        {
            return
                Execute(
                    (VerifyUserRequest r) =>
                        { return itsProcessor.VerifyUser(r); },
                    request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateUserReply 
        UpdateUser(UpdateUserRequest request)
        {
            throw new NotImplementedException();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteUserReply
        DeleteUser(DeleteUserRequest request)
        {
            return
                Execute(
                    (DeleteUserRequest r) =>
                        { return itsProcessor.DeleteUser(r); },
                    request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public GetUserReply 
        GetUser()
        {
            throw new NotImplementedException();
        }
    }
}

//  ##########################################################################
