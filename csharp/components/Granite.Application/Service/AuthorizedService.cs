//  ##########################################################################
//  # File Name: AuthorizedService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Interception;
using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;
using Granite.Domain.Session;
using Granite.Domain.User;
using Granite.ProvidedService.Transfer;
using Strata.Application.Messaging;
using Strata.Application.Service;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;

namespace Granite.Application.Service
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public abstract
    class AuthorizedService:
        AbstractService,
        IAuthorizationPropertySupplier
    {
        public ITenantManager           Manager { get; }
        public IAccessTokenRefresher    Refresher { get; }
        public IClientIpAddressRecorder Recorder { get; }

        private ISessionRepository itsRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        AuthorizedService(
            IUnitOfWorkProvider      provider,
            IActionQueue             queue,
            ILogger                  logger,
            ITenantManager           manager,
            IAccessTokenRefresher    refresher,
            IClientIpAddressRecorder recorder,
            ISessionRepository       repository): 
            base(provider,queue,logger)
        {
            Manager       = manager;
            Queue         = queue;
            Refresher     = refresher;
            Recorder      = recorder;
            itsRepository = repository;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual ISession
        GetSessionFrom(GraniteRequest request)
        {
            if (string.IsNullOrEmpty(request.AccessToken))
                return null;

            return itsRepository.GetUniqueWithAccessToken(request.AccessToken);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual IUser
        GetUserFrom(GraniteRequest request)
        {
            ISession session = GetSessionFrom(request);

            if (session == null)
                return null;

            return session.User;
        }
    }
}

//  ##########################################################################
