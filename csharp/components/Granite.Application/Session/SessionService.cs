//  ##########################################################################
//  # File Name: SessionService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Session;
using SystemEx.Injection;

namespace Granite.Application.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class SessionService:
        ISessionService
    {
        private ISessionProcessor itsProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public 
        SessionService(ISessionProcessor processor)
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
