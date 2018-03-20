//  ##########################################################################
//  # File Name: SessionService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Session;

namespace Granite.Application.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class SessionService:
        ISessionService
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        SessionService()
        {

        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public LoginReply 
        Login(LoginRequest request)
        {
            LoginReply reply = new LoginReply();

            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public LogoutReply 
        Logout(LogoutRequest request)
        {
            LogoutReply reply = new LogoutReply();

            return reply;
        }
    }
}

//  ##########################################################################
