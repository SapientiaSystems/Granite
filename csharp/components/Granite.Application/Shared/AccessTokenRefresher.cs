//  ##########################################################################
//  # File Name: AccessTokenRefresher.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using Granite.Common.Access;
using Granite.Domain.Session;
using Granite.ProvidedService.Transfer;
using SystemEx.Injection;

namespace Granite.Application.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class AccessTokenRefresher:
        IAccessTokenRefresher
    {
        private ISessionRepository itsSessions;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        AccessTokenRefresher(ISessionRepository sessions)
        {
            itsSessions = sessions;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void
        RefreshIfNeeded(GraniteReply reply)
        {
            ISession session = null;

            if (reply.SessionId == Guid.Empty)
                return;

            if (NotAuthorized(reply))
                return;

            session = itsSessions.GetUnique(reply.SessionId);

            if (session != null)
            {
                if (MustRefresh(session))
                {
                    session.Refresh();
                    session = itsSessions.Update(session);
                    reply.RefreshedAccessToken = session.AccessToken;
                }
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private bool
        NotAuthorized(GraniteReply reply)
        {
            return
                !reply.IsSuccessful &&
                reply.ExceptionType == typeof(AuthorizationException).Name;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private bool
        MustRefresh(ISession session)
        {
            return
                DateTime.Now - session.LastRefresh >
                TimeSpan.FromMinutes(5);

        }
    }
}

//  ##########################################################################
