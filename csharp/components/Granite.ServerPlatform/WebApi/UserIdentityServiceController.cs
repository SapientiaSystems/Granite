//  ##########################################################################
//  # File Name: UserIdentityServiceController.cs
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
    public
    class UserIdentityServiceController:
        ApiController,
        IUserIdentityService
    {
        private IUserIdentityService itsImp;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        UserIdentityServiceController(IUserIdentityService imp)
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
            reply.SentTimestamp = DateTime.UtcNow.Ticks/10000;
            reply.ExecutionMilliseconds =
                reply.SentTimestamp - request.ReceivedTimestamp;

            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("finish-registration")]
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
