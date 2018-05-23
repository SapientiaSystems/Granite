//  ##########################################################################
//  # File Name: SessionProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using Granite.Domain.Session;
using Granite.Domain.User;
using Granite.ProvidedService.Session;
using Granite.RequiredService.Authentication;
using Strata.Foundation.Logging;
using Strata.Persistence.UnitOfWork;
using Strata.Service.Core;
using SystemEx.Injection;
using System.Linq;
using Granite.Domain.Shared;

namespace Granite.Application.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class SessionProcessor:
        AbstractProcessor,
        ISessionProcessor
    {
        private IAuthenticationService itsAuthenticator;
        private ISessionRepository     itsSessionRepository;
        private IUserRepository        itsUserRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        SessionProcessor(
            IUnitOfWorkProvider    provider,
            IAuthenticationService authenticator,
            ISessionRepository     sessionRepository,
            IUserRepository        userRepository,
            ILogger                logger):
            base(provider,logger)
        {
            itsAuthenticator     = authenticator;
            itsSessionRepository = sessionRepository;
            itsUserRepository    = userRepository;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public LoginReply 
        Login(LoginRequest request)
        {
            AuthenticateReply reply = null;
            IUser             user = null;
            ISession          session = null;

            reply =
                itsAuthenticator.Authenticate(
                    new AuthenticateRequest()
                    {
                        Username = request.UserName,
                        Password = request.Password
                    });
            user =
                itsUserRepository.GetUniqueWithUserName(request.UserName);

            if (user == null)
                throw
                    new NoSuchEntityException(
                        typeof(IUser),
                        request.UserName);

            session =
                itsSessionRepository.Insert(
                    new StandardSession()
                    {
                        UserId          = user.UserId,
                        AccessToken     = reply.AccessToken,
                        AccessTokenType = reply.AccessTokenType,
                        RefreshToken    = reply.RefreshToken
                    });

            return
                new LoginReply()
                {
                    NewSession =
                        new SessionData()
                        {
                            SessionId       = session.SessionId,
                            UserId          = session.UserId,
                            AuthorizedRoles = GetAuthorizedRoles(session),
                            AccessToken     = session.AccessToken,
                            AccessTokenType = session.AccessTokenType
                        }
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public LogoutReply 
        Logout(LogoutRequest request)
        {
            throw new NotImplementedException();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public KeepAliveReply 
        KeepAlive(KeepAliveRequest request)
        {
            throw new NotImplementedException();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public string[]
        GetAuthorizedRoles(ISession session)
        {
            if (!session.IsUserValid)
                return Array.Empty<string>();

            return
                session
                    .User
                    .Roles
                    .Select(r => r.RoleName)
                    .ToArray();
        }
    }
}

//  ##########################################################################
