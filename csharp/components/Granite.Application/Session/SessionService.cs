//  ##########################################################################
//  # File Name: SessionService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Service;
using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;
using Granite.Domain.Session;
using Granite.ProvidedService.Session;
using Strata.Application.Messaging;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using SystemEx.Injection;

namespace Granite.Application.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class SessionService:
        AuthorizedService,
        ISessionService
    {
        private ISessionProcessor itsProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public 
        SessionService(
            IUnitOfWorkProvider      provider,
            IActionQueue             queue,
            ILogger                  logger,
            ITenantManager           manager,
            IAccessTokenRefresher    refresher,
            IClientIpAddressRecorder recorder,
            ISessionRepository       sessions,
            ISessionProcessor        processor):
            base(provider,queue,logger,manager,refresher,recorder,sessions)
        {
            itsProcessor = processor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public LoginReply 
        Login(LoginRequest request)
        {
            return itsProcessor.Login(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public LogoutReply 
        Logout(LogoutRequest request)
        {
            return itsProcessor.Logout(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public GetOrLoginReply
        GetOrLogin(GetOrLoginRequest request)
        {
            return itsProcessor.GetOrLogin(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public KeepAliveReply 
        KeepAlive(KeepAliveRequest request)
        {
            return itsProcessor.KeepAlive(request);
        }
    }
}

//  ##########################################################################
