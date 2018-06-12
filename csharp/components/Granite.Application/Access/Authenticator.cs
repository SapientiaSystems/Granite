//  ##########################################################################
//  # File Name: Authenticator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using Granite.Domain.Session;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using System;
using SystemEx.Injection;

namespace Granite.Application.Access
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class Authenticator:
        IAuthenticator
    {
        private ITenantRepository  itsTenants;
        private IUserRepository    itsUsers;
        private ISessionRepository itsSessions;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        Authenticator(
            ITenantRepository  tenants,
            IUserRepository    users,
            ISessionRepository sessions)
        {
            itsTenants  = tenants;
            itsUsers    = users;
            itsSessions = sessions;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void 
        AuthenticateTenant(Guid tenantId,string secretKey)
        {
            ITenant tenant = itsTenants.GetUnique(tenantId);

            if (tenant == null)
                throw
                    new AuthenticationException(
                        "Authentication failed: tenant not found.");

            if ( 
                !tenant
                    .SecretKey
                    .Matches(secretKey))
                throw
                    new AuthenticationException(
                        "Authentication failed: incorrect secret key.");

        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void 
        AuthenticateUser(string userName,string password)
        {
            IUser user = itsUsers.GetUniqueWithUserName(userName);

            if (user == null)
                throw
                    new AuthenticationException(
                        "Authentication failed: user not found.");

            if (
                !user
                    .Password
                    .Matches(password))
                throw
                    new AuthenticationException(
                        "Authentication failed: incorrect password.");
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void 
        AuthenticateSession(string accessToken)
        {
            ISession session = 
                itsSessions.GetUniqueWithAccessToken(accessToken);

            if (session == null)
                throw
                    new AuthenticationException(
                        "Authentication failed: session not found.");

            if (!session.IsOpen)
                throw
                    new AuthenticationException(
                        "Authentication failed: session is closed.");
        }
    }
}

//  ##########################################################################
