//  ##########################################################################
//  # File Name: TenantManager.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using Granite.Domain.Session;
using System;
using SystemEx.Injection;

namespace Granite.Application.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class TenantManager:
        ITenantManager
    {
        private ISessionRepository itsRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///
        [Inject]
        public
        TenantManager(ISessionRepository repository)
        {
            itsRepository = repository;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        public virtual void 
        ActivateTenant(
            IAccessTokenProvider tokenProvider,
            ISessionIdProvider   idProvider)
        {
            ISession session = null;

            if (HasAccessToken(tokenProvider))
                session = 
                    itsRepository.GetUniqueWithAccessToken(
                        tokenProvider.AccessToken);

            if (session == null)
                if (!HasSessionId(idProvider))
                    return;

            session = itsRepository.GetUnique(idProvider.SessionId);

            if (session != null)
                session.ActivateTenant();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        public virtual void 
        DeactivateTenant(
            IAccessTokenProvider tokenProvider,
            ISessionIdProvider   idProvider)
        {
            ISession session = null;

            if (HasAccessToken(tokenProvider))
                session =
                    itsRepository.GetUniqueWithAccessToken(
                        tokenProvider.AccessToken);

            if (session == null)
                if (!HasSessionId(idProvider))
                    return;

            session = itsRepository.GetUnique(idProvider.SessionId);

            if (session != null)
                session.DeactivateTenant();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///
        protected virtual bool
        HasAccessToken(IAccessTokenProvider provider)
        {
            return
                provider != null &&
                !string.IsNullOrEmpty(provider.AccessToken);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///
        protected virtual bool
        HasSessionId(ISessionIdProvider provider)
        {
            return
                provider != null &&
                provider.SessionId != Guid.Empty;
        }
    }
}

//  ##########################################################################
