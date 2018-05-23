//  ##########################################################################
//  # File Name: SessionService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Session;
using Strata.Foundation.Logging;
using Strata.Persistence.UnitOfWork;
using Strata.Service.Core;
using SystemEx.Injection;

namespace Granite.Application.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class SessionService:
        AbstractService,
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
            IUnitOfWorkProvider provider,
            ILogger             logger,
            ISessionProcessor   processor):
            base(provider,logger)
        {
            itsProcessor = processor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public LoginReply 
        Login(LoginRequest request)
        {
            return
                Execute(
                    (LoginRequest r) => { return itsProcessor.Login(r); },
                    request );
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public LogoutReply 
        Logout(LogoutRequest request)
        {
            return
                Execute(
                    (LogoutRequest r) => { return itsProcessor.Logout(r); },
                    request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public KeepAliveReply 
        KeepAlive(KeepAliveRequest request)
        {
            return
                Execute(
                    (KeepAliveRequest r) => 
                        { return itsProcessor.KeepAlive(r); },
                    request);
        }
    }
}

//  ##########################################################################
