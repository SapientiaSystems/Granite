//  ##########################################################################
//  # File Name: UserServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.User;
using System;
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
    [System.Web.Http.RoutePrefix("v1/user")]
    [RequireHttps]
    public
    class UserServiceController:
        ApiController,
        IUserService
    {
        private IUserService itsImp;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        UserServiceController(IUserService imp)
        {
            itsImp = imp;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("start-registration")]
        [RequireHttps]
        public StartRegistrationReply 
        StartRegistration(StartRegistrationRequest request)
        {
            StartRegistrationReply reply = null;

            request.ReceivedTimestamp = DateTime.UtcNow.Ticks/10000;
            reply = itsImp.StartRegistration(request);

            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("finish-registration")]
        [RequireHttps]
        public FinishRegistrationReply 
        FinishRegistration(FinishRegistrationRequest request)
        {
            return itsImp.FinishRegistration(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("update")]
        public UpdateReply 
        Update(UpdateRequest request)
        {
            return itsImp.Update(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("remove")]
        public RemoveReply
        Remove(RemoveRequest request)
        {
            return itsImp.Remove(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("current")]
        public GetCurrentReply 
        GetCurrent()
        {
            return itsImp.GetCurrent();
        }
    }
}

//  ##########################################################################
