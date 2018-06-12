//  ##########################################################################
//  # File Name: Authenticate.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Injection;
using Granite.Domain.Session;
using System;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Security.Principal;
using System.Threading;
using System.Threading.Tasks;
using System.Web.Http.Filters;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class Authenticate:
        Attribute,
        IAuthenticationFilter
    {
        public bool AllowMultiple { get; private set; }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public Task
        AuthenticateAsync(
            HttpAuthenticationContext context,
            CancellationToken cancellationToken)
        {
            return Task.Run(() => DoAuthenticate(context,cancellationToken));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public Task 
        ChallengeAsync(
            HttpAuthenticationChallengeContext context,
            CancellationToken                  cancellationToken)
        {
            return Task.FromResult(0);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected void
        DoAuthenticate(
            HttpAuthenticationContext context,
            CancellationToken         cancellationToken)
        {
            HttpRequestMessage        request = context.Request;
            AuthenticationHeaderValue authorization =
                                          request.Headers.Authorization;
            ISession                  session = null;
            IPrincipal                principal = null;

            if (authorization == null)
                return;

            if (authorization.Scheme != "Granite")
                return;

            if (string.IsNullOrEmpty(authorization.Parameter))
            {
                context.ErrorResult =
                    new AuthenticationFailureResult(
                        "Missing credentials",
                        request);
                return;
            }

            session = GetSession(authorization);

            if (session == null)
            {
                context.ErrorResult =
                    new AuthenticationFailureResult(
                        "Invalid credentials",
                        request);
                return;
            }

            principal = new GranitePrincipal(session.User);

            if (!principal.Identity.IsAuthenticated)
                context.ErrorResult =
                    new AuthenticationFailureResult(
                        "Invalid user",
                        request);
            else
                context.Principal = principal;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected ISession 
        GetSession(AuthenticationHeaderValue authenticationHeader)
        {
            ISessionRepository sessions =
                Global
                    .Container
                    .GetInstance<ISessionRepository>();

            return 
                sessions.GetUniqueWithAccessToken(
                    authenticationHeader.Parameter);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected bool 
        NotExpired(ISession session)
        {
            return
                DateTime.Now - session.LastRefresh < TimeSpan.FromMinutes(10);

        }

    }
}

//  ##########################################################################
