//  ##########################################################################
//  # File Name: UserService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Service;
using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;
using Granite.Domain.Session;
using Granite.ProvidedService.User;
using Strata.Application.Messaging;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using SystemEx.Injection;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class UserService:
        AuthorizedService,
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
            IUnitOfWorkProvider      provider,
            IActionQueue             queue,
            ILogger                  logger,
            ITenantManager           manager,
            IAccessTokenRefresher    refresher,
            IClientIpAddressRecorder recorder,
            ISessionRepository       sessions,
            IUserProcessor           processor) :
            base(provider,queue,logger,manager,refresher,recorder,sessions)
        {
            itsProcessor = processor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreateUserReply 
        CreateUser(CreateUserRequest request)
        {
            return itsProcessor.CreateUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public VerifyUserReply
        VerifyUser(VerifyUserRequest request)
        {
            return itsProcessor.VerifyUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public UpdateUserReply
        UpdateUser(UpdateUserRequest request)
        {
            return itsProcessor.UpdateUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeleteUserReply 
        DeleteUser(DeleteUserRequest request)
        {
            return itsProcessor.DeleteUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public GetUserReply
        GetUser(GetUserRequest request)
        {
            return itsProcessor.GetUser(request);
        }
    }
}

//  ##########################################################################
