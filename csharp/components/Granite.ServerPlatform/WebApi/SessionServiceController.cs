//  ##########################################################################
//  # File Name: SessionServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Injection;
using Granite.ProvidedService.Session;
using System.Web.Http;
using System.Web.Http.Cors;
using SystemEx.Injection;

namespace Granite.ServerPlatform.WebApi
{
    using RequireHttps = System.Web.Mvc.RequireHttpsAttribute;

    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [RoutePrefix("v1/session")]
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
        SessionServiceController()
        {
            itsImp = 
                Global
                    .Container
                    .GetInstance<ISessionService>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("login")]
        public LoginReply 
        Login(LoginRequest request)
        {
            return itsImp.Login(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("logout")]
        public LogoutReply 
        Logout(LogoutRequest request)
        {
            return itsImp.Logout(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("keep-alive")]
        public KeepAliveReply 
        KeepAlive(KeepAliveRequest request)
        {
            return itsImp.KeepAlive(request);
        }
    }
}

//  ##########################################################################
