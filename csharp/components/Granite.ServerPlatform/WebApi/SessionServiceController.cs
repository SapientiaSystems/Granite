//  ##########################################################################
//  # File Name: SessionServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Session;
using Ninject;
using System.Web.Http;

namespace Granite.ServerPlatform.WebApi
{
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
        SessionServiceController(ISessionService imp)
        {
            itsImp = imp;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("login")]
        [HttpPost]
        public LoginReply 
        Login(LoginRequest request)
        {
            return itsImp.Login(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("logout")]
        [HttpPost]
        public LogoutReply 
        Logout(LogoutRequest request)
        {
            return itsImp.Logout(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("get-or-login")]
        [HttpPost]
        public GetOrLoginReply
        GetOrLogin(GetOrLoginRequest request)
        {
            return itsImp.GetOrLogin(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("keep-alive")]
        [HttpPut]
        public KeepAliveReply 
        KeepAlive(KeepAliveRequest request)
        {
            return itsImp.KeepAlive(request);
        }
    }
}

//  ##########################################################################
