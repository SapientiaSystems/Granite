//  ##########################################################################
//  # File Name: SessionServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Session;
using System.Web.Http;
using System.Web.Mvc;
using SystemEx.Injection;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [System.Web.Http.RoutePrefix("v1/session")]
    public
    class SessionServiceController:
        ApiController,
        ISessionService
    {
        private ISessionService itsImp;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        SessionServiceController(ISessionService imp)
        {
            itsImp = imp;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("login")]
        [RequireHttps]
        public LoginReply 
        Login(LoginRequest request)
        {
            return itsImp.Login(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("logout")]
        [RequireHttps]
        public LogoutReply 
        Logout(LogoutRequest request)
        {
            return itsImp.Logout(request);
        }

    }
}

//  ##########################################################################
