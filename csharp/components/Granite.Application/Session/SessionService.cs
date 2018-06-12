//  ##########################################################################
//  # File Name: SessionService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Shared;
using Granite.Common.Access;
using Granite.Domain.Group;
using Granite.Domain.Role;
using Granite.Domain.Session;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.User;
using Strata.Application.Service;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using SystemEx.Injection;

namespace Granite.Application.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class SessionService:
        AbstractService,
        ISessionService,
        ISessionDomainEventObserver
    {
        private IAuthenticator     itsAuthenticator;
        private ITenantRepository  itsTenantRepository;
        private ISessionRepository itsSessionRepository;
        private IUserRepository    itsUserRepository;
        private IUserMapper        itsMapper;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        SessionService(
            IUnitOfWorkProvider provider,
            IAuthenticator      authenticator,
            ITenantRepository   tenantRepository,
            ISessionRepository  sessionRepository,
            IUserRepository     userRepository,
            ILogger             logger):
            base(provider,logger)
        {
            itsAuthenticator     = authenticator;
            itsTenantRepository  = tenantRepository;
            itsSessionRepository = sessionRepository;
            itsUserRepository    = userRepository;
            itsMapper            = new UserMapper();

            itsSessionRepository
                .Observers
                .Add(this);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public LoginReply 
        Login(LoginRequest request)
        {
            IUser    user = null;
            ISession session = null;

            try
            {
                itsAuthenticator.AuthenticateUser(
                        request.UserName,
                        request.Password);

                user =
                    itsUserRepository.GetUniqueWithUserName(request.UserName);

                if (user == null)
                    throw
                        new NoSuchEntityException(
                            typeof(IUser),
                            request.UserName);

                user.ActivateTenant();

                session = new StandardSession(user);
                session.Open();
                session = itsSessionRepository.Insert(session);

                return
                    new LoginReply()
                    {
                        IsSuccessful = true,
                        NewSession =
                            new SessionData()
                            {
                                SessionId = session.PrimaryId,
                                UserId = session.UserId,
                                AccessToken = session.AccessToken,
                                User = itsMapper.Map(session.User)
                            }
                    };
            }
            finally
            {
                if (user != null)
                    user.DeactivateTenant();
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public LogoutReply 
        Logout(LogoutRequest request)
        {
            ISession session = 
                itsSessionRepository.GetUniqueWithAccessToken(
                    request.AccessToken);

            if (session == null)
                return
                    new LogoutReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = "Session not found"
                    };

            session.Close();
            session = itsSessionRepository.Update(session);

            return
                new LogoutReply()
                {
                    SessionId = session.PrimaryId,
                    IsSuccessful = true
                };
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
        /// <inheritDoc/>
        /// 
        public void 
        OnEvent(ISessionDomainEvent evnt)
        {
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public void 
        OnException(Exception exception)
        {
        }
    }
}

//  ##########################################################################
