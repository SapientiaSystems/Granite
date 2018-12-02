//  ##########################################################################
//  # File Name: SessionServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using Granite.Common.Injection;
using Granite.ProvidedService.Session;
using System.Web.Http;
using SystemEx.Injection;

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
        private ISessionService          itsImp;
        private IClientIpAddressRecorder itsRecorder;

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
            itsRecorder =
                Global
                    .Container
                    .GetInstance<IClientIpAddressRecorder>();
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
