//  ##########################################################################
//  # File Name: SessionProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.Common.Access;
using Granite.Domain.Session;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.User;
using Strata.Application.Service;
using Strata.Domain.UnitOfWork;
using System;
using SystemEx.Injection;

namespace Granite.Application.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class SessionProcessor:
        AbstractProcessor,
        ISessionProcessor,
        ISessionDomainEventObserver
    {
        private IAuthenticator           itsAuthenticator;
        private ITenantRepository        itsTenantRepository;
        private ISessionRepository       itsSessionRepository;
        private IUserRepository          itsUserRepository;
        private IClientIpAddressRecorder itsRecorder;
        private IMapper                  itsMapper;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        SessionProcessor(
            IUnitOfWorkProvider      provider,
            IAuthenticator           authenticator,
            ITenantRepository        tenantRepository,
            ISessionRepository       sessionRepository,
            IUserRepository          userRepository,
            IClientIpAddressRecorder recorder,
            IMapper                  mapper):
            base(provider)
        {
            itsAuthenticator     = authenticator;
            itsTenantRepository  = tenantRepository;
            itsSessionRepository = sessionRepository;
            itsUserRepository    = userRepository;
            itsRecorder          = recorder;
            itsMapper            = mapper;

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

                if (itsRecorder.HasClientIpAddress)
                    session.ClientIpAddress = itsRecorder.ClientIpAddress;

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
                                User = 
                                    itsMapper.Map(session.User,new UserData())
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
        public GetOrLoginReply
        GetOrLogin(GetOrLoginRequest request)
        {
            ISession session = GetSession(request);

            try
            {
                session?.ActivateTenant();

                if (IsValid(session,request))
                    return
                        new GetOrLoginReply()
                        {
                            IsSuccessful = true,
                            Session = itsMapper.Map(session,new SessionData())
                        };

                return
                    itsMapper.Map(
                        Login(itsMapper.Map(request,new LoginRequest())),
                        new GetOrLoginReply());
            }
            finally
            {
                session?.DeactivateTenant();
            }
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

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        protected ISession
        GetSession(GetOrLoginRequest request)
        {
            return 
                itsSessionRepository.GetUniqueWithAccessToken(
                    request.AccessToken);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        protected bool 
        IsValid(ISession session,GetOrLoginRequest request)
        {
            return
                session != null &&
                session.IsOpen;
        }
    }
}

//  ##########################################################################
